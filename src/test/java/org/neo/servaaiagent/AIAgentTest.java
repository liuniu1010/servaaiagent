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
    private String speechTestSession = "speechTestSession";
    private String translateTestSession = "translateTestSession";
    private String coderTestSession = "coderTestSession";
    private String managerTestSession = "managerTestSession";

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
                storage.clearChatRecords(speechTestSession);
                storage.clearChatRecords(translateTestSession);
                storage.clearChatRecords(coderTestSession);
                storage.clearChatRecords(managerTestSession);
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

    private String loadBackgroundDesc(String coder) throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String fileName = coder + ".txt";
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        String backgroundDesc = IOUtil.inputStreamToString(inputStream);
        return backgroundDesc;
    }

    public void _testCoderAgent() throws Exception {
        CoderAgentIFC coderAgent = CoderAgentImpl.getInstance();
        String backgroundDesc = loadBackgroundDesc("codeadjustment");
        String requirement = "I have a java project under folder /home/liuniu/git/github/servaframe ";
        requirement += "\nThis is a maven project.";
        requirement += "\nPlease check all java files under main folder, in each java file, please check all import lines at the head of the file, adjust them in alphabet order";
        // String response = coderAgent.adjustCode(coderTestSession, null, requirement, backgroundDesc);
        // System.out.println("response = " + response);
    }

    public void _testManagerAgent() throws Exception {
        ManagerAgentIFC managerAgent = ManagerAgentImpl.getInstance("/tmp", "/tmp");
        String requirement = "please write java code with maven which calculate sum from 1 + 100";
        System.out.println("requirement = " + requirement);
        String response = managerAgent.runProject(managerTestSession, null, requirement);
        System.out.println("response = " + response);
    }

    public void _testEmail() {
        String to = "liuniu@tsinghua.org.cn";
        String subject = "this is a test";
        String body = "This email is only for test the api";

        try {
            EmailAgentImpl.getInstance().sendEmail(to, subject, body);
            System.out.println("send success");
        }
        catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void testGetLeftCredits() {
        int accountId = 1;
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        int leftCredits = accountAgent.getLeftCredits(accountId);
        System.out.println("leftCredits = " + leftCredits);
    }

    public void _testLogin() {
        String userName = "liuniu@tsinghua.org.cn";
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        String password = "mH9gbm";
        String loginSession = accountAgent.login(userName, password);

        accountAgent.checkSessionValid(loginSession);

        accountAgent.updateSession(loginSession);
    }

    public void testSendPassword() {
        String userName = "liuniu@tsinghua.org.cn";
        AccountAgentIFC accountAgent = AccountAgentImpl.getInstance();
        accountAgent.sendPassword(userName);
        System.out.println("send success");
    }

    public void testChatAgent() {
        ChatAgentIFC chatAgent = ChatAgentImpl.getInstance();
        String userInput = "Hello, I'm neo, nice to meet you!";
        System.out.println("userInput = " + userInput);
        String response = chatAgent.chat(chatTestSession, userInput);
        System.out.println("response = " + response);
    }

    public void testTranslateAgent() {
        TranslateAgentIFC translateAgent = TranslateAgentImpl.getInstance();
        String userInput1 = "Hello, I'm neo, nice to meet you!";
        String userInput2 = "这是一段翻译测试";
        String[] userInputs = new String[]{userInput1, userInput2};
        for(String userInput: userInputs) {
            System.out.println("userInput = " + userInput);
            String response = translateAgent.translate(chatTestSession, userInput);
            System.out.println("response = " + response);
        }
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

    public void testSpeechAgentTextToSpeech() {
        SpeechAgentIFC speechAgent = SpeechAgentImpl.getInstance("mp3");
        String userInput = "Blue sky outside the window, with white clouds and blue sea";
        String absolutePath = "/tmp/";
        System.out.println("userInput = " + userInput);
        String filePath = speechAgent.generateSpeech(speechTestSession, userInput, absolutePath);
        System.out.println("file generated = " + filePath); 
    }

    public void testSpeechAgentSpeechToText() {
        SpeechAgentIFC speechAgent = SpeechAgentImpl.getInstance("mp3");
        String filePath = "/tmp/audio_TcF2bG8YvM.mp3";
        System.out.println("filePath = " + filePath);
        String text = speechAgent.speechToText(speechTestSession, filePath);
        System.out.println("text = " + text);
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


