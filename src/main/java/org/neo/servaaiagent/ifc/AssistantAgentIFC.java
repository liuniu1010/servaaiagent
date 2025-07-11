package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface AssistantAgentIFC {
    public String chat(String alignedSession, String loginSession, String userInput);
    public String chat(DBConnectionIFC dbConnection, String alignedSession, String loginSession, String userInput);
}
