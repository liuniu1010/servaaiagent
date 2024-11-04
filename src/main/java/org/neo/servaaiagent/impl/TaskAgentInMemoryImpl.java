package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.util.IOUtil;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.impl.OpenAIImpl;
import org.neo.servaaibase.impl.GoogleAIImpl;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.TaskAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class TaskAgentInMemoryImpl implements TaskAgentIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TaskAgentInMemoryImpl.class);

    private TaskAgentInMemoryImpl() {
    }

    public static TaskAgentInMemoryImpl getInstance() {
        return new TaskAgentInMemoryImpl();
    }

    @Override
    public String executeTask(String session, NotifyCallbackIFC notifyCallback, String requirement) {
        String sandBoxUrl = getSandBoxUrl("task", "executecommand");
        int iterationDeep = 100;
        try {
            StorageIFC storage = StorageInMemoryImpl.getInstance();
            storage.clearChatRecords(session);
            String backgroundDesc = loadBackgroundDesc();
            // begin to executeTask
            return innerExecuteTask(session, sandBoxUrl, notifyCallback, requirement, requirement, backgroundDesc, iterationDeep);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
        finally {
            StorageIFC storage = StorageInMemoryImpl.getInstance();
            storage.clearChatRecords(session);
        }
    }

    @Override
    public String executeTask(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String requirement) {
        throw new NeoAIException("not supported");
    }

    private String innerExecuteTask(String session, String sandBoxUrl, NotifyCallbackIFC notifyCallback, String newInput, String requirement, String backgroundDesc, int iterateDeep) {
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
                for(AIModel.Call call: calls) {
                    if(!LinuxCommandCallForTaskImpl.isDefinedFunction(call.getMethodName())) {
                        continue;
                    }

                    hasCall = true;
                    if(!call.getMethodName().equals(LinuxCommandCallForTaskImpl.METHODNAME_EXECUTECOMMAND)) {
                        shouldStop = true;
                        declare = (String)promptStruct.getFunctionCall().callFunction(call);
                        if(call.getMethodName().equals(LinuxCommandCallForTaskImpl.METHODNAME_FAILTASK)) {
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
                    totalRunningResultDesc += "\nPlease continue to execute command to implement the requirement.";
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
                return innerExecuteTask(session, sandBoxUrl, notifyCallback, totalRunningResultDesc, requirement, backgroundDesc, iterateDeep - 1);
            }
            else {
                if(hasCall) {
                    return declare;
                }
                else {
                    String newHint = "You must call at least one of the three methods, executeCommand/finishTask/failTask, DONOT use multi_tool_use.parallel";
                    return innerExecuteTask(session, sandBoxUrl, notifyCallback, newHint, requirement, backgroundDesc, iterateDeep - 1);
                }
            }
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        } 
    }

    private String getSandBoxUrl(String executor, String action) {
        String configName = executor + "SandBoxUrl";
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
        systemHint += "\n\nNow, the task requirement you need to execute is:";
        systemHint += "\n" + requirement;
        promptStruct.setSystemHint(systemHint);
        promptStruct.setFunctionCall(LinuxCommandCallForTaskImpl.getInstance(session, sandBoxUrl));

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("taskModel");

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

    private String loadBackgroundDesc() throws Exception {
        String fileName = "linuxcommanderfortask.txt";
        return IOUtil.resourceFileToString(fileName);
    }
}
