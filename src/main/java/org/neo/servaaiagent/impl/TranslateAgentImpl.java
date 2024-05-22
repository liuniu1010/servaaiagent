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

import org.neo.servaaiagent.ifc.TranslateAgentIFC;

public class TranslateAgentImpl implements TranslateAgentIFC, DBSaveTaskIFC {
    private TranslateAgentImpl() {
    }

    public static TranslateAgentImpl getInstance() {
        return new TranslateAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String translate(String session, String userInput) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new TranslateAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return translate(dbConnection, session, userInput);
            }
        });
    }

    @Override
    public String translate(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.PromptStruct promptStruct = constructPromptStruct(dbConnection, session, userInput);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);
        if(chatResponse.getIsSuccess()) {
            return chatResponse.getMessage();
        }
        else {
            throw new RuntimeException(chatResponse.getMessage());
        }
    }

    private AIModel.PromptStruct constructPromptStruct(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        String systemHint = "You are a great language expert, you never response user prompt with your own idea, what you need to do is just translating input prompt, in case the input is English, you translate it into Chinese, in case the input is Chinese, you translate it into English";
        promptStruct.setUserInput(userInput);
        promptStruct.setSystemHint(systemHint);

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(DBConnectionIFC dbConnection, AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getChatModels();
        return superAI.fetchChatResponse(models[0], promptStruct);
    }
}
