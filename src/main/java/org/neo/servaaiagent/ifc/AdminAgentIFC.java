package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface AdminAgentIFC {
    public String chat(String session, String userInput);
    public String chat(DBConnectionIFC dbConnection, String session, String userInput);
}
