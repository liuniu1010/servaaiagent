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
import org.neo.servaaiagent.ifc.PersistentShellAgentIFC;

public class PersistentShellAgentImpl implements PersistentShellAgentIFC {
    Map<String, PersistentShell> shellCache = new ConcurrentHashMap<String, PersistentShell>();

    @Override
    public String execute(String session, String input) {
        try {
            PersistentShell shell = getShell(session);
            return shell.executeCommand(input);
        }
        catch(NeoAIException nex) {
            throw nex;
        }
        catch(Exception ex) {
            throw new NeoAIException(ex.getMessage(), ex);
        }
    }

    @Override
    public String execute(DBConnectionIFC dbConnection, String session, String input) {
        // to be implemented
        return null;
    }

    @Override 
    public void terminateShell(String session) {
        // to be implemented
    }

    @Override 
    public void terminateShell(DBConnectionIFC dbConnection, String session) {
        // to be implemented
    }

    private PersistentShell getShell(String session) throws Exception {
        if(shellCache.containsKey(session)) {
            return shellCache.get(session);
        }

        PersistentShell shell = new PersistentShell();
        shellCache.put(session, shell);
        return shell;
    }
}

class PersistentShell {
    private Process shellProcess;
    private BufferedWriter shellWriter;
    private BufferedReader shellReader;

    public PersistentShell() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(new String[]{"/bin/sh", "-c", ""});
        pb.redirectErrorStream(true);
        shellProcess = pb.start();

        shellWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
        shellReader = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));
    }

    public String executeCommand(String input) throws IOException {
        shellWriter.write(input);
        shellWriter.newLine();
        shellWriter.flush();

        // Add a unique marker to identify when the input output ends
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

    public void close() throws IOException, InterruptedException {
        shellWriter.write("exit");
        shellWriter.newLine();
        shellWriter.flush();
        shellProcess.waitFor();
        shellWriter.close();
        shellReader.close();
    }

    public void test() {
        try {
            PersistentShell shell = new PersistentShell();

            String output1 = shell.executeCommand("pwd");
            System.out.println("Current Directory:\n" + output1);

            shell.executeCommand("cd /tmp");
            String output2 = shell.executeCommand("pwd");
            System.out.println("Changed Directory:\n" + output2);

            shell.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

