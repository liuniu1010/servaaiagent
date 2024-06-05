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
import org.neo.servaaibase.impl.StorageInDBImpl;

import org.neo.servaaiagent.ifc.CoderAgentIFC;

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
    public String generateCode(String session, String requirement) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new CoderAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return generateCode(dbConnection, session, requirement);
            }
        });
    }

    @Override
    public String generateCode(DBConnectionIFC dbConnection, String session, String requirement) {
        return innerGenerateCode(dbConnection, session, requirement, requirement);
    }

    public String innerGenerateCode(DBConnectionIFC dbConnection, String session, String requirement, String newInput) {
        System.out.println("input for Coder = " + newInput);
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(newInput);

        AIModel.PromptStruct promptStruct = constructPromptStruct(dbConnection, session, requirement, newInput);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);
        String totalRunningResultDesc = "";
        if(chatResponse.getIsSuccess()) {
            List<AIModel.Call> calls = chatResponse.getCalls();
            boolean shouldStop = true;
            if(calls != null && calls.size() > 0) {
                for(AIModel.Call call: calls) {
                    if(call.getMethodName().equals(CoderCallImpl.METHODNAME_EXECUTECOMMAND)) {
                        shouldStop = false;
                    }
                    String runningResultDesc = (String)promptStruct.getFunctionCall().callFunction(call);
                    totalRunningResultDesc += runningResultDesc;
                }
                if(!shouldStop) {
                    totalRunningResultDesc += "\nPlease continue to write code to implement the requirement.";
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
                return innerGenerateCode(dbConnection, session, requirement, totalRunningResultDesc);
            }
            else {
                return chatResponse.getMessage();
            }
        }
        else {
            throw new RuntimeException(chatResponse.getMessage());
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

    private AIModel.PromptStruct constructPromptStruct(DBConnectionIFC dbConnection, String session, String requirement, String newInput) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(session);
        promptStruct.setChatRecords(chatRecords);
        promptStruct.setUserInput(newInput);
        String systemHint = "You are a profession java coder, expecially good at develop software under linux with command line tools";
        systemHint += "\nThe project path is /tmp/project1, all source code should be generated under this folder.";
        systemHint += "\njava and mvn has been installed, please generate your project based on maven.";
        systemHint += "\nYou can use any command to create/update/delete files with preferred content.";
        systemHint += "\nYou can use any command to fetch any information under the project folder.";
        systemHint += "\nFor example, run 'find /tmp/project1 -type f' to get all files recursively under project folder.";
        systemHint += "\nrun \"mkdir -p /tmp/project/com/demo/example to create folder in need.\"";
        systemHint += "\nrun \"echo 'file contents' > /tmp/project1/com/demo/example/file1.txt\" to generate file with preferred content.";
        systemHint += "\nrun \"cat /tmp/project1/com/demo/example/file1.txt\" to check the file content.";
        systemHint += "\nThe functionCall provides three functions which you can use:";
        systemHint += "\nFunction 'executeCommand' is to execute any command you need.";
        systemHint += "\nFunction 'finishCodeGeneration' is to declare that all code necessary are generated, ready to compile and test.";
        systemHint += "\nFunction 'failCodeGeneration' is to declare that you cannot generate code for the specified requirement.";
        systemHint += "\nFollow these steps to write code:";
        systemHint += "\n1. generate necessary java code";
        systemHint += "\n2. generate resource files if needed";
        systemHint += "\n3. generate junit java code which is to verfity functions in main code";
        systemHint += "\n4. generate necessary pom.xml to ensure we should use mvn command to build the code";
        systemHint += "\n5. run 'mvn test-compile' to ensure all code are compilable";
        systemHint += "\n6. run 'mvn test' to ensure all test cases passed";
        systemHint += "\nin cases if you met any exception in previous steps, please ajust code and try again until the code pass test";
        systemHint += "\n7. generate a Readme.md file under /tmp/project1/ to summarize the source code";
        systemHint += ", including function description";
        systemHint += ", environment requirement such as OS, java version, maven version, unit test steps.";
        systemHint += "\n";
        systemHint += "\n";
        systemHint += "\nNow, the requirement what you need to implment is: " + requirement;
        promptStruct.setSystemHint(systemHint);
        promptStruct.setFunctionCall(CoderCallImpl.getInstance());

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(DBConnectionIFC dbConnection, AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getChatModels();
        int tryTime = 2;
        for(int i = 0;i < tryTime;i++) {
            try {
                return superAI.fetchChatResponse(models[0], promptStruct);
            }
            catch(Exception ex) {
                // sometime LLM might generate error json which cannot be handled
                // try once more
                logger.info(ex.getMessage(), ex);
                continue;
            }
        }
        throw new RuntimeException("failed to generate code");
    }
}
