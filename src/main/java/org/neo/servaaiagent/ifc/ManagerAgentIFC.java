package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface ManagerAgentIFC {
    public String runProject(String alignedSession, NotifyCallbackIFC notifyCallback, String requirement);
    public String runProject(DBConnectionIFC dbConnection, String alignedSession, NotifyCallbackIFC notifyCallback, String requirement);
}
