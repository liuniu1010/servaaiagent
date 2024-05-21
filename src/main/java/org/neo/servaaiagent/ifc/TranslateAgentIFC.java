package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface TranslateAgentIFC {
    public String translate(String session, String userInput);
    public String translate(DBConnectionIFC dbConnection, String session, String userInput);
}
