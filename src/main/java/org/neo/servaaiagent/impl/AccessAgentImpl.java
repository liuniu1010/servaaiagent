package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.util.CommonUtil;

import org.neo.servaaiagent.ifc.AccessAgentIFC;

public class AccessAgentImpl implements AccessAgentIFC, DBQueryTaskIFC {
    private AccessAgentImpl() {
    }

    public static AccessAgentImpl getInstance() {
        return new AccessAgentImpl();
    }

    @Override
    public Object query(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public boolean verifyMaintenance() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (boolean)dbService.executeQueryTask(new AccessAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return verifyMaintenance(dbConnection);
            }
        });
    }

    @Override
    public boolean verifyMaintenance(DBConnectionIFC dbConnection) {
        int iValue = CommonUtil.getConfigValueAsInt(dbConnection, "verifyMaintenance");
        if(iValue == 1) {
            String fileName = "maintenanceInfo.txt";
            // String maintenanceInfo = IOUtil.resourceFileToString(fileName);

            // throw new NeoAIException();
        }

        return false;
    }

    @Override
    public boolean verifyUserName(String userName) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (boolean)dbService.executeQueryTask(new AccessAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return verifyUserName(dbConnection, userName);
            }
        });
    }

    @Override
    public boolean verifyUserName(DBConnectionIFC dbConnection, String userName) {
        return false;
    }

    @Override
    public boolean verifyIP(String IP) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (boolean)dbService.executeQueryTask(new AccessAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return verifyIP(dbConnection, IP);
            }
        });
    }

    @Override
    public boolean verifyIP(DBConnectionIFC dbConnection, String IP) {
        return false;
    }

    @Override
    public boolean verifyRegion(String IP) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (boolean)dbService.executeQueryTask(new AccessAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return verifyRegion(dbConnection, IP);
            }
        });
    }

    @Override
    public boolean verifyRegion(DBConnectionIFC dbConnection, String IP) {
        return false;
    }
}
