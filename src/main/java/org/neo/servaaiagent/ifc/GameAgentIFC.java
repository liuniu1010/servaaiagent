package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface GameAgentIFC {
    public String generatePageCode(String session, String userInput);
    public String generatePageCode(DBConnectionIFC dbConnection, String session, String userInput);
}
