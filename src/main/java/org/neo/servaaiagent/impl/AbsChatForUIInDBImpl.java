package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBAutoCommitSaveTaskIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ChatForUIIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.model.AgentModel;

abstract public class AbsChatForUIInDBImpl implements ChatForUIIFC, DBQueryTaskIFC, DBSaveTaskIFC, DBAutoCommitSaveTaskIFC {
    protected static String standardExceptionMessage = "Exception occurred! Please contact administrator";

    @Override
    public Object query(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public Object autoCommitSave(DBConnectionIFC dbConnection) {
        return null;
    }

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
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new AbsChatForUIInDBImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    return innerInitNewChat(dbConnection, params);
                }
            });
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String innerInitNewChat(DBConnectionIFC dbConnection, AgentModel.UIParams params) {
        String alignedSession = params.getAlignedSession();
        String sayHello = params.getSayHello();
        if(sayHello == null || sayHello.trim().equals("")) {
            sayHello = "Hello, How can I help you?";
        }

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.clearChatRecords(alignedSession);

        AIModel.ChatRecord chatRecord = new AIModel.ChatRecord(alignedSession);
        chatRecord.setIsRequest(false);
        chatRecord.setChatTime(new Date());
        chatRecord.setContent(sayHello);
        storage.addChatRecord(alignedSession, chatRecord);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(storage.getChatRecords(alignedSession), datetimeFormat);
    }

    @Override
    public String refresh(AgentModel.UIParams params) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeQueryTask(new AbsChatForUIInDBImpl() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    return innerRefresh(dbConnection, params);
                }
            });
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String innerRefresh(DBConnectionIFC dbConnection, AgentModel.UIParams params) {
        String alignedSession = params.getAlignedSession();

        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        return CommonUtil.renderChatRecords(storage.getChatRecords(alignedSession), datetimeFormat);
    }

    @Override
    public String echo(AgentModel.UIParams params) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeQueryTask(new AbsChatForUIInDBImpl() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    return innerEcho(dbConnection, params);
                }
            });
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String innerEcho(DBConnectionIFC dbConnection, AgentModel.UIParams params) {
        String alignedSession = params.getAlignedSession();
        String userInput = params.getUserInput();

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        List<AIModel.ChatRecord> chatRecordsInStorage = storage.getChatRecords(alignedSession);
 
        List<AIModel.ChatRecord> tmpChatRecords = new ArrayList<AIModel.ChatRecord>();
        tmpChatRecords.addAll(chatRecordsInStorage);

        AIModel.ChatRecord echoRecord = new AIModel.ChatRecord(alignedSession);
        echoRecord.setIsRequest(true);
        echoRecord.setChatTime(new Date());
        echoRecord.setContent(userInput);

        tmpChatRecords.add(echoRecord);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(tmpChatRecords, datetimeFormat);
    }
}

