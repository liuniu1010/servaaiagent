package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;

import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ManagerAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInDBImpl;
import org.neo.servaaiagent.model.AgentModel;

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
    public String fetchResponse(AgentModel.UIParams params) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeAutoCommitSaveTask(new CoderBotForUIImpl() {
                @Override
                public Object autoCommitSave(DBConnectionIFC dbConnection) {
                    return innerFetchResponse(dbConnection, params);
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

    private String innerFetchResponse(DBConnectionIFC dbConnection, AgentModel.UIParams params) {
        String session = params.getSession();
        NotifyCallbackIFC notifyCallback = params.getNotifyCallback();
        String userInput = params.getUserInput();

        ManagerAgentIFC managerAgent = ManagerAgentInMemoryImpl.getInstance(onlineFileAbsolutePath, relevantVisitPath);
        String declare = managerAgent.runProject(dbConnection, session, notifyCallback, userInput);
        return declare;
    }
}

