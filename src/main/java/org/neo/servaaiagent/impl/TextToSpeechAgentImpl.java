package org.neo.servaaiagent.impl;

import java.util.Date;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.impl.StorageInDBImpl;

import org.neo.servaaiagent.ifc.TextToSpeechAgentIFC;

public class TextToSpeechAgentImpl implements TextToSpeechAgentIFC, DBSaveTaskIFC {
    private String outputFormat = "mp3";
    private TextToSpeechAgentImpl() {
    }

    public static TextToSpeechAgentImpl getInstance() {
        return new TextToSpeechAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String generateSpeech(String session, String userInput, String onlineFileMountPoint) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new TextToSpeechAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return generateSpeech(dbConnection, session, userInput, onlineFileMountPoint);
            }
        });
    }

    @Override
    public String generateSpeech(DBConnectionIFC dbConnection, String session, String userInput, String onlineFileMountPoint) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);
        newRequestRecord.setChatTime(new Date());

        AIModel.TextToSpeechPrompt textToSpeechPrompt = constructTextToSpeechPrompt(dbConnection, session, userInput);
        String filePath = generateSpeechFromSuperAI(dbConnection, textToSpeechPrompt, onlineFileMountPoint);
        AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
        newResponseRecord.setIsRequest(false);
        String content = "<b>speech generated</b>";
        content += "<audio controls>";
        content += "<source src=\"" + filePath + "\" type=\"audio/" + outputFormat + "\">";
        content += "Your browser does not support the audio element";
        content += "</audio>";
        newResponseRecord.setContent(content);
        newResponseRecord.setChatTime(new Date());

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.addChatRecord(session, newRequestRecord);
        storage.addChatRecord(session, newResponseRecord);

        return filePath;
    }

    private AIModel.TextToSpeechPrompt constructTextToSpeechPrompt(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.TextToSpeechPrompt textToSpeechPrompt = new AIModel.TextToSpeechPrompt();
        textToSpeechPrompt.setUserInput(userInput);
        textToSpeechPrompt.setOutputFormat(outputFormat);

        return textToSpeechPrompt;
    }

    private String generateSpeechFromSuperAI(DBConnectionIFC dbConnection, AIModel.TextToSpeechPrompt textToSpeechPrompt, String onlineFileMountPoint) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getTextToSpeechModels();
        return superAI.generateSpeech(models[0], textToSpeechPrompt, onlineFileMountPoint);
    }
}
