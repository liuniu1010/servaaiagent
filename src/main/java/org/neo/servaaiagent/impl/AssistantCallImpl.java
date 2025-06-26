package org.neo.servaaiagent.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBQueryTaskIFC;
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
        AIModel.Function getCreditsTopupHistory = generateFunctionForGetCreditsTopupHistory();
        AIModel.Function getCreditsConsumedHistory = generateFunctionForGetCreditsConsumedHistory();

        List<AIModel.Function> functions = new ArrayList<AIModel.Function>();
        functions.add(getCreditsLeft);
        // functions.add(getCreditsTopupHistory);
        // functions.add(getCreditsConsumedHistory);

        return functions;
    }

    @Override
    public Object callFunction(AIModel.Call call) {
        if(call.getMethodName().equals(METHODNAME_GETCREDITSLEFT)) {
            return call_getCreditsLeft(call);
        }
        else if(call.getMethodName().equals(METHODNAME_GETCREDITSTOPUPHISTORY)) {
            return call_getCreditsTopupHistory(call);
        }
        else if(call.getMethodName().equals(METHODNAME_GETCREDITSCONSUMEDHISTORY)) {
            return call_getCreditsConsumedHistory(call);
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

    private static AIModel.Function generateFunctionForGetCreditsTopupHistory() {
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETCREDITSTOPUPHISTORY);
        function.setParams(params);
        function.setDescription("get credits topup history of the current user");

        return function;
    }

    private static AIModel.Function generateFunctionForGetCreditsConsumedHistory() {
        List<AIModel.FunctionParam> params = new ArrayList<AIModel.FunctionParam>();

        AIModel.Function function = new AIModel.Function();
        function.setMethodName(METHODNAME_GETCREDITSCONSUMEDHISTORY);
        function.setParams(params);
        function.setDescription("get credits consumed history of the current user");

        return function;
    }

    private static String METHODNAME_GETCREDITSLEFT = "getCreditsLeft";
    private String getCreditsLeft() {
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        int creditsLeft = accountAgent.getLeftCreditsWithSession(loginSession);
        String result = "the left credits count is: " + creditsLeft;
        return result;
    }

    private static String METHODNAME_GETCREDITSTOPUPHISTORY = "getCreditsTopupHistory";
    private String getCreditsTopupHistory() {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeQueryTask(new DBQueryTaskIFC() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    try {
                        return getCreditsTopupHistory(dbConnection);
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
            return "meet exception in getting topup history, the exception message is: " + ex.getMessage();
        }
    }

    private String getCreditsTopupHistory(DBConnectionIFC dbConnection) throws Exception {
        return "";
    }

    private static String METHODNAME_GETCREDITSCONSUMEDHISTORY = "getCreditsConsumedHistory";
    private String getCreditsConsumedHistory() {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeQueryTask(new DBQueryTaskIFC() {
                @Override
                public Object query(DBConnectionIFC dbConnection) {
                    try {
                        return getCreditsConsumedHistory(dbConnection);
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
            return "meet exception in getting topup history, the exception message is: " + ex.getMessage();
        }
    }

    private String getCreditsConsumedHistory(DBConnectionIFC dbConnection) {
        return null;
    }

    private String call_getCreditsLeft(AIModel.Call call) {
        return getCreditsLeft();
    }

    private String call_getCreditsTopupHistory(AIModel.Call call) {
        return getCreditsTopupHistory();
    }

    private String call_getCreditsConsumedHistory(AIModel.Call call) {
        return getCreditsConsumedHistory();
    }
}
