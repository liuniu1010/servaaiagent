package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface ManagerAgentIFC {
    public String assignTasks(String session, String requirement);
    public String assignTasks(DBConnectionIFC dbConnection, String session, String requirement);
}
