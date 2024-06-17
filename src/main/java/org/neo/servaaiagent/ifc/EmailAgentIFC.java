package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface EmailAgentIFC {
    public void sendEmail(String to, String subject, String body);
    public void sendEmail(DBConnectionIFC dbConnection, String to, String subject, String body);
}
