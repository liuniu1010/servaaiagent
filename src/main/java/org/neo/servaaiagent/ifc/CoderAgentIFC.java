package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface CoderAgentIFC {
    public String generateCode(String session, String requirement);
    public String generateCode(DBConnectionIFC dbConnection, String session, String requirement);
}
