package org.neo.servaaiagent.ifc;

import java.io.*;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaaiagent.ifc.PersistentShellAgentIFC;

public class PersistentShellAgentImpl implements PersistentShellAgentIFC {
    @Override
    public String execute(String session, String command) {
        // to be implemented
        return null;
    }

    @Override
    public String execute(DBConnectionIFC dbConnection, String session, String command) {
        // to be implemented
        return null;
    }

    @Override 
    public void terminateShell(String session) {
        // to be implemented
    }

    public void terminateShell(DBConnectionIFC dbConnection, String session) {
        // to be implemented
    }
}

class PersistentShell {
    private Process shellProcess;
    private BufferedWriter shellWriter;
    private BufferedReader shellReader;
    private BufferedReader shellErrorReader;

    public PersistentShell() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("/bin/bash");
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

