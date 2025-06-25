package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ChatAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInDBImpl;
import org.neo.servaaiagent.model.AgentModel;

public class ChatWithBotForUIImpl extends AbsChatForUIInDBImpl {
    private ChatWithBotForUIImpl() {
    }

    public static ChatWithBotForUIImpl getInstance() {
        return new ChatWithBotForUIImpl();
    }

    @Override
    public String fetchResponse(AgentModel.UIParams params) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new ChatWithBotForUIImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    return innerFetchResponse(dbConnection, params);
                }
            });
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(standardExceptionMessage, ex);
        }
    }

    private String innerFetchResponse(DBConnectionIFC dbConnection, AgentModel.UIParams params) {
        String alignedSession = params.getAlignedSession();
        String userInput = params.getUserInput();

        ChatAgentIFC chatAgent = ChatAgentImpl.getInstance();
        chatAgent.chat(dbConnection, alignedSession, userInput);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        return CommonUtil.renderChatRecords(storage.getChatRecords(alignedSession), datetimeFormat);
    }
}

