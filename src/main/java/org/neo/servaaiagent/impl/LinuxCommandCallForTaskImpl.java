package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.nio.charset.StandardCharsets;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.FunctionCallIFC;
import org.neo.servaaibase.util.CommonUtil;

public class LinuxCommandCallForTaskImpl implements FunctionCallIFC {
    private String session;
    private String sandBoxUrl;

    private LinuxCommandCallForTaskImpl() {
    }

    private LinuxCommandCallForTaskImpl(String inputSession, String inputSandBoxUrl) {
        session = inputSession;
        sandBoxUrl = inputSandBoxUrl;
    }

    public static LinuxCommandCallForTaskImpl getInstance(String inputSession, String inputSandBoxUrl) {
        return new LinuxCommandCallForTaskImpl(inputSession, inputSandBoxUrl);
    }

    @Override
    public List<AIModel.Function> getFunctions() {
        // executeCommand
        AIModel.Function executeCommandFunction = generateFunctionForExecuteCommand();
        AIModel.Function finishTaskFunction = generateFunctionForFinishTask();
        AIModel.Function failTaskFunction = generateFunctionForFailTask();

        List<AIModel.Function> functions = new ArrayList<AIModel.Function>();
        functions.add(executeCommandFunction);
        functions.add(finishTaskFunction);
        functions.add(failTaskFunction);
        return functions;
    }

    public static boolean isDefinedFunction(String functionName) {
        if(METHODNAME_EXECUTECOMMAND.equals(functionName)) {
            return true;
        }
        if(METHODNAME_FINISHTASK.equals(functionName)) {
            return true;
        }
        if(METHODNAME_FAILTASK.equals(functionName)) {
            return true;
        }
        return false;
    }

    @Override
    public Object callFunction(AIModel.Call call) {
        if(call.getMethodName().equals(METHODNAME_EXECUTECOMMAND)) {
            return call_executeCommand(call);
        }
        else if(call.getMethodName().equals(METHODNAME_FINISHTASK)) {
            return call_finishTask(call);
        }
        else if(call.getMethodName().equals(METHODNAME_FAILTASK)) {
            return call_failTask(call);
        }

        return null;
    }

    private AIModel.Function generateFunctionForExecuteCommand() {
        AIModel.FunctionParam param = new AIModel.FunctionParam();
        param.setName(EXECUTECOMMAND_PARAM_COMMAND);
        param.setDescription("the linux command to be executed directly, no adding any comments in this param");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_EXECUTECOMMAND);
        function.setParams(params);
        function.setDescription("to execute linux command on local machine");

        return function;
    }

    private AIModel.Function generateFunctionForFinishTask() {
        AIModel.FunctionParam param = new AIModel.FunctionParam();
        param.setName(FINISHTASK_PARAM_CONTENT);
        param.setDescription("the content you hope to declare of completion of the task");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_FINISHTASK);
        function.setParams(params);
        function.setDescription("to declare the task has been completedly finished");

        return function;
    }

    private AIModel.Function generateFunctionForFailTask() {
        AIModel.FunctionParam param = new AIModel.FunctionParam();
        param.setName(FAILTASK_PARAM_REASON);
        param.setDescription("the reason why the task could not be finished");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_FAILTASK);
        function.setParams(params);
        function.setDescription("to declare the task cannot be finished");

        return function;
    }

    protected static String METHODNAME_EXECUTECOMMAND = "executeCommand";
    private static String EXECUTECOMMAND_PARAM_COMMAND = "command";
    private String executeCommand(String command) {
        return CommonUtil.executeCommandSandBox(session, command, sandBoxUrl);
    }

    protected static String METHODNAME_FINISHTASK = "finishTask";
    protected static String FINISHTASK_PARAM_CONTENT = "content";
    private String finishTask(String content) {
        return content;
    }

    protected static String METHODNAME_FAILTASK = "failTask";
    private static String FAILTASK_PARAM_REASON = "reason";
    private String failTask(String reason) {
        return reason;
    }

    private String call_executeCommand(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String command = null;
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(EXECUTECOMMAND_PARAM_COMMAND)
                || param.getName().equals(AIModel.CallParam.UNKNOWN)) {
                command = param.getValue();
            }
        }

        String runningResultDesc = null;
        try {
            String commandResult = executeCommand(command);
            runningResultDesc = "You have run command\n```\n" + command + "\n```\nsuccess with result: ";
            if(command.startsWith("cat ")
                || command.startsWith("find ")) {
                runningResultDesc += "\n" + commandResult;  // should not reduce the result for these command
            }
            else {
                runningResultDesc += "\n" + adjustInputText(commandResult, 100); // try to reduce size 
            }
            runningResultDesc += "\n";
        }
        catch(Exception ex) {
            runningResultDesc = "You have run command\n```\n" + command + "\n```\nfailed with result: ";
            runningResultDesc += "\n" + adjustInputText(ex.getMessage(), 1500);
        }

        return runningResultDesc;
    }

    private String adjustInputText(String inputText, int maxByteLength) {
        if(inputText == null) {
            return null;
        }
        byte[] utf8Bytes = inputText.getBytes(StandardCharsets.UTF_8);
        if (utf8Bytes.length <= maxByteLength * 2) {
            return inputText;
        }
        String startPart = CommonUtil.truncateTextFromStart(inputText, maxByteLength);
        String endPart = CommonUtil.truncateTextFromEnd(inputText, maxByteLength);
        String adjustResult = startPart + "\n...\n" + endPart;
        return adjustResult;
    }

    private String call_finishTask(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String content = "";
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(FINISHTASK_PARAM_CONTENT)) {
                content = param.getValue();
            }
        }
        return finishTask(content);
    }

    private String call_failTask(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String reason = "";
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(FAILTASK_PARAM_REASON)) {
                reason = param.getValue();
            }
        }

        return failTask(reason);
    }
}
