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
    final static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(UtilityAgentRemoteImpl.class);
    final static String RAPIDAPI_SECRET = "X-RapidAPI-Proxy-Secret";

    private UtilityAgentRemoteImpl() {
    }

    public static UtilityAgentRemoteImpl getInstance() {
        return new UtilityAgentRemoteImpl();
    }

    @Override
    public AIModel.ChatResponse generatePageCode(String prompt, String code, String theFunction) {
        try {
            return innerGeneratePageCode(prompt, code, theFunction);
            // return innerGeneratePageCodeWithJob(prompt, code);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private AIModel.ChatResponse innerGeneratePageCode(String prompt, String code, String theFunction) throws Exception {
        String sUrl = getGameFactoryUrl() + "/generate";
        String jsonInput = generateJsonBodyToRemote(prompt, code, theFunction);
        String jsonResult = sendInputToRemoteWithPost(sUrl, jsonInput);
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
        String jsonResult = sendInputToRemoteWithPost(sUrl, jsonInput);
        ResultGameFactoryResponse resultGameFactoryResponse = extractResultGameFactoryResponse(jsonResult);
        return resultGameFactoryResponse;
    }

    private ResultGameFactoryResponse checkJob(String jobId) throws Exception {
        String sUrl = getGameFactoryUrl() + "/jobs/" + jobId;
        String jsonResult = sendInputToRemoteWithGet(sUrl);
        ResultGameFactoryResponse resultGameFactoryResponse = extractResultGameFactoryResponse(jsonResult);
        return resultGameFactoryResponse;
    }

    private String generateJsonBodyToRemote(String prompt, String code, String theFunction) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        
        jsonBody.addProperty("prompt", prompt);
        jsonBody.addProperty("code", code);
        jsonBody.addProperty("theFunction", theFunction);
        
        return gson.toJson(jsonBody);
    }

    private String generateJsonBodyOfCreateJob(String prompt, String code) {
        Gson gson = new Gson();
        JsonObject jsonBody = new JsonObject();
        
        jsonBody.addProperty("prompt", prompt);
        jsonBody.addProperty("code", code);
        
        return gson.toJson(jsonBody);
    }

    /**
     * Issue a POST request with a JSON body and return the server-supplied body as a String.
     */
    private String sendInputToRemoteWithPost(String sUrl, String jsonInput) throws Exception {
        jsonInput = CommonUtil.alignJson(jsonInput);
        logger.info("call remote api");
        logger.info("POST " + sUrl);
        logger.info("body = " + jsonInput);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(sUrl);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty(RAPIDAPI_SECRET, CommonUtil.getConfigValue(RAPIDAPI_SECRET));
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                IOUtil.stringToOutputStream(jsonInput, os);
            }

            return readResponse(conn);
        } 
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Issue a GET request with no body and return the server-supplied body as a String.
     */
    private String sendInputToRemoteWithGet(String sUrl) throws Exception {
        logger.info("call remote api");
        logger.info("GET " + sUrl);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(sUrl);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty(RAPIDAPI_SECRET, CommonUtil.getConfigValue(RAPIDAPI_SECRET));

            return readResponse(conn);
        } 
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    /**
     * Reads either the normal stream (2xx) or the error stream (4xx/5xx) from the connection.
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        InputStream in = (status >= 400)?conn.getErrorStream():conn.getInputStream();

        String body = CommonUtil.alignJson(IOUtil.inputStreamToString(in));
        logger.info("return from remote api");
        logger.info("HTTP " + status);
        logger.info("response = " + body);

        if(status >= 400) {
            throw new NeoAIException(body);
        }
        return body;
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
