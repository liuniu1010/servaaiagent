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
    public String generateCode(String session, String inputInstruction) {
        // no input dbConnection, start/commmit transaction itself
        DBServiceIFC dbService = ServiceFactory.getDBService();
        return (String)dbService.executeSaveTask(new CoderAgentImpl() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                return generateCode(dbConnection, session, inputInstruction);
            }
        });
    }

    @Override
    public String generateCode(DBConnectionIFC dbConnection, String session, String inputInstruction) {
        AIModel.ChatRecord newRequestRecord = new AIModel.ChatRecord(session);
        newRequestRecord.setChatTime(new Date());
        newRequestRecord.setIsRequest(true);
        newRequestRecord.setContent(inputInstruction);

        AIModel.PromptStruct promptStruct = constructPromptStruct(dbConnection, session, inputInstruction);
        AIModel.ChatResponse chatResponse = fetchChatResponseFromSuperAI(dbConnection, promptStruct);
        if(chatResponse.getIsSuccess()) {
            AIModel.Call call = extractFunctionCallFromChatResponse(chatResponse);
            if(call == null) {
                AIModel.ChatRecord newResponseRecord = new AIModel.ChatRecord(session);
                newResponseRecord.setChatTime(new Date());
                newResponseRecord.setIsRequest(false);
                newResponseRecord.setContent(chatResponse.getMessage());

                StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
                storage.addChatRecord(session, newRequestRecord);
                storage.addChatRecord(session, newResponseRecord);

                return chatResponse.getMessage();
            }
            else {
                String runningResult = promptStruct.getFunctionCall().callFunction(call); 
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

    private AIModel.PromptStruct constructPromptStruct(DBConnectionIFC dbConnection, String session, String inputInstruction) {
        AIModel.PromptStruct promptStruct = new AIModel.PromptStruct();
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        List<AIModel.ChatRecord> chatRecords = storage.getChatRecords(session);
        promptStruct.setChatRecords(chatRecords);
        promptStruct.setUserInput(inputInstruction);
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
        promptStruct.setSystemHint(systemHint);
        promptStruct.setFunctionCall(CoderCallImpl.getInstance());

        return promptStruct;
    }

    private AIModel.ChatResponse fetchChatResponseFromSuperAI(DBConnectionIFC dbConnection, AIModel.PromptStruct promptStruct) {
        SuperAIIFC superAI = AIFactory.getSuperAIInstance(dbConnection);
        String[] models = superAI.getChatModels();
        return superAI.fetchChatResponse(models[0], promptStruct);
    }
}
