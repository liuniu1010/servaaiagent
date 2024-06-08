package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.ArrayList;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.FunctionCallIFC;

public class AssignCallImpl implements FunctionCallIFC {
    private AssignCallImpl() {
    }

    public static AssignCallImpl getInstance() {
        return new AssignCallImpl();
    }

    @Override
    public List<AIModel.Function> getFunctions() {
        AIModel.Function assignTo = generateFunctionForAssignTo();

        List<AIModel.Function> functions = new ArrayList<AIModel.Function>();
        functions.add(assignTo);
        return functions;
    }

    @Override
    public Object callFunction(AIModel.Call call) {
        if(call.getMethodName().equals(METHODNAME_ASSIGNTO)) {
            return call_executeCommand(call);
        }

        return null;
    }

    private AIModel.Function generateFunctionForAssignTo() {
        AIModel.FunctionParam param = new AIModel.FunctionParam();
        param.setName(ASSIGNTO_PARAM_RECEIVER);
        String description = "the receiver to whom the task would be assigned.";
        description += "\nThe only possible option is one of " + ASSIGNTO_PARAM_RECEIVER_JAVAMAVENLINUX;
        description += "/" + ASSIGNTO_PARAM_RECEIVER_JAVAGRADLELINUX;
        description += "/" + ASSIGNTO_PARAM_RECEIVER_DOTNETLINUX;
        description += "/" + ASSIGNTO_PARAM_RECEIVER_PYTHON3LINUX;
        description += "/" + ASSIGNTO_PARAM_RECEIVER_NODEJSLINUX;
        param.setDescription("the receiver to whom the task will be assigned.");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_ASSIGNTO);
        function.setParams(params);
        function.setDescription("to assign task to a receiver");

        return function;
    }

    public static String METHODNAME_ASSIGNTO = "assignTo";
    public static String ASSIGNTO_PARAM_RECEIVER = "receiver";
    public static String ASSIGNTO_PARAM_RECEIVER_JAVAMAVENLINUX = "javamavenlinux";
    public static String ASSIGNTO_PARAM_RECEIVER_JAVAGRADLELINUX = "javagradlelinux";
    public static String ASSIGNTO_PARAM_RECEIVER_DOTNETLINUX = "dotnetlinux";
    public static String ASSIGNTO_PARAM_RECEIVER_PYTHON3LINUX = "python3linux";
    public static String ASSIGNTO_PARAM_RECEIVER_NODEJSLINUX = "nodejslinux";
    private String assignTo(String receiver) {
        return receiver;
    }

    private String call_executeCommand(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String receiver = null;
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(ASSIGNTO_PARAM_RECEIVER)) {
                receiver = param.getValue();
            }
        }

        return assignTo(receiver);
    }
}
