package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface SpeechAgentIFC {
    public String generateSpeech(String session, String userInput, String onlineFileAbsolutePath, String relavantVisitPath);
    public String generateSpeech(DBConnectionIFC dbConnection, String session, String userInput, String onlineFileAbsolutePath, String relavantVisitPath);

    public String speechToText(String session, String filePath, String relavantVisitPath);
    public String speechToText(DBConnectionIFC dbConnection, String session, String filePath, String relavantVisitPath);
}
