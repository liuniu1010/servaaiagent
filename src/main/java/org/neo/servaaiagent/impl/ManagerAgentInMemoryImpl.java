package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

import java.io.InputStream;
import java.io.File;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.util.IOUtil;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.impl.OpenAIImpl;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.CoderAgentIFC;
import org.neo.servaaiagent.ifc.ManagerAgentIFC;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class ManagerAgentInMemoryImpl implements ManagerAgentIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ManagerAgentInMemoryImpl.class);

    private String onlineFileAbsolutePath;
    private String relevantVisitPath;

    private ManagerAgentInMemoryImpl() {
    }

    private ManagerAgentInMemoryImpl(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        onlineFileAbsolutePath = inputOnlineFileAbsolutePath;
        relevantVisitPath = inputRelevantVisitPath;
    }

    public static ManagerAgentInMemoryImpl getInstance(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        return new ManagerAgentInMemoryImpl(inputOnlineFileAbsolutePath, inputRelevantVisitPath);
    }

    @Override
    public String runProject(String loginSession, NotifyCallbackIFC notifyCallback, String requirement) {
        try {
            beginProjectAndRecord(loginSession, requirement);

            String coder = chooseCoder(loginSession, requirement);
            String coderSession = "coder" + CommonUtil.getRandomString(5);
            String projectFolder = generateProjectFolderName(coderSession);
            String backgroundDesc = loadBackgroundDesc(coder);
            backgroundDesc = backgroundDesc.replace("<projectFolder>", projectFolder);
            CoderAgentIFC coderAgent = CoderAgentInMemoryImpl.getInstance();
            String declare = coderAgent.generateCode(coderSession, coder, notifyCallback, requirement, backgroundDesc, projectFolder);
            System.out.println("Declare = " + declare);
            if(notifyCallback != null) {
                notifyCallback.notify(declare);
            }

            // code generated, download it
            String base64OfProject = coderAgent.downloadCode(coderSession, coder, projectFolder);
            String fileName = coderSession + ".tar.gz";
            String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;
            IOUtil.rawBase64ToFile(base64OfProject, filePath);

            String relevantFilePath = CommonUtil.normalizeFolderPath(relevantVisitPath) + File.separator + fileName;

            declare += "\n<h3>Please click below link to download the source code</h3>";
            declare += "\n<a href=\"";
            declare += relevantFilePath; 
            declare += "\" download>Source Code</a>";

            consumeAndRecord(loginSession, declare);
            return declare;
        }
        catch(NeoAIException nex) {
            exceptionRecord(loginSession, nex);
            throw nex;
        }
        catch(Exception ex) {
            exceptionRecord(loginSession, ex);
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String runProject(DBConnectionIFC dbConnection, String loginSession, NotifyCallbackIFC notifyCallback, String requirement) {
        throw new NeoAIException("not supported");
    }

    private String generateProjectFolderName(String coderSession) {
        return "/tmp/" + coderSession + "/myProject";
    }

    private String chooseCoder(String loginSession, String requirement) throws Exception {
        AIModel.PromptStruct promptStruct = constructPromptStructForAssign(loginSession, requirement);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);

        if(chatResponse.getIsSuccess()) {
            return extractCoderFromChatResponse(chatResponse); 
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
    }

    private String loadBackgroundDesc(String worker) throws Exception {
        String fileName = worker + ".txt";
        return IOUtil.resourceFileToString(fileName);
    }

    private String extractCoderFromChatResponse(AIModel.ChatResponse chatResponse) {
        List<AIModel.Call> calls = chatResponse.getCalls();
        if(calls == null
            || calls.size() == 0) {
            return chatResponse.getMessage();
        }
        else {
            AIModel.Call call = calls.get(0);
            AIModel.CallParam param = call.getParams().get(0);
            return param.getValue();
        }
    }

    private AIModel.PromptStruct constructPromptStructForAssign(String loginSession, String requirement) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        String userInput = "Please choose a suitable coder to implement requirement:";
        userInput += "\n" + requirement;
        String cordinatorDesc = loadBackgroundDesc("taskcordinator"); 
        promptStruct.setUserInput(userInput);
        promptStruct.setSystemHint(cordinatorDesc);
        promptStruct.setFunctionCall(AssignCallImpl.getInstance());

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("codeModel");
        int tryTime = 3;
        int waitSeconds = 10; // first as 10 seconds
        for(int i = 0;i < tryTime;i++) {
            try {
                return superAI.fetchChatResponse(model, promptStruct);
            }
            catch(NeoAIException nex) {
                logger.error(nex.getMessage(), nex);
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_JSONSYNTAXERROR) {
                    // sometimes LLM might generate error json which cannot be handled
                    // try once more in this case
                    logger.info("Meet json syntax error from LLM, try again...");
                    continue;
                }
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_IOEXCEPTIONWITHLLM) {
                    // met ioexception with LLM, wait some seconds and try again
                    try {
                        logger.info("Meet IOException from LLM, wait " + waitSeconds + " seconds and try again...");
                        Thread.sleep(1000 * waitSeconds);
                        waitSeconds = waitSeconds * 2;
                    }
                    catch(InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                    continue;
                }
                else {
                    throw nex;
                }
            }
        }
        throw new NeoAIException("failed to generate code");
    }

    private void beginProjectAndRecord(String loginSession, String requirement) {
        AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(loginSession);
        codeRecord.setCreateTime(new Date());
        codeRecord.setRequirement(requirement);
        saveCodeRecordInDB(codeRecord);
    }

    private void exceptionRecord(String loginSession, Exception ex) {
        AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(loginSession);
        codeRecord.setCreateTime(new Date());
        codeRecord.setContent(ex.getMessage());
        saveCodeRecordInDB(codeRecord);
    }

    private void consumeAndRecord(String loginSession, String declare) {
        try {
            innerConsumeAndRecord(loginSession, declare);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void innerConsumeAndRecord(String loginSession, String declare) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new DBSaveTaskIFC() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(loginSession);
                codeRecord.setCreateTime(new Date());
                codeRecord.setContent(declare);
                innerSaveCodeRecordInDB(dbConnection, codeRecord);

                int consumedCreditsOnEach = CommonUtil.getConfigValueAsInt(dbConnection, "consumedCreditsOnEach");
                AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
                accountAgent.consumeCredits(dbConnection, loginSession, consumedCreditsOnEach);
                return null;
            }
        });
    }

    private void saveCodeRecordInDB(AIModel.CodeRecord codeRecord) {
        try {
            innerSaveCodeRecordInDB(codeRecord);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void innerSaveCodeRecordInDB(AIModel.CodeRecord codeRecord) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new DBSaveTaskIFC() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                innerSaveCodeRecordInDB(dbConnection, codeRecord);
                return null;
            }
        });
    }

    private void innerSaveCodeRecordInDB(DBConnectionIFC dbConnection, AIModel.CodeRecord codeRecord) {
        StorageIFC storageIFC = StorageInDBImpl.getInstance(dbConnection);
        storageIFC.addCodeRecord(codeRecord.getSession(), codeRecord);
    }

}
