package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

import java.io.InputStream;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.util.IOUtil;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.CoderAgentIFC;
import org.neo.servaaiagent.ifc.ManagerAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class ManagerAgentImpl implements ManagerAgentIFC, DBSaveTaskIFC {
    private ManagerAgentImpl() {
    }

    public static ManagerAgentImpl getInstance() {
        return new ManagerAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String runProject(String session, NotifyCallbackIFC notifyCallback, String requirement) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new ManagerAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return runProject(dbConnection, session, notifyCallback, requirement);
            }
        });
    }

    @Override
    public String runProject(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String requirement) {
        try {
            String coder = chooseCoder(dbConnection, session, requirement);
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
            String savePath = "/tmp/" + coderSession + ".tar.gz";
            IOUtil.rawBase64ToFile(base64OfProject, savePath);

            declare += "\nCode has been saved at " + savePath;
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

    private String chooseCoder(DBConnectionIFC dbConnection, String session, String requirement) throws Exception {
        AIModel.PromptStruct promptStruct = constructPromptStructForAssign(dbConnection, session, requirement);
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

    private AIModel.PromptStruct constructPromptStructForAssign(DBConnectionIFC dbConnection, String session, String requirement) throws Exception {
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
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getChatModels();
        return superAI.fetchChatResponse(models[0], promptStruct);
    }
}
