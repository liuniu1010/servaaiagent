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

import org.neo.servaaiagent.ifc.ImageAgentIFC;

public class ImageAgentImpl implements ImageAgentIFC, DBSaveTaskIFC {
    private ImageAgentImpl() {
    }

    public static ImageAgentImpl getInstance() {
        return new ImageAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String[] generateImages(String session, String userInput) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String[])dbService.executeSaveTask(new ImageAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return generateImages(dbConnection, session, userInput);
            }
        });
    }

    @Override
    public String[] generateImages(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);
        newRequestRecord.setChatTime(new Date());

        AIModel.ImagePrompt imagePrompt = constructImagePrompt(dbConnection, session, userInput);
        String[] urls = generateImagesFromSuperAI(dbConnection, imagePrompt);
        AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
        newResponseRecord.setIsRequest(false);
        newResponseRecord.setContent("<img src=\"" + urls[0] + "\">");
        newResponseRecord.setChatTime(new Date());

        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.addChatRecord(session, newRequestRecord);
        storage.addChatRecord(session, newResponseRecord);

        return urls;
    }

    private AIModel.ImagePrompt constructImagePrompt(DBConnectionIFC dbConnection, String session, String userInput) {
        AIModel.ImagePrompt imagePrompt = new AIModel.ImagePrompt();
        imagePrompt.setUserInput(userInput);

        return imagePrompt;
    }

    private String[] generateImagesFromSuperAI(DBConnectionIFC dbConnection, AIModel.ImagePrompt imagePrompt) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getImageModels();
        return superAI.generateImages(models[0], imagePrompt);
    }
}
