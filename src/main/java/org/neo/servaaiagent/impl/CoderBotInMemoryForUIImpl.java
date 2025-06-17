package org.neo.servaaiagent.impl;

import java.util.List;


import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ManagerAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInMemoryImpl;
import org.neo.servaaiagent.model.AgentModel;

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
    public String fetchResponse(AgentModel.UIParams params) {
        try {
            return innerFetchResponse(params);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String innerFetchResponse(AgentModel.UIParams params) {
        String session = params.getSession();
        NotifyCallbackIFC notifyCallback = params.getNotifyCallback();
        String userInput = params.getUserInput();

        ManagerAgentIFC managerAgent = ManagerAgentInMemoryImpl.getInstance(onlineFileAbsolutePath, relevantVisitPath);
        String declare = managerAgent.runProject(session, notifyCallback, userInput);
        return declare;
    }
}

