package org.neo.servaaiagent.ifc;

import java.io.OutputStream;

public interface NotifyCallbackIFC {
    public void notify(String information);
    public void registerWorkingThread();
    public void removeWorkingThread();
    public boolean isWorkingThread();
    public void changeOutputStream(OutputStream inputOutputStream);
    public void closeOutputStream();
    public void notifyHistory();
    public void clearHistory();
}
