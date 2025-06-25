package org.neo.servaaiagent.impl;

import java.util.List;


import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.TaskAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInMemoryImpl;
import org.neo.servaaiagent.model.AgentModel;

public class TaskBotInMemoryForUIImpl extends AbsChatForUIInMemoryImpl {
    private TaskBotInMemoryForUIImpl() {
    }

    public static TaskBotInMemoryForUIImpl getInstance() {
        return new TaskBotInMemoryForUIImpl();
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
        String alignedSession = params.getAlignedSession();
        NotifyCallbackIFC notifyCallback = params.getNotifyCallback();
        String userInput = params.getUserInput();

        TaskAgentIFC taskAgent = TaskAgentInMemoryImpl.getInstance();
        String declare = taskAgent.executeTask(alignedSession, notifyCallback, userInput);
        return declare;
    }
}

