package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;

import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ManagerAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInDBImpl;

public class CoderBotForUIImpl extends AbsChatForUIInDBImpl {
    private String onlineFileAbsolutePath;
    private String relevantVisitPath;
    private CoderBotForUIImpl() {
    }

    private CoderBotForUIImpl(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        onlineFileAbsolutePath = inputOnlineFileAbsolutePath;
        relevantVisitPath = inputRelevantVisitPath;
    }

    public static CoderBotForUIImpl getInstance(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        return new CoderBotForUIImpl(inputOnlineFileAbsolutePath, inputRelevantVisitPath);
    }

    @Override
    public String fetchResponse(String session, String userInput, List<String> attachFiles) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeAutoCommitSaveTask(new CoderBotForUIImpl(onlineFileAbsolutePath, relevantVisitPath) {
                @Override
                public Object autoCommitSave(DBConnectionIFC dbConnection) {
                    return innerFetchResponse(dbConnection, session, null, userInput);
                }
            });
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String fetchResponse(String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeAutoCommitSaveTask(new CoderBotForUIImpl() {
                @Override
                public Object autoCommitSave(DBConnectionIFC dbConnection) {
                    return innerFetchResponse(dbConnection, session, notifyCallback, userInput);
                }
            });
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String innerFetchResponse(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String userInput) {
        ManagerAgentIFC managerAgent = ManagerAgentInMemoryImpl.getInstance(onlineFileAbsolutePath, relevantVisitPath);
        String declare = managerAgent.runProject(dbConnection, session, notifyCallback, userInput);
        return declare;
    }
}

