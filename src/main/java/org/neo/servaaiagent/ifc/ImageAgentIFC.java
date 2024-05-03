package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface ImageAgentIFC {
    public String[] generateImages(String session, String userInput);
    public String[] generateImages(DBConnectionIFC dbConnection, String session, String userInput);
}
