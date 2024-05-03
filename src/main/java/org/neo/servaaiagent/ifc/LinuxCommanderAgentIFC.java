package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface LinuxCommanderAgentIFC {
    public String execute(String session, String userInput);
    public String execute(DBConnectionIFC dbConnection, String session, String userInput);
    public String generateCommand(String session, String userInput);
    public String generateCommand(DBConnectionIFC dbConnection, String session, String userInput);
}
