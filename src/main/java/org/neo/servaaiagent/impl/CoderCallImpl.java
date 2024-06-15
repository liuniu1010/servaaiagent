package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.FunctionCallIFC;
import org.neo.servaaibase.util.CommonUtil;

public class CoderCallImpl implements FunctionCallIFC {
    private String session;
    private String sandBoxUrl;

    private CoderCallImpl() {
    }

    private CoderCallImpl(String inputSession, String inputSandBoxUrl) {
        session = inputSession;
        sandBoxUrl = inputSandBoxUrl;
    }

    public static CoderCallImpl getInstance(String inputSession, String inputSandBoxUrl) {
        return new CoderCallImpl(inputSession, inputSandBoxUrl);
    }

    @Override
    public List<AIModel.Function> getFunctions() {
        // executeCommand
        AIModel.Function executeCommandFunction = generateFunctionForExecuteCommand();
        AIModel.Function finishCodeGeneration = generateFunctionForFinishCodeGeneration();
        AIModel.Function failCodeGeneration = generateFunctionForFailCodeGeneration();

        List<AIModel.Function> functions = new ArrayList<AIModel.Function>();
        functions.add(executeCommandFunction);
        functions.add(finishCodeGeneration);
        functions.add(failCodeGeneration);
        return functions;
    }

    @Override
    public Object callFunction(AIModel.Call call) {
        if(call.getMethodName().equals(METHODNAME_EXECUTECOMMAND)) {
            return call_executeCommand(call);
        }
        else if(call.getMethodName().equals(METHODNAME_FINISHCODEGENERATION)) {
            return call_finishCodeGeneration(call);
        }
        else if(call.getMethodName().equals(METHODNAME_FAILCODEGENERATION)) {
            return call_failCodeGeneration(call);
        }

        return null;
    }

    private AIModel.Function generateFunctionForExecuteCommand() {
        AIModel.FunctionParam param = new AIModel.FunctionParam();
        param.setName(EXECUTECOMMAND_PARAM_COMMAND);
        param.setDescription("the linux command to be executed");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_EXECUTECOMMAND);
        function.setParams(params);
        function.setDescription("to execute linux command on local machine");

        return function;
    }

    private AIModel.Function generateFunctionForFinishCodeGeneration() {
        AIModel.FunctionParam param = new AIModel.FunctionParam();
        param.setName(FINISHCODEGENERATION_PARAM_CONTENT);
        param.setDescription("the content you hope to declare of completion of the task");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_FINISHCODEGENERATION);
        function.setParams(params);
        function.setDescription("to declare the code task has been completedly finished");

        return function;
    }

    private AIModel.Function generateFunctionForFailCodeGeneration() {
        AIModel.FunctionParam param = new AIModel.FunctionParam();
        param.setName(FAILCODEGENERATION_PARAM_REASON);
        param.setDescription("the reason why the code could not be generated");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_FAILCODEGENERATION);
        function.setParams(params);
        function.setDescription("to declare the code cannot be generated");

        return function;
    }

    protected static String METHODNAME_EXECUTECOMMAND = "executeCommand";
    private static String EXECUTECOMMAND_PARAM_COMMAND = "command";
    private String executeCommand(String command) {
        return CommonUtil.executeCommandSandBox(session, command, sandBoxUrl);
    }

    protected static String METHODNAME_FINISHCODEGENERATION = "finishCodeGeneration";
    protected static String FINISHCODEGENERATION_PARAM_CONTENT = "content";
    private String finishCodeGeneration(String content) {
        return content;
    }

    protected static String METHODNAME_FAILCODEGENERATION = "failCodeGeneration";
    private static String FAILCODEGENERATION_PARAM_REASON = "reason";
    private String failCodeGeneration(String reason) {
        return reason;
    }

    private String call_executeCommand(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String command = null;
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(EXECUTECOMMAND_PARAM_COMMAND)) {
                command = param.getValue();
            }
        }

        String runningResultDesc = null;
        try {
            String commandResult = executeCommand(command);
            runningResultDesc = "You have run command \n" + command + "\n success with result: ";
            runningResultDesc += "\n" + commandResult;
            runningResultDesc += "\n";
        }
        catch(Exception ex) {
            runningResultDesc = "You have run command \n" + command + "\n failed with result: ";
            runningResultDesc += "\n" + ex.getMessage();
        }

        return runningResultDesc;
    }

    private String call_finishCodeGeneration(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String content = "";
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(FINISHCODEGENERATION_PARAM_CONTENT)) {
                content = param.getValue();
            }
        }
        return finishCodeGeneration(content);
    }

    private String call_failCodeGeneration(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String reason = "";
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(FAILCODEGENERATION_PARAM_REASON)) {
                reason = param.getValue();
            }
        }

        return failCodeGeneration(reason);
    }
}
