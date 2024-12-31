package org.neo.servaaiagent.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

    Map<String, ShellIFC> shellCache = new ConcurrentHashMap<String, ShellIFC>();

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
            ShellIFC shell = shellCache.get(session);
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

        ShellIFC shell = getOrCreateShell(session);
        try {
            return shell.executeCommand(command);
        }
        catch(TimeoutException tex) {
            terminateShell(session);
            throw tex;
        }
        catch(IOException iex) {
            terminateShell(session);
            return innerExecute(session, command, repeatDeep - 1);
        }
    }

    private ShellIFC getOrCreateShell(String session) throws Exception {
        if(shellCache.containsKey(session)) {
            return shellCache.get(session);
        }

        ShellIFC shell = null;
        if(CommonUtil.isUnix()) {
            shell = new LinuxBashShell();
        }
        else {
            shell = new WindowsShell();
        }
        shellCache.put(session, shell);
        return shell;
    }
}

interface ShellIFC {
    public String executeCommand(String command) throws IOException, TimeoutException;
    public void close();
}

abstract class AbsShell implements ShellIFC {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbsShell.class);
    protected Process shellProcess;
    protected BufferedWriter shellWriter;
    protected BufferedReader shellReader;

    protected void flushCommand(String command) throws IOException {
        shellWriter.write(command);
        shellWriter.newLine();
        shellWriter.flush();
        logger.debug("command flushed: " + command);
    }

    @Override
    public void close() {
        try {
            // Try to gracefully terminate the process
            if (shellProcess.isAlive()) {
                shellWriter.write("exit");
                shellWriter.newLine();
                shellWriter.flush();
            }

            // Give the process a moment to terminate gracefully
            if (!shellProcess.waitFor(1, TimeUnit.SECONDS)) {
                // Forcefully terminate the process if it didn't exit
                shellProcess.destroyForcibly();
            }
        } 
        catch (IOException | InterruptedException e) {
            logger.error("Error while closing shell process: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status if interrupted
        } 
        finally {
            // Ensure resources are cleaned up
            try {
                if (shellWriter != null) {
                    shellWriter.close();
                }
                if (shellReader != null) {
                    shellReader.close();
                }
            } 
            catch (IOException e) {
                logger.error("Error while closing shell I/O streams: " + e.getMessage());
            }
        }
    }

    protected String stripPercent(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("^%|%$", "");
    }

    protected String stringListToString(List<String> stringList) {
        String result = "";
        for(String output: stringList) {
            result += output + "\n";
        }

        return result;
    }
}

class LinuxBashShell extends AbsShell {
    public LinuxBashShell() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("bash");
        pb.redirectErrorStream(true);
        shellProcess = pb.start();

        shellWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
        shellReader = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));
    }

    @Override
    public String executeCommand(String command) throws IOException, TimeoutException {
        // append command with extra tail
        String echoExitCodeCommand = "echo $?";
        String marker = "END_OF_COMMAND_OUTPUT_" + System.currentTimeMillis();
        String echoMarker = "echo " + marker;

        String commandWithTail = command + " ; " + echoExitCodeCommand + " ; " + echoMarker;

        // flush the input command with tail
        flushCommand(commandWithTail);

        List<String> listOutput = new ArrayList<String>();
        long startTime = System.currentTimeMillis();
        long timeoutMillis = 1000*30;  // 30 seconds as timeout
        String line;
        while(true) {
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                throw new TimeoutException("Timed out with output:\n " + stringListToString(listOutput));
            }

            if(shellReader.ready()) {
                line = shellReader.readLine();
                logger.debug("read line: " + line);
                if (line.contains(marker)) {
                    logger.debug("break out loop to return");
                    break;
                }
                listOutput.add(line);
                startTime = System.currentTimeMillis();  // reset start time
            }
            else {
                try {
                    Thread.sleep(100); // Sleep for 100 milliseconds
                } 
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    throw new IOException("Thread was interrupted while waiting for shell output.", ie);
                }
            }
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

        String result = stringListToString(listOutput);

        if(exitCode == 0) {
            return result;
        }
        else {
            throw new NeoAIException(result);
        }
    }
}

class WindowsShell extends AbsShell {
    public WindowsShell() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("cmd", "/q");
        pb.redirectErrorStream(true);
        shellProcess = pb.start();

        shellWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
        shellReader = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));
    }

    @Override
    public String executeCommand(String command) throws IOException, TimeoutException {
        // append command with extra tail
        String echoExitCodeCommand = "echo %%ERRORLEVEL%%";
        String marker = "END_OF_COMMAND_OUTPUT_" + System.nanoTime();
        String echoMarker = "echo " + marker;

        String commandWithTail = command + " & " + echoExitCodeCommand + " & " + echoMarker;

        // flush the input command with tail
        flushCommand(commandWithTail);

        List<String> listOutput = new ArrayList<String>();
        long startTime = System.nanoTime();
        long timeoutInMillis = 30*1000;  // 30 seconds as timeout
        InputStream inputStream = shellProcess.getInputStream();
        while(true) {
            long currentTimeNano = System.nanoTime();
            if ((currentTimeNano - startTime)/(1000*1000.0) > timeoutInMillis) {
                throw new TimeoutException("Timed out with output:\n " + stringListToString(listOutput));
            }

            // if(shellReader.ready()) {
            if (inputStream.available() > 0) {
                byte[] byteBuffer = new byte[inputStream.available()];
                int bytesRead = inputStream.read(byteBuffer);
                String inputString = new String(byteBuffer, 0, bytesRead);

                List<String> lines = splitByNewline(inputString);
                boolean shouldBreak = false;
                for(String line: lines) {
                    logger.debug("read line: " + line);
                    if (line.contains(marker)) {
                        logger.debug("break out loop to return");
                        shouldBreak = true;
                        break;
                    }
                    listOutput.add(line);
                    startTime = System.nanoTime();  // reset start time
                }
                if(shouldBreak) {
                    break;
                }
            }
            else {
                try {
                    Thread.sleep(100); // Sleep for 100 milliseconds
                } 
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    throw new IOException("Thread was interrupted while waiting for shell output.", ie);
                }
            }
        }

        int exitCode = -1;
        String lastLine = listOutput.get(listOutput.size() - 1);
        String exitCodeStr = lastLine.toString().trim();
        exitCodeStr = stripPercent(exitCodeStr);
        if (!exitCodeStr.isEmpty()) {
            try {
                exitCode = Integer.parseInt(exitCodeStr);
            } catch (NumberFormatException e) {
                exitCode = 1; 
            }
        }

        // the last line only indicates command success or fail, remove it from the result
        listOutput.remove(listOutput.size() - 1);

        String result = stringListToString(listOutput);

        if(exitCode == 0) {
            return result;
        }
        else {
            throw new NeoAIException(result);
        }
    }

    private List<String> splitByNewline(String input) {
        List<String> lines = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return lines; // Return an empty list for null or empty input
        }

        String[] splitLines = input.split("\n");
        for (String line : splitLines) {
            lines.add(line); // Add each line to the list
        }

        return lines;
    }
}
