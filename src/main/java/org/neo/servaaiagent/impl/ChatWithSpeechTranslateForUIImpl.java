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
import org.neo.servaaiagent.ifc.TranslateAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInDBImpl;
import org.neo.servaaiagent.model.AgentModel;

public class ChatWithSpeechTranslateForUIImpl extends AbsChatForUIInDBImpl {
    private String outputFormat = "mp3";
    private String onlineFileAbsolutePath;
    private String relevantVisitPath;
    private ChatWithSpeechTranslateForUIImpl() {
    }

    private ChatWithSpeechTranslateForUIImpl(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        onlineFileAbsolutePath = inputOnlineFileAbsolutePath;
        relevantVisitPath = inputRelevantVisitPath;
    }

    public static ChatWithSpeechTranslateForUIImpl getInstance(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        return new ChatWithSpeechTranslateForUIImpl(inputOnlineFileAbsolutePath, inputRelevantVisitPath);
    }

    @Override
    public String fetchResponse(AgentModel.UIParams params) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new ChatWithSpeechTranslateForUIImpl(onlineFileAbsolutePath, relevantVisitPath) {
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
        String alignedSession = params.getAlignedSession();
        String userInput = params.getUserInput();
        List<String> attachFiles = params.getAttachFiles();

        String base64 = attachFiles.get(0);
        String fileName = CommonUtil.base64ToFile(base64, onlineFileAbsolutePath);
        String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;

        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(alignedSession);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setChatTime(new Date());
        String relevantFilePath = CommonUtil.normalizeFolderPath(relevantVisitPath) + File.separator + fileName;

        SpeechAgentIFC speechAgent = SpeechAgentImpl.getInstance(outputFormat);
        TranslateAgentIFC translateAgent = TranslateAgentImpl.getInstance();
        String text = speechAgent.speechToText(dbConnection, alignedSession, filePath);

        String content = "<b>" + text + "</b>";
        content += "<br><audio controls>";
        content += "<source src=\"" + relevantFilePath + "\" type=\"audio/" + outputFormat + "\">";
        content += "Your browser does not support the audio element";
        content += "</audio>";
        newRequestRecord.setContent(content);

        String translation = translateAgent.translate(dbConnection, alignedSession, text);
        String translateFileName = speechAgent.generateSpeech(dbConnection, alignedSession, translation, onlineFileAbsolutePath);

        String relevantTranslateFilePath = CommonUtil.normalizeFolderPath(relevantVisitPath) + File.separator + translateFileName;

        String responseText = "<b>" + translation + "</b>";
        responseText += "<br><audio controls>";
        responseText += "<source src=\"" + relevantTranslateFilePath + "\" type=\"audio/" + outputFormat + "\">";
        responseText += "Your browser does not support the audio element";
        responseText += "</audio>";

        AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(alignedSession);
        newResponseRecord.setIsRequest(false);
        newResponseRecord.setContent(responseText);
        newResponseRecord.setChatTime(new Date());

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.addChatRecord(alignedSession, newRequestRecord);
        storage.addChatRecord(alignedSession, newResponseRecord);

        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        return CommonUtil.renderChatRecords(storage.getChatRecords(alignedSession), datetimeFormat);
    }
}

