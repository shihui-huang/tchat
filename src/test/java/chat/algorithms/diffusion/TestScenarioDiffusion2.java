package chat.algorithms.diffusion;

import chat.client.Client;
import chat.client.algorithms.chat.ChatMsgContent;
import chat.common.Interceptors;
import chat.common.Log;
import chat.common.Scenario;
import chat.common.VectorClock;
import chat.server.Server;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static chat.common.Log.LOGGER_NAME_CHAT;
import static chat.common.Log.LOGGER_NAME_COMM;
import static chat.common.Log.LOGGER_NAME_DIFFUSION;
import static chat.common.Log.LOGGER_NAME_ELECTION;
import static chat.common.Log.LOGGER_NAME_GEN;
import static chat.common.Log.LOGGER_NAME_INTERCEPT;
import static chat.common.Log.LOGGER_NAME_TEST;
import static chat.common.Log.LOG_ON;
import static chat.common.Log.TEST;

public class TestScenarioDiffusion2 extends Scenario {
    @Test
    @Override
    public void constructAndRun() throws Exception {
        Scenario.setIsJUnitScenario(); // mandatory
        Log.configureALogger(LOGGER_NAME_CHAT, Level.WARN);
        Log.configureALogger(LOGGER_NAME_INTERCEPT, Level.WARN);
        Log.configureALogger(LOGGER_NAME_COMM, Level.WARN);
        Log.configureALogger(LOGGER_NAME_ELECTION, Level.WARN);
        Log.configureALogger(LOGGER_NAME_DIFFUSION, Level.WARN);
        Log.configureALogger(LOGGER_NAME_GEN, Level.WARN);
        Log.configureALogger(LOGGER_NAME_TEST, Level.WARN);

        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("starting the servers...");
        }
        Server s1 = instanciateAServer("1");
        sleep(500);

        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("starting the clients...");
        }
        // start the clients
        Client c1 = instanciateAClient(s1.identity());
        sleep(500);
        Client c2 = instanciateAClient(s1.identity());
        sleep(500);
        Client c3 = instanciateAClient(s1.identity());

        Interceptors.setInterceptionEnabled(true);

        Predicate<ChatMsgContent> conditionForInterceptingI1OnC3 = msg -> msg.getSender() == c1.identity();
        Predicate<ChatMsgContent> conditionForExecutingI1OnC3 = msg -> c1.getVectorClock().getEntry(c1.identity()) == 2;
        Consumer<ChatMsgContent> treatmentI1OnC3 = msg -> chat.client.algorithms.chat.ChatAction.CHAT_MESSAGE
                .execute(c3, new ChatMsgContent(msg.getSender(), msg.getSequenceNumber(),
                        msg.getContent() + ", intercepted at client c3 by i1", new VectorClock(msg.getVectorClock())));
        Interceptors.addAnInterceptor("i1", s1, conditionForInterceptingI1OnC3, conditionForExecutingI1OnC3,
                treatmentI1OnC3);

        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("starting the test of the algorithms...");
        }
        emulateAnInputLineFromTheConsoleForAClient(c1, "message 3 from c1 (" + c1 + ")");
        emulateAnInputLineFromTheConsoleForAClient(c2, "message 4 from c2 (" + c2 + ")");
        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("end of the scenario.");
        }

        // Wait and then flush stdout (necessary for IDEs such as Eclipse).
        // Without sleep, not all outputs.
        // Without flush, no output in Eclipse for instance.
        sleep(3000);
        System.out.flush();

        VectorClock vectorClock = new VectorClock();
        vectorClock.setEntry(100, 1);
        vectorClock.setEntry(101, 1);
        vectorClock.setEntry(102, 0);
        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("vectorClock=" + vectorClock);
            TEST.info("c1 vectorClock=" + c1.getVectorClock());
            TEST.info("c2 vectorClock=" + c2.getVectorClock());
            TEST.info("c3 vectorClock=" + c3.getVectorClock());
        }
        Assert.assertTrue(vectorClock.isEqualTo(c1.getVectorClock()));
        Assert.assertTrue(vectorClock.isEqualTo(c2.getVectorClock()));
        Assert.assertTrue(vectorClock.isEqualTo(c3.getVectorClock()));
    }
}
