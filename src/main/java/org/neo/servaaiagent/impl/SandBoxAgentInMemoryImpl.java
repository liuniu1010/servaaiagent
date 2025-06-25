package org.neo.servaaiagent.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.util.IOUtil;
import org.neo.servaaibase.NeoAIException;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaiagent.ifc.SandBoxAgentIFC;

public class SandBoxAgentInMemoryImpl implements SandBoxAgentIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SandBoxAgentInMemoryImpl.class);

    private SandBoxAgentInMemoryImpl() {
    }

    public static SandBoxAgentIFC getInstance() {
        return new SandBoxAgentInMemoryImpl();
    }

    @Override
    public String executeCommand(String alignedSession, String commandSandBox, String sUrl) {
        try {
            logger.debug("prepare to send command to sandbox, command: " + commandSandBox);
            logger.debug("url: " + sUrl);
            String jsonCommandSandBox = generateJsonBodyForSandBox(alignedSession, commandSandBox);
            logger.debug("jsonCommandSandBox = " + jsonCommandSandBox);
            String jsonResultSandBox = sendCommandToSandBox(jsonCommandSandBox, sUrl);
            logger.debug("sandbox result: " + jsonResultSandBox);
            ResultSandBox resultSandBox = extractResultSandBox(jsonResultSandBox);
            if(resultSandBox.getIsSuccess()) {
                return resultSandBox.getMessage();
            }
            else {
                throw new NeoAIException(resultSandBox.getMessage());
            }
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
    }

    @Override
    public String executeCommand(DBConnectionIFC dbConnection, String alignedSession, String command, String sUrl) {
        throw new NeoAIException("not supported");
    }

    @Override
    public String downloadProject(String alignedSession, String projectFolder, String sUrl) {
        try {
            String jsonDownloadSandBox = generateJsonBodyForSandBox(alignedSession, projectFolder);
            String jsonResultSandBox = sendDownloadToSandBox(jsonDownloadSandBox, sUrl);
            ResultSandBox resultSandBox = extractResultSandBox(jsonResultSandBox);
            if(resultSandBox.getIsSuccess()) {
                return resultSandBox.getMessage();
            }
            else {
                throw new NeoAIException(resultSandBox.getMessage());
            }
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
    }

    @Override
    public String downloadProject(DBConnectionIFC dbConnection, String alignedSession, String projectFolder, String sUrl) {
        throw new NeoAIException("not supported");
    }

    @Override
    public void terminateShell(String alignedSession, String sUrl) {
        try {
            String jsonTerminationSandBox = generateJsonBodyForSandBox(alignedSession, "");
            String jsonResultSandBox = sendTerminationToSandBox(jsonTerminationSandBox, sUrl);
            ResultSandBox resultSandBox = extractResultSandBox(jsonResultSandBox);
            if(resultSandBox.getIsSuccess()) {
                return;
            }
            else {
                throw new NeoAIException(resultSandBox.getMessage());
            }
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
    }

    @Override
    public void terminateShell(DBConnectionIFC dbConnection, String alignedSession, String sUrl) {
        throw new NeoAIException("not supported");
    }

    @Override
    public boolean isUnix(String alignedSession, String sUrl) {
        try {
            String jsonIsUnixSandBox = generateJsonBodyForSandBox(alignedSession, "");
            String jsonResultSandBox = sendIsUnixToSandBox(jsonIsUnixSandBox, sUrl);
            ResultSandBox resultSandBox = extractResultSandBox(jsonResultSandBox);
            if(resultSandBox.getIsSuccess()) {
                return resultSandBox.getMessage().trim().toLowerCase().equals("yes")?true:false;
            }
            else {
                throw new NeoAIException(resultSandBox.getMessage());
            }
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex);
        }
    }

    @Override
    public boolean isUnix(DBConnectionIFC dbConnection, String alignedSession, String sUrl) {
        throw new NeoAIException("not supported");
    }

    private String generateJsonBodyForSandBox(String alignedSession, String input) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        
        jsonBody.addProperty("loginSession", alignedSession);
        jsonBody.addProperty("userInput", input);
    
        return gson.toJson(jsonBody);
    }

    private String sendCommandToSandBox(String jsonCommandSandBox, String sUrl) throws Exception {
        String urlWithAction = sUrl + "/" + "executecommand";
        return sendInputToSandBox(jsonCommandSandBox, urlWithAction);
    }

    private String sendDownloadToSandBox(String jsonDownloadSandBox, String sUrl) throws Exception {
        String urlWithAction = sUrl + "/" + "downloadproject";
        return sendInputToSandBox(jsonDownloadSandBox, urlWithAction);
    }

    private String sendTerminationToSandBox(String jsonTerminationSandBox, String sUrl) throws Exception {
        String urlWithAction = sUrl + "/" + "terminateshell";
        return sendInputToSandBox(jsonTerminationSandBox, urlWithAction);
    }

    private String sendIsUnixToSandBox(String jsonIsUnixSandBox, String sUrl) throws Exception {
        String urlWithAction = sUrl + "/" + "isunix";
        return sendInputToSandBox(jsonIsUnixSandBox, urlWithAction);
    }

    private String sendInputToSandBox(String jsonInput, String sUrl) throws Exception {
        jsonInput = CommonUtil.alignJson(jsonInput);
        logger.debug("call sandbox api, jsonInput = " + jsonInput);
        logger.debug("sUrl = " + sUrl);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()){
                IOUtil.stringToOutputStream(jsonInput, os);
            }

            try (InputStream in = connection.getInputStream()){
                String response = IOUtil.inputStreamToString(in);
                response = CommonUtil.alignJson(response);
                logger.debug("return from sandbox api, response = " + response);
                return response;
            }
        }
        catch(java.net.ConnectException cex) {
            try (InputStream errIn = connection.getErrorStream()) {
                if(errIn == null) {
                    logger.error("get ConnectException from sandbox api, ", cex);
                }
                else {
                    String errorResponse = IOUtil.inputStreamToString(errIn);
                    logger.error("get ConnectException from sandbox api, response = " + errorResponse, cex);
                }
            }
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_IOEXCEPTIONWITHSANDBOX, "The associated sandbox is not ready", cex);
        }
        catch(IOException iex) {
            try (InputStream errIn = connection.getErrorStream()) {
                if(errIn == null) {
                    logger.error("get IOException from sandbox api, ", iex);
                }
                else {
                    String errorResponse = IOUtil.inputStreamToString(errIn);
                    logger.error("get IOException from sandbox api, response = " + errorResponse, iex);
                }
            }
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_IOEXCEPTIONWITHSANDBOX, "The associated sandbox is not ready", iex);
        }
        catch(Exception ex) {
            try (InputStream errIn = connection.getErrorStream()) {
                if(errIn == null) {
                    logger.error("get Exception from sandbox api, ", ex);
                }
                else {
                    String errorResponse = IOUtil.inputStreamToString(errIn);
                    logger.error("get Exception from sandbox api, response = " + errorResponse, ex);
                }
            }
            throw new NeoAIException(ex);
        }
        finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    private ResultSandBox extractResultSandBox(String jsonResultSandBox) {
        Gson gson = new Gson();
        return gson.fromJson(jsonResultSandBox, ResultSandBox.class);
    }

    static class ResultSandBox {
        private boolean isSuccess;
        private String message;

        public boolean getIsSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }
    }
}
