package org.neo.servaaiagent.ifc;

import java.util.List;
import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface VisionAgentIFC {
    public String vision(String alignedSession, String userInput, List<String> attachFiles);
    public String vision(DBConnectionIFC dbConnection, String alignedSession, String userInput, List<String> attachFiles);
}
