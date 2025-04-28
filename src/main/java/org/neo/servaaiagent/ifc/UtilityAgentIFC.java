package org.neo.servaaiagent.ifc;

import java.util.List;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface UtilityAgentIFC {
    public String generatePageCode(String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles);
    public String generatePageCode(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles);

    public String getRecentPageCode(String session);
    public String getRecentPageCode(DBConnectionIFC dbConnection, String session);
}
