package org.neo.servaaiagent.impl;

import java.util.List;

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
    private String onlineFileMountPoint;
    private String relavantVisitPath;
    private ChatWithSpeechExpertForUIImpl() {
    }

    private ChatWithSpeechExpertForUIImpl(String inputOnlineFileMountPoint, String inputRelavantVisitPath) {
        onlineFileMountPoint = onlineFileMountPoint;
        relavantVisitPath = inputRelavantVisitPath;
    }

    public static ChatWithSpeechExpertForUIImpl getInstance(String inputMountPoint, String inputRelavantVisitPath) {
        return new ChatWithSpeechExpertForUIImpl(inputMountPoint, inputRelavantVisitPath);
    }

    @Override
    public String fetchResponse(String session, String userInput, List<String> attachFiles) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new ChatWithSpeechExpertForUIImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    return innerFetchResponse(dbConnection, session, userInput);
                }
            });
        }
        catch(Exception ex) {
            throw new RuntimeException(standardExceptionMessage, ex);
        }
    }

    private String innerFetchResponse(DBConnectionIFC dbConnection, String session, String userInput) {
        SpeechAgentIFC speechAgent = SpeechAgentImpl.getInstance();
        speechAgent.generateSpeech(dbConnection, session, userInput, onlineFileMountPoint, relavantVisitPath);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }
}

