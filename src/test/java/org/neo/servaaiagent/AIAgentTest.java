package org.neo.servaframe;

import java.util.*;
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
}

