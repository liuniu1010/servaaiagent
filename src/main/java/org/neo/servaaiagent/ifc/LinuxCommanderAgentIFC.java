package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface LinuxCommanderAgentIFC {
    public String execute(String alignedSession, String userInput);
    public String execute(DBConnectionIFC dbConnection, String alignedSession, String userInput);
    public String generateCommand(String alignedSession, String userInput);
    public String generateCommand(DBConnectionIFC dbConnection, String alignedSession, String userInput);
    public String generateAndExecute(String alignedSession, String userInput);
    public String generateAndExecute(DBConnectionIFC dbConnection, String alignedSession, String userInput);
}
