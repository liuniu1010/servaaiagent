package org.neo.servaaiagent.impl;

import java.util.Date;
import java.io.File;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.impl.StorageInDBImpl;

import org.neo.servaaiagent.ifc.SpeechAgentIFC;

public class SpeechAgentImpl implements SpeechAgentIFC, DBSaveTaskIFC {
    private String outputFormat = "mp3";
    private SpeechAgentImpl() {
    }

    public static SpeechAgentImpl getInstance() {
        return new SpeechAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String generateSpeech(String session, String userInput, String onlineFileAbsolutePath, String relavantVisitPath) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new SpeechAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return generateSpeech(dbConnection, session, userInput, onlineFileAbsolutePath, relavantVisitPath);
            }
        });
    }

    @Override
    public String generateSpeech(DBConnectionIFC dbConnection, String session, String userInput, String onlineFileAbsolutePath, String relavantVisitPath) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);
        newRequestRecord.setChatTime(new Date());

        AIModel.TextToSpeechPrompt TextToSpeechPrompt = constructTextToSpeechPrompt(dbConnection, session, userInput);
        String fileName = generateSpeechFromSuperAI(dbConnection, TextToSpeechPrompt, onlineFileAbsolutePath);
        String relavantFilePath = CommonUtil.normalizeFolderPath(relavantVisitPath) + File.separator + fileName;
        String absoluteFilePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;
        AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
        newResponseRecord.setIsRequest(false);
        String content = "<b>speech generated</b>";
        content += "<audio controls>";
        content += "<source src=\"" + relavantFilePath + "\" type=\"audio/" + outputFormat + "\">";
        content += "Your browser does not support the audio element";
        content += "</audio>";
        newResponseRecord.setContent(content);
        newResponseRecord.setChatTime(new Date());

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.addChatRecord(session, newRequestRecord);
        storage.addChatRecord(session, newResponseRecord);

        return absoluteFilePath;
    }

    private AIModel.TextToSpeechPrompt constructTextToSpeechPrompt(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.TextToSpeechPrompt TextToSpeechPrompt = new AIModel.TextToSpeechPrompt();
        TextToSpeechPrompt.setUserInput(userInput);
        TextToSpeechPrompt.setOutputFormat(outputFormat);

        return TextToSpeechPrompt;
    }

    private String generateSpeechFromSuperAI(DBConnectionIFC dbConnection, AIModel.TextToSpeechPrompt TextToSpeechPrompt, String onlineFileAbsolutePath) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getTextToSpeechModels();
        return superAI.generateSpeech(models[0], TextToSpeechPrompt, onlineFileAbsolutePath);
    }
}
