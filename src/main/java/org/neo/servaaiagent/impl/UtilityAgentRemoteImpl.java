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
    public AIModel.ChatResponse generatePageCode(String prompt, String code) {
        try {
            // return innerGeneratePageCode(prompt, code);
            return innerGeneratePageCodeWithJob(prompt, code);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private AIModel.ChatResponse innerGeneratePageCode(String prompt, String code) throws Exception {
        String sUrl = getGameFactoryUrl() + "/generate";
        String jsonInput = generateJsonBodyToRemote(prompt, code);
        String jsonResult = sendInputToRemote("POST", jsonInput, sUrl);
        ResultGameFactoryResponse resultGameFactoryResponse = extractResultGameFactoryResponse(jsonResult);

        if(resultGameFactoryResponse.getJobStatus().equals(ResultGameFactoryResponse.JOB_STATUS_DONE)) {
            return new AIModel.ChatResponse(true, resultGameFactoryResponse.getCode());
        }
        else {
            return new AIModel.ChatResponse(false, resultGameFactoryResponse.getMessage());
        }
    }

    private AIModel.ChatResponse innerGeneratePageCodeWithJob(String prompt, String code) throws Exception {
        ResultGameFactoryResponse resultGameFactoryResponse = createJob(prompt, code);
        String jobId = resultGameFactoryResponse.getJobId();
        int waitSeconds = 10;
        int waitTimes = 10;
        for(int i = 0;i < waitTimes;i++) {
            Thread.sleep(1000*waitSeconds);
            resultGameFactoryResponse = checkJob(jobId);
            if(resultGameFactoryResponse.getJobStatus().equals(ResultGameFactoryResponse.JOB_STATUS_DONE)) {
                return new AIModel.ChatResponse(true, resultGameFactoryResponse.getCode());
            }
            else if(resultGameFactoryResponse.getJobStatus().equals(ResultGameFactoryResponse.JOB_STATUS_FAILED)) {
                return new AIModel.ChatResponse(false, resultGameFactoryResponse.getMessage());
            }
            continue;
        }
        return new AIModel.ChatResponse(false, "time out");
    }

    private ResultGameFactoryResponse createJob(String prompt, String code) throws Exception {
        String sUrl = getGameFactoryUrl() + "/jobs";
        String jsonInput = generateJsonBodyOfCreateJob(prompt, code);
        String jsonResult = sendInputToRemote("POST",jsonInput, sUrl);
        ResultGameFactoryResponse resultGameFactoryResponse = extractResultGameFactoryResponse(jsonResult);
        return resultGameFactoryResponse;
    }

    private ResultGameFactoryResponse checkJob(String jobId) throws Exception {
        String sUrl = getGameFactoryUrl() + "/jobs/" + jobId;
        String jsonResult = sendInputToRemote("GET", "", sUrl);
        ResultGameFactoryResponse resultGameFactoryResponse = extractResultGameFactoryResponse(jsonResult);
        return resultGameFactoryResponse;
    }

    private String generateJsonBodyToRemote(String prompt, String code) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        
        jsonBody.addProperty("prompt", prompt);
        jsonBody.addProperty("code", code);
        
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyOfCreateJob(String prompt, String code) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        
        jsonBody.addProperty("prompt", prompt);
        jsonBody.addProperty("code", code);
        
        return gson.toJson(jsonBody);
    }

    private String sendInputToRemote(String method, String jsonInput, String sUrl) throws Exception {
        jsonInput = CommonUtil.alignJson(jsonInput);
        logger.debug("call remote api, jsonInput = " + jsonInput);
        logger.debug("sUrl = " + sUrl);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
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

    private ResultGameFactoryResponse extractResultGameFactoryResponse(String jsonResultGameFactoryResponse) {
        Gson gson = new Gson();
        return gson.fromJson(jsonResultGameFactoryResponse, ResultGameFactoryResponse.class);
    }

    static class ResultGameFactoryResponse {
        public final static String JOB_STATUS_INPROGRESS = "inprogress";
        public final static String JOB_STATUS_DONE = "done";
        public final static String JOB_STATUS_FAILED = "failed";

        private String jobId = "";
        private String jobStatus = "";   // inprogress/done/failed 
        private String code = "";
        private String message = "";

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String inputJobId) {
            jobId = inputJobId == null?"":inputJobId;
        }

        public String getJobStatus() {
            return jobStatus;
        }

        public void setJobStatus(String inputJobStatus) {
            jobStatus = inputJobStatus == null?"":inputJobStatus;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String input_code) {
            code = input_code == null?"":input_code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String inputMessage) {
            message = inputMessage == null?"":inputMessage;
        }
    }
}
