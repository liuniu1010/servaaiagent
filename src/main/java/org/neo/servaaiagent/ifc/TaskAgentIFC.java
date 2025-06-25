package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface TaskAgentIFC {
    public String executeTask(String alignedSession, NotifyCallbackIFC notifyCallback, String requirement);
    public String executeTask(DBConnectionIFC dbConnection, String alignedSession, NotifyCallbackIFC notifyCallback, String requirement);
}
