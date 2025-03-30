package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaframe.util.IOUtil;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.GameAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInMemoryImpl;

public class GameBotInMemoryForUIImpl extends AbsChatForUIInMemoryImpl {
    private GameBotInMemoryForUIImpl() {
    }

    public static GameBotInMemoryForUIImpl getInstance() {
        return new GameBotInMemoryForUIImpl();
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
        storage.removeCodeFeedback(session);
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
}

