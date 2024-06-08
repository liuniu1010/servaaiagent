package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.CoderAgentIFC;
import org.neo.servaaiagent.ifc.ManagerAgentIFC;

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
    public String runProject(String session, String requirement) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new ManagerAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return runProject(dbConnection, session, requirement);
            }
        });
    }

    @Override
    public String runProject(DBConnectionIFC dbConnection, String session, String requirement) {
        return chooseCoder(dbConnection, session, requirement); 
    }

    private String chooseCoder(DBConnectionIFC dbConnection, String session, String requirement) {
        AIModel.PromptStruct promptStruct = constructPromptStructForAssign(dbConnection, session, requirement);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);

        if(chatResponse.getIsSuccess()) {
            return extractCoderFromChatResponse(chatResponse); 
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
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

    private AIModel.PromptStruct constructPromptStructForAssign(DBConnectionIFC dbConnection, String session, String requirement) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        String userInput = "Please choose a suitable coder to implement requirement:";
        userInput += "\n" + requirement;
        String systemHint = "You are a professional cordinator, your are responsible of choosing";
        systemHint += " a suiable coder to implement the requirment.";
        systemHint += "\n" + AssignCallImpl.ASSIGNTO_PARAM_RECEIVER_JAVAMAVENLINUX + " is good at coding with java and maven under linux.";
        systemHint += "\n" + AssignCallImpl.ASSIGNTO_PARAM_RECEIVER_JAVAGRADLELINUX + " is good at coding with java and gradle under linux";
        systemHint += "\n" + AssignCallImpl.ASSIGNTO_PARAM_RECEIVER_DOTNETLINUX + " is good at coding with .net under linux";
        systemHint += "\n" + AssignCallImpl.ASSIGNTO_PARAM_RECEIVER_PYTHON3LINUX + " is good at coding with python3 under linux";
        systemHint += "\n" + AssignCallImpl.ASSIGNTO_PARAM_RECEIVER_NODEJSLINUX + " is good at coding with node.js under linux";
        systemHint += "\nYou must call function " + AssignCallImpl.METHODNAME_ASSIGNTO + " to assign task";
        systemHint += "\nparameter " + AssignCallImpl.ASSIGNTO_PARAM_RECEIVER + " should be whom you think is the best coder to implement the requirement.";
        systemHint += "\nIf you think none of them are suitable, please random choose one";
        promptStruct.setUserInput(userInput);
        promptStruct.setSystemHint(systemHint);
        promptStruct.setFunctionCall(AssignCallImpl.getInstance());

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(DBConnectionIFC dbConnection, AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getChatModels();
        return superAI.fetchChatResponse(models[0], promptStruct);
    }
}
