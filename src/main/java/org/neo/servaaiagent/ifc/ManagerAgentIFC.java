package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface ManagerAgentIFC {
    public String runProject(String session, String requirement);
    public String runProject(DBConnectionIFC dbConnection, String session, String requirement);
}
