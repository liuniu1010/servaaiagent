package org.neo.servaaiagent.impl;

import java.util.List;

import org.neo.servaframe.ServiceFactory;
import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaframe.interfaces.DBServiceIFC;
import org.neo.servaframe.interfaces.DBSaveTaskIFC;

import org.neo.servaaibase.ifc.StorageIFC;
import org.neo.servaaibase.impl.StorageInDBImpl;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.ImageAgentIFC;
import org.neo.servaaiagent.ifc.NotifyCallbackIFC;
import org.neo.servaaiagent.impl.AbsChatForUIInDBImpl;

public class ChatWithImageExpertForUIImpl extends AbsChatForUIInDBImpl {
    private ChatWithImageExpertForUIImpl() {
    }

    public static ChatWithImageExpertForUIImpl getInstance() {
        return new ChatWithImageExpertForUIImpl();
    }

    @Override
    public String fetchResponse(String session, String userInput, List<String> attachFiles) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new ChatWithImageExpertForUIImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    return innerFetchResponse(dbConnection, session, userInput);
                }
            });
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(standardExceptionMessage, ex);
        }
    }

    @Override
    public String fetchResponse(String session, NotifyCallbackIFC notifyCallback, String userInput, List<String> attachFiles) {
        try {
            DBServiceIFC dbService = ServiceFactory.getDBService();
            return (String)dbService.executeSaveTask(new ChatWithImageExpertForUIImpl() {
                @Override
                public Object save(DBConnectionIFC dbConnection) {
                    return innerFetchResponse(dbConnection, session, userInput);
                }
            });
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(standardExceptionMessage, ex);
        }
    }

    private String innerFetchResponse(DBConnectionIFC dbConnection, String session, String userInput) {
        ImageAgentIFC imageAgent = ImageAgentImpl.getInstance();
        imageAgent.generateImages(dbConnection, session, userInput);
        String datetimeFormat = CommonUtil.getConfigValue(dbConnection, "DateTimeFormat");
        StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
        return CommonUtil.renderChatRecords(storage.getChatRecords(session), datetimeFormat);
    }
}

