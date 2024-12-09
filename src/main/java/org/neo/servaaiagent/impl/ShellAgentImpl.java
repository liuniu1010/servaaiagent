package org.neo.servaaiagent.ifc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;
import org.neo.servaaiagent.ifc.ShellAgentIFC;

public class ShellAgentImpl implements ShellAgentIFC {
    private static ShellAgentIFC instance = new ShellAgentImpl();

    private ShellAgentImpl() {
    }

    public static ShellAgentIFC getInstance() {
        return instance;
    }

    Map<String, Shell> shellCache = new ConcurrentHashMap<String, Shell>();

    @Override
    public String execute(String session, String command) {
        try {
            Shell shell = getOrCreateShell(session);
            return shell.executeCommand(command);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String execute(DBConnectionIFC dbConnection, String session, String command) {
        throw new NeoAIException("not supported");
    }

    @Override 
    public void terminateShell(String session) {
        if(shellCache.containsKey(session)) {
            Shell shell = shellCache.get(session);
            shellCache.remove(session);
            shell.close();
        }
    }

    @Override 
    public void terminateShell(DBConnectionIFC dbConnection, String session) {
        throw new NeoAIException("not supported");
    }

    private Shell getOrCreateShell(String session) throws Exception {
        if(shellCache.containsKey(session)) {
            return shellCache.get(session);
        }

        Shell shell = new Shell();
        shellCache.put(session, shell);
        return shell;
    }
}

class Shell {
    private Process shellProcess;
    private BufferedWriter shellWriter;
    private BufferedReader shellReader;

    public Shell() throws IOException {
        String commander = CommonUtil.isUnix()?"sh":"cmd";
        ProcessBuilder pb = new ProcessBuilder(commander);
        pb.redirectErrorStream(true);
        shellProcess = pb.start();

        shellWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
        shellReader = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));
    }

    public String executeCommand(String command) throws IOException {
        shellWriter.write(command);
        shellWriter.newLine();
        shellWriter.flush();

        // Add a unique marker to identify when the command output ends
        String marker = "END_OF_COMMAND_OUTPUT_" + System.currentTimeMillis();
        shellWriter.write("echo " + marker);
        shellWriter.newLine();
        shellWriter.flush();

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = shellReader.readLine()) != null) {
            if (line.contains(marker)) {
                break;
            }
            output.append(line).append("\n");
        }

        return output.toString();
    }

    public void close() {
        try {
            shellWriter.write("exit");
            shellWriter.newLine();
            shellWriter.flush();
            shellProcess.waitFor();
            shellWriter.close();
            shellReader.close();
        }
        catch(IOException iex) {
        }
        catch(InterruptedException itex) {
        }
    }
}

