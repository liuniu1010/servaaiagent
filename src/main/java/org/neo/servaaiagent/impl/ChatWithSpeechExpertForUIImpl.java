package org.neo.servaaiagent.impl;

import java.util.Date;
import java.util.List;
import java.io.File;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.SpeechAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInDBImpl;
import org.neo.servaaiagent.model.AgentModel;

public class ChatWithSpeechExpertForUIImpl extends AbsChatForUIInDBImpl {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ChatWithSpeechExpertForUIImpl.class);
    private String outputFormat = "mp3";
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
    public String fetchResponse(AgentModel.UIParams params) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new ChatWithSpeechExpertForUIImpl(onlineFileAbsolutePath, relevantVisitPath) {
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

        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(alignedSession);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);
        newRequestRecord.setChatTime(new Date());

        SpeechAgentIFC speechAgent = SpeechAgentImpl.getInstance(outputFormat);
        String fileName = speechAgent.generateSpeech(dbConnection, alignedSession, userInput, onlineFileAbsolutePath);

        String relevantFilePath = CommonUtil.normalizeFolderPath(relevantVisitPath) + File.separator + fileName;
        AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(alignedSession);
        newResponseRecord.setIsRequest(false);
        String content = "<b>speech generated</b>";
        content += "<audio controls>";
        content += "<source src=\"" + relevantFilePath + "\" type=\"audio/" + outputFormat + "\">";
        content += "Your browser does not support the audio element";
        content += "</audio>";
        newResponseRecord.setContent(content);
        newResponseRecord.setChatTime(new Date());

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.addChatRecord(alignedSession, newRequestRecord);
        storage.addChatRecord(alignedSession, newResponseRecord);

        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(storage.getChatRecords(alignedSession), datetimeFormat);
    }
}

