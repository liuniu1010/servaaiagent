package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.util.IOUtil;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.impl.StorageInMemoryImpl;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.TaskAgentIFC;
import org.neo.servaaiagent.ifc.SandBoxAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class TaskAgentInMemoryImpl implements TaskAgentIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TaskAgentInMemoryImpl.class);

    private TaskAgentInMemoryImpl() {
    }

    public static TaskAgentInMemoryImpl getInstance() {
        return new TaskAgentInMemoryImpl();
    }

    @Override
    public String executeTask(String alignedSession, NotifyCallbackIFC notifyCallback, String requirement) {
        String sandBoxUrl = getSandBoxUrl("task");
        int iterationDeep = 100;
        try {
            SandBoxAgentIFC sandBoxAgent = SandBoxAgentInMemoryImpl.getInstance();
            StorageIFC storage = StorageInMemoryImpl.getInstance();
            storage.clearChatRecords(alignedSession);
            String backgroundDesc = loadBackgroundDesc(alignedSession, sandBoxUrl);
            // begin to executeTask
            String result = innerExecuteTask(alignedSession, sandBoxUrl, notifyCallback, requirement, requirement, backgroundDesc, iterationDeep);
            sandBoxAgent.terminateShell(alignedSession, sandBoxUrl);
            return result;
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
        finally {
            StorageIFC storage = StorageInMemoryImpl.getInstance();
            storage.clearChatRecords(alignedSession);
        }
    }

    @Override
    public String executeTask(DBConnectionIFC dbConnection, String alignedSession, NotifyCallbackIFC notifyCallback, String requirement) {
        throw new NeoAIException("not supported");
    }

    private String innerExecuteTask(String alignedSession, String sandBoxUrl, NotifyCallbackIFC notifyCallback, String newInput, String requirement, String backgroundDesc, int iterationDeep) {
        if(iterationDeep <= 0) {
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
                    totalRunningResultDesc += "\nPlease continue to execute command to implement the task requirement.";
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
                    return innerExecuteTask(alignedSession, sandBoxUrl, notifyCallback, totalRunningResultDesc, requirement, backgroundDesc, iterationDeep - 1);
                }
                else {
                    String newHint = "You must call at least one of the three methods, executeCommand/finishTask/failTask, DONOT use multi_tool_use.parallel";
                    return innerExecuteTask(alignedSession, sandBoxUrl, notifyCallback, newHint, requirement, backgroundDesc, iterationDeep - 1);
                }
            }
            else {
                declare = "\n<b>" + declare + "</b>";
                return declare;
            }
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        } 
    }

    private String getSandBoxUrl(String executor) {
        String configName = executor + "SandBoxUrl";
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
        systemHint += "\n\nNow, the task requirement you need to execute is:";
        systemHint += "\n" + requirement;
        promptStruct.setSystemHint(systemHint);
        promptStruct.setFunctionCall(LinuxCommandCallForTaskImpl.getInstance(alignedSession, sandBoxUrl));

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance();
        String model = CommonUtil.getConfigValue("taskModel");
        return superAI.fetchChatResponse(model, promptStruct);
    }

    private String loadBackgroundDesc(String alignedSession, String sandBoxUrl) throws Exception {
        SandBoxAgentIFC sandBoxAgent = SandBoxAgentInMemoryImpl.getInstance();
        boolean isUnix = sandBoxAgent.isUnix(alignedSession, sandBoxUrl);
        String fileName = isUnix?"linuxcommanderfortask.txt":"windowscommanderfortask.txt";
        return IOUtil.resourceFileToString(fileName);
    }
}
