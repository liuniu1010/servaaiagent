package org.neo.servaaiagent.ifc;

import java.util.List;
import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface VisionAgentIFC {
    public String vision(String session, String userInput, List<String> attachedFiles);
    public String vision(DBConnectionIFC dbConnection, String session, String userInput, List<String> attachedFiles);
}
