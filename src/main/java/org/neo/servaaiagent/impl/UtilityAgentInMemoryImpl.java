package org.neo.servaaiagent.impl;

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

import org.neo.servaaiagent.ifc.UtilityAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class UtilityAgentInMemoryImpl implements UtilityAgentIFC {
    private final static String ENDOFCODE = "*****ENDOFCODE*****";
    private UtilityAgentInMemoryImpl() {
    }

    public static UtilityAgentInMemoryImpl getInstance() {
        return new UtilityAgentInMemoryImpl();
    }

    @Override
    public String generatePageCode(String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
        try {
            if(attachFiles != null && attachFiles.size() > 0) {
                return returnAttachedPageCode(session, notifyCallback, attachFiles.get(0));
            }
            else {
                return innerGeneratePageCode(session, notifyCallback, userInput);
            }
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String generatePageCode(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
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

    private void checkWorkingThread(NotifyCallbackIFC notifyCallback) {
        if(notifyCallback != null) {
            if(!notifyCallback.isWorkingThread()) {
                throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_NOT_WORKING_THREAD);
            }
        }
    }

    private String returnAttachedPageCode(String session, NotifyCallbackIFC notifyCallback, String attachFileInBase64) throws Exception {
        String pageCode = CommonUtil.base64ToString(attachFileInBase64);
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        checkWorkingThread(notifyCallback);
        storage.clearCodeFeedbacks(session);
        
        AIModel.CodeFeedback newFeedback = new AIModel.CodeFeedback(session);
        newFeedback.setCodeContent(pageCode);
        newFeedback.setIndex(AIModel.CodeFeedback.INDEX_CODECONTENT);
        storage.pushCodeFeedback(session, newFeedback);

        String informationToReturn = pageCode;
        if(notifyCallback != null) {
            notifyCallback.notify(informationToReturn + ENDOFCODE);
        }

        return informationToReturn;
    }

    private String innerGeneratePageCode(String session, NotifyCallbackIFC notifyCallback, String userInput) throws Exception {
        String utilitybotDesc = loadUtilityBotDesc();
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        checkWorkingThread(notifyCallback);
        AIModel.CodeFeedback lastFeedback = storage.peekCodeFeedback(session);
        String lastCodeContent = null;
        if(lastFeedback != null) {
            lastCodeContent = lastFeedback.getCodeContent();
        }

        AIModel.CodeFeedback newFeedback = new AIModel.CodeFeedback(session);
        newFeedback.setFeedback(userInput);
        newFeedback.setIndex(AIModel.CodeFeedback.INDEX_FEEDBACK);
        checkWorkingThread(notifyCallback);
        storage.pushCodeFeedback(session, newFeedback);

        AIModel.PromptStruct promptStruct = constructPromptStruct(session, utilitybotDesc, userInput, lastCodeContent);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);

        if(chatResponse.getIsSuccess()) {
            String pageCode = "";
            String failReason = "";
            List<AIModel.Call> calls = chatResponse.getCalls();
            boolean hasCall = false;
            String informationToReturn = "";
            if(calls != null && calls.size() > 0) {
                for(AIModel.Call call: calls) {
                    if(!UtilityCallImpl.isDefinedFunction(call.getMethodName())) {
                        continue;
                    }

                    hasCall = true;
                    if(call.getMethodName().equals(UtilityCallImpl.METHODNAME_GENERATEPAGECODE)) {
                        pageCode = (String)promptStruct.getFunctionCall().callFunction(call);
                        informationToReturn = pageCode;
                    }
                    else {
                        failReason = (String)promptStruct.getFunctionCall().callFunction(call);
                        informationToReturn = failReason;
                    }

                    // fill codeFeedback and save in storage
                    checkWorkingThread(notifyCallback);
                    AIModel.CodeFeedback codeFeedback = storage.peekCodeFeedback(session);

                    // codeFeedback should not be null now in theory
                    codeFeedback.setCodeContent(pageCode);
                    codeFeedback.setIndex(AIModel.CodeFeedback.INDEX_CODECONTENT);

                    break;
                }
            }

            if(notifyCallback != null) {
                notifyCallback.notify(informationToReturn + ENDOFCODE);
            }

            return informationToReturn;
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
    }

    private String innerGetRecentPageCode(String session) throws Exception {
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        AIModel.CodeFeedback codeFeedback = storage.peekCodeFeedback(session);

        if(codeFeedback == null) {
            return "";
        }
        else {
            return codeFeedback.getCodeContent();
        }
    }

    private String loadUtilityBotDesc() throws Exception {
        String fileName = "utilitybot.txt";
        return IOUtil.resourceFileToString(fileName);
    }

    private AIModel.PromptStruct constructPromptStruct(String session, String utilitybotDesc, String userInput, String codeContent) throws Exception {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        if(codeContent == null || codeContent.trim().equals("")) {
            String adjustInput = userInput;
            adjustInput += "\n\nPlease always use function call generatePageCode to generate the page code";
            adjustInput += ", or use function call failCodeGeneration to declare the reason that it is impossible to implement.";
            promptStruct.setUserInput(adjustInput);
            promptStruct.setSystemHint(utilitybotDesc);
            promptStruct.setFunctionCall(UtilityCallImpl.getInstance());
        }
        else {
            String adjustInput = "the code\n```\n";
            adjustInput += codeContent;
            adjustInput += "\n```\ngot below feedback:\n```\n";
            adjustInput += userInput;
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
