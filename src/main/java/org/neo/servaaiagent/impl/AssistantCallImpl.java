package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;
import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.model.SQLStruct;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.FunctionCallIFC;
import org.neo.servaaibase.util.CommonUtil;

import org.neo.servaaiagent.ifc.AccountAgentIFC;

public class AssistantCallImpl implements FunctionCallIFC {
    private String loginSession;
    private AssistantCallImpl() {
    }

    private AssistantCallImpl(String inputLoginSession) {
        loginSession = inputLoginSession;
    }

    public static AssistantCallImpl getInstance() {
        return new AssistantCallImpl();
    }

    public static AssistantCallImpl newInstance(String inputLoginSession) {
        return new AssistantCallImpl(inputLoginSession);
    }

    @Override
    public List<AIModel.Function> getFunctions() {
        return innerGetFunctions();
    }

    private static List<AIModel.Function> innerGetFunctions() {
        AIModel.Function getCreditsLeft = generateFunctionForGetCreditsLeft();

        List<AIModel.Function> functions = new ArrayList<AIModel.Function>();
        functions.add(getCreditsLeft);

        return functions;
    }

    @Override
    public Object callFunction(AIModel.Call call) {
        if(call.getMethodName().equals(METHODNAME_GETCREDITSLEFT)) {
            return call_getCreditsLeft(call);
        }

        return null;
    }

    public static boolean isDefinedFunction(String functionName) {
        boolean isInDefined = false;
        List<AIModel.Function> functions = innerGetFunctions();
        for(AIModel.Function function: functions) {
            if(function.getMethodName().equals(functionName)) {
                return true;
            }
        }
        return false;
    }

    private static AIModel.Function generateFunctionForGetCreditsLeft() {
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETCREDITSLEFT);
        function.setParams(params);
        function.setDescription("get the left credits count of current user");

        return function;
    }

    private static String METHODNAME_GETCREDITSLEFT = "getCreditsLeft";
    private String getCreditsLeft() {
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        int creditsLeft = accountAgent.getLeftCreditsWithSession(loginSession);
        String result = "the left credits count is: " + creditsLeft;
        return result;
    }

    private String call_getCreditsLeft(AIModel.Call call) {
        return getCreditsLeft();
    }
}
