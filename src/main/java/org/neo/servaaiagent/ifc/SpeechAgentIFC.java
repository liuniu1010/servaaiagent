package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface SpeechAgentIFC {
    public String generateSpeech(String session, String userInput, String onlineFileMountPoint, String relavantVisitPath);
    public String generateSpeech(DBConnectionIFC dbConnection, String session, String userInput, String onlineFileMountPoint, String relavantVisitPath);
}
