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
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.impl.OpenAIImpl;
import org.neo.servaaibase.impl.GoogleAIImpl;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.CoderAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class CoderAgentInMemoryImpl implements CoderAgentIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CoderAgentInMemoryImpl.class);

    private CoderAgentInMemoryImpl() {
    }

    public static CoderAgentInMemoryImpl getInstance() {
        return new CoderAgentInMemoryImpl();
    }

    @Override
    public String generateCode(String session, String coder, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder) {
        String sandBoxUrl = getSandBoxUrl(coder, "executecommand");
        int codeIterationRounds = CommonUtil.getConfigValueAsInt("codeIterationRounds");
        int codeIterationDeep = CommonUtil.getConfigValueAsInt("codeInterationDeep");
        for(int i = 0;i < codeIterationRounds;i++) {
            try {
                // init projectFolder, clean codesession
                projectFolder = projectFolder.trim();
                String command = "mkdir -p " + projectFolder + " && rm -rf " + projectFolder + "/*";
                CommonUtil.executeCommandSandBox(session, command, sandBoxUrl);
                logger.info("command:\n" + command + "\nexecuted success in sandbox");
                StorageIFC storage = StorageInMemoryImpl.getInstance();
                storage.clearChatRecords(session);

                // begin to generate code
                return innerGenerateCode(session, sandBoxUrl, notifyCallback, requirement, requirement, backgroundDesc, codeIterationDeep);
            }
            catch(NeoAIException nex) {
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED) {
                    logger.error(nex.getMessage());
                    if(i < codeIterationRounds - 1) {
                        String information = "System: Max iteration deep exceeded, maybe we started from a wrong direction, let's reset and try again from the start point.";
                        System.out.println(information);
                        if(notifyCallback != null) {
                            notifyCallback.notify(information);
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
    public String generateCode(DBConnectionIFC dbConnection, String session, String coder, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder) {
        throw new NeoAIException("not supported");
    }

    @Override
    public String downloadCode(String session, String coder, String projectFolder) {
        try {
            String sandBoxUrl = getSandBoxUrl(coder, "download");
            String base64OfCode = CommonUtil.downloadProjectSandBox(session, projectFolder, sandBoxUrl);
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
    public String downloadCode(DBConnectionIFC dbConnection, String session, String coder, String projectFolder) {
        throw new NeoAIException("not supported");
    }

    private String innerGenerateCode(String session, String sandBoxUrl, NotifyCallbackIFC notifyCallback, String newInput, String requirement, String backgroundDesc, int iterateDeep) {
        if(iterateDeep <= 0) {
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED);
        }

        String information = "Request: " + newInput;
        System.out.println(information);
        if(notifyCallback != null) {
            notifyCallback.notify(information);
        }
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(newInput);

        AIModel.PromptStruct promptStruct = constructPromptStruct(session, sandBoxUrl, newInput, requirement, backgroundDesc);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(promptStruct);
        information = "Response: " + chatResponse.getMessage();
        System.out.println(information);
        if(notifyCallback != null) {
            notifyCallback.notify(information);
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

            AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
            newResponseRecord.setChatTime(new Date());
            newResponseRecord.setIsRequest(false);
            newResponseRecord.setContent(chatResponse.getMessage());

            StorageIFC storage = StorageInMemoryImpl.getInstance();
            storage.addChatRecord(session, newRequestRecord);
            storage.addChatRecord(session, newResponseRecord);

            if(!shouldStop) {
                return innerGenerateCode(session, sandBoxUrl, notifyCallback, totalRunningResultDesc, requirement, backgroundDesc, iterateDeep - 1);
            }
            else {
                if(hasCall) {
                    return declare;
                }
                else {
                    String newHint = "You must call at least one of the three methods, executeCommand/finishCodeGeneration/failCodeGeneration";
                    return innerGenerateCode(session, sandBoxUrl, notifyCallback, newHint, requirement, backgroundDesc, iterateDeep - 1);
                }
            }
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        } 
    }

    private String getSandBoxUrl(String coder, String action) {
        String configName = coder + "SandBoxUrl";
        return CommonUtil.getConfigValue(configName) + "/" + action;
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

    private AIModel.PromptStruct constructPromptStruct(String session, String sandBoxUrl, String newInput, String requirement, String backgroundDesc) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInMemoryImpl.getInstance();
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(session);

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
        promptStruct.setFunctionCall(CoderCallImpl.getInstance(session, sandBoxUrl));

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("codeModel");

        int retryTimesOnLLMException = CommonUtil.getConfigValueAsInt("retryTimesOnLLMException");
        int waitSeconds = CommonUtil.getConfigValueAsInt("firstWaitSecondsOnLLMException");
        for(int i = 0;i < retryTimesOnLLMException;i++) {
            try {
                return superAI.fetchChatResponse(model, promptStruct);
            }
            catch(NeoAIException nex) {
                logger.error(nex.getMessage(), nex);
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_IOEXCEPTIONWITHLLM 
                    || nex.getCode() == NeoAIException.NEOAIEXCEPTION_JSONSYNTAXERROR ) {
                    // met ioexception or syntax exception with LLM, wait some seconds and try again
                    try {
                        logger.info("Meet IOException or syntax exception from LLM, wait " + waitSeconds + " seconds and try again...");
                        Thread.sleep(1000 * waitSeconds);
                        waitSeconds = waitSeconds * 2;
                    }
                    catch(InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                    continue;
                }
                else {
                    throw nex;
                }
            }
        }
        throw new NeoAIException("Max iteration deep exceeded!");
    }
}