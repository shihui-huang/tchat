package chat.algorithms.election;

import chat.common.Interceptors;
import chat.common.Log;
import chat.common.Scenario;
import chat.server.Server;
import chat.server.algorithms.election.ElectionStatus;
import chat.server.algorithms.election.ElectionTokenContent;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static chat.common.Log.*;
import static chat.common.Log.TEST;

public class TestScenarioElectionPlusieursCandidats extends Scenario {
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

        Interceptors.setInterceptionEnabled(true);
        Predicate<ElectionTokenContent> conditionForInterceptingI1OnS2 = msg -> msg.getSender() == s1.identity();
        Predicate<ElectionTokenContent> conditionForExecutingI1OnS2 = msg -> true;
        Consumer<ElectionTokenContent> treatmentI1OnS2 = msg -> chat.server.algorithms.election.ElectionAction.TOKEN_MESSAGE.execute(s2, msg);
        Interceptors.addAnInterceptor("i1", s2, conditionForInterceptingI1OnS2, conditionForExecutingI1OnS2, treatmentI1OnS2);

        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("starting the test of the algorithms...");
        }
        emulateAnInputLineFromTheConsoleForAServer(s5, "election");
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
