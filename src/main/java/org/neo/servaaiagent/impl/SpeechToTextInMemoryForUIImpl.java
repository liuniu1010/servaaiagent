package org.neo.servaaiagent.impl;

import java.util.List;
import java.io.File;

import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.SpeechAgentIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInMemoryImpl;
import org.neo.servaaiagent.model.AgentModel;

public class SpeechToTextInMemoryForUIImpl extends AbsChatForUIInMemoryImpl {
    private String outputFormat = "mp3";
    private String onlineFileAbsolutePath;
    private String relevantVisitPath;

    private SpeechToTextInMemoryForUIImpl() {
    }

    private SpeechToTextInMemoryForUIImpl(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        onlineFileAbsolutePath = inputOnlineFileAbsolutePath;
        relevantVisitPath = inputRelevantVisitPath;
    }

    public static SpeechToTextInMemoryForUIImpl getInstance(String inputOnlineFileAbsolutePath, String inputRelevantVisitPath) {
        return new SpeechToTextInMemoryForUIImpl(inputOnlineFileAbsolutePath, inputRelevantVisitPath);
    }

    @Override
    public String sendAudio(AgentModel.UIParams params) {
        try {
            return innerSendAudio(params);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    private String innerSendAudio(AgentModel.UIParams params) throws Exception {
        String alignedSession = params.getAlignedSession();
        String userInput = params.getUserInput();
        List<String> attachFiles = params.getAttachFiles();

        String base64 = attachFiles.get(0);
        String fileName = CommonUtil.base64ToFile(base64, onlineFileAbsolutePath);
        String filePath = CommonUtil.normalizeFolderPath(onlineFileAbsolutePath) + File.separator + fileName;

        SpeechAgentIFC speechAgent = SpeechAgentImpl.getInstance(outputFormat);
        String text = speechAgent.speechToText(alignedSession, filePath);
        return text;
    }
}

