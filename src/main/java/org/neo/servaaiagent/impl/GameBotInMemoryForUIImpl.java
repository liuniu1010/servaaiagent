package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaframe.util.IOUtil;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.AssistantAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInMemoryImpl;

public class GameBotInMemoryForUIImpl extends AbsChatForUIInMemoryImpl {
    private GameBotInMemoryForUIImpl() {
    }

    public static GameBotInMemoryForUIImpl getInstance() {
        return new GameBotInMemoryForUIImpl();
    }

    @Override
    public String fetchResponse(String session, String userInput, List<String> attachFiles) {
        try {
            return innerFetchResponse(session, userInput);
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
            return innerFetchResponse(session, userInput);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(standardExceptionMessage, ex);
        }
    }

    private String innerFetchResponse(String session, String userInput) throws Exception {
        return IOUtil.resourceFileToString("test.html");
    }
}

