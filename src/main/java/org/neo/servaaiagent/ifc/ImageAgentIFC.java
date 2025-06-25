package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface ImageAgentIFC {
    public String[] generateImages(String alignedSession, String userInput);
    public String[] generateImages(DBConnectionIFC dbConnection, String alignedSession, String userInput);
}
