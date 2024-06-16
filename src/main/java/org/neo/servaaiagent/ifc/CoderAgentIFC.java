package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface CoderAgentIFC {
    public String generateCode(String session, String coder, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder);
    public String generateCode(DBConnectionIFC dbConnection, String coder, String session, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder);

    public String downloadCode(String session, String coder, String projectFolder);
    public String downloadCode(DBConnectionIFC dbConnection, String session, String coder, String projectFolder);
}
