package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

import java.io.InputStream;
import java.io.File;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBAutoCommitSaveTaskIFC;
import org.neo.servaframe.util.IOUtil;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.impl.OpenAIImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.CoderAgentIFC;
import org.neo.servaaiagent.ifc.ManagerAgentIFC;
import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class ManagerAgentImpl implements ManagerAgentIFC, DBAutoCommitSaveTaskIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ManagerAgentImpl.class);

    private String onlineFileAbsolutePath;
    private String relevantVisitPath;

    private ManagerAgentImpl() {
    }

    private ManagerAgentImpl(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        onlineFileAbsolutePath = inputOnlineFileAbsolutePath;
        relevantVisitPath = inputRelevantVisitPath;
    }

    public static ManagerAgentImpl getInstance(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        return new ManagerAgentImpl(inputOnlineFileAbsolutePath, inputRelevantVisitPath);
    }

    @Override
    public Object autoCommitSave(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String runProject(String loginSession, NotifyCallbackIFC notifyCallback, String requirement) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeAutoCommitSaveTask(new ManagerAgentImpl(onlineFileAbsolutePath, relevantVisitPath) {
            @Override
            public Object autoCommitSave(DBConnectionIFC dbConnection) {
                return runProject(dbConnection, loginSession, notifyCallback, requirement);
            }
        });
    }

    @Override
    public String runProject(DBConnectionIFC dbConnection, String loginSession, NotifyCallbackIFC notifyCallback, String requirement) {
        try {
            String coder = chooseCoder(dbConnection, loginSession, requirement);
            String coderSession = "coder" + CommonUtil.getRandomString(5);
            String projectFolder = generateProjectFolderName(coderSession);
            String backgroundDesc = loadBackgroundDesc(coder);
            backgroundDesc = backgroundDesc.replace("<projectFolder>", projectFolder);
            CoderAgentIFC coderAgent = CoderAgentImpl.getInstance();
            String declare = coderAgent.generateCode(dbConnection, coderSession, coder, notifyCallback, requirement, backgroundDesc, projectFolder);
            System.out.println("Declare = " + declare);
            if(notifyCallback != null) {
                notifyCallback.notify(declare);
            }

            // code generated, download it
            String base64OfProject = coderAgent.downloadCode(dbConnection, coderSession, coder, projectFolder);
            // String savePath = "/tmp/" + coderSession + ".tar.gz";
            String fileName = coderSession + ".tar.gz";
            String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;
            System.out.println("debug: filePath = " + filePath);
            IOUtil.rawBase64ToFile(base64OfProject, filePath);

            String relevantFilePath = CommonUtil.normalizeFolderPath(relevantVisitPath) + File.separator + fileName;
            System.out.println("debug: relevantFilePath = " + relevantFilePath);

            declare += "\n<h3>Please click below link to download the source code</h3>";
            declare += "\n<a href=\"";
            declare += relevantFilePath; 
            declare += "\" download>Source Code</a>";

            int consumedCreditsOnEach = CommonUtil.getConfigValueAsInt(dbConnection, "consumedCreditsOnEach");
            AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
            accountAgent.consumeCredits(dbConnection, loginSession, consumedCreditsOnEach);
            return declare;
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String generateProjectFolderName(String coderSession) {
        return "/tmp/" + coderSession + "/myProject";
    }

    private String chooseCoder(DBConnectionIFC dbConnection, String loginSession, String requirement) throws Exception {
        AIModel.PromptStruct promptStruct = constructPromptStructForAssign(dbConnection, loginSession, requirement);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);

        if(chatResponse.getIsSuccess()) {
            return extractCoderFromChatResponse(chatResponse); 
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
    }

    private String loadBackgroundDesc(String worker) throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String fileName = worker + ".txt";
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        String backgroundDesc = IOUtil.inputStreamToString(inputStream);
        return backgroundDesc;
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

    private AIModel.PromptStruct constructPromptStructForAssign(DBConnectionIFC dbConnection, String loginSession, String requirement) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        String userInput = "Please choose a suitable coder to implement requirement:";
        userInput += "\n" + requirement;
        String cordinatorDesc = loadBackgroundDesc("taskcordinator"); 
        promptStruct.setUserInput(userInput);
        promptStruct.setSystemHint(cordinatorDesc);
        promptStruct.setFunctionCall(AssignCallImpl.getInstance());

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(DBConnectionIFC dbConnection, AIModel.PromptStruct promptStruct) {
        // SuperAIIFC superAI = OpenAIImpl.getInstance(dbConnection);
        // String model = OpenAIImpl.gpt_4o;

        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String model = CommonUtil.getConfigValue(dbConnection, "codeModel");
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
}
