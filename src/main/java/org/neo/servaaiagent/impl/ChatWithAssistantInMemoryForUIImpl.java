package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.AssistantAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInMemoryImpl;
import org.neo.servaaiagent.model.AgentModel;

public class ChatWithAssistantInMemoryForUIImpl extends AbsChatForUIInMemoryImpl {
    private ChatWithAssistantInMemoryForUIImpl() {
    }

    public static ChatWithAssistantInMemoryForUIImpl getInstance() {
        return new ChatWithAssistantInMemoryForUIImpl();
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

    private String innerFetchResponse(AgentModel.UIParams params) {
        String session = params.getSession();
        String userInput = params.getUserInput();

        AssistantAgentIFC assistantAgent = AssistantAgentInMemoryImpl.getInstance();
        assistantAgent.chat(session, userInput);
        String datetimeFormat = CommonUtil.getConfigValue("DateTimeFormat");
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }
}

