package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.ArrayList;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.FunctionCallIFC;
import org.neo.servaaibase.util.CommonUtil;

public class CoderCallImpl implements FunctionCallIFC {
    private CoderCallImpl() {
    }

    public static CoderCallImpl getInstance() {
        return new CoderCallImpl();
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
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_FINISHCODEGENERATION);
        function.setParams(params);
        function.setDescription("to declare the code have been generated, ready to compile and test");

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
        return CommonUtil.executeCommand(command);
    }

    protected static String METHODNAME_FINISHCODEGENERATION = "finishCodeGeneration";
    private String finishCodeGeneration() {
        return "code are all generated, ready to compile and test";
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
        return finishCodeGeneration();
    }

    private String call_failCodeGeneration(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String reason = null;
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(FAILCODEGENERATION_PARAM_REASON)) {
                reason = param.getValue();
            }
        }

        return failCodeGeneration(reason);
    }
}
