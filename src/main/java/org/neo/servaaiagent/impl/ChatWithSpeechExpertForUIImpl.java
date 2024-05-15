package org.neo.servaaiagent.impl;

import java.util.List;
import org.apache.log4j.Logger;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.util.CommonUtil;

import org.neo.servaaiagent.ifc.SpeechAgentIFC;
import org.neo.servaaiagent.impl.AbsChatForUIImpl;

public class ChatWithSpeechExpertForUIImpl extends AbsChatForUIImpl {
    final static Logger logger = Logger.getLogger(ChatWithSpeechExpertForUIImpl.class);
    private String onlineFileAbsolutePath;
    private String relevantVisitPath;
    private ChatWithSpeechExpertForUIImpl() {
    }

    private ChatWithSpeechExpertForUIImpl(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        onlineFileAbsolutePath = inputOnlineFileAbsolutePath;
        relevantVisitPath = inputRelevantVisitPath;
    }

    public static ChatWithSpeechExpertForUIImpl getInstance(String inputAbsolutePath, String inputRelevantVisitPath) {
        return new ChatWithSpeechExpertForUIImpl(inputAbsolutePath, inputRelevantVisitPath);
    }

    @Override
    public String fetchResponse(String session, String userInput, List<String> attachFiles) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new ChatWithSpeechExpertForUIImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    return innerFetchResponse(dbConnection, session, userInput, onlineFileAbsolutePath, relevantVisitPath);
                }
            });
        }
        catch(Exception ex) {
            throw new RuntimeException(standardExceptionMessage, ex);
        }
    }

    private String innerFetchResponse(DBConnectionIFC dbConnection, String session, String userInput, String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        SpeechAgentIFC speechAgent = SpeechAgentImpl.getInstance();
        speechAgent.generateSpeech(dbConnection, session, userInput, inputOnlineFileAbsolutePath, inputRelevantVisitPath);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }
}

