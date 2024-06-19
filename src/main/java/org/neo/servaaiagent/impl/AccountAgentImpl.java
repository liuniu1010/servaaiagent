package org.neo.servaaiagent.impl;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.AccountAgentIFC;

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
        return null;
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

    private void innerSendPassword(DBConnectionIFC dbConnection, String username) {
        if(!CommonUtil.isValidEmail(username)){
            throw new NeoAIException("not a valid email address!");
        }

        String standardEmailAddress = username.trim().toLowerCase();
        String password = CommonUtil.getRandomString(6);
        String encryptedPassword = CommonUtil.getSaltedHash(password);

        // check if this account is already exist
        String sql = "select id";
    }
}
