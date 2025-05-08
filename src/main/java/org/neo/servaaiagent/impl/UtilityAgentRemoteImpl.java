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

    @Override
    public AIModel.ChatResponse generatePageCodeWithJob(String requirement, String code) {
        try {
            return innerGeneratePageCodeWithJob(requirement, code);
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

    private AIModel.ChatResponse innerGeneratePageCodeWithJob(String requirement, String code) throws Exception {
        ResultGameFactoryResponse resultGameFactoryResponse = createJob(requirement, code);
        String jobId = resultGameFactoryResponse.getJob_id();
        int waitSeconds = 10;
        int waitTimes = 10;
        for(int i = 0;i < waitTimes;i++) {
            Thread.sleep(1000*waitSeconds);
            resultGameFactoryResponse = checkJob(jobId);
            if(resultGameFactoryResponse.getJob_status().equals(ResultGameFactoryResponse.JOB_STATUS_DONE)) {
                return new AIModel.ChatResponse(true, resultGameFactoryResponse.getCode());
            }
            else if(resultGameFactoryResponse.getJob_status().equals(ResultGameFactoryResponse.JOB_STATUS_FAILED)) {
                return new AIModel.ChatResponse(false, resultGameFactoryResponse.getMessage());
            }
            else if(resultGameFactoryResponse.getJob_status().equals(ResultGameFactoryResponse.JOB_STATUS_CANCELLED)) {
                return new AIModel.ChatResponse(false, resultGameFactoryResponse.getMessage());
            }
            continue;
        }
        return new AIModel.ChatResponse(false, "time out");
    }

    private ResultGameFactoryResponse createJob(String requirement, String code) throws Exception {
        String sUrl = getGameFactoryUrl() + "/createjob";
        String jsonInput = generateJsonBodyOfCreateJob(requirement, code);
        String jsonResult = sendInputToRemote(jsonInput, sUrl);
        ResultGameFactoryResponse resultGameFactoryResponse = extractResultGameFactoryResponse(jsonResult);
        return resultGameFactoryResponse;
    }

    private ResultGameFactoryResponse checkJob(String jobId) throws Exception {
        String sUrl = getGameFactoryUrl() + "/checkjob";
        String jsonInput = generateJsonBodyOfCheckJob(jobId);
        String jsonResult = sendInputToRemote(jsonInput, sUrl);
        ResultGameFactoryResponse resultGameFactoryResponse = extractResultGameFactoryResponse(jsonResult);
        return resultGameFactoryResponse;
    }

    private String generateJsonBodyToRemote(String userInput, String fileContent) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        
        jsonBody.addProperty("userInput", userInput);
        jsonBody.addProperty("fileContent", fileContent);
        
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyOfCreateJob(String requirement, String code) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        
        jsonBody.addProperty("requirement", requirement);
        jsonBody.addProperty("code", code);
        
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyOfCheckJob(String jobId) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        
        jsonBody.addProperty("job_id", jobId);
        
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

    private ResultGameFactoryResponse extractResultGameFactoryResponse(String jsonResultGameFactoryResponse) {
        Gson gson = new Gson();
        return gson.fromJson(jsonResultGameFactoryResponse, ResultGameFactoryResponse.class);
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

    static class ResultGameFactoryResponse {
        public final static String JOB_STATUS_INPROGRESS = "inprogress";
        public final static String JOB_STATUS_DONE = "done";
        public final static String JOB_STATUS_FAILED = "failed";
        public final static String JOB_STATUS_CANCELLED = "cancelled";

        private String job_id = "";
        private String job_status = "";   // inprogress/done/failed 
        private String code = "";
        private String message = "";

        public String getJob_id() {
            return job_id;
        }

        public void setJob_id(String input_job_id) {
            job_id = input_job_id == null?"":input_job_id;
        }

        public String getJob_status() {
            return job_status;
        }

        public void setJob_status(String input_job_status) {
            job_status = input_job_status == null?"":input_job_status;
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
