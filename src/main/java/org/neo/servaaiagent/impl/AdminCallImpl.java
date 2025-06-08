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
        AIModel.Function getConfigVariables = generateFunctionForGetConfigVariables();
        AIModel.Function getConfigVariableValue = generateFunctionForGetConfigVariableValue();
        AIModel.Function setConfigVariableValue = generateFunctionForSetConfigVariableValue();
        AIModel.Function removeAccount = generateFunctionForRemoveAccount();

        List<AIModel.Function> functions = new ArrayList<AIModel.Function>();
        functions.add(getRegisterNumber);
        functions.add(getOnlineNumber);
        functions.add(getRegisterUsers);
        functions.add(getOnlineUsers);
        functions.add(getConfigVariables);
        functions.add(getConfigVariableValue);
        functions.add(setConfigVariableValue);
        functions.add(removeAccount);
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

        if(call.getMethodName().equals(METHODNAME_GETCONFIGVARIABLES)) {
            return call_getConfigVariables(call);
        }

        if(call.getMethodName().equals(METHODNAME_GETCONFIGVARIABLEVALUE)) {
            return call_getConfigVariableValue(call);
        }

        if(call.getMethodName().equals(METHODNAME_SETCONFIGVARIABLEVALUE)) {
            return call_setConfigVariableValue(call);
        }

        if(call.getMethodName().equals(METHODNAME_REMOVEACCOUNT)) {
            return call_removeAccount(call);
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

    private static AIModel.Function generateFunctionForGetConfigVariables() {
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETCONFIGVARIABLES);
        function.setParams(params);
        function.setDescription("get all config variables");

        return function;
    }

    private static AIModel.Function generateFunctionForGetConfigVariableValue() {
        AIModel.FunctionParam param = new AIModel.FunctionParam();
        param.setName(GETCONFIGVARIABLEVALUE_PARAM_CONFIGNAME);
        param.setDescription("the name of the config variable");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETCONFIGVARIABLEVALUE);
        function.setParams(params);
        function.setDescription("get the config value of the preferred config name");

        return function;
    }

    private static AIModel.Function generateFunctionForSetConfigVariableValue() {
        AIModel.FunctionParam param1 = new AIModel.FunctionParam();
        param1.setName(SETCONFIGVARIABLEVALUE_PARAM_CONFIGNAME);
        param1.setDescription("the name of the config variable");

        AIModel.FunctionParam param2 = new AIModel.FunctionParam();
        param2.setName(SETCONFIGVARIABLEVALUE_PARAM_CONFIGVALUE);
        param2.setDescription("the value to be set to the config variable");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param1);
        params.add(param2);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_SETCONFIGVARIABLEVALUE);
        function.setParams(params);
        function.setDescription("set the value to the preferred config name");

        return function;
    }

    private static AIModel.Function generateFunctionForRemoveAccount() {
        AIModel.FunctionParam param1 = new AIModel.FunctionParam();
        param1.setName(REMOVEACCOUNT_PARAM_USERNAME);
        param1.setDescription("the username of the account, always an email address");

        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();
        params.add(param1);

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_REMOVEACCOUNT);
        function.setParams(params);
        function.setDescription("remove the account with preferred username");

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

    private static String METHODNAME_GETCONFIGVARIABLES = "getConfigVariables";
    private String getConfigVariables() {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeQueryTask(new DBQueryTaskIFC() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    try {
                        return getConfigVariables(dbConnection);
                    }
                    catch(RuntimeException rex) {
                        throw rex;
                    }
                    catch(Exception ex) {
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                }
            });
        }
        catch(Exception ex) {
            return "meet exception in getting config variables, the exception message is: " + ex.getMessage();
        }
    }

    private String getConfigVariables(DBConnectionIFC dbConnection) throws Exception {
        String sql = "select configname";
        sql += " from configs";
        sql += " order by id";

        List<Map<String, Object>> variables = dbConnection.query(sql);
        if(variables == null
            || variables.isEmpty()) {
            return "There is none of any variables";
        }

        String result = "The system contains such variables:";
        for(Map<String, Object> map: variables) {
            String configname = map.get("configname").toString();
            result += "\n" + configname;
        }

        return result;
    }

    private String call_getConfigVariables(AIModel.Call call) {
        return getConfigVariables();
    }

    private static String METHODNAME_GETCONFIGVARIABLEVALUE = "getConfigVariableValue";
    private static String GETCONFIGVARIABLEVALUE_PARAM_CONFIGNAME = "configName";
    private String getConfigVariableValue(String configName) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeQueryTask(new DBQueryTaskIFC() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    try {
                        return getConfigVariableValue(dbConnection, configName);
                    }
                    catch(RuntimeException rex) {
                        throw rex;
                    }
                    catch(Exception ex) {
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                }
            });
        }
        catch(Exception ex) {
            return "meet exception in getting config variable value of " + configName + ", the exception message is: " + ex.getMessage();
        }
    }

    private String getConfigVariableValue(DBConnectionIFC dbConnection, String configName) throws Exception {
        String configValue = CommonUtil.getConfigValue(dbConnection, configName);

        if(configValue == null) {
            return "No such config variable: " + configName;
        }

        String result = "The config value of " + configName + " is: " + configValue;
        return result;
    }

    private String call_getConfigVariableValue(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String configName = "";
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(GETCONFIGVARIABLEVALUE_PARAM_CONFIGNAME)) {
                configName = param.getValue();
            }
        }

        return getConfigVariableValue(configName);
    }

    private static String METHODNAME_REMOVEACCOUNT = "removeAccount";
    private static String REMOVEACCOUNT_PARAM_USERNAME = "username";
    private String removeAccount(String username) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new DBSaveTaskIFC() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    try {
                        return removeAccount(dbConnection, username);
                    }
                    catch(RuntimeException rex) {
                        throw rex;
                    }
                    catch(Exception ex) {
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                }
            });
        }
        catch(Exception ex) {
            return "meet exception in removing account " + username + ", the exception message is: " + ex.getMessage();
        }
    }

    private String removeAccount(DBConnectionIFC dbConnection, String username) {
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        accountAgent.removeAccount(dbConnection, username);

        String result = "remove account " + username + " success";
        return result;
    }

    private static String METHODNAME_SETCONFIGVARIABLEVALUE = "setConfigVariableValue";
    private static String SETCONFIGVARIABLEVALUE_PARAM_CONFIGNAME = "configName";
    private static String SETCONFIGVARIABLEVALUE_PARAM_CONFIGVALUE = "configValue";
    private String setConfigVariableValue(String configName, String configValue) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new DBSaveTaskIFC() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    try {
                        return setConfigVariableValue(dbConnection, configName, configValue);
                    }
                    catch(RuntimeException rex) {
                        throw rex;
                    }
                    catch(Exception ex) {
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                }
            });
        }
        catch(Exception ex) {
            return "meet exception in setting config variable value of " + configName + ", the exception message is: " + ex.getMessage();
        }
    }

    private String setConfigVariableValue(DBConnectionIFC dbConnection, String configName, String configValue) throws Exception {
        String configValueInDB = CommonUtil.getConfigValue(dbConnection, configName);

        if(configValueInDB == null) {
            return "No such config variable: " + configName;
        }

        String sql = "update configs";
        sql += " set configvalue = ?";
        sql += " where configname = ?";

        List<Object> params = new ArrayList<Object>();
        params.add(configValue);
        params.add(configName);

        SQLStruct sqlStruct = new SQLStruct(sql, params);
        dbConnection.execute(sqlStruct);

        String result = "change config variable of " + configName + " to " + configValue + " success";
        return result;
    }

    private String call_setConfigVariableValue(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String configName = "";
        String configValue = "";
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(SETCONFIGVARIABLEVALUE_PARAM_CONFIGNAME)) {
                configName = param.getValue();
            }
            if(param.getName().equals(SETCONFIGVARIABLEVALUE_PARAM_CONFIGVALUE)) {
                configValue = param.getValue();
            }
        }

        return setConfigVariableValue(configName, configValue);
    }

    private String call_removeAccount(AIModel.Call call) {
        List<AIModel.CallParam> params = call.getParams();
        String username = "";
        for(AIModel.CallParam param: params) {
            if(param.getName().equals(REMOVEACCOUNT_PARAM_USERNAME)) {
                username = param.getValue();
            }
        }

        return removeAccount(username);
    }
}
