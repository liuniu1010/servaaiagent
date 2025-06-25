package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface SandBoxAgentIFC {
    public String executeCommand(String alignedSession, String command, String sUrl);
    public String executeCommand(DBConnectionIFC dbConnection, String alignedSession, String command, String sUrl);

    public String downloadProject(String alignedSession, String projectFolder, String sUrl);
    public String downloadProject(DBConnectionIFC dbConnection, String alignedSession, String projectFolder, String sUrl);

    public void terminateShell(String alignedSession, String sUrl);
    public void terminateShell(DBConnectionIFC dbConnection, String alignedSession, String sUrl);

    public boolean isUnix(String alignedSessioni, String sUrl);
    public boolean isUnix(DBConnectionIFC dbConnection, String alignedSession, String sUrl);
}
