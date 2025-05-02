package org.neo.servaaiagent.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.neo.servaframe.util.IOUtil;

import org.neo.servaaibase.model.AIModel;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.UtilityAgentIFC;

public class UtilityAgentRemoteImpl implements UtilityAgentIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UtilityAgentRemoteImpl.class);

    private UtilityAgentRemoteImpl() {
    }

    public static UtilityAgentRemoteImpl getInstance() {
        return new UtilityAgentRemoteImpl();
    }

    @Override
    public AIModel.ChatResponse generatePageCode(String userInput, String fileContent) {
        try {
            return innerGeneratePageCode(userInput, fileContent);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private AIModel.ChatResponse innerGeneratePageCode(String userInput, String fileContent) throws Exception {
        String sUrl = getGameFactoryUrl() + "/generate";
        String jsonInput = generateJsonBodyToRemote(userInput, fileContent);
        String jsonResult = sendInputToRemote(jsonInput, sUrl);
        ResultRemote resultRemote = extractResultRemote(jsonResult);
        return new AIModel.ChatResponse(resultRemote.getIsSuccess(), resultRemote.getMessage());
    }

    private String generateJsonBodyToRemote(String userInput, String fileContent) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        
        jsonBody.addProperty("userInput", userInput);
        jsonBody.addProperty("fileContent", fileContent);
        
        return gson.toJson(jsonBody);
    }

    private String sendInputToRemote(String jsonInput, String sUrl) throws Exception {
        jsonInput = CommonUtil.alignJson(jsonInput);
        logger.debug("call remote api, jsonInput = " + jsonInput);
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
                logger.debug("return from remote api, response = " + response);
                return response;
            }
        }
        catch(java.net.ConnectException cex) {
            try (InputStream errIn = connection.getErrorStream()) {
                if(errIn == null) {
                    logger.error("get ConnectException from remote api, ", cex);
                }
                else {
                    String errorResponse = IOUtil.inputStreamToString(errIn);
                    logger.error("get ConnectException from remote api, response = " + errorResponse, cex);
                }
            }
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_IOEXCEPTIONWITHSANDBOX, "The associated remote interface is not ready", cex);
        }
        catch(IOException iex) {
            try (InputStream errIn = connection.getErrorStream()) {
                if(errIn == null) {
                    logger.error("get IOException from remote api, ", iex);
                }
                else {
                    String errorResponse = IOUtil.inputStreamToString(errIn);
                    logger.error("get IOException from remote api, response = " + errorResponse, iex);
                }
            }
            throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_IOEXCEPTIONWITHSANDBOX, "The associated remote interface is not ready", iex);
        }
        catch(Exception ex) {
            try (InputStream errIn = connection.getErrorStream()) {
                if(errIn == null) {
                    logger.error("get Exception from remote api, ", ex);
                }
                else {
                    String errorResponse = IOUtil.inputStreamToString(errIn);
                    logger.error("get Exception from remote api, response = " + errorResponse, ex);
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

    private String getGameFactoryUrl() {
        return CommonUtil.getConfigValue("gameFactoryUrl");
    }

    private ResultRemote extractResultRemote(String jsonResultRemote) {
        Gson gson = new Gson();
        return gson.fromJson(jsonResultRemote, ResultRemote.class);
    }

    static class ResultRemote {
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
