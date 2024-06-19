package org.neo.servaaiagent.impl;

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
    public boolean checkLogin(String session) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (boolean)dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return checkLogin(dbConnection, session);
            }
        });
    }

    @Override
    public boolean checkLogin(DBConnectionIFC dbConnection, String session) {
        return false;
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
    public void consumeCredits(DBConnectionIFC dbConnetion, String session, int credits) {
    }

    @Override
    public boolean checkCredits(String session) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (boolean)dbService.executeSaveTask(new AccountAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return checkCredits(dbConnection, session);
            }
        });
    }

    @Override
    public boolean checkCredits(DBConnectionIFC dbConnection, String session) {
        return false;
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
        
        String standardExceptionMessage = "Your input username and password are not matched!";
        VersionEntity versionEntity = dbConnection.querySingleAsVersionEntity(AgentModel.UserAccount.ENTITYNAME, sqlStruct);
        if(versionEntity == null) {
            throw new NeoAIException(standardExceptionMessage);
        }

        AgentModel.UserAccount userAccount = new AgentModel.UserAccount(versionEntity);

        if(!CommonUtil.checkPassword(password, userAccount.getEncryptedPassword())) {
            throw new NeoAIException(standardExceptionMessage);
        }

        // passed, generate login session
        String session = CommonUtil.getRandomString(8);

        AgentModel.LoginSession loginSession = new AgentModel.LoginSession(session);
        loginSession.setAccountId(userAccount.getId());
        loginSession.setExpireTime(new Date());
        dbConnection.insert(loginSession.getVersionEntity());

        return session;
    }
}
