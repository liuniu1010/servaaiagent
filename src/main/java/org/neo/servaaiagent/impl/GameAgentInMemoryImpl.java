package org.neo.servaaiagent.impl;

import java.util.Date;
import java.util.List;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.util.IOUtil;

import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.GameAgentIFC;

public class GameAgentInMemoryImpl implements GameAgentIFC {
    private GameAgentInMemoryImpl() {
    }

    public static GameAgentInMemoryImpl getInstance() {
        return new GameAgentInMemoryImpl();
    }

    @Override
    public String generatePageCode(String session, String userInput) {
        try {
            return innerGeneratePageCode(session, userInput);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String generatePageCode(DBConnectionIFC dbConnection, String session, String userInput) {
        throw new NeoAIException("not support!");
    }

    private String innerGeneratePageCode(String session, String userInput) throws Exception {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);

        String gamebotDesc = loadGameBotDesc();
        AIModel.PromptStruct promptStruct = constructPromptStructForGameBot(session, gamebotDesc, userInput);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);

        if(chatResponse.getIsSuccess()) {
            String pageCode = null;
            String failReason = null;
            List<AIModel.Call> calls = chatResponse.getCalls();
            if(calls != null && calls.size() > 0) {
                for(AIModel.Call call: calls) {
                    if(!GameCallImpl.isDefinedFunction(call.getMethodName())) {
                        continue;
                    }
                    if(call.getMethodName().equals(GameCallImpl.METHODNAME_GENERATEPAGECODE)) {
                        pageCode = (String)promptStruct.getFunctionCall().callFunction(call);
                    }
                    else {
                        failReason = (String)promptStruct.getFunctionCall().callFunction(call);
                    }

                    break;
                }
            }

            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
            newResponseRecord.setChatTime(new Date());
            newResponseRecord.setIsRequest(false);
            newResponseRecord.setContent(chatResponse.getMessage());

            StorageIFC storage = StorageInMemoryImpl.getInstance();
            storage.addChatRecord(session, newRequestRecord);
            storage.addChatRecord(session, newResponseRecord);

            return chatResponse.getMessage();
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
    }

    private String loadGameBotDesc() throws Exception {
        String fileName = "gamebot.txt";
        return IOUtil.resourceFileToString(fileName);
    }

    private AIModel.PromptStruct constructPromptStructForGameBot(String session, String gamebotDesc, String userInput) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(session);
        promptStruct.setChatRecords(chatRecords);

        promptStruct.setUserInput(userInput); 
        promptStruct.setSystemHint(gamebotDesc);
        promptStruct.setFunctionCall(GameCallImpl.getInstance());

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("gameModel");

        return superAI.fetchChatResponse(model, promptStruct);
    }
}
