package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface TranslateAgentIFC {
    public String translate(String alignedSession, String userInput);
    public String translate(DBConnectionIFC dbConnection, String alignedSession, String userInput);
}
