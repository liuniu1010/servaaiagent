package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.ArrayList;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.ifc.FunctionCallIFC;

import org.neo.servaaiagent.ifc.AccountAgentIFC;

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
    private String getRegisterNumber() {
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        int registerNumber = accountAgent.getRegisterNumber();

        String response;
        if(registerNumber <= 0) {
            response = "None user registered";
        }
        else if(registerNumber == 1) {
            response = "There is only 1 user registered";
        }
        else {
            return "there are " + registerNumber + " registered users";
        }
        return response;
    }

    private String call_getRegisterNumber(AIModel.Call call) {
        return getRegisterNumber();
    }

    private static String METHODNAME_GETONLINENUMBER = "getOnlineNumber";
    private String getOnlineNumber() {
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        int onlineNumber = accountAgent.getOnlineNumber();

        String response;
        if(onlineNumber <= 0) {
            response = "No user online just now";
        }
        else if(onlineNumber == 1) {
            response = "There is only 1 user online now";
        }
        else {
            return "there are " + onlineNumber + " online users currently";
        }
        return response;
    }

    private String call_getOnlineNumber(AIModel.Call call) {
        return getOnlineNumber();
    }
}
