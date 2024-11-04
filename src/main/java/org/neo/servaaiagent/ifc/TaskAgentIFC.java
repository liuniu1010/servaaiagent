package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface TaskAgentIFC {
    public String executeTask(String session, NotifyCallbackIFC notifyCallback, String requirement);
    public String executeTask(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String requirement);
}
