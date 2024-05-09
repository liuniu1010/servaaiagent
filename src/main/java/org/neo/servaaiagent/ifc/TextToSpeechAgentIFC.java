package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface TextToSpeechAgentIFC {
    public String generateSpeech(String session, String userInput, String onlineFileMountPoint);
    public String generateSpeech(DBConnectionIFC dbConnection, String session, String userInput, String onlineFileMountPoint);
}
