package org.neo.servaaiagent.ifc;

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

    public void purchaseCredits(String loginSession, int credits);
    public void purchaseCredits(DBConnectionIFC dbConnection, String loginSession, int credits);

    public void purchaseCredits(long accountId, int credits);
    public void purchaseCredits(DBConnectionIFC dbConnection, long accountId, int credits);

    public void consumeCredits(String loginSession, int credits);
    public void consumeCredits(DBConnectionIFC dbConnetion, String loginSession, int credits);

    public void consumeCredits(long accountId, int credits);
    public void consumeCredits(DBConnectionIFC dbConnetion, long accountId, int credits);

    public int getLeftCredits(String loginSession);
    public int getLeftCredits(DBConnectionIFC dbConnection, String loginSession);

    public int getLeftCredits(long accountId);
    public int getLeftCredits(DBConnectionIFC dbConnection, long accountId);

    public void checkCredits(String loginSession);
    public void checkCredits(DBConnectionIFC dbConnection, String loginSession);

    public void checkCredits(long accountId);
    public void checkCredits(DBConnectionIFC dbConnection, long accountId);

    public int getRegisterNumber();
    public int getRegisterNumber(DBConnectionIFC dbConnection);

    public int getOnlineNumber();
    public int getOnlineNumber(DBConnectionIFC dbConnection);
}
