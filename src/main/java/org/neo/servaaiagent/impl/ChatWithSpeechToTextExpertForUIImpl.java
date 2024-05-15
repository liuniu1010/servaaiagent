package org.neo.servaaiagent.impl;

import java.util.List;
import java.io.File;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.util.CommonUtil;

import org.neo.servaaiagent.ifc.SpeechAgentIFC;
import org.neo.servaaiagent.impl.AbsChatForUIImpl;

public class ChatWithSpeechToTextExpertForUIImpl extends AbsChatForUIImpl {
    private String onlineFileAbsolutePath;
    private String relevantVisitPath;
    private ChatWithSpeechToTextExpertForUIImpl() {
    }

    private ChatWithSpeechToTextExpertForUIImpl(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        onlineFileAbsolutePath = inputOnlineFileAbsolutePath;
        relevantVisitPath = inputRelevantVisitPath;
    }

    public static ChatWithSpeechToTextExpertForUIImpl getInstance(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        return new ChatWithSpeechToTextExpertForUIImpl(inputOnlineFileAbsolutePath, inputRelevantVisitPath);
    }

    @Override
    public String fetchResponse(String session, String userInput, List<String> attachFiles) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new ChatWithSpeechToTextExpertForUIImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    try {
                        return innerFetchResponse(dbConnection, session, userInput, attachFiles);
                    }
                    catch(RuntimeException rex) {
                        throw rex;
                    }
                    catch(Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
        catch(Exception ex) {
            throw new RuntimeException(standardExceptionMessage, ex);
        }
    }

    private String innerFetchResponse(DBConnectionIFC dbConnection, String session, String userInput, List<String> attachFiles) throws Exception {
        String base64 = attachFiles.get(0);
        String fileName = CommonUtil.base64ToFile(base64, onlineFileAbsolutePath);
        String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;

        SpeechAgentIFC speechAgent = SpeechAgentImpl.getInstance();
        speechAgent.speechToText(dbConnection, session, filePath, relevantVisitPath);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }
}

