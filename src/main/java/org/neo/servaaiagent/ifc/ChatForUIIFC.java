package org.neo.servaaiagent.ifc;

import java.util.List;

import org.neo.servaaiagent.model.AgentModel;

/*
 * this interface will return rendered result
 * which would be shown in UI side
 */
public interface ChatForUIIFC {
    public String sendAudio(AgentModel.UIParams params);
    public String fetchResponse(AgentModel.UIParams params);
    public String initNewChat(AgentModel.UIParams params);
    public String refresh(AgentModel.UIParams params);
    public String echo(AgentModel.UIParams params);
}
