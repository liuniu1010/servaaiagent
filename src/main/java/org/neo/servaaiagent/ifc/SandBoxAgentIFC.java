package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface SandBoxAgentIFC {
    public String executeCommand(String session, String command, String sUrl);
    public String executeCommand(DBConnectionIFC dbConnection, String session, String command, String sUrl);

    public String downloadProject(String session, String projectFolder, String sUrl);
    public String downloadProject(DBConnectionIFC dbConnection, String session, String projectFolder, String sUrl);

    public void terminateShell(String session, String sUrl);
    public void terminateShell(DBConnectionIFC dbConnection, String session, String sUrl);
}
