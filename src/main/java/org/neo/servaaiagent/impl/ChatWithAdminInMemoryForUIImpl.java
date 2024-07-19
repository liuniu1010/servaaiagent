package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.AdminAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIImpl;

public class ChatWithAdminInMemoryForUIImpl extends AbsChatForUIImpl {
    private ChatWithAdminInMemoryForUIImpl() {
    }

    public static ChatWithAdminInMemoryForUIImpl getInstance() {
        return new ChatWithAdminInMemoryForUIImpl();
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

    private String innerFetchResponse(String session, String userInput) {
        AdminAgentIFC adminAgent = AdminAgentInMemoryImpl.getInstance();
        adminAgent.chat(session, userInput);
        String datetimeFormat = CommonUtil.getConfigValue("DateTimeFormat");
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }
}

