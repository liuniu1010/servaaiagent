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

public class ChatWithSpeechToTextExpertForUIImpl extends AbsChatForUIInDBImpl {
    private String outputFormat = "mp3";
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
    public String fetchResponse(AgentModel.UIParams params) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new ChatWithSpeechToTextExpertForUIImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    try {
                        return innerFetchResponse(dbConnection, params);
                    }
                    catch(NeoAIException nex) {
                        throw nex;
                    }
                    catch(Exception ex) {
                        throw new NeoAIException(ex);
                    }
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

    private String innerFetchResponse(DBConnectionIFC dbConnection, AgentModel.UIParams params) throws Exception {
        String session = params.getSession();
        String userInput = params.getUserInput();
        List<String> attachFiles = params.getAttachFiles();

        String base64 = attachFiles.get(0);
        String fileName = CommonUtil.base64ToFile(base64, onlineFileAbsolutePath);
        String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;

        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setIsRequest(true);
        String relevantFilePath = CommonUtil.normalizeFolderPath(relevantVisitPath) + File.separator + fileName;
        String content = "<b>input speech</b>";
        content += "<audio controls>";
        content += "<source src=\"" + relevantFilePath + "\" type=\"audio/" + outputFormat + "\">";
        content += "Your browser does not support the audio element";
        content += "</audio>";
        newRequestRecord.setContent(content);
        newRequestRecord.setChatTime(new Date());

        SpeechAgentIFC speechAgent = SpeechAgentImpl.getInstance(outputFormat);
        String text = speechAgent.speechToText(dbConnection, session, filePath);

        AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
        newResponseRecord.setIsRequest(false);
        newResponseRecord.setContent(text);
        newResponseRecord.setChatTime(new Date());

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.addChatRecord(session, newRequestRecord);
        storage.addChatRecord(session, newResponseRecord);

        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }
}

