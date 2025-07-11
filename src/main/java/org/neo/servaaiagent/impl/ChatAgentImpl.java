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

import org.neo.servaaiagent.ifc.ChatAgentIFC;

public class ChatAgentImpl implements ChatAgentIFC, DBSaveTaskIFC {
    private ChatAgentImpl() {
    }

    public static ChatAgentImpl getInstance() {
        return new ChatAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String chat(String alignedSession, String userInput) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new ChatAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return chat(dbConnection, alignedSession, userInput);
            }
        });
    }

    @Override
    public String chat(DBConnectionIFC dbConnection, String alignedSession, String userInput) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(alignedSession);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);

        AIModel.PromptStruct promptStruct = constructPromptStruct(dbConnection, alignedSession, userInput);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);
        if(chatResponse.getIsSuccess()) {
            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(alignedSession);
            newResponseRecord.setChatTime(new Date());
            newResponseRecord.setIsRequest(false);
            newResponseRecord.setContent(chatResponse.getMessage());

            StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
            storage.addChatRecord(alignedSession, newRequestRecord);
            storage.addChatRecord(alignedSession, newResponseRecord);

            return chatResponse.getMessage();
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        } 
    }

    private AIModel.PromptStruct constructPromptStruct(DBConnectionIFC dbConnection, String alignedSession, String userInput) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(alignedSession);
        promptStruct.setChatRecords(chatRecords);
        promptStruct.setUserInput(userInput);

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(DBConnectionIFC dbConnection, AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getChatModels();
        return superAI.fetchChatResponse(models[0], promptStruct);
    }
}
