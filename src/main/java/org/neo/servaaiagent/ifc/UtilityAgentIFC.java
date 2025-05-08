package org.neo.servaaiagent.ifc;

import org.neo.servaaibase.model.AIModel;

public interface UtilityAgentIFC {
    public AIModel.ChatResponse generatePageCode(String userInput, String fileContent);
    public AIModel.ChatResponse generatePageCodeWithJob(String requirement, String code);
}
