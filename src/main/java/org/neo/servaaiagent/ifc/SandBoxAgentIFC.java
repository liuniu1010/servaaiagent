package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface SandBoxAgentIFC {
    public String execute(String session, String command, String sUrl);
    public String execute(DBConnectionIFC dbConnection, String session, String command, String sUrl);

    public void terminateShell(String session, String sUrl);
    public void terminateShell(DBConnectionIFC dbConnection, String session, String sUrl);
}
