package chat.algorithms.mutex;

import chat.common.Interceptors;
import chat.common.Log;
import chat.common.Scenario;
import chat.server.Server;
import chat.server.algorithms.mutex.MutexAction;
import chat.server.algorithms.mutex.MutexRequestTokenContent;
import chat.server.algorithms.mutex.MutexToken;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static chat.common.Log.*;

public class TestScenarioMutexAvecConcurrence extends Scenario {
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

        Interceptors.setInterceptionEnabled(true);
        Predicate<MutexRequestTokenContent> conditionForInterceptingI1OnS5 = msg -> msg.getSender() == s2.identity();
        Predicate<MutexRequestTokenContent> conditionForExecutingI1OnS5 = msg -> s5.getNs() == 1;
        Consumer<MutexRequestTokenContent> treatmentI1OnS5 = msg -> MutexAction.REQUEST_TOKEN_MESSAGE.execute(s5, msg);
        Interceptors.addAnInterceptor("i1", s5, conditionForInterceptingI1OnS5, conditionForExecutingI1OnS5, treatmentI1OnS5);

        if (LOG_ON && TEST.isInfoEnabled()) {
            TEST.info("starting the test of the algorithms...");
        }
        emulateAnInputLineFromTheConsoleForAServer(s1, "election");
        sleep(500);
        emulateAnInputLineFromTheConsoleForAServer(s1, "critical section");
        sleep(500);
        emulateAnInputLineFromTheConsoleForAServer(s2, "critical section");
        sleep(500);
        emulateAnInputLineFromTheConsoleForAServer(s5, "critical section");
        sleep(500);
        emulateAnInputLineFromTheConsoleForAServer(s1, "critical section");
        sleep(500);
        emulateAnInputLineFromTheConsoleForAServer(s2, "critical section");
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
        dem_s1.put(2, 2);
        dem_s1.put(5, 1);
        Map<Integer, Integer> dem_s2 = new HashMap<>();
        dem_s2.put(1, 1);
        dem_s2.put(5, 1);
        Map<Integer, Integer> dem_s3 = new HashMap<>();
        dem_s3.put(1, 1);
        dem_s3.put(2, 2);
        dem_s3.put(5, 1);
        Map<Integer, Integer> dem_s4 = new HashMap<>();
        dem_s4.put(1, 1);
        dem_s4.put(2, 2);
        dem_s4.put(5, 1);
        Map<Integer, Integer> dem_s5 = new HashMap<>();
        dem_s5.put(1, 1);
        dem_s5.put(2, 2);
        if (LOG_ON && TEST.isDebugEnabled()) {
            TEST.debug("dem_s1=" + s1.getDem());
            TEST.debug("dem_s2=" + s2.getDem());
            TEST.debug("dem_s3=" + s3.getDem());
            TEST.debug("dem_s4=" + s4.getDem());
            TEST.debug("dem_s5=" + s5.getDem());
        }
        Assert.assertEquals(dem_s1, s1.getDem());
        Assert.assertEquals(dem_s2, s2.getDem());
        Assert.assertEquals(dem_s3, s3.getDem());
        Assert.assertEquals(dem_s4, s4.getDem());
        Assert.assertEquals(dem_s5, s5.getDem());

        // jet test
        Assert.assertNull(s1.getJet());
        Assert.assertNotNull(s2.getJet());
        Assert.assertNull(s3.getJet());
        Assert.assertNull(s4.getJet());
        Assert.assertNull(s5.getJet());

        MutexToken actualToken = new MutexToken(s2.getJet());
        MutexToken expectedToken = new MutexToken();
        expectedToken.setEntry(s1.identity(), 1);
        expectedToken.setEntry(s2.identity(), 1);
        expectedToken.setEntry(s3.identity(), 0);
        expectedToken.setEntry(s4.identity(), 0);
        expectedToken.setEntry(s5.identity(), 1);
        if (LOG_ON && TEST.isDebugEnabled()) {
            TEST.debug("expectedToken= " + expectedToken);
            TEST.debug("actualToken= " + actualToken);
        }
        Assert.assertTrue(expectedToken.isEqualTo(actualToken));

        // ns test
        if (LOG_ON && TEST.isDebugEnabled()) {
            TEST.debug("ns_s1= " + s1.getNs());
            TEST.debug("ns_s2= " + s1.getNs());
            TEST.debug("ns_s3= " + s1.getNs());
            TEST.debug("ns_s4= " + s1.getNs());
            TEST.debug("ns_s5= " + s1.getNs());
        }
        Assert.assertEquals(1, s1.getNs());
        Assert.assertEquals(2, s2.getNs());
        Assert.assertEquals(0, s3.getNs());
        Assert.assertEquals(0, s4.getNs());
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
