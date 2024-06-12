package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Date;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.ServiceFactory;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.SuperAIIFC;
import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.factory.AIFactory;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.impl.OpenAIImpl;
import org.neo.servaaibase.impl.GoogleAIImpl;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.CoderAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class CoderAgentImpl implements CoderAgentIFC, DBSaveTaskIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CoderAgentImpl.class);

    private CoderAgentImpl() {
    }

    public static CoderAgentImpl getInstance() {
        return new CoderAgentImpl();
    }

    @Override
    public Object save(DBConnectionIFC dbConnection) {
        return null;
    }

    @Override
    public String generateCode(String session, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new CoderAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return generateCode(dbConnection, session, notifyCallback, requirement, backgroundDesc, projectFolder);
            }
        });
    }

    @Override
    public String generateCode(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String requirement, String backgroundDesc, String projectFolder) {
        int retryTimes = 2;
        int iterateDeep = 15;
        for(int i = 0;i < retryTimes;i++) {
            try {
                // init projectFolder, clean codesession
                projectFolder = projectFolder.trim();
                String command = "mkdir -p " + projectFolder + " && rm -rf " + projectFolder + "/*";
                CommonUtil.executeCommandInSandBox(command, "");
                logger.info("command:\n" + command + "\nexecuted success in sandbox");
                StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
                storage.clearChatRecords(session);

                // begin to generate code
                return innerGenerateCode(dbConnection, session, notifyCallback, requirement, requirement, backgroundDesc, iterateDeep);
            }
            catch(NeoAIException nex) {
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED) {
                    logger.error(nex.getMessage());
                    continue;
                }
                else {
                    throw nex;
                }
            }
        }
        throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_MAXITERATIONDEEP_EXCEED);
    }

    public String innerGenerateCode(DBConnectionIFC dbConnection, String session, NotifyCallbackIFC notifyCallback, String newInput, String requirement, String backgroundDesc, int iterateDeep) {
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

        AIModel.PromptStruct promptStruct = constructPromptStruct(dbConnection, session, newInput, requirement, backgroundDesc);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);
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

            StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
            storage.addChatRecord(session, newRequestRecord);
            storage.addChatRecord(session, newResponseRecord);

            if(!shouldStop) {
                return innerGenerateCode(dbConnection, session, notifyCallback, totalRunningResultDesc, requirement, backgroundDesc, iterateDeep - 1);
            }
            else {
                if(hasCall) {
                    return declare;
                }
                else {
                    String newHint = "You must call at least one of the three methods, executeCommand/finishCodeGeneration/failCodeGeneration";
                    return innerGenerateCode(dbConnection, session, notifyCallback, newHint, requirement, backgroundDesc, iterateDeep - 1);
                }
            }
        }
        else {
            throw new NeoAIException(chatResponse.getMessage());
        } 
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

    private AIModel.PromptStruct constructPromptStruct(DBConnectionIFC dbConnection, String session, String newInput, String requirement, String backgroundDesc) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(session);
        promptStruct.setChatRecords(chatRecords);
        promptStruct.setUserInput(newInput);
        String systemHint = backgroundDesc;
        systemHint += "\n\nNow, the requirement you need to implement is:";
        systemHint += "\n" + requirement;
        promptStruct.setSystemHint(systemHint);
        promptStruct.setFunctionCall(CoderCallImpl.getInstance());

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(DBConnectionIFC dbConnection, AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = OpenAIImpl.getInstance(dbConnection);
        String model = OpenAIImpl.gpt_4o;
        // SuperAIIFC superAI = GoogleAIImpl.getInstance(dbConnection);
        // String model = GoogleAIImpl.gemini_1_5_pro_latest;

        int tryTime = 5;
        int waitSeconds = 2; // first as 2 seconds
        for(int i = 0;i < tryTime;i++) {
            try {
                return superAI.fetchChatResponse(model, promptStruct);
            }
            catch(NeoAIException nex) {
                logger.error(nex.getMessage(), nex);
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_JSONSYNTAXERROR) {
                    // sometimes LLM might generate error json which cannot be handled
                    // try once more in this case
                    logger.info("Meet json syntax error from LLM, try again...");
                    continue;
                }
                if(nex.getCode() == NeoAIException.NEOAIEXCEPTION_IOEXCEPTIONWITHLLM) {
                    // met ioexception with LLM, wait some seconds and try again
                    try {
                        logger.info("Meet IOException from LLM, wait " + waitSeconds + " seconds and try again...");
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
        throw new NeoAIException("failed to generate code");
    }
}
