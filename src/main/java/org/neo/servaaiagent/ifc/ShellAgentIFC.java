package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface ShellAgentIFC {
    public String execute(String alignedSession, String command);
    public String execute(DBConnectionIFC dbConnection, String alignedSession, String command);

    public void terminateShell(String alignedSession);
    public void terminateShell(DBConnectionIFC dbConnection, String alignedSession);

    public boolean isUnix(String alignedSession);
    public boolean isUnix(DBConnectionIFC dbConnection, String alignedSession);
}
