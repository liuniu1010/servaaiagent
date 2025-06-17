package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.model.AgentModel;

abstract public class AbsChatForUIInMemoryImpl implements ChatForUIIFC {
    protected static String standardExceptionMessage = "Exception occurred! Please contact administrator";

    @Override
    public String sendAudio(AgentModel.UIParams params) {
        throw new NeoAIException("not support! Please implement this method in extended class"); 
    }

    @Override
    public String fetchResponse(AgentModel.UIParams params) {
        throw new NeoAIException("not support! Please implement this method in extended class"); 
    }

    @Override
    public String initNewChat(AgentModel.UIParams params) {
        try {
            return innerInitNewChat(params);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String innerInitNewChat(AgentModel.UIParams params) {
        String session = params.getSession();
        String sayHello = params.getSayHello();
        if(sayHello == null || sayHello.trim().equals("")) {
            // use default
            sayHello = "Hello, How can I help you?";
        }

        StorageIFC storage = StorageInMemoryImpl.getInstance();
        storage.clearChatRecords(session);

        AIModel.ChatRecord chatRecord = new AIModel.ChatRecord(session);
        chatRecord.setIsRequest(false);
        chatRecord.setChatTime(new Date());
        chatRecord.setContent(sayHello);
        storage.addChatRecord(session, chatRecord);
        String datetimeFormat = CommonUtil.getConfigValue("DateTimeFormat");
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }

    @Override
    public String refresh(AgentModel.UIParams params) {
        try {
            return innerRefresh(params);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String innerRefresh(AgentModel.UIParams params) {
        String session = params.getSession();

        String datetimeFormat = CommonUtil.getConfigValue("DateTimeFormat");
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }

    @Override
    public String echo(AgentModel.UIParams params) {
        try {
            return innerEcho(params);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String innerEcho(AgentModel.UIParams params) {
        String session = params.getSession();
        String userInput = params.getUserInput();

        StorageIFC storage = StorageInMemoryImpl.getInstance();
        List<AIModel.ChatRecord> chatRecordsInStorage = storage.getChatRecords(session);
 
        List<AIModel.ChatRecord> tmpChatRecords = new ArrayList<AIModel.ChatRecord>();
        tmpChatRecords.addAll(chatRecordsInStorage);

        AIModel.ChatRecord echoRecord = new AIModel.ChatRecord(session);
        echoRecord.setIsRequest(true);
        echoRecord.setChatTime(new Date());
        echoRecord.setContent(userInput);

        tmpChatRecords.add(echoRecord);
        String datetimeFormat = CommonUtil.getConfigValue("DateTimeFormat");
        return CommonUtil.renderChatRecords(tmpChatRecords, datetimeFormat);
    }
}

