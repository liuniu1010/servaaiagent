package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

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
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.CoderAgentIFC;
import org.neo.servaaiagent.ifc.ManagerAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class ManagerAgentInMemoryImpl implements ManagerAgentIFC {
    final static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ManagerAgentInMemoryImpl.class);

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
    public String runProject(String alignedSession, NotifyCallbackIFC notifyCallback, String requirement) {
        try {
            beginProjectAndRecord(alignedSession, requirement);

            String coder = chooseCoder(alignedSession, requirement);
            String coderSession = "coder" + CommonUtil.getRandomString(8);
            String projectFolder = generateProjectFolderName(coderSession);
            String backgroundDesc = loadBackgroundDesc(coder);
            backgroundDesc = backgroundDesc.replace("<projectFolder>", projectFolder);
            CoderAgentIFC coderAgent = CoderAgentInMemoryImpl.getInstance();
            String declare = coderAgent.generateCode(coderSession, coder, notifyCallback, requirement, backgroundDesc, projectFolder);
            System.out.println("Declare = " + declare);
            if(notifyCallback != null) {
                notifyCallback.notify("<br>" + CommonUtil.renderToShowAsOrigin(declare));
            }

            // code generated, download it
            String base64OfProject = coderAgent.downloadCode(coderSession, coder, projectFolder);
            String fileName = coderSession + ".tar.gz";
            String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;
            IOUtil.rawBase64ToFile(base64OfProject, filePath);

            String relevantFilePath = CommonUtil.normalizeFolderPath(relevantVisitPath) + File.separator + fileName;

            declare += "<br><h3>Please click below link to download the source code</h3>";
            declare += "<a href=\"";
            declare += relevantFilePath; 
            declare += "\" download>Source Code</a>";

            endProjectAndRecord(alignedSession, declare);
            return declare;
        }
        catch(NeoAIException nex) {
            exceptionRecord(alignedSession, nex);
            throw nex;
        }
        catch(Exception ex) {
            exceptionRecord(alignedSession, ex);
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String runProject(DBConnectionIFC dbConnection, String alignedSession, NotifyCallbackIFC notifyCallback, String requirement) {
        throw new NeoAIException("not supported");
    }

    private String generateProjectFolderName(String coderSession) {
        return "/tmp/" + coderSession + "/myProject";
    }

    private String chooseCoder(String alignedSession, String requirement) throws Exception {
        AIModel.PromptStruct promptStruct = constructPromptStructForAssign(alignedSession, requirement);
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

    private AIModel.PromptStruct constructPromptStructForAssign(String alignedSession, String requirement) throws Exception {
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
        return superAI.fetchChatResponse(model, promptStruct);
    }

    private void beginProjectAndRecord(String alignedSession, String requirement) {
        AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(alignedSession);
        codeRecord.setCreateTime(new Date());
        codeRecord.setRequirement(requirement);
    }

    private void endProjectAndRecord(String alignedSession, String declare) {
        AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(alignedSession);
        codeRecord.setCreateTime(new Date());
        codeRecord.setContent(declare);
    }

    private void exceptionRecord(String alignedSession, Exception ex) {
        AIModel.CodeRecord codeRecord = new AIModel.CodeRecord(alignedSession);
        codeRecord.setCreateTime(new Date());
        codeRecord.setContent(ex.getMessage());
    }
}
