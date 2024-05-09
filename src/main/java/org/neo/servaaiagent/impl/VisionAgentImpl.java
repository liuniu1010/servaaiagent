package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.ArrayList;
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

import org.neo.servaaiagent.ifc.VisionAgentIFC;

public class VisionAgentImpl implements VisionAgentIFC, DBSaveTaskIFC {
    private VisionAgentImpl() {
    }

    public static VisionAgentImpl getInstance() {
        return new VisionAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String vision(String session, String userInput, List<String> attachFiles) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new VisionAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return vision(dbConnection, session, userInput, attachFiles);
            }
        });
    }

    @Override
    public String vision(DBConnectionIFC dbConnection, String session, String userInput, List<String> attachFiles) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(userInput);
        newRequestRecord.setChatTime(new Date());

        AIModel.PromptStruct promptStruct = constructPromptStruct(dbConnection, session, userInput, attachFiles);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);
        if(chatResponse.getIsSuccess()) {
            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
            newResponseRecord.setIsRequest(false);
            newResponseRecord.setContent(chatResponse.getMessage());
            newResponseRecord.setChatTime(new Date());

            StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
            storage.addChatRecord(session, newRequestRecord);
            storage.addChatRecord(session, newResponseRecord);

            return chatResponse.getMessage(); 
        }
        else {
            throw new RuntimeException(chatResponse.getMessage());
        }
    }

    private AIModel.PromptStruct constructPromptStruct(DBConnectionIFC dbConnection, String session, String userInput, List<String> attachFiles) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(session);
        promptStruct.setChatRecords(chatRecords);
        promptStruct.setUserInput(userInput);

        if(attachFiles != null
             && attachFiles.size() > 0) {
            AIModel.AttachmentGroup attachmentGroup = new AIModel.AttachmentGroup();
            List<AIModel.Attachment> attachments = new ArrayList<AIModel.Attachment>();
            for(String attachFile: attachFiles) {
                AIModel.Attachment attachment = new AIModel.Attachment();
                attachment.setContent(attachFile);
                attachments.add(attachment);
            }
            attachmentGroup.setAttachments(attachments);
            promptStruct.setAttachmentGroup(attachmentGroup);
        }

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(DBConnectionIFC dbConnection, AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getVisionModels();
        return superAI.fetchChatResponse(models[0], promptStruct);
    } 
}
