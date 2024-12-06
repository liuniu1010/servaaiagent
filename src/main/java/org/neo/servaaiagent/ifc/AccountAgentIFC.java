package org.neo.servaaiagent.ifc;

import java.util.List;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface AccountAgentIFC {
    public void sendPassword(String username, String sourceIP);
    public void sendPassword(DBConnectionIFC dbConnection, String username, String sourceIP);

    public String login(String username, String password, String sourceIP);
    public String login(DBConnectionIFC dbConnection, String username, String password, String sourceIP);

    public void logout(String loginSession);
    public void logout(DBConnectionIFC dbConnection, String loginSession);

    public void updateSession(String loginSession);
    public void updateSession(DBConnectionIFC dbConnection, String loginSession);

    public void checkSessionValid(String loginSession);
    public void checkSessionValid(DBConnectionIFC dbConnection, String loginSession);

    public void purchaseCreditsWithSession(String loginSession, int credits);
    public void purchaseCreditsWithSession(DBConnectionIFC dbConnection, String loginSession, int credits);

    public void purchaseCreditsWithAccount(String accountId, int credits);
    public void purchaseCreditsWithAccount(DBConnectionIFC dbConnection, String accountId, int credits);

    public void consumeCreditsWithSession(String loginSession, int credits);
    public void consumeCreditsWithSession(DBConnectionIFC dbConnetion, String loginSession, int credits);

    public void consumeCreditsWithAccount(String accountId, int credits);
    public void consumeCreditsWithAccount(DBConnectionIFC dbConnetion, String accountId, int credits);

    public int getLeftCreditsWithSession(String loginSession);
    public int getLeftCreditsWithSession(DBConnectionIFC dbConnection, String loginSession);

    public int getLeftCreditsWithAccount(String accountId);
    public int getLeftCreditsWithAccount(DBConnectionIFC dbConnection, String accountId);

    public void checkCreditsWithSession(String loginSession);
    public void checkCreditsWithSession(DBConnectionIFC dbConnection, String loginSession);

    public void checkCreditsWithAccount(String accountId);
    public void checkCreditsWithAccount(DBConnectionIFC dbConnection, String accountId);

    public int getRegisterNumber();
    public int getRegisterNumber(DBConnectionIFC dbConnection);

    public List<String> getRegisterUsers();
    public List<String> getRegisterUsers(DBConnectionIFC dbConnection);

    public int getOnlineNumber();
    public int getOnlineNumber(DBConnectionIFC dbConnection);

    public List<String> getOnlineUsers();
    public List<String> getOnlineUsers(DBConnectionIFC dbConnection);
}
