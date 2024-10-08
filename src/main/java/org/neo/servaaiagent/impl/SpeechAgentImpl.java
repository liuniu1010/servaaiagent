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
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.SpeechAgentIFC;

public class SpeechAgentImpl implements SpeechAgentIFC, DBSaveTaskIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SpeechAgentImpl.class);
    private String outputFormat;
    private SpeechAgentImpl() {
    }

    private SpeechAgentImpl(String format) {
        outputFormat = format;
    }

    public static SpeechAgentImpl getInstance(String format) {
        return new SpeechAgentImpl(format);
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String generateSpeech(String session, String userInput, String onlineFileAbsolutePath) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new SpeechAgentImpl(outputFormat) {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return generateSpeech(dbConnection, session, userInput, onlineFileAbsolutePath);
            }
        });
    }

    @Override
    public String generateSpeech(DBConnectionIFC dbConnection, String session, String userInput, String onlineFileAbsolutePath) {
        AIModel.TextToSpeechPrompt TextToSpeechPrompt = constructTextToSpeechPrompt(dbConnection, session, userInput);
        return generateSpeechFromSuperAI(dbConnection, TextToSpeechPrompt, onlineFileAbsolutePath);
    }

    @Override
    public String speechToText(String session, String filePath) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new SpeechAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return speechToText(dbConnection, session, filePath);
            }
        });
    }

    @Override
    public String speechToText(DBConnectionIFC dbConnection, String session, String filePath) {
        AIModel.Attachment attachment = new AIModel.Attachment();
        attachment.setContent(filePath);
        AIModel.ChatResponse chatResponse = speechToTextFromSuperAI(dbConnection, attachment);
        if(chatResponse.getIsSuccess()) {
            return chatResponse.getMessage();
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        }
    }

    private AIModel.TextToSpeechPrompt constructTextToSpeechPrompt(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.TextToSpeechPrompt textToSpeechPrompt = new AIModel.TextToSpeechPrompt();
        textToSpeechPrompt.setUserInput(userInput);
        textToSpeechPrompt.setOutputFormat(this.outputFormat);

        return textToSpeechPrompt;
    }

    private String generateSpeechFromSuperAI(DBConnectionIFC dbConnection, AIModel.TextToSpeechPrompt textToSpeechPrompt, String onlineFileAbsolutePath) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getTextToSpeechModels();
        return superAI.generateSpeech(models[0], textToSpeechPrompt, onlineFileAbsolutePath);
    }

    private AIModel.ChatResponse speechToTextFromSuperAI(DBConnectionIFC dbConnection, AIModel.Attachment attachment) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getSpeechToTextModels();
        return superAI.speechToText(models[0], attachment); 
    }
}
