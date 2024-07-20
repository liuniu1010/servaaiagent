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
        return innerGetFunctions();
    }

    private static List<AIModel.Function> innerGetFunctions() {
        AIModel.Function getRegisterNumber = generateFunctionForGetRegisterNumber();
        AIModel.Function getOnlineNumber = generateFunctionForGetOnlineNumber();
        AIModel.Function getRegisterUsers = generateFunctionForGetRegisterUsers();
        AIModel.Function getOnlineUsers = generateFunctionForGetOnlineUsers();

        List<AIModel.Function> functions = new ArrayList<AIModel.Function>();
        functions.add(getRegisterNumber);
        functions.add(getOnlineNumber);
        functions.add(getRegisterUsers);
        functions.add(getOnlineUsers);
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

        if(call.getMethodName().equals(METHODNAME_GETREGISTERUSERS)) {
            return call_getRegisterUsers(call);
        }

        if(call.getMethodName().equals(METHODNAME_GETONLINEUSERS)) {
            return call_getOnlineUsers(call);
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

    private static AIModel.Function generateFunctionForGetRegisterNumber() {
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETREGISTERNUMBER);
        function.setParams(params);
        function.setDescription("get registered user number");

        return function;
    }

    private static AIModel.Function generateFunctionForGetRegisterUsers() {
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETREGISTERUSERS);
        function.setParams(params);
        function.setDescription("get all registered usernames");

        return function;
    }

    private static AIModel.Function generateFunctionForGetOnlineNumber() {
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETONLINENUMBER);
        function.setParams(params);
        function.setDescription("get online user number");

        return function;
    }

    private static AIModel.Function generateFunctionForGetOnlineUsers() {
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETONLINEUSERS);
        function.setParams(params);
        function.setDescription("get all online usernames");

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

    private static String METHODNAME_GETREGISTERUSERS = "getRegisterUsers";
    private String getRegisterUsers() {
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        List<String> userList = accountAgent.getRegisterUsers();

        String response;
        if(userList == null || userList.size() == 0) {
            response = "no register users";
        }
        else {
            response = "register users:";
            for(String username: userList) {
                response += "\n\t" + username;
            }
        }
        return response;
    }

    private String call_getRegisterUsers(AIModel.Call call) {
        return getRegisterUsers();
    }

    private static String METHODNAME_GETONLINEUSERS = "getOnlineUsers";
    private String getOnlineUsers() {
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        List<String> userList = accountAgent.getOnlineUsers();

        String response;
        if(userList == null || userList.size() == 0) {
            response = "no online users";
        }
        else {
            response = "online users:";
            for(String username: userList) {
                response += "\n\t" + username;
            }
        }
        return response;
    }

    private String call_getOnlineUsers(AIModel.Call call) {
        return getOnlineUsers();
    }
}
