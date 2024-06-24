package org.neo.servaaiagent.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.model.SQLStruct;
import org.neo.servaframe.model.VersionEntity;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.AccountAgentIFC;
import org.neo.servaaiagent.ifc.EmailAgentIFC;
import org.neo.servaaiagent.model.AgentModel;

public class AccountAgentImpl implements AccountAgentIFC, DBSaveTaskIFC {
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
    public void sendPassword(String username) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                sendPassword(dbConnection, username);
                return null;
            }
        });
    }

    @Override
    public void sendPassword(DBConnectionIFC dbConnection, String username) {
        try {
            innerSendPassword(dbConnection, username);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String login(String username, String password) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return login(dbConnection, username, password);
            }
        });
    }

    @Override
    public String login(DBConnectionIFC dbConnection, String username, String password) {
        try {
            return innerLogin(dbConnection, username, password);
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
        // no input dbConnection, start/commmit transaction itself
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
        // no input dbConnection, start/commmit transaction itself
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
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                checkSessionValid(dbConnection, loginSession);
                return null;
            }
        });
    }

    @Override
    public void checkSessionValid(DBConnectionIFC dbConnection, String loginSession) {
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
        // no input dbConnection, start/commmit transaction itself
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
        // no input dbConnection, start/commmit transaction itself
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
        // no input dbConnection, start/commmit transaction itself
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
        // no input dbConnection, start/commmit transaction itself
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
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (int)dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
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
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (int)dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
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
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
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
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
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

    private void innerSendPassword(DBConnectionIFC dbConnection, String username) throws Exception {
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
            AgentModel.UserAccount userAccount = new AgentModel.UserAccount(standardEmailAddress);
            userAccount.setEncryptedPassword(encryptedPassword);
            userAccount.setRegistTime(new Date());
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

    private String innerLogin(DBConnectionIFC dbConnection, String username, String password) throws Exception {
        if(!CommonUtil.isValidEmail(username)){
            throw new NeoAIException("not a valid email address!");
        }

        String standardEmailAddress = username.trim().toLowerCase();

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
        String loginSession = CommonUtil.getRandomString(8);
        int expireMinutes = CommonUtil.getConfigValueAsInt(dbConnection, "loginSessionExpireMinutes");
        Date expireTime = CommonUtil.addTimeSpan(new Date(), Calendar.MINUTE, expireMinutes);

        AgentModel.LoginSession modelLoginSession = new AgentModel.LoginSession(loginSession);
        modelLoginSession.setAccountId(userAccount.getId());
        modelLoginSession.setExpireTime(expireTime);
        dbConnection.insert(modelLoginSession.getVersionEntity());

        return loginSession;
    }

    private long getAccountId(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        String sql = "select accountid";
        sql += " from loginloginSession";
        sql += " where loginSession = ?";
        sql += " and expiretime > ?";

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
        String sql = "delete from loginSession";
        sql += " where loginSession = ?";
        List<Object> params = new ArrayList<Object>();
        params.add(loginSession);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct); 
    }

    private void innerUpdateSession(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        int expireMinutes = CommonUtil.getConfigValueAsInt(dbConnection, "loginSessionExpireMinutes");
        Date expireTime = CommonUtil.addTimeSpan(new Date(), Calendar.MINUTE, expireMinutes);
        String sql = "update loginloginSession";
        sql += " set expiretime = ?";
        sql += " where loginSession = ?";
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
        throw new NeoAIException("No left credits!");
    }

    private void innerCheckCredits(DBConnectionIFC dbConnection, long accountId) throws Exception {
        if(innerGetLeftCredits(dbConnection, accountId) > 0) {
            return;
        }
        throw new NeoAIException("No left credits!");
    }
}
