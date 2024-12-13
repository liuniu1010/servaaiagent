package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface ManagerAgentIFC {
    public String runProject(String session, NotifyCallbackIFC notifyCallback, String requirement);
    public String runProject(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String requirement);
}
