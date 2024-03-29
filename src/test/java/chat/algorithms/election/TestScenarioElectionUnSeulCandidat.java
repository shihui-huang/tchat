package chat.algorithms.election;

import chat.common.Log;
import chat.common.Scenario;
import chat.server.Server;
import chat.server.algorithms.election.ElectionStatus;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import static chat.common.Log.*;
import static chat.common.Log.TEST;

public class TestScenarioElectionUnSeulCandidat extends Scenario {
    @Test
    @Override
    public void constructAndRun() throws Exception {
        Scenario.setIsJUnitScenario(); // mandatory
        Log.configureALogger(LOGGER_NAME_CHAT, Level.WARN);
        Log.configureALogger(LOGGER_NAME_INTERCEPT, Level.WARN);
        Log.configureALogger(LOGGER_NAME_COMM, Level.WARN);
        Log.configureALogger(LOGGER_NAME_ELECTION, Level.WARN);
        Log.configureALogger(LOGGER_NAME_GEN, Level.WARN);
        Log.configureALogger(LOGGER_NAME_TEST, Level.WARN);

        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("starting the servers...");
        }
        Server s1 = instanciateAServer("1");
        sleep(500);
        Server s2 = instanciateAServer("2 localhost 1");
        sleep(500);
        Server s3 = instanciateAServer("3 localhost 2");
        sleep(500);
        Server s4 = instanciateAServer("4 localhost 3");
        sleep(500);
        Server s5 = instanciateAServer("5 localhost 1 localhost 2");
        sleep(500);
        Server s6 = instanciateAServer("6 localhost 3");
        sleep(500);

        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("starting the test of the algorithms...");
        }
        emulateAnInputLineFromTheConsoleForAServer(s1, "election");
        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("end of the scenario.");
        }

        // Wait and then flush stdout (necessary for IDEs such as Eclipse).
        // Without sleep, not all outputs.
        // Without flush, no output in Eclipse for instance.
        sleep(3000);
        System.out.flush();
        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("");
        }

        // the scenario has ended, then access the state of clients without synchronised
        // blocks.
        Assert.assertEquals(ElectionStatus.LEADER, s1.getStatus());
        Assert.assertEquals(ElectionStatus.NON_LEADER, s2.getStatus());
        Assert.assertEquals(ElectionStatus.NON_LEADER, s3.getStatus());
        Assert.assertEquals(ElectionStatus.NON_LEADER, s4.getStatus());
        Assert.assertEquals(ElectionStatus.NON_LEADER, s5.getStatus());
        Assert.assertEquals(ElectionStatus.NON_LEADER, s6.getStatus());

        Assert.assertEquals(s1.identity(), s1.getWin());
        Assert.assertEquals(s1.identity(), s2.getWin());
        Assert.assertEquals(s1.identity(), s3.getWin());
        Assert.assertEquals(s1.identity(), s4.getWin());
        Assert.assertEquals(s1.identity(), s5.getWin());
        Assert.assertEquals(s1.identity(), s6.getWin());

        // finish properly
        emulateAnInputLineFromTheConsoleForAServer(s1, "quit");
        sleep(100);
        emulateAnInputLineFromTheConsoleForAServer(s2, "quit");
        sleep(100);
        emulateAnInputLineFromTheConsoleForAServer(s3, "quit");
        sleep(100);
        emulateAnInputLineFromTheConsoleForAServer(s4, "quit");
        sleep(100);
        emulateAnInputLineFromTheConsoleForAServer(s5, "quit");
        sleep(100);
        emulateAnInputLineFromTheConsoleForAServer(s6, "quit");
        sleep(100);
    }
}
