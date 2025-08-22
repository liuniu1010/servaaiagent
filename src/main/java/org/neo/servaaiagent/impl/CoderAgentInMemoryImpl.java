package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

import org.neo.servaframe.interfaces.DBConnectionIFC;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.CoderAgentIFC;
import org.neo.servaaiagent.ifc.SandBoxAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class CoderAgentInMemoryImpl implements CoderAgentIFC {
    final static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(CoderAgentInMemoryImpl.class);

    private CoderAgentInMemoryImpl() {
    }

    public static CoderAgentInMemoryImpl getInstance() {
        return new CoderAgentInMemoryImpl();
    }

    @Override
    public String generateCode(String alignedSession, String coder, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder) {
        SandBoxAgentIFC sandBoxAgent = SandBoxAgentInMemoryImpl.getInstance();
        String sandBoxUrl = getSandBoxUrl(coder);
        int codeIterationRounds = CommonUtil.getConfigValueAsInt("codeIterationRounds");
        int codeIterationDeep = CommonUtil.getConfigValueAsInt("codeInterationDeep");
        for(int i = 0;i < codeIterationRounds;i++) {
            try {
                // init projectFolder, clean codesession
                projectFolder = projectFolder.trim();
                String command = "mkdir -p " + projectFolder + " && rm -rf " + projectFolder + "/*";
                sandBoxAgent.executeCommand(alignedSession, command, sandBoxUrl);
                logger.debug("command:\n" + command + "\nexecuted success in sandbox");
                StorageIFC storage = StorageInMemoryImpl.getInstance();
                storage.clearChatRecords(alignedSession);

                // begin to generate code
                return innerGenerateCode(alignedSession, sandBoxUrl, notifyCallback, requirement, requirement, backgroundDesc, codeIterationDeep);
            }
            catch(NeoAIException nex) {
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED) {
                    logger.error(nex.getMessage());
                    if(i < codeIterationRounds - 1) {
                        String information = "System: Max iteration deep exceeded, maybe we started from a wrong direction, let's reset and try again from the start point.";
                        System.out.println(information);
                        if(notifyCallback != null) {
                            notifyCallback.notify("<br>" + CommonUtil.renderToShowAsOrigin(information));
                        }
                    }
                    continue;
                }
                else {
                    throw nex;
                }
            }
            finally {
                StorageIFC storage = StorageInMemoryImpl.getInstance();
                storage.clearChatRecords(alignedSession);
            }
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED);
    }

    @Override
    public String generateCode(DBConnectionIFC dbConnection, String alignedSession, String coder, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder) {
        throw new NeoAIException("not supported");
    }

    @Override
    public String downloadCode(String alignedSession, String coder, String projectFolder) {
        try {
            SandBoxAgentIFC sandBoxAgent = SandBoxAgentInMemoryImpl.getInstance();
            String sandBoxUrl = getSandBoxUrl(coder);
            String base64OfCode = sandBoxAgent.downloadProject(alignedSession, projectFolder, sandBoxUrl);
            sandBoxAgent.terminateShell(alignedSession, sandBoxUrl);
            return base64OfCode;
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
    }

    @Override
    public String downloadCode(DBConnectionIFC dbConnection, String alignedSession, String coder, String projectFolder) {
        throw new NeoAIException("not supported");
    }

    private String innerGenerateCode(String alignedSession, String sandBoxUrl, NotifyCallbackIFC notifyCallback, String newInput, String requirement, String backgroundDesc, int iterateDeep) {
        if(iterateDeep <= 0) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED);
        }

        String information = "Request: " + newInput;
        System.out.println(information);
        if(notifyCallback != null) {
            notifyCallback.notify("<br>" + CommonUtil.renderToShowAsOrigin(information));
        }
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(alignedSession);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(newInput);

        AIModel.PromptStruct promptStruct = constructPromptStruct(alignedSession, sandBoxUrl, newInput, requirement, backgroundDesc);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);
        information = "Response: " + chatResponse.getMessage();
        System.out.println(information);
        if(notifyCallback != null) {
            notifyCallback.notify("<br>" + CommonUtil.renderToShowAsOrigin(information));
        }
        String totalRunningResultDesc = "";
        String declare = null;
        if(chatResponse.getIsSuccess()) {
            List<AIModel.Call> calls = chatResponse.getCalls();
            boolean shouldStop = false;
            boolean hasCall = false;
            if(calls != null && calls.size() > 0) {
                for(AIModel.Call call: calls) {
                    if(!CoderCallImpl.isDefinedFunction(call.getMethodName())) {
                        continue;
                    }

                    hasCall = true;
                    if(!call.getMethodName().equals(CoderCallImpl.METHODNAME_EXECUTECOMMAND)) {
                        shouldStop = true;
                        declare = (String)promptStruct.getFunctionCall().callFunction(call);
                        if(call.getMethodName().equals(CoderCallImpl.METHODNAME_FAILCODEGENERATION)) {
                            // declare fail
                            throw new NeoAIException(declare);
                        }
                    }
                    else {
                        String runningResultDesc = (String)promptStruct.getFunctionCall().callFunction(call);
                        totalRunningResultDesc += runningResultDesc;
                    }
                }
                if(!shouldStop) {
                    totalRunningResultDesc += "\nPlease continue to adjust code to implement the requirement.";
                }
            }

            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(alignedSession);
            newResponseRecord.setChatTime(new Date());
            newResponseRecord.setIsRequest(false);
            newResponseRecord.setContent(chatResponse.getMessage());

            StorageIFC storage = StorageInMemoryImpl.getInstance();
            storage.addChatRecord(alignedSession, newRequestRecord);
            storage.addChatRecord(alignedSession, newResponseRecord);
 
            if(!shouldStop) {
                if(hasCall) {
                    return innerGenerateCode(alignedSession, sandBoxUrl, notifyCallback, totalRunningResultDesc, requirement, backgroundDesc, iterateDeep - 1);
                }
                else {
                    String newHint = "You must call at least one of the three methods, executeCommand/finishCodeGeneration/failCodeGeneration, DONOT use multi_tool_use.parallel";
                    return innerGenerateCode(alignedSession, sandBoxUrl, notifyCallback, newHint, requirement, backgroundDesc, iterateDeep - 1);
                }
            }
            else {
                return declare;
            }
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        } 
    }

    private String getSandBoxUrl(String coder) {
        String configName = coder + "SandBoxUrl";
        return CommonUtil.getConfigValue(configName);
    }

    private AIModel.Call extractFunctionCallFromChatResponse(AIModel.ChatResponse chatResponse) {
        List<AIModel.Call> calls = chatResponse.getCalls();
        if(calls == null
            || calls.size() == 0) {
            return null;
        }
        else {
            AIModel.Call call = calls.get(0);
            return call; 
        }
    }

    private AIModel.PromptStruct constructPromptStruct(String alignedSession, String sandBoxUrl, String newInput, String requirement, String backgroundDesc) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(alignedSession);

        // reduce chatrecord's content
        for(AIModel.ChatRecord chatRecord: chatRecords) {
            if(!chatRecord.getIsRequest()) {
                chatRecord.setContent("");  // reduce the content for it is useless
            }
        }

        promptStruct.setChatRecords(chatRecords);
        promptStruct.setUserInput(newInput);
        String systemHint = backgroundDesc;
        systemHint += "\n\nNow, the requirement you need to implement is:";
        systemHint += "\n" + requirement;
        promptStruct.setSystemHint(systemHint);
        promptStruct.setFunctionCall(CoderCallImpl.getInstance(alignedSession, sandBoxUrl));

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("codeModel");
        return superAI.fetchChatResponse(model, promptStruct);
    }
}
