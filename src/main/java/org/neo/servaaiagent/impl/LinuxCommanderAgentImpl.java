package org.neo.servaaiagent.impl;

import java.util.Date;
import java.util.List;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.ifc.FunctionCallIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.impl.StorageInDBImpl;

import org.neo.servaaiagent.ifc.LinuxCommanderAgentIFC;

public class LinuxCommanderAgentImpl implements LinuxCommanderAgentIFC, DBSaveTaskIFC {
    private LinuxCommanderAgentImpl() {
    }

    public static LinuxCommanderAgentImpl getInstance() {
        return new LinuxCommanderAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    public String execute(String session, String userInput) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new LinuxCommanderAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return execute(dbConnection, session, userInput);
            }
        });
    }

    public String execute(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);
        newRequestRecord.setChatTime(new Date());

        String runningResult = "";
        try {
            runningResult = CommonUtil.executeCommand(userInput);
        }
        catch(Exception ex) {
            runningResult = ex.getMessage();
        }

        String result = "$ " + userInput + "\n" + runningResult;
        AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
        newResponseRecord.setIsRequest(false);
        newResponseRecord.setContent(result);
        newResponseRecord.setChatTime(new Date());

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.addChatRecord(session, newRequestRecord);
        storage.addChatRecord(session, newResponseRecord);

        return result;
    }

    public String generateCommand(String session, String userInput) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new LinuxCommanderAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return generateCommand(dbConnection, session, userInput);
            }
        });
    }

    public String generateCommand(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);
        newRequestRecord.setChatTime(new Date());

        AIModel.PromptStruct promptStruct = constructPromptStruct(dbConnection, session, userInput);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);
        if(chatResponse.getIsSuccess()) {
            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
            newResponseRecord.setIsRequest(false);
            String command = extractCommandFromChatResponse(chatResponse);
            newResponseRecord.setContent(command);
            newResponseRecord.setChatTime(new Date());

            StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
            storage.addChatRecord(session, newRequestRecord);
            storage.addChatRecord(session, newResponseRecord);

            return command;
        }
        else {
            throw new RuntimeException(chatResponse.getMessage());
        }
    }

    private AIModel.PromptStruct constructPromptStruct(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(session);
        promptStruct.setChatRecords(chatRecords);
        promptStruct.setUserInput(userInput);

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(DBConnectionIFC dbConnection, AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        FunctionCallIFC functionCallIFC = LinuxCommandCallImpl.getInstance();
        String[] models = superAI.getChatModels();
        return superAI.fetchChatResponse(models[0], promptStruct, functionCallIFC);
    }

    private String extractCommandFromChatResponse(AIModel.ChatResponse chatResponse) {
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

    private String executeCommandFromChatResponse(AIModel.ChatResponse chatResponse) {
        List<AIModel.Call> calls = chatResponse.getCalls();
        if(calls == null
            || calls.size() == 0) {
            return chatResponse.getMessage();
        }
        else {
            AIModel.Call call = calls.get(0);
            String command = call.getParams().get(0).getValue();
            Object runningResult = null;
            try {
                FunctionCallIFC functionCallIFC = LinuxCommandCallImpl.getInstance();
                runningResult = functionCallIFC.callFunction(call);
            }
            catch(Exception ex) {
                runningResult = ex.getMessage();
            }
            String result = "$ " + command + "\n" + runningResult;
            return result;
        }
    }

    public String generateAndExecute(String session, String userInput) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new LinuxCommanderAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return generateAndExecute(dbConnection, session, userInput);
            }
        });
    }

    public String generateAndExecute(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);
        newRequestRecord.setChatTime(new Date());

        AIModel.PromptStruct promptStruct = constructPromptStruct(dbConnection, session, userInput);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);
        if(chatResponse.getIsSuccess()) {
            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
            newResponseRecord.setIsRequest(false);
            String runningResult = executeCommandFromChatResponse(chatResponse);
            newResponseRecord.setContent(runningResult);
            newResponseRecord.setChatTime(new Date());

            StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
            storage.addChatRecord(session, newRequestRecord);
            storage.addChatRecord(session, newResponseRecord);

            return runningResult;
        }
        else {
            throw new RuntimeException(chatResponse.getMessage());
        }
    }
}
