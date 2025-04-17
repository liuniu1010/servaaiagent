package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.ArrayList;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.FunctionCallIFC;

public class GameCallImpl implements FunctionCallIFC {
    private GameCallImpl() {
    }

    public static GameCallImpl getInstance() {
        return new GameCallImpl();
    }

    @Override
    public List<AIModel.Function> getFunctions() {
        // executeCommand
        AIModel.Function generatePageCodeFunction = generateFunctionForGeneratePageCode();
        AIModel.Function failCodeGenerationFunction = generateFunctionForFailCodeGeneration();

        List<AIModel.Function> functions = new ArrayList<AIModel.Function>();
        functions.add(generatePageCodeFunction);
        functions.add(failCodeGenerationFunction);
        return functions;
    }

    public static boolean isDefinedFunction(String functionName) {
        if(METHODNAME_GENERATEPAGECODE.equals(functionName)) {
            return true;
        }
        if(METHODNAME_FAILCODEGENERATION.equals(functionName)) {
            return true;
        }
        return false;
    }

    @Override
    public Object callFunction(AIModel.Call call) {
        if(call.getMethodName().equals(METHODNAME_GENERATEPAGECODE)) {
            return call_generatePageCode(call);
        }
        else if(call.getMethodName().equals(METHODNAME_FAILCODEGENERATION)) {
            return call_failCodeGeneration(call);
        }

        return null;
    }

    private AIModel.Function generateFunctionForGeneratePageCode() {
        AIModel.FunctionParam param = new AIModel.FunctionParam();
        param.setName(GENERATEPAGECODE_PARAM_PAGECODE);
        param.setDescription("the pure page code, including html, css, js of one page");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GENERATEPAGECODE);
        function.setParams(params);
        function.setDescription("to generate page code which would run in browser");

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

    protected static String METHODNAME_GENERATEPAGECODE = "generatePageCode";
    private static String GENERATEPAGECODE_PARAM_PAGECODE = "pageCode";
    private String generatePageCode(String pageCode) {
        return pageCode;
    }

    protected static String METHODNAME_FAILCODEGENERATION = "failCodeGeneration";
    private static String FAILCODEGENERATION_PARAM_REASON = "reason";
    private String failCodeGeneration(String reason) {
        return reason;
    }

    private String call_generatePageCode(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String pageCode = "";
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(GENERATEPAGECODE_PARAM_PAGECODE)) {
                pageCode = param.getValue();
            }
        }
        return generatePageCode(pageCode);
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
