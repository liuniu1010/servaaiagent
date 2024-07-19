package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ManagerAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInMemoryImpl;

public class CoderBotInMemoryForUIImpl extends AbsChatForUIInMemoryImpl {
    private String onlineFileAbsolutePath;
    private String relevantVisitPath;
    private CoderBotInMemoryForUIImpl() {
    }

    private CoderBotInMemoryForUIImpl(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        onlineFileAbsolutePath = inputOnlineFileAbsolutePath;
        relevantVisitPath = inputRelevantVisitPath;
    }

    public static CoderBotInMemoryForUIImpl getInstance(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        return new CoderBotInMemoryForUIImpl(inputOnlineFileAbsolutePath, inputRelevantVisitPath);
    }

    @Override
    public String fetchResponse(String loginSession, String userInput, List<String> attachFiles) {
        try {
            return innerFetchResponse(loginSession, null, userInput);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String fetchResponse(String loginSession, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
        try {
            return innerFetchResponse(loginSession, notifyCallback, userInput);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String innerFetchResponse(String loginSession, NotifyCallbackIFC notifyCallback, String userInput) {
        ManagerAgentIFC managerAgent = ManagerAgentInMemoryImpl.getInstance(onlineFileAbsolutePath, relevantVisitPath);
        String declare = managerAgent.runProject(loginSession, notifyCallback, userInput);
        return declare;
    }
}

