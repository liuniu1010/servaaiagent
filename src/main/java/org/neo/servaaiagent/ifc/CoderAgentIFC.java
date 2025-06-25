package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface CoderAgentIFC {
    public String generateCode(String alignedSession, String coder, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder);
    public String generateCode(DBConnectionIFC dbConnection, String alignedSession, String coder, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder);

    public String downloadCode(String alignedSession, String coder, String projectFolder);
    public String downloadCode(DBConnectionIFC dbConnection, String alignedSession, String coder, String projectFolder);
}
