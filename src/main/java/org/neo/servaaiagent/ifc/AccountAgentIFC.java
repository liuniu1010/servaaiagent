package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface AccountAgentIFC {
    public void sendPassword(String username);
    public void sendPassword(DBConnectionIFC dbConnection, String username);

    public String login(String username, String password);
    public String login(DBConnectionIFC dbConnection, String username, String password);

    public void logout(String session);
    public void logout(DBConnectionIFC dbConnection, String session);

    public void updateLogin(String session);
    public void updateLogin(DBConnectionIFC dbConnection, String session);

    public void checkLogin(String session);
    public void checkLogin(DBConnectionIFC dbConnection, String session);

    public void purchaseCredits(String session, int credits);
    public void purchaseCredits(DBConnectionIFC dbConnection, String session, int credits);

    public void purchaseCredits(long accountId, int credits);
    public void purchaseCredits(DBConnectionIFC dbConnection, long accountId, int credits);

    public void consumeCredits(String session, int credits);
    public void consumeCredits(DBConnectionIFC dbConnetion, String session, int credits);

    public void consumeCredits(long accountId, int credits);
    public void consumeCredits(DBConnectionIFC dbConnetion, long accountId, int credits);

    public int getLeftCredits(String session);
    public int getLeftCredits(DBConnectionIFC dbConnection, String session);

    public int getLeftCredits(long accountId);
    public int getLeftCredits(DBConnectionIFC dbConnection, long accountId);

    public void checkCredits(String session);
    public void checkCredits(DBConnectionIFC dbConnection, String session);

    public void checkCredits(long accountId);
    public void checkCredits(DBConnectionIFC dbConnection, long accountId);
}
