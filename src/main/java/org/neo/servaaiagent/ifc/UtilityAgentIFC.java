package org.neo.servaaiagent.ifc;

import org.neo.servaaibase.model.AIModel;

public interface UtilityAgentIFC {
    public AIModel.ChatResponse generatePageCode(String prompt, String code, String theFunction);
}
