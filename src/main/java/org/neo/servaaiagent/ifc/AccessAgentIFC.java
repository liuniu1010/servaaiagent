package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface AccessAgentIFC {
    public boolean verifyMaintenance();
    public boolean verifyMaintenance(DBConnectionIFC dbConnection);

    public boolean verifyUsername(String username); 
    public boolean verifyUsername(DBConnectionIFC dbConnection, String username);

    public boolean verifyAdminByUsername(String username); 
    public boolean verifyAdminByUsername(DBConnectionIFC dbConnection, String username);

    public boolean verifyAdminByLoginSession(String loginSession); 
    public boolean verifyAdminByLoginSession(DBConnectionIFC dbConnection, String loginSession);

    public boolean verifyIP(String IP);
    public boolean verifyIP(DBConnectionIFC dbConnection, String IP);

    public boolean verifyRegion(String IP);
    public boolean verifyRegion(DBConnectionIFC dbConnection, String IP);

    public boolean verifySecret(String secretName, String secretValue);
    public boolean verifySecret(DBConnectionIFC dbConnection, String secretName, String secretValue);

    public void ensureAdminByUsername(String username);
    public void ensureAdminByUsername(DBConnectionIFC dbConnection, String username);

    public void ensureAdminByLoginSession(String loginSession);
    public void ensureAdminByLoginSession(DBConnectionIFC dbConnection, String loginSession);
}
