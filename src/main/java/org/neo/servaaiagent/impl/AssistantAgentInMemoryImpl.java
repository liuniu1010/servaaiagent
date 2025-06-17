package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;
import java.util.Map;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.util.IOUtil;

import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.NeoAIException;


import org.neo.servaaiagent.ifc.AssistantAgentIFC;

public class AssistantAgentInMemoryImpl implements AssistantAgentIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AssistantAgentInMemoryImpl.class);
    private AssistantAgentInMemoryImpl() {
    }

    public static AssistantAgentInMemoryImpl getInstance() {
        return new AssistantAgentInMemoryImpl();
    }

    @Override
    public String chat(String session, String userInput) {
        try {
            return innerChat(session, userInput);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String chat(DBConnectionIFC dbConnection, String session, String userInput) {
        throw new NeoAIException("not support!");
    }

    private String innerChat(String session, String userInput) throws Exception {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);

        String assistantDesc = loadAssistantDesc();
        AIModel.PromptStruct promptStruct = constructPromptStructForAssistant(session, assistantDesc, userInput);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);

        if(chatResponse.getIsSuccess()) {
            List<AIModel.Call> calls = chatResponse.getCalls();
            boolean hasCall = false;
            String totalFunctionCallResultDesc = "";
            String summarizeResult = "";
            if(calls != null && calls.size() > 0) {
                for(AIModel.Call call: calls) {
                    if(!AssistantCallImpl.isDefinedFunction(call.getMethodName())) {
                        continue;
                    }
    
                    hasCall = true;
                    String functionCallResultDesc = (String)promptStruct.getFunctionCall().callFunction(call);
                    totalFunctionCallResultDesc += "\n" + functionCallResultDesc;
                }
            }

            if(hasCall) {
                // summarize call results
                summarizeResult = summarizeCallResults(session, userInput, totalFunctionCallResultDesc);
            }
            else {
                summarizeResult = chatResponse.getMessage();
            }

            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
            newResponseRecord.setChatTime(new Date());
            newResponseRecord.setIsRequest(false);
            newResponseRecord.setContent(summarizeResult);

            StorageIFC storage = StorageInMemoryImpl.getInstance();
            storage.addChatRecord(session, newRequestRecord);
            storage.addChatRecord(session, newResponseRecord);

            return chatResponse.getMessage();
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
    }

    private String loadAssistantDesc() throws Exception {
        String fileName = "neoaiassistant.txt";
        String fileContent = IOUtil.resourceFileToString(fileName);

        String[] consumedConfigNames = new String[]{"consumedCreditsOnCoderBot"
                                                   ,"consumedCreditsOnSpeechSplit"
                                                   ,"consumedCreditsOnUtilityBot"
                                                   ,"consumedCreditsOnChatWithAssistant"
                                                   ,"consumedCreditsOnSpeechToText"};
        Map<String, String> consumedConfigMap = CommonUtil.getConfigValues(consumedConfigNames);
        for(String consumedConfigName: consumedConfigNames) {
            fileContent = fileContent.replace("<" + consumedConfigName + ">", consumedConfigMap.get(consumedConfigName));
        }

        return fileContent;
    }

    private String summarizeCallResults(String session, String userInput, String totalFunctionCallResultDesc) throws Exception {
        AIModel.PromptStruct promptStruct = constructPromptStructForSummarize(session, userInput, totalFunctionCallResultDesc);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);
        if(chatResponse.getIsSuccess()) {
            return chatResponse.getMessage();
        }
        else {
            logger.error("error occurred in summarize, return original text");
            return totalFunctionCallResultDesc;
        }
    }

    private AIModel.PromptStruct constructPromptStructForSummarize(String session, String userInput, String totalFunctionCallResultDesc) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(session);
        promptStruct.setChatRecords(chatRecords);

        promptStruct.setUserInput(userInput);
        promptStruct.setSystemHint("Please answer user's question according to below Information:\n" + totalFunctionCallResultDesc);

        return promptStruct;
    }

    private AIModel.PromptStruct constructPromptStructForAssistant(String session, String assistantDesc, String userInput) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(session);
        promptStruct.setChatRecords(chatRecords);

        promptStruct.setUserInput(userInput);
        promptStruct.setSystemHint(assistantDesc);
        promptStruct.setFunctionCall(AssistantCallImpl.getInstance());

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("assistantModel");

        return superAI.fetchChatResponse(model, promptStruct);
    }
}
