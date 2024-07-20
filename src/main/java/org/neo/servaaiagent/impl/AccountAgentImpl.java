package org.neo.servaaiagent.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.model.SQLStruct;
import org.neo.servaframe.model.VersionEntity;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.EmailAgentIFC;
import org.neo.servaaiagent.model.AgentModel;

public class AccountAgentImpl implements AccountAgentIFC, DBQueryTaskIFC, DBSaveTaskIFC {
    private AccountAgentImpl() {
    }

    public static AccountAgentImpl getInstance() {
        return new AccountAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public Object query(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public void sendPassword(String username, String sourceIP) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                sendPassword(dbConnection, username, sourceIP);
                return null;
            }
        });
    }

    @Override
    public void sendPassword(DBConnectionIFC dbConnection, String username, String sourceIP) {
        try {
            innerSendPassword(dbConnection, username, sourceIP);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String login(String username, String password, String sourceIP) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return login(dbConnection, username, password, sourceIP);
            }
        });
    }

    @Override
    public String login(DBConnectionIFC dbConnection, String username, String password, String sourceIP) {
        try {
            return innerLogin(dbConnection, username, password, sourceIP);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void logout(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                logout(dbConnection, loginSession);
                return null;
            }
        });
    }

    @Override
    public void logout(DBConnectionIFC dbConnection, String loginSession) {
        try {
            innerLogout(dbConnection, loginSession);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void updateSession(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                updateSession(dbConnection, loginSession);
                return null;
            }
        });
    }

    @Override
    public void updateSession(DBConnectionIFC dbConnection, String loginSession) {
        try {
            innerUpdateSession(dbConnection, loginSession);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void checkSessionValid(String loginSession) {
        if(loginSession == null
            || loginSession.trim().equals("")) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_SESSION_INVALID);
        }

        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                checkSessionValid(dbConnection, loginSession);
                return null;
            }
        });
    }

    @Override
    public void checkSessionValid(DBConnectionIFC dbConnection, String loginSession) {
        if(loginSession == null
            || loginSession.trim().equals("")) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_SESSION_INVALID);
        }

        try {
            innerCheckSessionValid(dbConnection, loginSession);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void purchaseCredits(String loginSession, int credits) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                purchaseCredits(dbConnection, loginSession, credits);
                return null;
            }
        });
    }

    @Override
    public void purchaseCredits(DBConnectionIFC dbConnection, String loginSession, int credits) {
        try {
            innerPurchaseCredits(dbConnection, loginSession, credits);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void purchaseCredits(long accountId, int credits) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                purchaseCredits(dbConnection, accountId, credits);
                return null;
            }
        });
    }

    @Override
    public void purchaseCredits(DBConnectionIFC dbConnection, long accountId, int credits) {
        try {
            innerPurchaseCredits(dbConnection, accountId, credits);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void consumeCredits(String loginSession, int credits) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                consumeCredits(dbConnection, loginSession, credits);
                return null;
            }
        });
    }

    @Override
    public void consumeCredits(DBConnectionIFC dbConnection, String loginSession, int credits) {
        try {
            innerConsumeCredits(dbConnection, loginSession, credits);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void consumeCredits(long accountId, int credits) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                consumeCredits(dbConnection, accountId, credits);
                return null;
            }
        });
    }

    @Override
    public void consumeCredits(DBConnectionIFC dbConnection, long accountId, int credits) {
        try {
            innerConsumeCredits(dbConnection, accountId, credits);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public int getLeftCredits(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (int)dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return getLeftCredits(dbConnection, loginSession);
            }
        });
    }

    @Override
    public int getLeftCredits(DBConnectionIFC dbConnection, String loginSession) {
        try {
            return innerGetLeftCredits(dbConnection, loginSession);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public int getLeftCredits(long accountId) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (int)dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return getLeftCredits(dbConnection, accountId);
            }
        });
    }

    @Override
    public int getLeftCredits(DBConnectionIFC dbConnection, long accountId) {
        try {
            return innerGetLeftCredits(dbConnection, accountId);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void checkCredits(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                checkCredits(dbConnection, loginSession);
                return null;
            }
        });
    }

    @Override
    public void checkCredits(DBConnectionIFC dbConnection, String loginSession) {
        try {
            innerCheckCredits(dbConnection, loginSession);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void checkCredits(long accountId) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                checkCredits(dbConnection, accountId);
                return null;
            }
        });
    }

    @Override
    public void checkCredits(DBConnectionIFC dbConnection, long accountId) {
        try {
            innerCheckCredits(dbConnection, accountId);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public int getRegisterNumber() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (int)dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return getRegisterNumber(dbConnection);
            }
        });
    }

    @Override
    public int getRegisterNumber(DBConnectionIFC dbConnection) {
        try {
            return innerGetRegisterNumber(dbConnection);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public List<String> getRegisterUsers() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (List<String>)dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return getRegisterUsers(dbConnection);
            }
        });
    }

    @Override
    public List<String> getRegisterUsers(DBConnectionIFC dbConnection) {
        try {
            return innerGetRegisterUsers(dbConnection);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public List<String> getOnlineUsers() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (List<String>)dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return getOnlineUsers(dbConnection);
            }
        });
    }

    @Override
    public List<String> getOnlineUsers(DBConnectionIFC dbConnection) {
        try {
            return innerGetOnlineUsers(dbConnection);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public int getOnlineNumber() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (int)dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return getOnlineNumber(dbConnection);
            }
        });
    }

    @Override
    public int getOnlineNumber(DBConnectionIFC dbConnection) {
        try {
            return innerGetOnlineNumber(dbConnection);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private void innerSendPassword(DBConnectionIFC dbConnection, String username, String sourceIP) throws Exception {
        if(!CommonUtil.isValidEmail(username)){
            throw new NeoAIException("not a valid email address!");
        }

        String standardEmailAddress = username.trim().toLowerCase();
        String password = CommonUtil.getRandomString(6);
        String encryptedPassword = CommonUtil.getSaltedHash(password);

        // check if this account is already exist
        String sql = "select id";
        sql += " from useraccount";
        sql += " where username = ?";
        List<Object> params = new ArrayList<Object>();
        params.add(standardEmailAddress);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        Object oId = dbConnection.queryScalar(sqlStruct);

        if(oId == null) {
            int iValue = CommonUtil.getConfigValueAsInt(dbConnection, "verifyMaxRegisterNumber");
            if(iValue != 0) {
                int maxRegisterNumber = CommonUtil.getConfigValueAsInt(dbConnection, "maxRegisterNumber");
                int registerNumber = getRegisterNumber(dbConnection);
                if(registerNumber >= maxRegisterNumber) {
                    throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_MAXREGISTERNUMBER_EXCEED);
                }
            }

            AgentModel.UserAccount userAccount = new AgentModel.UserAccount(standardEmailAddress);
            userAccount.setEncryptedPassword(encryptedPassword);
            userAccount.setRegistTime(new Date());
            userAccount.setIP(sourceIP);
            dbConnection.insert(userAccount.getVersionEntity());

            // for new register user, auto topup some initial credits for trying
            int topupCredits = CommonUtil.getConfigValueAsInt(dbConnection, "topupOnRegister");
            innerPurchaseCredits(dbConnection, userAccount.getId(), topupCredits);
        }
        else {
            long id = Long.parseLong(oId.toString());
            VersionEntity versionEntity = dbConnection.loadVersionEntityById(AgentModel.UserAccount.ENTITYNAME, id);
            AgentModel.UserAccount userAccount = new AgentModel.UserAccount(versionEntity);
            userAccount.setEncryptedPassword(encryptedPassword);
            dbConnection.update(userAccount.getVersionEntity());
        }

        // send password to user
        String subject = "Your Password";
        String body = "Your new password to login to Neo CoderBot is: <br><b>" + password + "</b>";

        EmailAgentIFC emailAgent = EmailAgentImpl.getInstance();
        emailAgent.sendEmail(dbConnection, standardEmailAddress, subject, body);
    }

    private String innerLogin(DBConnectionIFC dbConnection, String username, String password, String sourceIP) throws Exception {
        if(!CommonUtil.isValidEmail(username)){
            throw new NeoAIException("not a valid email address!");
        }

        String standardEmailAddress = username.trim().toLowerCase();

        int iValue = CommonUtil.getConfigValueAsInt(dbConnection, "verifyMaxOnlineNumber");
        if(iValue != 0) {
            int maxOnlineNumber = CommonUtil.getConfigValueAsInt(dbConnection, "maxOnlineNumber");
            int onlineNumber = getOnlineNumber(dbConnection);
            if(onlineNumber >= maxOnlineNumber) {
                throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_MAXONLINENUMBER_EXCEED);
            }
        }

        // read encyrpted password from DB
        String sql = "select *";
        sql += " from useraccount";
        sql += " where username = ?";
        List<Object> params = new ArrayList<Object>();
        params.add(standardEmailAddress);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        
        VersionEntity versionEntity = dbConnection.querySingleAsVersionEntity(AgentModel.UserAccount.ENTITYNAME, sqlStruct);
        if(versionEntity == null) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_LOGIN_FAIL);
        }

        AgentModel.UserAccount userAccount = new AgentModel.UserAccount(versionEntity);

        if(!CommonUtil.checkPassword(password, userAccount.getEncryptedPassword())) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_LOGIN_FAIL);
        }

        // passed, generate login loginSession
        String loginSession = CommonUtil.getRandomString(10);
        int expireMinutes = CommonUtil.getConfigValueAsInt(dbConnection, "sessionExpireMinutes");
        Date expireTime = CommonUtil.addTimeSpan(new Date(), Calendar.MINUTE, expireMinutes);

        AgentModel.LoginSession modelLoginSession = new AgentModel.LoginSession(loginSession);
        modelLoginSession.setAccountId(userAccount.getId());
        modelLoginSession.setExpireTime(expireTime);
        modelLoginSession.setIP(sourceIP);
        modelLoginSession.setIsDeleted(false);
        dbConnection.insert(modelLoginSession.getVersionEntity());

        return loginSession;
    }

    private long getAccountId(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        String sql = "select accountid";
        sql += " from loginsession";
        sql += " where session = ?";
        sql += " and expiretime > ?";
        sql += " and isdeleted = 0";

        List<Object> params = new ArrayList<Object>();
        params.add(loginSession);
        params.add(new Date());

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        
        Object oResult = dbConnection.queryScalar(sqlStruct);
        if(oResult == null) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_SESSION_INVALID);
        }
        return Long.parseLong(oResult.toString());
    }

    private void innerCheckSessionValid(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        getAccountId(dbConnection, loginSession);
    }

    private void innerLogout(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        String sql = "update loginsession";
        sql += " set isdeleted = 1";
        sql += " where session = ?";
        List<Object> params = new ArrayList<Object>();
        params.add(loginSession);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct); 
    }

    private void innerUpdateSession(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        int expireMinutes = CommonUtil.getConfigValueAsInt(dbConnection, "sessionExpireMinutes");
        Date expireTime = CommonUtil.addTimeSpan(new Date(), Calendar.MINUTE, expireMinutes);
        String sql = "update loginsession";
        sql += " set expiretime = ?";
        sql += " where session = ?";
        List<Object> params = new ArrayList<Object>();
        params.add(expireTime);
        params.add(loginSession);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct); 
    }

    private void innerPurchaseCredits(DBConnectionIFC dbConnection, String loginSession, int credits) throws Exception {
        long accountId = getAccountId(dbConnection, loginSession);
        innerPurchaseCredits(dbConnection, accountId, credits);
    }

    private void innerPurchaseCredits(DBConnectionIFC dbConnection, long accountId, int credits) throws Exception {
        int expireMonths = CommonUtil.getConfigValueAsInt(dbConnection, "creditsExpireMonths");
        Date expireTime = CommonUtil.addTimeSpan(new Date(), Calendar.MONTH, expireMonths);
        AgentModel.ChasedCredits chasedCredits = new AgentModel.ChasedCredits(accountId);
        chasedCredits.setCredits(credits);
        chasedCredits.setExpireTime(expireTime);

        dbConnection.insert(chasedCredits.getVersionEntity());
    }

    private void innerConsumeCredits(DBConnectionIFC dbConnection, String loginSession, int credits) throws Exception {
        long accountId = getAccountId(dbConnection, loginSession);
        innerConsumeCredits(dbConnection, accountId, credits);
    }

    private void innerConsumeCredits(DBConnectionIFC dbConnection, long accountId, int credits) throws Exception {
        Date consumeTime = new Date();
        AgentModel.ConsumedCredits consumedCredits = new AgentModel.ConsumedCredits(accountId);
        consumedCredits.setCredits(credits);
        consumedCredits.setConsumeTime(consumeTime);

        dbConnection.insert(consumedCredits.getVersionEntity());
    }

    private int innerGetLeftCredits(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        long accountId = getAccountId(dbConnection, loginSession);
        return innerGetLeftCredits(dbConnection, accountId);
    }

    private int innerGetLeftCredits(DBConnectionIFC dbConnection, long accountId) throws Exception {
        int chasedSum = getChasedCredits(dbConnection, accountId);
        int consumedSum = getConsumedCredits(dbConnection, accountId);

        return (chasedSum - consumedSum);
    }

    private int getChasedCredits(DBConnectionIFC dbConnection, long accountId) throws Exception {
        String sql = "select sum(cc.credits) as sumofchasedcredits";
        sql += " from chasedcredits cc";
        sql += " where cc.accountid = ?";

        List<Object> params = new ArrayList<Object>();
        params.add(accountId);

        SQLStruct sqlStruct = new SQLStruct(sql, params); 

        Object oSum = dbConnection.queryScalar(sqlStruct);

        int sum = 0;
        if(oSum != null) {
            sum = Integer.parseInt(oSum.toString());
        }
        return sum;
    }

    private int getConsumedCredits(DBConnectionIFC dbConnection, long accountId) throws Exception {
        String sql = "select sum(cc.credits) as sumofconsumedcredits";
        sql += " from consumedcredits cc";
        sql += " where cc.accountId = ?";

        List<Object> params = new ArrayList<Object>();
        params.add(accountId);

        SQLStruct sqlStruct = new SQLStruct(sql, params); 

        Object oSum = dbConnection.queryScalar(sqlStruct);

        int sum = 0;
        if(oSum != null) {
            sum = Integer.parseInt(oSum.toString());
        }
        return sum;
    }

    private void innerCheckCredits(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        if(innerGetLeftCredits(dbConnection, loginSession) > 0) {
            return;
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_NOCREDITS_LEFT);
    }

    private void innerCheckCredits(DBConnectionIFC dbConnection, long accountId) throws Exception {
        if(innerGetLeftCredits(dbConnection, accountId) > 0) {
            return;
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_NOCREDITS_LEFT);
    }

    private int innerGetRegisterNumber(DBConnectionIFC dbConnection) throws Exception {
        String sql = "select count(*) as number";
        sql += " from useraccount";

        Object oValue = dbConnection.queryScalar(sql);
        if(oValue == null) {
            return 0;
        }
        else {
            return Integer.parseInt(oValue.toString());
        }
    }

    private List<String> innerGetRegisterUsers(DBConnectionIFC dbConnection) throws Exception {
        String sql = "select username";
        sql += " from useraccount";

        List<Map<String, Object>> lResult = dbConnection.query(sql);
        List<String> userList = new ArrayList<String>();
        if(lResult == null) {
            return userList;
        }

        for(Map<String, Object> map: lResult) {
            userList.add(map.get("username").toString());
        }
        return userList;
    }

    private int innerGetOnlineNumber(DBConnectionIFC dbConnection) throws Exception {
        String sql = "select count(*) as number";
        sql += " from loginsession";
        sql += " where expiretime > ?";
        sql += " and isdeleted = 0";

        List<Object> params = new ArrayList<Object>();
        params.add(new Date());

        SQLStruct sqlStruct = new SQLStruct(sql, params);

        Object oValue = dbConnection.queryScalar(sqlStruct);
        if(oValue == null) {
            return 0;
        }
        else {
            return Integer.parseInt(oValue.toString());
        }
    }

    private List<String> innerGetOnlineUsers(DBConnectionIFC dbConnection) throws Exception {
        String sql = "select distinct ua.username as username";
        sql += " from useraccount ua";
        sql += " join loginsession ls on ls.accountid = ua.id";
        sql += " where expiretime > ?";
        sql += " and isdeleted = 0";

        List<Object> params = new ArrayList<Object>();
        params.add(new Date());

        SQLStruct sqlStruct = new SQLStruct(sql, params);

        List<Map<String, Object>> lResult = dbConnection.query(sqlStruct);
        List<String> userList = new ArrayList<String>();
        if(lResult == null) {
            return userList;
        }

        for(Map<String, Object> map: lResult) {
            userList.add(map.get("username").toString());
        }
        return userList;
    }
}
