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

    @Override
    public String getRecentPageCode(String session) {
        try {
            return innerGetRecentPageCode(session);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String getRecentPageCode(DBConnectionIFC dbConnection, String session) {
        throw new NeoAIException("not support!");
    }

    private String innerGeneratePageCode(String session, String userInput) throws Exception {
        String gamebotDesc = loadGameBotDesc();
        AIModel.PromptStruct promptStruct = constructPromptStruct(session, gamebotDesc, userInput);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);

        if(chatResponse.getIsSuccess()) {
            String pageCode = "";
            String failReason = "";
            List<AIModel.Call> calls = chatResponse.getCalls();
            boolean hasCall = false;
            String informationToReturn = "";
            if(calls != null && calls.size() > 0) {
                for(AIModel.Call call: calls) {
                    if(!GameCallImpl.isDefinedFunction(call.getMethodName())) {
                        continue;
                    }

                    hasCall = true;
                    if(call.getMethodName().equals(GameCallImpl.METHODNAME_GENERATEPAGECODE)) {
                        pageCode = (String)promptStruct.getFunctionCall().callFunction(call);
                        informationToReturn = pageCode;
                    }
                    else {
                        failReason = (String)promptStruct.getFunctionCall().callFunction(call);
                        informationToReturn = failReason;
                    }

                    // fill codeFeedback and save in storage
                    StorageIFC storage = StorageInMemoryImpl.getInstance();
                    AIModel.CodeFeedback codeFeedback = storage.getCodeFeedback(session);
                    if(codeFeedback == null) {
                        codeFeedback = new AIModel.CodeFeedback();
                    }
                    codeFeedback.setRequirement(promptStruct.getSystemHint());
                    codeFeedback.setCodeContent(pageCode);
                    codeFeedback.setFeedback(promptStruct.getUserInput());
                    storage.putCodeFeedback(session, codeFeedback);

                    break;
                }
            }

            return informationToReturn;
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
    }

    private String innerGetRecentPageCode(String session) throws Exception {
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        AIModel.CodeFeedback codeFeedback = storage.getCodeFeedback(session);

        if(codeFeedback == null) {
            return "";
        }
        else {
            return codeFeedback.getCodeContent();
        }
    }

    private String loadGameBotDesc() throws Exception {
        String fileName = "gamebot.txt";
        return IOUtil.resourceFileToString(fileName);
    }

    private AIModel.PromptStruct constructPromptStruct(String session, String gamebotDesc, String userInput) throws Exception {
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        AIModel.CodeFeedback codeFeedback = storage.getCodeFeedback(session);
        if(codeFeedback == null) {
            return constructPromptStructAsRequirement(session, userInput, gamebotDesc);
        }
        else {
            return constructPromptStructAsFeedback(session, userInput, codeFeedback);
        } 
    }

    private AIModel.PromptStruct constructPromptStructAsRequirement(String session, String userInput, String gamebotDesc) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        promptStruct.setUserInput(userInput);
        String systemHint = gamebotDesc;
        systemHint += "\n\nNow, the requirement you need to implement is:";
        systemHint += "\n" + userInput;
        promptStruct.setSystemHint(systemHint);
        promptStruct.setFunctionCall(GameCallImpl.getInstance());
        return promptStruct;
    }

    private AIModel.PromptStruct constructPromptStructAsFeedback(String session, String userInput, AIModel.CodeFeedback codeFeedback) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        String feedback = "the code\n```\n";
        feedback += codeFeedback.getCodeContent();
        feedback += "\n```\ngot below feedback:\n";
        feedback += userInput;
        feedback += "\n\nPlease always use function call generatePageCode to regenerate the page code";
        feedback += ", or use function call failCodeGeneration to declare the reason that it is impossible to implement.";
        promptStruct.setUserInput(feedback);
        promptStruct.setSystemHint(codeFeedback.getRequirement());
        promptStruct.setFunctionCall(GameCallImpl.getInstance());

        return promptStruct; 
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("gameModel");

        return superAI.fetchChatResponse(model, promptStruct);
    }
}
