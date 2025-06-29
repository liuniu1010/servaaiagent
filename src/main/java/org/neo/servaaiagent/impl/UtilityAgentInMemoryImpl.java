package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.util.IOUtil;

import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.UtilityAgentIFC;

public class UtilityAgentInMemoryImpl implements UtilityAgentIFC {
    private UtilityAgentInMemoryImpl() {
    }

    public static UtilityAgentInMemoryImpl getInstance() {
        return new UtilityAgentInMemoryImpl();
    }

    @Override
    public AIModel.ChatResponse generatePageCode(String prompt, String code, String theFunction) {
        try {
            return innerGeneratePageCode(prompt, code, theFunction);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private AIModel.ChatResponse innerGeneratePageCode(String prompt, String code, String theFunction) throws Exception {
        String gamebotDesc = loadUtilityBotDesc(theFunction);

        AIModel.PromptStruct promptStruct = constructPromptStruct(gamebotDesc, prompt, code);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);

        if(chatResponse.getIsSuccess()) {
            String pageCode = "";
            String failReason = "";
            List<AIModel.Call> calls = chatResponse.getCalls();
            boolean hasCall = false;

            if(calls != null && calls.size() > 0) {
                for(AIModel.Call call: calls) {
                    if(!UtilityCallImpl.isDefinedFunction(call.getMethodName())) {
                        continue;
                    }

                    hasCall = true;
                    if(call.getMethodName().equals(UtilityCallImpl.METHODNAME_GENERATEPAGECODE)) {
                        pageCode = (String)promptStruct.getFunctionCall().callFunction(call);
                        return new AIModel.ChatResponse(true, pageCode);
                    }
                    else {
                        failReason = (String)promptStruct.getFunctionCall().callFunction(call);
                        return new AIModel.ChatResponse(false, failReason);
                    }
                }
            }

            return new AIModel.ChatResponse(false, chatResponse.getMessage());
        }
        else {
            return new AIModel.ChatResponse(false, chatResponse.getMessage());
        }
    }

    private String loadUtilityBotDesc(String theFunction) throws Exception {
        String fileName = theFunction + ".txt";
        return IOUtil.resourceFileToString(fileName);
    }

    private AIModel.PromptStruct constructPromptStruct(String utilitybotDesc, String prompt, String code) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        if(code == null || code.trim().equals("")) {
            String adjustInput = prompt;
            adjustInput += "\n\nPlease always use function call generatePageCode to generate the page code";
            adjustInput += ", or use function call failCodeGeneration to declare the reason that it is impossible to implement.";
            promptStruct.setUserInput(adjustInput);
            promptStruct.setSystemHint(utilitybotDesc);
            promptStruct.setFunctionCall(UtilityCallImpl.getInstance());
        }
        else {
            String adjustInput = "the code\n```\n";
            adjustInput += code;
            adjustInput += "\n```\ngot below feedback:\n```\n";
            adjustInput += prompt;
            adjustInput += "\n```\nPlease analyse the code and update the code according to the above feedback.";
            adjustInput += "\n\nPlease always use function call generatePageCode to regenerate the page code";
            adjustInput += ", or use function call failCodeGeneration to declare the reason that it is impossible to implement.";
            promptStruct.setUserInput(adjustInput);
            promptStruct.setSystemHint(utilitybotDesc);
            promptStruct.setFunctionCall(UtilityCallImpl.getInstance());
        }

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("utilityModel");

        return superAI.fetchChatResponse(model, promptStruct);
    }
}
