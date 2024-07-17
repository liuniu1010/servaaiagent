package org.neo.servaaiagent.impl;

import java.util.List;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.util.IOUtil;

import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.model.AIModel;
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
        String adminDesc = loadAdminDesc();
        AIModel.PromptStruct promptStruct = constructPromptStructForAdmin(adminDesc, userInput);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);

        if(chatResponse.getIsSuccess()) {
            List<AIModel.Call> calls = chatResponse.getCalls();
            boolean shouldStop = false;
            boolean hasCall = false;
            String totalRunningResultDesc = "";
            if(calls != null && calls.size() > 0) {
                for(AIModel.Call call: calls) {
                    if(!CoderCallImpl.isDefinedFunction(call.getMethodName())) {
                        continue;
                    }

                    hasCall = true;
                    String runningResultDesc = (String)promptStruct.getFunctionCall().callFunction(call);
                    totalRunningResultDesc += "\n" + runningResultDesc;
                }
                if(!shouldStop) {
                    totalRunningResultDesc += "\nPlease continue to adjust code to implement the requirement.";
                }
            }

            // add code here
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
        return null;
    }

    private String loadAdminDesc() throws Exception {
        String fileName = "coderbotadmin.txt";
        return IOUtil.resourceFileToString(fileName);
    }

    private AIModel.PromptStruct constructPromptStructForAdmin(String adminDesc, String userInput) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        promptStruct.setUserInput(userInput);
        promptStruct.setSystemHint(adminDesc);
        promptStruct.setFunctionCall(AdminCallImpl.getInstance());

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = superAI.getChatModels()[0];

        return superAI.fetchChatResponse(model, promptStruct);
    }
}
