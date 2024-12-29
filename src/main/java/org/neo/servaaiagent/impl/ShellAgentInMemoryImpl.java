package org.neo.servaaiagent.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaaibase.util.CommonUtil;
import org.neo.servaaibase.NeoAIException;
import org.neo.servaaiagent.ifc.ShellAgentIFC;

public class ShellAgentInMemoryImpl implements ShellAgentIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ShellAgentInMemoryImpl.class);
    private static ShellAgentIFC instance = new ShellAgentInMemoryImpl();

    private ShellAgentInMemoryImpl() {
    }

    public static ShellAgentIFC getInstance() {
        return instance;
    }

    Map<String, Shell> shellCache = new ConcurrentHashMap<String, Shell>();

    @Override
    public String execute(String session, String command) {
        try {
            int repeatDeep = 2;
            return innerExecute(session, command, repeatDeep);
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

    @Override
    public boolean isUnix(String session) {
        return CommonUtil.isUnix();
    }

    public boolean isUnix(DBConnectionIFC dbConnection, String session) {
        throw new NeoAIException("not supported");
    }

    private String innerExecute(String session, String command, int repeatDeep) throws Exception {
        if(repeatDeep <= 0) {
            throw new RuntimeException("Shell crashed!");
        }

        Shell shell = getOrCreateShell(session);
        try {
            return shell.executeCommand(command);
        }
        catch(IOException iex) {
            terminateShell(session);
            return innerExecute(session, command, repeatDeep - 1);
        }
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
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Shell.class);
    private Process shellProcess;
    private BufferedWriter shellWriter;
    private BufferedReader shellReader;

    public Shell() throws IOException {
        String commander = CommonUtil.isUnix()?"bash":"cmd";
        ProcessBuilder pb = new ProcessBuilder(commander);
        pb.redirectErrorStream(true);
        shellProcess = pb.start();

        shellWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
        shellReader = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));
    }

    private void flushCommand(String command) throws IOException {
        shellWriter.write(command);
        shellWriter.newLine();
        shellWriter.flush();
        logger.debug("command flushed: " + command);
    }

    public String executeCommand(String command) throws IOException {
        // append command with extra tail
        String exitCodeCommand = CommonUtil.isUnix() ? "echo $?" : "echo %ERRORLEVEL%";
        String marker = "END_OF_COMMAND_OUTPUT_" + System.currentTimeMillis();

        String commandWithTail;
        if(CommonUtil.isUnix()) {
            commandWithTail = command + " ; " + exitCodeCommand + " ; " + marker;
        }
        else {
            commandWithTail = command + " & " + exitCodeCommand + " & " + marker;
        }

        // flush the input command with tail
        flushCommand(commandWithTail);

        List<String> listOutput = new ArrayList<String>();
        String line;
        while ((line = shellReader.readLine()) != null) {
            logger.debug("read line: " + line);
            if (line.contains(marker)) {
                logger.debug("break out loop to return");
                break;
            }
            listOutput.add(line);
        }

        int exitCode = -1;
        String lastLine = listOutput.get(listOutput.size() - 1);
        String exitCodeStr = lastLine.toString().trim();
        if (!exitCodeStr.isEmpty()) {
            try {
                exitCode = Integer.parseInt(exitCodeStr);
            } catch (NumberFormatException e) {
                exitCode = 1; 
            }
        }

        // the last line only indicates command success or fail, remove it from the result
        listOutput.remove(listOutput.size() - 1);

        String result = "";
        for(String output: listOutput) {
            result += output + "\n";
        }

        if(exitCode == 0) {
            return result;
        }
        else {
            throw new NeoAIException(result);
        }
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

