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
        param.setDescription(description);

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_ASSIGNTO);
        function.setParams(params);
        function.setDescription("to assign task to a receiver");

        return function;
    }

    private static String METHODNAME_ASSIGNTO = "assignTo";
    private static String ASSIGNTO_PARAM_RECEIVER = "receiver";
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
