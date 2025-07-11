package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.util.IOUtil;

import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.NeoAIException;


import org.neo.servaaiagent.ifc.AdminAgentIFC;

public class AdminAgentInMemoryImpl implements AdminAgentIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AdminAgentInMemoryImpl.class);
    private AdminAgentInMemoryImpl() {
    }

    public static AdminAgentInMemoryImpl getInstance() {
        return new AdminAgentInMemoryImpl();
    }

    @Override
    public String chat(String alignedSession, String userInput) {
        try {
            return innerChat(alignedSession, userInput);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String chat(DBConnectionIFC dbConnection, String alignedSession, String userInput) {
        throw new NeoAIException("not support!");
    }

    private String innerChat(String alignedSession, String userInput) throws Exception {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(alignedSession);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);

        String adminDesc = loadAdminDesc();
        AIModel.PromptStruct promptStruct = constructPromptStructForAdmin(alignedSession, adminDesc, userInput);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);

        if(chatResponse.getIsSuccess()) {
            List<AIModel.Call> calls = chatResponse.getCalls();
            boolean hasCall = false;
            String totalFunctionCallResultDesc = "";
            String summarizeResult = "";
            if(calls != null && calls.size() > 0) {
                for(AIModel.Call call: calls) {
                    if(!AdminCallImpl.isDefinedFunction(call.getMethodName())) {
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

            return summarizeResult;
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
    }

    private String loadAdminDesc() throws Exception {
        String fileName = "neoaiadmin.txt";
        return IOUtil.resourceFileToString(fileName);
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

    private AIModel.PromptStruct constructPromptStructForAdmin(String alignedSession, String adminDesc, String userInput) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(alignedSession);
        promptStruct.setChatRecords(chatRecords);

        promptStruct.setUserInput(userInput);
        promptStruct.setSystemHint(adminDesc);
        promptStruct.setFunctionCall(AdminCallImpl.getInstance());

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("adminModel");

        return superAI.fetchChatResponse(model, promptStruct);
    }
}
