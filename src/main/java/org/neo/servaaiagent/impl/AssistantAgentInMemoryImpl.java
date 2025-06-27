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

import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.AssistantAgentIFC;

public class AssistantAgentInMemoryImpl implements AssistantAgentIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AssistantAgentInMemoryImpl.class);
    private AssistantAgentInMemoryImpl() {
    }

    public static AssistantAgentInMemoryImpl getInstance() {
        return new AssistantAgentInMemoryImpl();
    }

    @Override
    public String chat(String alignedSession, String loginSession, String userInput) {
        try {
            return innerChat(alignedSession, loginSession, userInput);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String chat(DBConnectionIFC dbConnection, String alignedSession, String loginSession, String userInput) {
        throw new NeoAIException("not support!");
    }

    private String innerChat(String alignedSession, String loginSession, String userInput) throws Exception {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(alignedSession);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);

        String assistantDesc = loadAssistantDesc(loginSession);
        AIModel.PromptStruct promptStruct = constructPromptStructForAssistant(alignedSession, loginSession, assistantDesc, userInput);
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
                summarizeResult = summarizeCallResults(alignedSession, userInput, totalFunctionCallResultDesc);
            }
            else {
                summarizeResult = chatResponse.getMessage();
            }

            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(alignedSession);
            newResponseRecord.setChatTime(new Date());
            newResponseRecord.setIsRequest(false);
            newResponseRecord.setContent(summarizeResult);

            StorageIFC storage = StorageInMemoryImpl.getInstance();
            storage.addChatRecord(alignedSession, newRequestRecord);
            storage.addChatRecord(alignedSession, newResponseRecord);

            return chatResponse.getMessage();
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
    }

    private String loadAssistantDesc(String loginSession) throws Exception {
        String fileName = "neoaiassistant.txt";
        String fileContent = IOUtil.resourceFileToString(fileName);

        String[] configNames = new String[]{"consumedCreditsOnCoderBot"
                                           ,"consumedCreditsOnSpeechSplit"
                                           ,"consumedCreditsOnUtilityBot"
                                           ,"consumedCreditsOnChatWithAssistant"
                                           ,"consumedCreditsOnSpeechToText"
                                           ,"paymentLinkOnStripe"
                                           ,"topupOnRegister"};
        Map<String, String> configMap = CommonUtil.getConfigValues(configNames);
        for(String configName: configNames) {
            if(configName.equals("paymentLinkOnStripe")) {
                AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
                String userName = accountAgent.getUserNameWithSession(loginSession);
                String paymentLink = configMap.get(configName) + "?prefilled_email=" + userName;
                fileContent = fileContent.replace("<" + configName + ">", paymentLink);
            }
            else {
                fileContent = fileContent.replace("<" + configName + ">", configMap.get(configName));
            }
        }

        return fileContent;
    }

    private String summarizeCallResults(String alignedSession, String userInput, String totalFunctionCallResultDesc) throws Exception {
        AIModel.PromptStruct promptStruct = constructPromptStructForSummarize(alignedSession, userInput, totalFunctionCallResultDesc);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);
        if(chatResponse.getIsSuccess()) {
            return chatResponse.getMessage();
        }
        else {
            logger.error("error occurred in summarize, return original text");
            return totalFunctionCallResultDesc;
        }
    }

    private AIModel.PromptStruct constructPromptStructForSummarize(String alignedSession, String userInput, String totalFunctionCallResultDesc) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(alignedSession);
        promptStruct.setChatRecords(chatRecords);

        promptStruct.setUserInput(userInput);
        promptStruct.setSystemHint("Please answer user's question according to below Information:\n" + totalFunctionCallResultDesc);

        return promptStruct;
    }

    private AIModel.PromptStruct constructPromptStructForAssistant(String alignedSession, String loginSession, String assistantDesc, String userInput) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(alignedSession);
        promptStruct.setChatRecords(chatRecords);

        promptStruct.setUserInput(userInput);
        promptStruct.setSystemHint(assistantDesc);
        promptStruct.setFunctionCall(AssistantCallImpl.newInstance(loginSession));

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("assistantModel");

        return superAI.fetchChatResponse(model, promptStruct);
    }
}
