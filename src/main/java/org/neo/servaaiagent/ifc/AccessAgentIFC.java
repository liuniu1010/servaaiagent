package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface AccessAgentIFC {
    public boolean verifyMaintenance();
    public boolean verifyMaintenance(DBConnectionIFC dbConnection);

    public boolean verifyUserName(String userName); 
    public boolean verifyUserName(DBConnectionIFC dbConnection, String userName);

    public boolean verifyIP(String IP);
    public boolean verifyIP(DBConnectionIFC dbConnection, String IP);

    public boolean verifyRegion(String IP);
    public boolean verifyRegion(DBConnectionIFC dbConnection, String IP);

}
