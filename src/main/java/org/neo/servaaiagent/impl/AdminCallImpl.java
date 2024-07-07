package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.ArrayList;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.FunctionCallIFC;

public class AdminCallImpl implements FunctionCallIFC {
    private AdminCallImpl() {
    }

    public static AdminCallImpl getInstance() {
        return new AdminCallImpl();
    }

    @Override
    public List<AIModel.Function> getFunctions() {
        AIModel.Function getRegisterNumber = generateFunctionForGetRegisterNumber();
        AIModel.Function getOnlineNumber = generateFunctionForGetOnlineNumber();

        List<AIModel.Function> functions = new ArrayList<AIModel.Function>();
        functions.add(getRegisterNumber);
        functions.add(getOnlineNumber);
        return functions;
    }

    @Override
    public Object callFunction(AIModel.Call call) {
        if(call.getMethodName().equals(METHODNAME_GETREGISTERNUMBER)) {
            return call_getRegisterNumber(call);
        }

        if(call.getMethodName().equals(METHODNAME_GETONLINENUMBER)) {
            return call_getOnlineNumber(call);
        }

        return null;
    }

    private AIModel.Function generateFunctionForGetRegisterNumber() {
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETREGISTERNUMBER);
        function.setParams(params);
        function.setDescription("get registered user number");

        return function;
    }

    private AIModel.Function generateFunctionForGetOnlineNumber() {
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETONLINENUMBER);
        function.setParams(params);
        function.setDescription("get online user number");

        return function;
    }

    private static String METHODNAME_GETREGISTERNUMBER = "getRegisterNumber";
    private int getRegisterNumber() {
        // to be implemented
        return -1;
    }

    private int call_getRegisterNumber(AIModel.Call call) {
        return getRegisterNumber();
    }

    private static String METHODNAME_GETONLINENUMBER = "getOnlineNumber";
    private int getOnlineNumber() {
        // to be implemented
        return -1;
    }

    private int call_getOnlineNumber(AIModel.Call call) {
        return getOnlineNumber();
    }
}
