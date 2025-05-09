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
    public String sendAudio(String session, String userInput, List<String> attachFiles) {
        throw new NeoAIException("not support! Please implement this method in extended class");
    }

    @Override
    public String fetchResponse(String session, String userInput, List<String> attachFiles) {
        throw new NeoAIException("not support! Please implement this method in extended class");
    }

    public String fetchResponse(String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
        throw new NeoAIException("not support! Please implement this method in extended class");
    }

    @Override
    public String initNewChat(String session) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new AbsChatForUIInDBImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    String defaultSayHello = "Hello, How can I help you?";
                    return innerInitNewChat(dbConnection, session, defaultSayHello);
                }

                @Override
                public String fetchResponse(String session, String userInput, List<String> attachFiles) {
                    return null;
                }

                @Override
                public String fetchResponse(String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
                    return null;
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

    @Override
    public String initNewChat(String session, String sayHello) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new AbsChatForUIInDBImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    return innerInitNewChat(dbConnection, session, sayHello);
                }

                @Override
                public String fetchResponse(String session, String userInput, List<String> attachFiles) {
                    return null;
                }

                @Override
                public String fetchResponse(String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
                    return null;
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

    private String innerInitNewChat(DBConnectionIFC dbConnection, String session, String sayHello) {
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.clearChatRecords(session);

        AIModel.ChatRecord chatRecord = new AIModel.ChatRecord(session);
        chatRecord.setIsRequest(false);
        chatRecord.setChatTime(new Date());
        chatRecord.setContent(sayHello);
        storage.addChatRecord(session, chatRecord);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }

    @Override
    public String refresh(String session) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeQueryTask(new AbsChatForUIInDBImpl() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    return innerRefresh(dbConnection, session);
                }

                @Override
                public String fetchResponse(String session, String userInput, List<String> attachFiles) {
                    return null;
                }

                @Override
                public String fetchResponse(String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
                    return null;
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

    private String innerRefresh(DBConnectionIFC dbConnection, String session) {
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }

    @Override
    public String echo(String session, String userInput) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeQueryTask(new AbsChatForUIInDBImpl() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    return innerEcho(dbConnection, session, userInput);
                }

                @Override
                public String fetchResponse(String session, String userInput, List<String> attachFiles) {
                    return null;
                }

                @Override
                public String fetchResponse(String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
                    return null;
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

