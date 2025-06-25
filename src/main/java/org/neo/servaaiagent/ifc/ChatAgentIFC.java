package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface ChatAgentIFC {
    public String chat(String alignedSession, String userInput);
    public String chat(DBConnectionIFC dbConnection, String alignedSession, String userInput);
}
