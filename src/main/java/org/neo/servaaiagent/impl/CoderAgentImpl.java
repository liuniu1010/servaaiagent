package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBAutoCommitSaveTaskIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.CoderAgentIFC;
import org.neo.servaaiagent.ifc.SandBoxAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class CoderAgentImpl implements CoderAgentIFC, DBAutoCommitSaveTaskIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CoderAgentImpl.class);

    private CoderAgentImpl() {
    }

    public static CoderAgentImpl getInstance() {
        return new CoderAgentImpl();
    }

    @Override
    public Object autoCommitSave(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String generateCode(String alignedSession, String coder, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeAutoCommitSaveTask(new CoderAgentImpl() {
            @Override
            public Object autoCommitSave(DBConnectionIFC dbConnection) {
                return generateCode(dbConnection, alignedSession, coder, notifyCallback, requirement, backgroundDesc, projectFolder);
            }
        });
    }

    @Override
    public String generateCode(DBConnectionIFC dbConnection, String alignedSession, String coder, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder) {
        String sandBoxUrl = getSandBoxUrl(dbConnection, coder);
        int codeIterationRounds = CommonUtil.getConfigValueAsInt(dbConnection, "codeIterationRounds");
        int codeIterationDeep = CommonUtil.getConfigValueAsInt(dbConnection, "codeInterationDeep");
        SandBoxAgentIFC sandBoxAgent = SandBoxAgentInMemoryImpl.getInstance();
        for(int i = 0;i < codeIterationRounds;i++) {
            try {
                // init projectFolder, clean codesession
                projectFolder = projectFolder.trim();
                String command = "mkdir -p " + projectFolder + " && rm -rf " + projectFolder + "/*";
                sandBoxAgent.executeCommand(alignedSession, command, sandBoxUrl);
                logger.debug("command:\n" + command + "\nexecuted success in sandbox");
                StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
                storage.clearChatRecords(alignedSession);

                // begin to generate code
                return innerGenerateCode(dbConnection, alignedSession, sandBoxUrl, notifyCallback, requirement, requirement, backgroundDesc, codeIterationDeep);
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
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED);
    }

    @Override
    public String downloadCode(String alignedSession, String coder, String projectFolder) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeAutoCommitSaveTask(new CoderAgentImpl() {
            @Override
            public Object autoCommitSave(DBConnectionIFC dbConnection) {
                return downloadCode(dbConnection, alignedSession, coder, projectFolder);
            }
        });
    }

    @Override
    public String downloadCode(DBConnectionIFC dbConnection, String alignedSession, String coder, String projectFolder) {
        try {
            SandBoxAgentIFC sandBoxAgent = SandBoxAgentInMemoryImpl.getInstance();
            String sandBoxUrl = getSandBoxUrl(dbConnection, coder);
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

    private String innerGenerateCode(DBConnectionIFC dbConnection, String alignedSession, String sandBoxUrl, NotifyCallbackIFC notifyCallback, String newInput, String requirement, String backgroundDesc, int iterateDeep) {
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

        AIModel.PromptStruct promptStruct = constructPromptStruct(dbConnection, alignedSession, sandBoxUrl, newInput, requirement, backgroundDesc);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);
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
                hasCall = true;
                for(AIModel.Call call: calls) {
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

            StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
            storage.addChatRecord(alignedSession, newRequestRecord);
            storage.addChatRecord(alignedSession, newResponseRecord);

            if(!shouldStop) {
                return innerGenerateCode(dbConnection, alignedSession, sandBoxUrl, notifyCallback, totalRunningResultDesc, requirement, backgroundDesc, iterateDeep - 1);
            }
            else {
                if(hasCall) {
                    return declare;
                }
                else {
                    String newHint = "You must call at least one of the three methods, executeCommand/finishCodeGeneration/failCodeGeneration";
                    return innerGenerateCode(dbConnection, alignedSession, sandBoxUrl, notifyCallback, newHint, requirement, backgroundDesc, iterateDeep - 1);
                }
            }
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        } 
    }

    private String getSandBoxUrl(DBConnectionIFC dbConnection, String coder) {
        String configName = coder + "SandBoxUrl";
        return CommonUtil.getConfigValue(dbConnection, configName);
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

    private AIModel.PromptStruct constructPromptStruct(DBConnectionIFC dbConnection, String alignedSession, String sandBoxUrl, String newInput, String requirement, String backgroundDesc) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
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

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(DBConnectionIFC dbConnection, AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String model = CommonUtil.getConfigValue(dbConnection, "codeModel");
        return superAI.fetchChatResponse(model, promptStruct);
    }
}
