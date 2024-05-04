package org.neo.servaframe;

import java.util.*;
import java.io.*;
import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.neo.servaframe.util.*;
import org.neo.servaframe.interfaces.*;
import org.neo.servaframe.model.*;

import org.neo.servaaibase.util.*;
import org.neo.servaaibase.ifc.*;
import org.neo.servaaibase.impl.*;
import org.neo.servaaibase.model.*;

import org.neo.servaaiagent.ifc.*;
import org.neo.servaaiagent.impl.*;
/**
 * Unit test 
 */
public class AIAgentTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AIAgentTest( String testName ) {
        super( testName );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cleanDatabase();
    }

    @Override
    protected void tearDown() throws Exception {
        // Code to clean up resources after each test method
        super.tearDown();
    }

    private String chatTestSession = "chatTestSession";
    private String imageTestSession = "imageTestSession";
    private String visionTestSession = "visionTestSession";
    private String commandTestSession = "commandTestSession";

    private void cleanDatabase() {
        DBServiceIFC dbService = ServiceFactory.getDBService();
        dbService.executeSaveTask(new DBSaveTaskIFC() {
            @Override
            public Object save(DBConnectionIFC dbConnection) {
                StorageIFC storage = StorageInDBImpl.getInstance(dbConnection);
                storage.clearChatRecords(chatTestSession);
                storage.clearChatRecords(imageTestSession);
                storage.clearChatRecords(visionTestSession);
                storage.clearChatRecords(commandTestSession);
                return null;
            }
        });
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( AIAgentTest.class );
    }

    public void testChatAgent() {
        ChatAgentIFC chatAgent = ChatAgentImpl.getInstance();
        String userInput = "Hello, I'm neo, nice to meet you!";
        String response = chatAgent.chat(chatTestSession, userInput);

        System.out.println("userInput = " + userInput);
        System.out.println("response = " + response);
    }

    public void testImageAgent() {
        ImageAgentIFC imageAgent = ImageAgentImpl.getInstance();
        String userInput = "Blue sky outside the window, with white clouds and blue sea";
        String[] urls = imageAgent.generateImages(imageTestSession, userInput);
        System.out.println("userInput = " + userInput);
        for(String url: urls) {
            System.out.println("image url = " + url);
        }
    }

    public void testVisionAgent() throws Exception {
        VisionAgentIFC visionAgent = VisionAgentImpl.getInstance();
        String userInput = "Hello, please give me an description of the images";
        InputStream in1 = new FileInputStream("/tmp/dogandcat.png");
        String rawBase64OfAttach1 = IOUtil.inputStreamToRawBase64(in1);
        String file1Content = "data:image/png;base64," + rawBase64OfAttach1;

        InputStream in2 = new FileInputStream("/tmp/image.jpg");
        String rawBase64OfAttach2 = IOUtil.inputStreamToRawBase64(in2);
        String file2Content = "data:image/jpeg;base64," + rawBase64OfAttach2;

        List<String> attachFiles = new ArrayList<String>();
        attachFiles.add(file1Content);
        attachFiles.add(file2Content);
        String response = visionAgent.vision(visionTestSession, userInput, attachFiles);

        System.out.println("userInput = " + userInput);
        System.out.println("response = " + response);
    }

    public void testLinuxCommanderAgent() {
        String userInput = "please check amount of disk left";
        LinuxCommanderAgentIFC commandIFC = LinuxCommanderAgentImpl.getInstance();

        System.out.println("test generateCommand");
        String response = commandIFC.generateCommand(commandTestSession, userInput);
        System.out.println("userInput = " + userInput);
        System.out.println("response = " + response);

        System.out.println("test generateAndExecute");
        response = commandIFC.generateAndExecute(commandTestSession, userInput);
        System.out.println("userInput = " + userInput);
        System.out.println("response = " + response);

        System.out.println("test execute");
        userInput = "ls -l";
        response = commandIFC.generateAndExecute(commandTestSession, userInput);
        System.out.println("userInput = " + userInput);
        System.out.println("response = " + response);
    }
}


