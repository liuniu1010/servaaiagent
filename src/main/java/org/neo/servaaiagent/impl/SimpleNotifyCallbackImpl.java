package org.neo.servaaiagent.impl;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.neo.servaaibase.NeoAIException;

import org.neo.servaaiagent.ifc.NotifyCallbackIFC;

public class SimpleNotifyCallbackImpl implements NotifyCallbackIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SimpleNotifyCallbackImpl.class);

    private OutputStream  outputStream = null;
    private int workingThreadHashCode = 0;
    private String history = "";

    private SimpleNotifyCallbackImpl() {
    }

    public SimpleNotifyCallbackImpl(OutputStream inputOutputStream) {
        outputStream = inputOutputStream;
    }

    @Override
    public void notify(String information) {
        try {
            if(!isWorkingThread()) {
                throw new NeoAIException(NeoAIException.NEOAIEXCEPTION_NOT_WORKING_THREAD);
            }
            flushInformation(information);
            history += information;
        }
        catch(NeoAIException nex) {
            throw nex;
        }   
        catch(Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    @Override
    public void registerWorkingThread() {
        workingThreadHashCode = Thread.currentThread().hashCode();
    }

    @Override
    public void removeWorkingThread() {
        workingThreadHashCode = 0;
    }

    @Override
    public boolean isWorkingThread() {
        return workingThreadHashCode == Thread.currentThread().hashCode();
    }

    @Override
    public void changeOutputStream(OutputStream inputOutputStream) {
        closeOutputStream(); // close original output stream before switch to new one
        outputStream = inputOutputStream;
    }

    @Override
    public void notifyHistory() {
        try {
            flushInformation(history);
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    @Override
    public void clearHistory() {
        history = "";
    }

    @Override
    public void closeOutputStream() {
        if(outputStream == null) {
            return;
        }
        try {
            outputStream.close();
        }
        catch(Exception ex) {
            logger.error(ex.getMessage());
        }       
    }

    private void flushInformation(String toFlush) throws Exception {
        outputStream.write(toFlush.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }
}
