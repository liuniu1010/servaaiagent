package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface SpeechAgentIFC {
    public String generateSpeech(String alignedSession, String userInput, String onlineFileAbsolutePath);
    public String generateSpeech(DBConnectionIFC dbConnection, String alignedSession, String userInput, String onlineFileAbsolutePath);

    public String speechToText(String alignedSession, String filePath);
    public String speechToText(DBConnectionIFC dbConnection, String alignedSession, String filePath);
}
