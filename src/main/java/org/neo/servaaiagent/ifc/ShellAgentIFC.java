package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface ShellAgentIFC {
    public String execute(String session, String command);
    public String execute(DBConnectionIFC dbConnection, String session, String command);

    public void terminateShell(String session);
    public void terminateShell(DBConnectionIFC dbConnection, String session);
}
