package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface AccountAgentIFC {
    public void sendPassword(String username);
    public void sendPassword(DBConnectionIFC dbConnection, String username);

    public String login(String username, String password);
    public String login(DBConnectionIFC dbConnection, String username, String password);

    public boolean updateLogin(String session);
    public boolean updateLogin(DBConnectionIFC dbConnection, String session);

    public boolean checkLogin(String session);
    public boolean checkLogin(DBConnectionIFC dbConnection, String session);

    public void purchaseCredits(String session, int credits);
    public void purchaseCredits(DBConnectionIFC dbConnection, String session, int credits);

    public void consumeCredits(String session, int credits);
    public void consumeCredits(DBConnectionIFC dbConnetion, String session, int credits);

    public boolean checkCredits(String session);
    public boolean checkCredits(DBConnectionIFC dbConnection, String session);
}
