package org.neo.servaaiagent.ifc;

import java.util.List;

/*
 * this interface will return rendered result
 * which would be shown in UI side
 */
public interface ChatForUIIFC {
    public String fetchResponse(String session, String userInput, List<String> attachFiles);
    public String initNewChat(String session);
    public String refresh(String session);
    public String echo(String session, String userInput);
}
