package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.ArrayList;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.FunctionCallIFC;
import org.neo.servaaibase.util.CommonUtil;

public class LinuxCommandCallImpl implements FunctionCallIFC {
    private LinuxCommandCallImpl() {
    }

    public static LinuxCommandCallImpl getInstance() {
        return new LinuxCommandCallImpl();
    }

    @Override
    public List<AIModel.Function> getFunctions() {
        // executeCommand
        AIModel.Function executeCommand = generateFunctionForExecuteCommand();

        List<AIModel.Function> functions = new ArrayList<AIModel.Function>();
        functions.add(executeCommand);
        return functions;
    }

    @Override
    public Object callFunction(AIModel.Call call) {
        if(call.getMethodName().equals(METHODNAME_EXECUTECOMMAND)) {
            return call_executeCommand(call);
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

    private static String METHODNAME_EXECUTECOMMAND = "executeCommand";
    private static String EXECUTECOMMAND_PARAM_COMMAND = "command";
    private String executeCommand(String command) {
        return CommonUtil.executeCommand(command);
    }

    private String call_executeCommand(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String command = null;
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(EXECUTECOMMAND_PARAM_COMMAND)) {
                command = param.getValue();
            }
        }

        return executeCommand(command);
    }
}
