package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ManagerAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIImpl;

public class CoderBotForUIImpl extends AbsChatForUIImpl {
    private CoderBotForUIImpl() {
    }

    public static CoderBotForUIImpl getInstance() {
        return new CoderBotForUIImpl();
    }

    @Override
    public String fetchResponse(String session, String userInput, List<String> attachFiles) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new CoderBotForUIImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
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
            return (String)dbService.executeSaveTask(new CoderBotForUIImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
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
        ManagerAgentIFC managerAgent = ManagerAgentImpl.getInstance();
        String declare = managerAgent.runProject(dbConnection, session, notifyCallback, userInput);
        return declare;
    }
}

