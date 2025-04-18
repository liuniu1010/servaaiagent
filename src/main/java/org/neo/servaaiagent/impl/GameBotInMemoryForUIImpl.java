package org.neo.servaaiagent.impl;

import java.util.List;
import java.io.File;


import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.SpeechAgentIFC;
import org.neo.servaaiagent.ifc.GameAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInMemoryImpl;

public class GameBotInMemoryForUIImpl extends AbsChatForUIInMemoryImpl {
    private String outputFormat = "mp3";
    private String onlineFileAbsolutePath;
    private String relevantVisitPath;

    private GameBotInMemoryForUIImpl() {
    }

    private GameBotInMemoryForUIImpl(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        onlineFileAbsolutePath = inputOnlineFileAbsolutePath;
        relevantVisitPath = inputRelevantVisitPath;
    }

    public static GameBotInMemoryForUIImpl getInstance(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        return new GameBotInMemoryForUIImpl(inputOnlineFileAbsolutePath, inputRelevantVisitPath);
    }

    @Override
    public String sendAudio(String session, String userInput, List<String> attachFiles) {
        try {
            return innerSendAudio(session, userInput, attachFiles);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String initNewChat(String session) {
        try {
            return innerInitNewChat(session);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String initNewChat(String session, String sayHello) {
        try {
            return innerInitNewChat(session);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override 
    public String refresh(String session) {
        try {
            return innerRefresh(session);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String fetchResponse(String session, String userInput, List<String> attachFiles) {
        try {
            return innerFetchResponse(session, null, userInput);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(standardExceptionMessage, ex);
        }
    }

    @Override
    public String fetchResponse(String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
        try {
            return innerFetchResponse(session, notifyCallback, userInput);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(standardExceptionMessage, ex);
        }
    }

    private String innerInitNewChat(String session) {
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        storage.clearCodeFeedbacks(session);
        return "";
    }

    private String innerFetchResponse(String session, NotifyCallbackIFC notifyCallback, String userInput) throws Exception {
        GameAgentIFC gameAgent = GameAgentInMemoryImpl.getInstance();
        return gameAgent.generatePageCode(session, notifyCallback, userInput);
    }

    private String innerRefresh(String session) throws Exception {
        GameAgentIFC gameAgent = GameAgentInMemoryImpl.getInstance();
        return gameAgent.getRecentPageCode(session); 
    }

    private String innerSendAudio(String session, String userInput, List<String> attachFiles) throws Exception {
        String base64 = attachFiles.get(0);
        String fileName = CommonUtil.base64ToFile(base64, onlineFileAbsolutePath);
        String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;

        SpeechAgentIFC speechAgent = SpeechAgentImpl.getInstance(outputFormat);
        String text = speechAgent.speechToText(session, filePath);
        return text;
    }
}

