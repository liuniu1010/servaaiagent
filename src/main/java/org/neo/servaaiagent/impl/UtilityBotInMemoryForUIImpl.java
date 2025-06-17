package org.neo.servaaiagent.impl;

import java.util.List;
import java.io.File;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;
import org.neo.servaaibase.model.AIModel;

import org.neo.servaaiagent.ifc.UtilityAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInMemoryImpl;
import org.neo.servaaiagent.model.AgentModel;

public class UtilityBotInMemoryForUIImpl extends AbsChatForUIInMemoryImpl {
    private String onlineFileAbsolutePath;
    private String relevantVisitPath;
    private final static String ENDOFCODE = "*****ENDOFCODE*****";

    private UtilityBotInMemoryForUIImpl() {
    }

    private UtilityBotInMemoryForUIImpl(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        onlineFileAbsolutePath = inputOnlineFileAbsolutePath;
        relevantVisitPath = inputRelevantVisitPath;
    }

    public static UtilityBotInMemoryForUIImpl getInstance(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        return new UtilityBotInMemoryForUIImpl(inputOnlineFileAbsolutePath, inputRelevantVisitPath);
    }

    @Override
    public String initNewChat(AgentModel.UIParams params) {
        try {
            return innerInitNewChat(params);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override 
    public String refresh(AgentModel.UIParams params) {
        try {
            return innerRefresh(params);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String fetchResponse(AgentModel.UIParams params) {
        try {
            return innerFetchResponse(params);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(standardExceptionMessage, ex);
        }
    }

    private String innerInitNewChat(AgentModel.UIParams params) {
        String session = params.getSession();

        StorageIFC storage = StorageInMemoryImpl.getInstance();
        storage.clearCodeFeedbacks(session);
        return "";
    }

    private String innerFetchResponse(AgentModel.UIParams params) throws Exception {
        String session = params.getSession();
        NotifyCallbackIFC notifyCallback = params.getNotifyCallback();
        String userInput = params.getUserInput();
        List<String> attachFiles = params.getAttachFiles();

        if(attachFiles != null && attachFiles.size() > 0) {
            return returnAttachedPageCode(session, notifyCallback, attachFiles.get(0));
        }
        else {
            return innerGeneratePageCode(session, notifyCallback, userInput);
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

        UtilityAgentIFC utilityAgent = UtilityAgentInMemoryImpl.getInstance();
        // UtilityAgentIFC utilityAgent = UtilityAgentRemoteImpl.getInstance();
        AIModel.ChatResponse chatResponse = utilityAgent.generatePageCode(userInput, lastCodeContent);

        // fill codeFeedback and save in storage
        checkWorkingThread(notifyCallback);
        AIModel.CodeFeedback codeFeedback = storage.peekCodeFeedback(session);

        // codeFeedback should not be null now in theory
        codeFeedback.setCodeContent(chatResponse.getMessage());
        codeFeedback.setIndex(AIModel.CodeFeedback.INDEX_CODECONTENT);

        if(chatResponse.getIsSuccess()) {
            String informationToReturn = chatResponse.getMessage();
            if(notifyCallback != null) {
                notifyCallback.notify(informationToReturn + ENDOFCODE);
            } 
            return informationToReturn;
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
    }

    private String innerRefresh(AgentModel.UIParams params) throws Exception {
        String session = params.getSession();

        StorageIFC storage = StorageInMemoryImpl.getInstance();
        AIModel.CodeFeedback codeFeedback = storage.peekCodeFeedback(session);
    
        if(codeFeedback == null) {
            return "";
        }
        else {
            return codeFeedback.getCodeContent();
        }
    }

    private void checkWorkingThread(NotifyCallbackIFC notifyCallback) {
        if(notifyCallback != null) {
            if(!notifyCallback.isWorkingThread()) {
                throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_NOT_WORKING_THREAD);
            }
        }
    }
}

