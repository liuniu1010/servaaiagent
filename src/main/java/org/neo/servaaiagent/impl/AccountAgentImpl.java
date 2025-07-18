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
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AccountAgentImpl.class);
    private final static String CHASED_SOURCE_ONTOPUP = "topupOnRegister";
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
    public String loginWithOAuth(String username, String sourceIP) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return loginWithOAuth(dbConnection, username, sourceIP);
            }
        });
    }

    @Override
    public String loginWithOAuth(DBConnectionIFC dbConnection, String username, String sourceIP) {
        try {
            return innerLoginWithOAuth(dbConnection, username, sourceIP);
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
    public String getUserNameWithSession(String loginSession) {
        if(loginSession == null
            || loginSession.trim().equals("")) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_SESSION_INVALID);
        }

        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return getUserNameWithSession(dbConnection, loginSession);
            }
        });
    }

    @Override
    public String getUserNameWithSession(DBConnectionIFC dbConnection, String loginSession) {
        if(loginSession == null
            || loginSession.trim().equals("")) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_SESSION_INVALID);
        }

        try {
            return innerGetUserNameWithSession(dbConnection, loginSession);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void purchaseCreditsWithSession(String loginSession, int credits, String chasedSource) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                purchaseCreditsWithSession(dbConnection, loginSession, credits, chasedSource);
                return null;
            }
        });
    }

    @Override
    public void purchaseCreditsWithSession(DBConnectionIFC dbConnection, String loginSession, int credits, String chasedSource) {
        try {
            innerPurchaseCreditsWithSession(dbConnection, loginSession, credits, chasedSource);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void purchaseCreditsWithAccount(String accountId, int credits, String chasedSource) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                purchaseCreditsWithAccount(dbConnection, accountId, credits, chasedSource);
                return null;
            }
        });
    }

    @Override
    public void purchaseCreditsWithAccount(DBConnectionIFC dbConnection, String accountId, int credits, String chasedSource) {
        try {
            innerPurchaseCreditsWithAccount(dbConnection, accountId, credits, chasedSource, null);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void topupWithPayment(String username, int credits, String chasedSource, String transactionId) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                topupWithPayment(dbConnection, username, credits, chasedSource, transactionId);
                return null;
            }
        });
    }

    @Override
    public void topupWithPayment(DBConnectionIFC dbConnection, String username, int credits, String chasedSource, String transactionId) {
        try {
            innerTopupWithPayment(dbConnection, username, credits, chasedSource, transactionId);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void removeAccount(String username) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                removeAccount(dbConnection, username);
                return null;
            }
        });
    }

    @Override
    public void removeAccount(DBConnectionIFC dbConnection, String username) {
        try {
            innerRemoveAccount(dbConnection, username);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void consumeCreditsWithSession(String loginSession, int credits, String consumeFunction) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                consumeCreditsWithSession(dbConnection, loginSession, credits, consumeFunction);
                return null;
            }
        });
    }

    @Override
    public void consumeCreditsWithSession(DBConnectionIFC dbConnection, String loginSession, int credits, String consumeFunction) {
        try {
            innerConsumeCreditsWithSession(dbConnection, loginSession, credits, consumeFunction);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void consumeCreditsWithAccount(String accountId, int credits, String consumeFunction) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                consumeCreditsWithAccount(dbConnection, accountId, credits, consumeFunction);
                return null;
            }
        });
    }

    @Override
    public void consumeCreditsWithAccount(DBConnectionIFC dbConnection, String accountId, int credits, String consumeFunction) {
        try {
            innerConsumeCreditsWithAccount(dbConnection, accountId, credits, consumeFunction);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public int getLeftCreditsWithSession(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (int)dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return getLeftCreditsWithSession(dbConnection, loginSession);
            }
        });
    }

    @Override
    public int getLeftCreditsWithSession(DBConnectionIFC dbConnection, String loginSession) {
        try {
            return innerGetLeftCreditsWithSession(dbConnection, loginSession);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public int getLeftCreditsWithAccount(String accountId) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (int)dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                return getLeftCreditsWithAccount(dbConnection, accountId);
            }
        });
    }

    @Override
    public int getLeftCreditsWithAccount(DBConnectionIFC dbConnection, String accountId) {
        try {
            return innerGetLeftCreditsWithAccount(dbConnection, accountId);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void checkCreditsWithSession(String loginSession) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                checkCreditsWithSession(dbConnection, loginSession);
                return null;
            }
        });
    }

    @Override
    public void checkCreditsWithSession(DBConnectionIFC dbConnection, String loginSession) {
        try {
            innerCheckCreditsWithSession(dbConnection, loginSession);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public void checkCreditsWithAccount(String accountId) {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeQueryTask(new AccountAgentImpl() {
            @Override
            public Object query(DBConnectionIFC dbConnection) {
                checkCreditsWithAccount(dbConnection, accountId);
                return null;
            }
        });
    }

    @Override
    public void checkCreditsWithAccount(DBConnectionIFC dbConnection, String accountId) {
        try {
            innerCheckCreditsWithAccount(dbConnection, accountId);
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

    private String getAccountIdFromUsername(DBConnectionIFC dbConnection, String username) throws Exception {
        if(!CommonUtil.isValidEmail(username)){
            throw new NeoAIException("not a valid email address!");
        }

        String standardEmailAddress = username.trim().toLowerCase();

        // check if this account is already exist
        String sql = "select id";
        sql += " from useraccount";
        sql += " where username = ?";
        List<Object> params = new ArrayList<Object>();
        params.add(standardEmailAddress);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        return (String)dbConnection.queryScalar(sqlStruct);
    }

    private String registerNewUserOrUpdateIfExist(DBConnectionIFC dbConnection, String username, String sourceIP, boolean updatePassword) throws Exception {
        String accountId = getAccountIdFromUsername(dbConnection, username);
        String standardEmailAddress = username.trim().toLowerCase();
        String password = CommonUtil.getRandomString(6);
        String encryptedPassword = CommonUtil.getSaltedHash(password);

        AgentModel.UserAccount userAccount = null;
        if(accountId == null) {
            int iValue = CommonUtil.getConfigValueAsInt(dbConnection, "verifyMaxRegisterNumber");
            if(iValue != 0) {
                int maxRegisterNumber = CommonUtil.getConfigValueAsInt(dbConnection, "maxRegisterNumber");
                int registerNumber = getRegisterNumber(dbConnection);
                if(registerNumber >= maxRegisterNumber) {
                    throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_MAXREGISTERNUMBER_EXCEED);
                }
            }

            userAccount = new AgentModel.UserAccount(standardEmailAddress);
            userAccount.setEncryptedPassword(encryptedPassword);
            userAccount.setRegistTime(new Date());
            userAccount.setIP(sourceIP);
            dbConnection.insert(userAccount.getVersionEntity());

            // for new register user, auto topup some initial credits for trying
            int topupCredits = CommonUtil.getConfigValueAsInt(dbConnection, "topupOnRegister");
            innerPurchaseCreditsWithAccount(dbConnection, userAccount.getId(), topupCredits, CHASED_SOURCE_ONTOPUP, null);
            return password;
        }
        else if(updatePassword) {
            VersionEntity versionEntity = dbConnection.loadVersionEntityById(AgentModel.UserAccount.ENTITYNAME, accountId);
            userAccount = new AgentModel.UserAccount(versionEntity);
            userAccount.setEncryptedPassword(encryptedPassword);
            dbConnection.update(userAccount.getVersionEntity());
            return password;
        }

        return null;
    }

    private void innerSendPassword(DBConnectionIFC dbConnection, String username, String sourceIP) throws Exception {
        String password = registerNewUserOrUpdateIfExist(dbConnection, username, sourceIP, true);
        String standardEmailAddress = username.trim().toLowerCase();

        // send password to user
        String subject = "Your Password for NeoAI";
        String body = "Your new password to login to Neo AI is: <br><b>" + password + "</b>";

        EmailAgentIFC emailAgent = EmailAgentImpl.getInstance();
        emailAgent.sendEmail(dbConnection, standardEmailAddress, subject, body);
    }

    private void preLoginCheck(DBConnectionIFC dbConnection, String username) throws Exception {
        if(!CommonUtil.isValidEmail(username)){
            throw new NeoAIException("not a valid email address!");
        }

        int iValue = CommonUtil.getConfigValueAsInt(dbConnection, "verifyMaxOnlineNumber");
        if(iValue != 0) {
            int maxOnlineNumber = CommonUtil.getConfigValueAsInt(dbConnection, "maxOnlineNumber");
            int onlineNumber = getOnlineNumber(dbConnection);
            if(onlineNumber >= maxOnlineNumber) {
                throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_MAXONLINENUMBER_EXCEED);
            }
        }
    }

    private String generateLoginSession(DBConnectionIFC dbConnection, String accountId, String sourceIP) throws Exception {
        String loginSession = CommonUtil.getRandomString(10);
        int expireMinutes = CommonUtil.getConfigValueAsInt(dbConnection, "sessionExpireMinutes");
        Date expireTime = CommonUtil.addTimeSpan(new Date(), Calendar.MINUTE, expireMinutes);

        AgentModel.LoginSession modelLoginSession = new AgentModel.LoginSession(loginSession);
        modelLoginSession.setAccountId(accountId);
        modelLoginSession.setExpireTime(expireTime);
        modelLoginSession.setIP(sourceIP);
        modelLoginSession.setIsDeleted(false);
        dbConnection.insert(modelLoginSession.getVersionEntity());

        return loginSession;
    }

    private String getAccountIdWithLocalLoginVerify(DBConnectionIFC dbConnection, String username, String password) throws Exception {
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

        return userAccount.getId();
    }

    private String getAccountIdWithOAuthLogin(DBConnectionIFC dbConnection, String username, String sourceIP) throws Exception {
        registerNewUserOrUpdateIfExist(dbConnection, username, sourceIP, false);
        return getAccountIdFromUsername(dbConnection, username);
    }

    private String innerLogin(DBConnectionIFC dbConnection, String username, String password, String sourceIP) throws Exception {
        preLoginCheck(dbConnection, username);
        String accountId = getAccountIdWithLocalLoginVerify(dbConnection, username, password);
        return generateLoginSession(dbConnection, accountId, sourceIP);
    }

    private String innerLoginWithOAuth(DBConnectionIFC dbConnection, String username, String sourceIP) throws Exception {
        preLoginCheck(dbConnection, username);
        String accountId = getAccountIdWithOAuthLogin(dbConnection, username, sourceIP);
        return generateLoginSession(dbConnection, accountId, sourceIP);
    }

    private String getAccountId(DBConnectionIFC dbConnection, String loginSession) throws Exception {
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
        return oResult.toString();
    }

    private String getUserName(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        String sql = "select ua.username as username";
        sql += " from useraccount ua";
        sql += " join loginsession ls on ls.accountid = ua.id";
        sql += " where ls.session = ?";
        sql += " and ls.expiretime > ?";
        sql += " and ls.isdeleted = 0";

        List<Object> params = new ArrayList<Object>();
        params.add(loginSession);
        params.add(new Date());

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        
        Object oResult = dbConnection.queryScalar(sqlStruct);
        if(oResult == null) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_SESSION_INVALID);
        }
        return oResult.toString();
    }

    private void innerCheckSessionValid(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        getAccountId(dbConnection, loginSession);
    }

    private String innerGetUserNameWithSession(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        return getUserName(dbConnection, loginSession);
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

    private void innerPurchaseCreditsWithSession(DBConnectionIFC dbConnection, String loginSession, int credits, String chasedSource) throws Exception {
        String accountId = getAccountId(dbConnection, loginSession);
        innerPurchaseCreditsWithAccount(dbConnection, accountId, credits, chasedSource, null);
    }

    private void innerPurchaseCreditsWithAccount(DBConnectionIFC dbConnection, String accountId, int credits, String chasedSource, String transactionId) throws Exception {
        if(transactionId != null 
            && !transactionId.trim().equals("")) {
            // ensure idempotency
            String sql = "select id";
            sql += " from chasedcredits";
            sql += " where transactionid = ?";

            List<Object> params = new ArrayList<Object>();
            params.add(transactionId);

            SQLStruct sqlStruct = new SQLStruct(sql, params);
            Object id = dbConnection.queryScalar(sqlStruct);
            if(id != null) {
                return;
            }
        }
        int expireMonths = CommonUtil.getConfigValueAsInt(dbConnection, "creditsExpireMonths");
        Date createTime = new Date();
        Date expireTime = CommonUtil.addTimeSpan(createTime, Calendar.MONTH, expireMonths);
        AgentModel.ChasedCredits chasedCredits = new AgentModel.ChasedCredits(accountId);
        chasedCredits.setCredits(credits);
        chasedCredits.setCreateTime(createTime);
        chasedCredits.setExpireTime(expireTime);
        chasedCredits.setChasedSource(chasedSource);
        chasedCredits.setTransactionId(transactionId);

        dbConnection.insert(chasedCredits.getVersionEntity());
    }

    private void innerTopupWithPayment(DBConnectionIFC dbConnection, String username, int credits, String chasedSource, String transactionId) throws Exception {
        String accountId = getAccountIdFromUsername(dbConnection, username);

        if(accountId == null) {
            // in case the account not exists, create a new one and topup a default credits first
            String standardEmailAddress = username.trim().toLowerCase();
            String password = CommonUtil.getRandomString(6);
            String encryptedPassword = CommonUtil.getSaltedHash(password);

            AgentModel.UserAccount userAccount = new AgentModel.UserAccount(standardEmailAddress);
            userAccount.setEncryptedPassword(encryptedPassword);
            userAccount.setRegistTime(new Date());
            userAccount.setIP("");
            dbConnection.insert(userAccount.getVersionEntity());

            int topupCredits = CommonUtil.getConfigValueAsInt(dbConnection, "topupOnRegister");
            accountId = userAccount.getId();
            innerPurchaseCreditsWithAccount(dbConnection, accountId, topupCredits, CHASED_SOURCE_ONTOPUP, null);
        }

        // topup with the payment amount
        innerPurchaseCreditsWithAccount(dbConnection, accountId, credits, chasedSource, transactionId);
    }

    private void removeLoginSessionsByAccountId(DBConnectionIFC dbConnection, String accountId) throws Exception {
        String sql = "delete from loginsession";
        sql += " where accountid = ?";
        
        List<Object> params = new ArrayList<Object>();
        params.add(accountId);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct); 
    }

    private void removeChasedCreditsByAccountId(DBConnectionIFC dbConnection, String accountId) throws Exception {
        String sql = "delete from chasedcredits";
        sql += " where accountid = ?";
        
        List<Object> params = new ArrayList<Object>();
        params.add(accountId);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct); 
    }

    private void removeConsumedCreditsByAccountId(DBConnectionIFC dbConnection, String accountId) throws Exception {
        String sql = "delete from consumedcredits";
        sql += " where accountid = ?";
        
        List<Object> params = new ArrayList<Object>();
        params.add(accountId);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct); 
    }

    private void innerRemoveAccount(DBConnectionIFC dbConnection, String username) throws Exception {
        String accountId = getAccountIdFromUsername(dbConnection, username);
        if(accountId == null) {
            throw new NeoAIException("no such account: " + username);
        } 

        removeLoginSessionsByAccountId(dbConnection, accountId);
        removeChasedCreditsByAccountId(dbConnection, accountId);
        removeConsumedCreditsByAccountId(dbConnection, accountId);

        String sql = "delete from useraccount";
        sql += " where id = ?";
        
        List<Object> params = new ArrayList<Object>();
        params.add(accountId);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct); 
    }

    private void innerConsumeCreditsWithSession(DBConnectionIFC dbConnection, String loginSession, int credits, String consumeFunction) throws Exception {
        String accountId = getAccountId(dbConnection, loginSession);
        innerConsumeCreditsWithAccount(dbConnection, accountId, credits, consumeFunction);
    }

    private void innerConsumeCreditsWithAccount(DBConnectionIFC dbConnection, String accountId, int credits, String consumeFunction) throws Exception {
        if(credits <= 0) {
            return;
        }
        Date consumeTime = new Date();
        AgentModel.ConsumedCredits consumedCredits = new AgentModel.ConsumedCredits(accountId);
        consumedCredits.setCredits(credits);
        consumedCredits.setConsumeTime(consumeTime);
        consumedCredits.setConsumeFunction(consumeFunction);

        dbConnection.insert(consumedCredits.getVersionEntity());
    }

    private int innerGetLeftCreditsWithSession(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        String accountId = getAccountId(dbConnection, loginSession);
        return innerGetLeftCreditsWithAccount(dbConnection, accountId);
    }

    private int innerGetLeftCreditsWithAccount(DBConnectionIFC dbConnection, String accountId) throws Exception {
        int chasedSum = getChasedCreditsWithAccount(dbConnection, accountId);
        int consumedSum = getConsumedCreditsWithAccount(dbConnection, accountId);

        return (chasedSum - consumedSum);
    }

    private int getChasedCreditsWithAccount(DBConnectionIFC dbConnection, String accountId) throws Exception {
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

    private int getConsumedCreditsWithAccount(DBConnectionIFC dbConnection, String accountId) throws Exception {
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

    private void innerCheckCreditsWithSession(DBConnectionIFC dbConnection, String loginSession) throws Exception {
        if(innerGetLeftCreditsWithSession(dbConnection, loginSession) > 0) {
            return;
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_NOCREDITS_LEFT);
    }

    private void innerCheckCreditsWithAccount(DBConnectionIFC dbConnection, String accountId) throws Exception {
        if(innerGetLeftCreditsWithAccount(dbConnection, accountId) > 0) {
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
        sql += " where ls.expiretime > ?";
        sql += " and ls.isdeleted = 0";

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
