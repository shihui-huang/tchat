package chat.algorithms.mutex;

import chat.common.Interceptors;
import chat.common.Log;
import chat.common.Scenario;
import chat.server.Server;
import chat.server.algorithms.mutex.MutexAction;
import chat.server.algorithms.mutex.MutexRequestTokenContent;
import chat.server.algorithms.mutex.MutexStatus;
import chat.server.algorithms.mutex.MutexToken;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static chat.common.Log.*;

public class TestScenarioMutexSansConcurrence extends Scenario {
    @Test
    @Override
    public void constructAndRun() throws Exception {
        Scenario.setIsJUnitScenario(); // mandatory
        Log.configureALogger(LOGGER_NAME_CHAT, Level.WARN);
        Log.configureALogger(LOGGER_NAME_INTERCEPT, Level.WARN);
        Log.configureALogger(LOGGER_NAME_COMM, Level.WARN);
        Log.configureALogger(LOGGER_NAME_ELECTION, Level.WARN);
        Log.configureALogger(LOGGER_NAME_MUTEX, Level.WARN);
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
        Server s5 = instanciateAServer("5 localhost 4");
        sleep(500);

        /*
        Interceptors.setInterceptionEnabled(true);

        Predicate<MutexRequestTokenContent> conditionForIntercepting = msg -> true;

        Predicate<MutexRequestTokenContent> conditionForExecutingI1OnS1 = msg -> s1.getMutexStatus() == MutexStatus.DANS_SC && msg.getNs() > s1.getDem().getOrDefault(msg.getSender(), 0);
        Consumer<MutexRequestTokenContent> treatmentI1OnS1 = msg -> MutexAction.REQUEST_TOKEN_MESSAGE.execute(s1, msg);
        Interceptors.addAnInterceptor("i1", s1, conditionForIntercepting, conditionForExecutingI1OnS1, treatmentI1OnS1);

        Predicate<MutexRequestTokenContent> conditionForExecutingI2OnS2 = msg -> s2.getMutexStatus() == MutexStatus.DANS_SC && msg.getNs() > s2.getDem().getOrDefault(msg.getSender(), 0);
        Consumer<MutexRequestTokenContent> treatmentI2S2 = msg -> MutexAction.REQUEST_TOKEN_MESSAGE.execute(s2, msg);
        Interceptors.addAnInterceptor("i2", s2, conditionForIntercepting, conditionForExecutingI2OnS2, treatmentI2S2);

        Predicate<MutexRequestTokenContent> conditionForExecutingI3OnS3 = msg -> s3.getMutexStatus() == MutexStatus.DANS_SC && msg.getNs() > s3.getDem().getOrDefault(msg.getSender(), 0);
        Consumer<MutexRequestTokenContent> treatmentI3OnS3 = msg -> MutexAction.REQUEST_TOKEN_MESSAGE.execute(s3, msg);
        Interceptors.addAnInterceptor("i3", s3, conditionForIntercepting, conditionForExecutingI3OnS3, treatmentI3OnS3);

        Predicate<MutexRequestTokenContent> conditionForExecutingI4OnS4 = msg -> s4.getMutexStatus() == MutexStatus.DANS_SC && msg.getNs() > s4.getDem().getOrDefault(msg.getSender(), 0);
        Consumer<MutexRequestTokenContent> treatmentI4OnS4 = msg -> MutexAction.REQUEST_TOKEN_MESSAGE.execute(s4, msg);
        Interceptors.addAnInterceptor("i4", s4, conditionForIntercepting, conditionForExecutingI4OnS4, treatmentI4OnS4);

        Predicate<MutexRequestTokenContent> conditionForExecutingI5OnS5 = msg -> s5.getMutexStatus() == MutexStatus.DANS_SC && msg.getNs() > s5.getDem().getOrDefault(msg.getSender(), 0);
        Consumer<MutexRequestTokenContent> treatmentI5OnS5 = msg -> MutexAction.REQUEST_TOKEN_MESSAGE.execute(s5, msg);
        Interceptors.addAnInterceptor("i5", s5, conditionForIntercepting, conditionForExecutingI5OnS5, treatmentI5OnS5);
         */

        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("starting the test of the algorithms...");
        }
        emulateAnInputLineFromTheConsoleForAServer(s1, "election");
        sleep(500);
        emulateAnInputLineFromTheConsoleForAServer(s2, "critical section");
        sleep(500);
        emulateAnInputLineFromTheConsoleForAServer(s1, "critical section");
        sleep(500);
        emulateAnInputLineFromTheConsoleForAServer(s4, "critical section");
        sleep(500);
        emulateAnInputLineFromTheConsoleForAServer(s5, "critical section");
        sleep(500);
        emulateAnInputLineFromTheConsoleForAServer(s3, "critical section");
        sleep(500);
        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("end of the scenario.");
        }

        // Wait and then flush stdout (necessary for IDEs such as Eclipse).
        // Without sleep, not all outputs.
        // Without flush, no output in Eclipse for instance.
        sleep(3000);
        System.out.flush();

        // the scenario has ended

        // dem test
        Map<Integer, Integer> dem_s1 = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            if (i != 1) {
                dem_s1.put(i, 1);
            }
        }
        Map<Integer, Integer> dem_s2 = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            if (i != 2) {
                dem_s2.put(i, 1);
            }
        }
        Map<Integer, Integer> dem_s3 = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            if (i != 3) {
                dem_s3.put(i, 1);
            }
        }
        Map<Integer, Integer> dem_s4 = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            if (i != 4) {
                dem_s4.put(i, 1);
            }
        }
        Map<Integer, Integer> dem_s5 = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            if (i != 5) {
                dem_s5.put(i, 1);
            }
        }
        Assert.assertEquals(dem_s1, s1.getDem());
        Assert.assertEquals(dem_s2, s2.getDem());
        Assert.assertEquals(dem_s3, s3.getDem());
        Assert.assertEquals(dem_s4, s4.getDem());
        Assert.assertEquals(dem_s5, s5.getDem());

        // jet test
        Assert.assertNull(s1.getJet());
        Assert.assertNull(s2.getJet());
        Assert.assertNotNull(s3.getJet());
        Assert.assertNull(s4.getJet());
        Assert.assertNull(s5.getJet());

        MutexToken expectedToken = new MutexToken();
        expectedToken.setEntry(s1.identity(), 1);
        expectedToken.setEntry(s2.identity(), 1);
        expectedToken.setEntry(s3.identity(), 0);
        expectedToken.setEntry(s4.identity(), 1);
        expectedToken.setEntry(s5.identity(), 1);

        MutexToken actualToken = new MutexToken(s3.getJet());

        Assert.assertTrue(expectedToken.isEqualTo(actualToken));

        // ns test
        Assert.assertEquals(1, s1.getNs());
        Assert.assertEquals(1, s2.getNs());
        Assert.assertEquals(1, s3.getNs());
        Assert.assertEquals(1, s4.getNs());
        Assert.assertEquals(1, s5.getNs());

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
    }
}
