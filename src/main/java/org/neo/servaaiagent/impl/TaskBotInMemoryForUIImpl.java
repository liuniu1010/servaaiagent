package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.TaskAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInMemoryImpl;

public class TaskBotInMemoryForUIImpl extends AbsChatForUIInMemoryImpl {
    private TaskBotInMemoryForUIImpl() {
    }

    public static TaskBotInMemoryForUIImpl getInstance() {
        return new TaskBotInMemoryForUIImpl();
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
        TaskAgentIFC taskAgent = TaskAgentInMemoryImpl.getInstance();
        String declare = taskAgent.executeTask(loginSession, notifyCallback, userInput);
        return declare;
    }
}

