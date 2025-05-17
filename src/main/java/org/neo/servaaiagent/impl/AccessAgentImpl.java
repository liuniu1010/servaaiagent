package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.ArrayList;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.model.SQLStruct;
import org.neo.servaframe.util.IOUtil;
import org.neo.servaframe.util.ConfigUtil;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

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
        try {
            return innerVerifyMaintenance(dbConnection);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean verifyUsername(String username) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (boolean)dbService.executeQueryTask(new AccessAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return verifyUsername(dbConnection, username);
            }
        });
    }

    @Override
    public boolean verifyUsername(DBConnectionIFC dbConnection, String username) {
        try {
            return innerVerifyUsername(dbConnection, username);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean verifyAdminByUsername(String username) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (boolean)dbService.executeQueryTask(new AccessAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return verifyAdminByUsername(dbConnection, username);
            }
        });
    }

    @Override
    public boolean verifyAdminByUsername(DBConnectionIFC dbConnection, String username) {
        try {
            return innerVerifyAdminByUsername(dbConnection, username);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean verifyAdminByLoginSession(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (boolean)dbService.executeQueryTask(new AccessAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return verifyAdminByLoginSession(dbConnection, loginSession);
            }
        });
    }

    @Override
    public boolean verifyAdminByLoginSession(DBConnectionIFC dbConnection, String loginSession) {
        try {
            return innerVerifyAdminByLoginSession(dbConnection, loginSession);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
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
        try {
            return innerVerifyIP(dbConnection, IP);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
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
        try {
            return innerVerifyRegion(dbConnection, IP);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean verifySecret(String secretName, String secretValue) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (boolean)dbService.executeQueryTask(new AccessAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return verifySecret(dbConnection, secretName, secretValue);
            }
        });
    }

    @Override
    public boolean verifySecret(DBConnectionIFC dbConnection, String secretName, String secretValue) {
        try {
            return innerVerifySecret(dbConnection, secretName, secretValue);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void ensureAdminByUsername(String username) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new AccessAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                ensureAdminByUsername(dbConnection, username);
                return null;
            }
        });
    }

    @Override
    public void ensureAdminByUsername(DBConnectionIFC dbConnection, String username) {
        try {
            innerEnsureAdminByUsername(dbConnection, username);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void ensureAdminByLoginSession(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new AccessAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                ensureAdminByLoginSession(dbConnection, loginSession);
                return null;
            }
        });
    }

    @Override
    public void ensureAdminByLoginSession(DBConnectionIFC dbConnection, String loginSession) {
        try {
            innerEnsureAdminByLoginSession(dbConnection, loginSession);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private boolean innerVerifyMaintenance(DBConnectionIFC dbConnection) throws Exception {
        int iValue = CommonUtil.getConfigValueAsInt(dbConnection, "verifyMaintenance");
        if(iValue != 0) {
            String fileName = "maintenanceInfo.txt";
            String maintenanceInfo = IOUtil.resourceFileToString(fileName);

            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_IN_MAINTENANCE, maintenanceInfo);
        }
        return false;
    }

    private boolean innerVerifyUsername(DBConnectionIFC dbConnection, String username) throws Exception {
        if(!CommonUtil.isValidEmail(username)){
            throw new NeoAIException("not a valid email address!");
        }

        String standardEmailAddress = username.trim().toLowerCase();

        int iValue = CommonUtil.getConfigValueAsInt(dbConnection, "verifyUsername");
        if(iValue != 0) {
            String whiteListUsername = "WhiteListUsername.txt";
            List<String> whiteList = ConfigUtil.getTextFileInLines(whiteListUsername);
            boolean isInWhileList = whiteList.contains(standardEmailAddress);
            if(isInWhileList) {
                return true;
            }

            String blackListUsername = "BlackListUsername.txt";
            List<String> blackList = ConfigUtil.getTextFileInLines(blackListUsername);
            boolean isInBlackList = blackList.contains(standardEmailAddress);
            if(isInBlackList) {
                throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_USERNAME_IN_BLACKLIST);
            }
        }
        return false;
    }

    private boolean innerVerifyAdminByUsername(DBConnectionIFC dbConnection, String username) throws Exception {
        if(!CommonUtil.isValidEmail(username)){
            throw new NeoAIException("not a valid email address!");
        }

        String standardEmailAddress = username.trim().toLowerCase();

        String whiteListAdmin = "WhiteListAdmin.txt";
        List<String> whiteList = ConfigUtil.getTextFileInLines(whiteListAdmin);
        boolean isInWhileList = whiteList.contains(standardEmailAddress);
        if(isInWhileList) {
            return true;
        }
        else {
            return false;
        }
    }

    private void innerEnsureAdminByUsername(DBConnectionIFC dbConnection, String username) throws Exception {
        if(!CommonUtil.isValidEmail(username)){
            throw new NeoAIException("not a valid email address!");
        }

        String standardEmailAddress = username.trim().toLowerCase();

        String whiteListAdmin = "WhiteListAdmin.txt";
        List<String> whiteList = ConfigUtil.getTextFileInLines(whiteListAdmin);
        boolean isInWhileList = whiteList.contains(standardEmailAddress);
        if(isInWhileList) {
            return;
        }

        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_ADMIN_NOTIN_WHITELIST);
    }

    private boolean innerVerifyAdminByLoginSession(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        if(loginSession == null
            || loginSession.trim().equals("")) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_SESSION_INVALID);
        }

        String sql = "select ua.username as username";
        sql += " from useraccount ua";
        sql += " join loginsession ls on ls.accountid = ua.id";
        sql += " where ls.session = ?";

        List<Object> params = new ArrayList<Object>();
        params.add(loginSession);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        Object oUsername = dbConnection.queryScalar(sqlStruct);

        if(oUsername == null) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_ADMIN_NOTIN_WHITELIST);
        }

        return innerVerifyAdminByUsername(dbConnection, oUsername.toString());
    }

    private void innerEnsureAdminByLoginSession(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        if(loginSession == null
            || loginSession.trim().equals("")) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_SESSION_INVALID);
        }

        String sql = "select ua.username as username";
        sql += " from useraccount ua";
        sql += " join loginsession ls on ls.accountid = ua.id";
        sql += " where ls.session = ?";

        List<Object> params = new ArrayList<Object>();
        params.add(loginSession);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        Object oUsername = dbConnection.queryScalar(sqlStruct);

        if(oUsername == null) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_ADMIN_NOTIN_WHITELIST);
        }

        innerEnsureAdminByUsername(dbConnection, oUsername.toString());
    }

    private boolean innerVerifyIP(DBConnectionIFC dbConnection, String IP) throws Exception {
        int iValue = CommonUtil.getConfigValueAsInt(dbConnection, "verifyIP");
        if(iValue != 0) {
            String whiteListIP = "WhiteListIP.txt";
            List<String> whiteList = ConfigUtil.getTextFileInLines(whiteListIP);
            boolean isInWhileList = whiteList.contains(IP);
            if(isInWhileList) {
                return true;
            }

            String blackListIP = "BlackListIP.txt";
            List<String> blackList = ConfigUtil.getTextFileInLines(blackListIP);
            boolean isInBlackList = blackList.contains(IP);
            if(isInBlackList) {
                throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_IP_IN_BLACKLIST);
            }
        }
        return false;
    }

    private boolean innerVerifyRegion(DBConnectionIFC dbConnection, String IP) throws Exception {
        int iValue = CommonUtil.getConfigValueAsInt(dbConnection, "verifyRegion");
        if(iValue != 0) {
            String regionIsoCode = CommonUtil.getCountryIsoCodeAlpha2ByIP(IP);
            String whiteListRegionsIsoCode = "WhiteListRegionsIsoCode.txt";
            List<String> whiteList = ConfigUtil.getTextFileInLines(whiteListRegionsIsoCode);
            boolean isInWhileList = whiteList.contains(regionIsoCode);
            if(isInWhileList) {
                return false;
            }
            else {
                throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_REGION_NOTIN_WHITELIST);
            }
        }
        return false;
    }

    private boolean innerVerifySecret(DBConnectionIFC dbConnection, String secretName, String secretValue) {
        String secretValueInDB = CommonUtil.getConfigValue(dbConnection, secretName);
        return secretValue.equals(secretValueInDB); 
    }
}
