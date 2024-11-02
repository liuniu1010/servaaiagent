package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface PersistentShellAgentIFC {
    public String execute(String session, String input);
    public String execute(DBConnectionIFC dbConnection, String session, String input);

    public void terminateShell(String session);
    public void terminateShell(DBConnectionIFC dbConnection, String session);
}
