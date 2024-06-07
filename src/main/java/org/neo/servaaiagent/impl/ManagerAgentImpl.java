package org.neo.servaaiagent.impl;

import java.util.List;
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

import org.neo.servaaiagent.ifc.CoderAgentIFC;
import org.neo.servaaiagent.ifc.ManagerAgentIFC;

public class ManagerAgentImpl implements ManagerAgentIFC, DBSaveTaskIFC {
    private ManagerAgentImpl() {
    }

    public static ManagerAgentImpl getInstance() {
        return new ManagerAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String assignTasks(String session, String requirement) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new ManagerAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return assignTasks(dbConnection, session, requirement);
            }
        });
    }

    @Override
    public String assignTasks(DBConnectionIFC dbConnection, String session, String requirement) {
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        storage.clearChatRecords(session);

        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(requirement);

        CoderAgentIFC coderAgent = CoderAgentImpl.getInstance();
        String coderSession = session + "_coder";
        storage.clearChatRecords(coderSession);
        System.out.println("requirement = " + requirement);
        String backgroundDesc = ""; // read it from config file
        String coderResult = coderAgent.generateCode(dbConnection, coderSession, requirement, backgroundDesc);
        System.out.println("coderResult = " + coderResult);

        AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
        newResponseRecord.setChatTime(new Date());
        newResponseRecord.setIsRequest(false);
        newResponseRecord.setContent(coderResult);

        storage.addChatRecord(session, newRequestRecord);
        storage.addChatRecord(session, newResponseRecord);

        return coderResult;
    }
}
