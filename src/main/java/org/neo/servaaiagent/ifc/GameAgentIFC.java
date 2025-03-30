package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface GameAgentIFC {
    public String generatePageCode(String session, NotifyCallbackIFC notifyCallback, String userInput);
    public String generatePageCode(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String userInput);

    public String getRecentPageCode(String session);
    public String getRecentPageCode(DBConnectionIFC dbConnection, String session);
}
