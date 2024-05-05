package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;

import org.neo.servaaiagent.ifc.ChatAgentIFC;
import org.neo.servaaiagent.ifc.ChatForUIIFC;

abstract public class AbsChatForUIImpl implements ChatForUIIFC, DBQueryTaskIFC, DBSaveTaskIFC {
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
    public String initNewChat(String session) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new AbsChatForUIImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    return innerInitNewChat(dbConnection, session);
                }

                @Override
                public String fetchResponse(String session, String userInput, List<String> attachFiles) {
                    return null;
                }
            });
        }
        catch(Exception ex) {
            throw new RuntimeException(standardExceptionMessage, ex);
        }
    }

    private String innerInitNewChat(DBConnectionIFC dbConnection, String session) {
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.clearChatRecords(session);

        AIModel.ChatRecord chatRecord = new AIModel.ChatRecord(session);
        chatRecord.setIsRequest(false);
        chatRecord.setChatTime(new Date());
        chatRecord.setContent("Hello, How can I help you?");
        storage.addChatRecord(session, chatRecord);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }

    @Override
    public String refresh(String session) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeQueryTask(new AbsChatForUIImpl() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    return innerRefresh(dbConnection, session);
                }

                @Override
                public String fetchResponse(String session, String userInput, List<String> attachFiles) {
                    return null;
                }
            });
        }
        catch(Exception ex) {
            throw new RuntimeException(standardExceptionMessage, ex);
        }
    }

    private String innerRefresh(DBConnectionIFC dbConnection, String session) {
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }

    @Override
    public String echo(String session, String userInput) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeQueryTask(new AbsChatForUIImpl() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    return innerEcho(dbConnection, session, userInput);
                }

                @Override
                public String fetchResponse(String session, String userInput, List<String> attachFiles) {
                    return null;
                }
            });
        }
        catch(Exception ex) {
            throw new RuntimeException(standardExceptionMessage, ex);
        }
    }

    private String innerEcho(DBConnectionIFC dbConnection, String session, String userInput) {
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        List<AIModel.ChatRecord> chatRecordsInStorage = storage.getChatRecords(session);
 
        List<AIModel.ChatRecord> tmpChatRecords = new ArrayList<AIModel.ChatRecord>();
        tmpChatRecords.addAll(chatRecordsInStorage);

        AIModel.ChatRecord echoRecord = new AIModel.ChatRecord(session);
        echoRecord.setIsRequest(true);
        echoRecord.setChatTime(new Date());
        echoRecord.setContent(userInput);

        tmpChatRecords.add(echoRecord);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(tmpChatRecords, datetimeFormat);
    }
}

