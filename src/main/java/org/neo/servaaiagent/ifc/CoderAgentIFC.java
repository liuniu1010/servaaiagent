package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface CoderAgentIFC {
    public String generateCode(String session, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder);
    public String generateCode(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder);
}
