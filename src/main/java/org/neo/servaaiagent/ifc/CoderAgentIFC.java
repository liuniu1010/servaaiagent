package org.neo.servaaiagent.ifc;

import org.neo.servaframe.interfaces.DBConnectionIFC;

public interface CoderAgentIFC {
    public String generateCode(String session, String inputInstruction);
    public String generateCode(DBConnectionIFC dbConnection, String session, String inputInstruction);
}
