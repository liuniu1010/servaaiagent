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
    public void updateLogin(String session) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                updateLogin(dbConnection, session);
                return null;
            }
        });
    }

    @Override
    public void updateLogin(DBConnectionIFC dbConnection, String session) {
        try {
            innerUpdateLogin(dbConnection, session);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void checkLogin(String session) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                checkLogin(dbConnection, session);
                return null;
            }
        });
    }

    @Override
    public void checkLogin(DBConnectionIFC dbConnection, String session) {
        try {
            innerCheckLogin(dbConnection, session);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void purchaseCredits(String session, int credits) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                purchaseCredits(dbConnection, session, credits);
                return null;
            }
        });
    }

    @Override
    public void purchaseCredits(DBConnectionIFC dbConnection, String session, int credits) {
        try {
            innerPurchaseCredits(dbConnection, session, credits);
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
    public void consumeCredits(String session, int credits) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                consumeCredits(dbConnection, session, credits);
                return null;
            }
        });
    }

    @Override
    public void consumeCredits(DBConnectionIFC dbConnection, String session, int credits) {
        try {
            innerConsumeCredits(dbConnection, session, credits);
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
    public int getLeftCredits(String session) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (int)dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return getLeftCredits(dbConnection, session);
            }
        });
    }

    @Override
    public int getLeftCredits(DBConnectionIFC dbConnection, String session) {
        try {
            return innerGetLeftCredits(dbConnection, session);
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
    public void checkCredits(String session) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                checkCredits(dbConnection, session);
                return null;
            }
        });
    }

    @Override
    public void checkCredits(DBConnectionIFC dbConnection, String session) {
        try {
            innerCheckCredits(dbConnection, session);
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

        // passed, generate login session
        String session = CommonUtil.getRandomString(8);
        int expireMinutes = CommonUtil.getConfigValueAsInt(dbConnection, "sessionExpireMinutes");
        Date expireTime = CommonUtil.addTimeSpan(new Date(), Calendar.MINUTE, expireMinutes);

        AgentModel.LoginSession loginSession = new AgentModel.LoginSession(session);
        loginSession.setAccountId(userAccount.getId());
        loginSession.setExpireTime(expireTime);
        dbConnection.insert(loginSession.getVersionEntity());

        return session;
    }

    private long getAccountId(DBConnectionIFC dbConnection, String session) throws Exception {
        String sql = "select accountid";
        sql += " from loginsession";
        sql += " where session = ?";
        sql += " and expiretime > ?";

        List<Object> params = new ArrayList<Object>();
        params.add(session);
        params.add(new Date());

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        
        Object oResult = dbConnection.queryScalar(sqlStruct);
        if(oResult == null) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_SESSION_INVALID);
        }
        return Long.parseLong(oResult.toString());
    }

    private void innerCheckLogin(DBConnectionIFC dbConnection, String session) throws Exception {
        getAccountId(dbConnection, session);
    }

    private void innerUpdateLogin(DBConnectionIFC dbConnection, String session) throws Exception {
        int expireMinutes = CommonUtil.getConfigValueAsInt(dbConnection, "sessionExpireMinutes");
        Date expireTime = CommonUtil.addTimeSpan(new Date(), Calendar.MINUTE, expireMinutes);
        String sql = "update loginsession";
        sql += " set expiretime = ?";
        sql += " where session = ?";
        List<Object> params = new ArrayList<Object>();
        params.add(expireTime);
        params.add(session);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct); 
    }

    private void innerPurchaseCredits(DBConnectionIFC dbConnection, String session, int credits) throws Exception {
        long accountId = getAccountId(dbConnection, session);
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

    private void innerConsumeCredits(DBConnectionIFC dbConnection, String session, int credits) throws Exception {
        long accountId = getAccountId(dbConnection, session);
        innerConsumeCredits(dbConnection, accountId, credits);
    }

    private void innerConsumeCredits(DBConnectionIFC dbConnection, long accountId, int credits) throws Exception {
        Date consumeTime = new Date();
        AgentModel.ConsumedCredits consumedCredits = new AgentModel.ConsumedCredits(accountId);
        consumedCredits.setCredits(credits);
        consumedCredits.setConsumeTime(consumeTime);

        dbConnection.insert(consumedCredits.getVersionEntity());
    }

    private int innerGetLeftCredits(DBConnectionIFC dbConnection, String session) throws Exception {
        long accountId = getAccountId(dbConnection, session);
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

    private void innerCheckCredits(DBConnectionIFC dbConnection, String session) throws Exception {
        if(innerGetLeftCredits(dbConnection, session) > 0) {
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
