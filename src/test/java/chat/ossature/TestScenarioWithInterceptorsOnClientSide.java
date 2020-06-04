// CHECKSTYLE:OFF
/**
This file is part of the teaching unit TSP/CSC4509.

Copyright (C) 2012-2020 Télécom SudParis

This is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This software platform is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with the muDEBS platform. If not, see <http://www.gnu.org/licenses/>.

Initial developer(s): Denis Conan
Contributor(s):
 */
package chat.ossature;

import static chat.common.Log.LOGGER_NAME_CHAT;
import static chat.common.Log.LOGGER_NAME_COMM;
import static chat.common.Log.LOGGER_NAME_ELECTION;
import static chat.common.Log.LOGGER_NAME_GEN;
import static chat.common.Log.LOGGER_NAME_INTERCEPT;
import static chat.common.Log.LOGGER_NAME_TEST;
import static chat.common.Log.LOG_ON;
import static chat.common.Log.TEST;

import java.util.function.Consumer;
import java.util.function.Predicate;

import chat.common.VectorClock;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import chat.client.Client;
import chat.client.algorithms.chat.ChatMsgContent;
import chat.common.Interceptors;
import chat.common.Log;
import chat.common.Scenario;
import chat.server.Server;

public class TestScenarioWithInterceptorsOnClientSide extends Scenario {

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
		Server s3 = instanciateAServer("3 localhost 1 localhost 2");
		sleep(500);
		Server s4 = instanciateAServer("4 localhost 3");
		sleep(500);
		if (LOG_ON && TEST.isInfoEnabled()) {
			TEST.info("starting the clients...");
		}
		// start the clients
		Client c0 = instanciateAClient(s1.identity());
		sleep(500);
		Client c1 = instanciateAClient(s1.identity());
		sleep(500);
		Client c2 = instanciateAClient(s3.identity());
		sleep(500);
		Client c3 = instanciateAClient(s4.identity());
		sleep(500);
		Client c4 = instanciateAClient(s4.identity());
		sleep(1000);
		Interceptors.setInterceptionEnabled(true);
		Predicate<ChatMsgContent> conditionForInterceptingI1OnC1 = msg -> msg.getSender() == c0.identity();
		Predicate<ChatMsgContent> conditionForExecutingI1OnC1 = msg -> true;
		Consumer<ChatMsgContent> treatmentI1OnC1 = msg -> chat.client.algorithms.chat.ChatAction.CHAT_MESSAGE
				.execute(c1, new ChatMsgContent(msg.getSender(), msg.getSequenceNumber(),
						msg.getContent() + ", intercepted at client c1 by i1", new VectorClock(msg.getVectorClock())));
		Interceptors.addAnInterceptor("i1", c1, conditionForInterceptingI1OnC1, conditionForExecutingI1OnC1,
				treatmentI1OnC1);
		Predicate<ChatMsgContent> conditionForInterceptingI2OnC2 = msg -> msg.getSender() == c0.identity();
		Predicate<ChatMsgContent> conditionForExecutingI2OnC2 = msg -> true;
		Consumer<ChatMsgContent> treatmentI2OnC2 = msg -> chat.client.algorithms.chat.ChatAction.CHAT_MESSAGE
				.execute(c2, new ChatMsgContent(msg.getSender(), msg.getSequenceNumber(),
						msg.getContent() + ", intercepted at client c2 by i2", new VectorClock(msg.getVectorClock())));
		Interceptors.addAnInterceptor("i2", c2, conditionForInterceptingI2OnC2, conditionForExecutingI2OnC2,
				treatmentI2OnC2);
		Predicate<ChatMsgContent> conditionForInterceptingI3OnC1 = msg -> msg.getSender() == c2.identity();
		// next lambda for emulating situation in which message from client c2 to
		// client c1 never received
		Predicate<ChatMsgContent> conditionForExecutingI3OnC1 = msg -> false;
		Consumer<ChatMsgContent> treatmentI3OnC1 = msg -> chat.client.algorithms.chat.ChatAction.CHAT_MESSAGE
				.execute(c1, new ChatMsgContent(msg.getSender(), msg.getSequenceNumber(),
						msg.getContent() + ", intercepted at client c1 by i3", new VectorClock(msg.getVectorClock())));
		Interceptors.addAnInterceptor("i3", c1, conditionForInterceptingI3OnC1, conditionForExecutingI3OnC1,
				treatmentI3OnC1);
		Consumer<ChatMsgContent> treatmentI4OnC1 = msg -> chat.client.algorithms.chat.ChatAction.CHAT_MESSAGE
				.execute(c1, new ChatMsgContent(msg.getSender(), msg.getSequenceNumber(),
						msg.getContent() + ", intercepted at client c1 by i4", new VectorClock(msg.getVectorClock())));
		// useless interceptor since the condition of the interception is the same as
		// the one of i1
		// ---i.e. i4 is stored after i1 in the list of interceptors, and messages that
		// are going to be intercepted by i1 are removed before being intercepted by i4
		Interceptors.addAnInterceptor("i4", c1, conditionForInterceptingI1OnC1, conditionForExecutingI1OnC1,
				treatmentI4OnC1);
		if (LOG_ON && TEST.isInfoEnabled()) {
			TEST.info("starting the test of the algorithms...");
		}
		emulateAnInputLineFromTheConsoleForAClient(c0, "message 0 from c0 (" + c0 + ")");
		emulateAnInputLineFromTheConsoleForAClient(c1, "message 1 from c1 (" + c1 + ")");
		emulateAnInputLineFromTheConsoleForAClient(c2, "message 2 from c2 (" + c2 + ")");
		emulateAnInputLineFromTheConsoleForAClient(c3, "message 3 from c3 (" + c3 + ")");
		emulateAnInputLineFromTheConsoleForAClient(c4, "message 4 from c4 (" + c4 + ")");
		if (LOG_ON && TEST.isInfoEnabled()) {
			TEST.info("end of the scenario.");
		}
		// Wait and then flush stdout (necessary for IDEs such as Eclipse).
		// Without sleep, not all outputs.
		// Without flush, no output in Eclipse for instance.
		sleep(3000);
		System.out.flush();
		if (LOG_ON && TEST.isInfoEnabled()) {
			TEST.info("the message intercepted by i3 should never be delivered to c1");
			TEST.info("no message should be intercepted by i4");
		}
		// the scenario has ended, then access the state of clients without synchronised
		// blocks.
		Assert.assertEquals(1, c0.getNbChatMsgContentSent());
		Assert.assertEquals(1, c1.getNbChatMsgContentSent());
		Assert.assertEquals(1, c2.getNbChatMsgContentSent());
		Assert.assertEquals(1, c3.getNbChatMsgContentSent());
		Assert.assertEquals(1, c4.getNbChatMsgContentSent());
		Assert.assertEquals(4, c0.getNbChatMsgContentReceived());
		Assert.assertEquals(3, c1.getNbChatMsgContentReceived()); // due to interceptor i3
		Assert.assertEquals(4, c2.getNbChatMsgContentReceived());
		Assert.assertEquals(4, c3.getNbChatMsgContentReceived());
		Assert.assertEquals(4, c4.getNbChatMsgContentReceived());
		// finish properly
		emulateAnInputLineFromTheConsoleForAClient(c0, "quit");
		sleep(100);
		emulateAnInputLineFromTheConsoleForAClient(c1, "quit");
		sleep(100);
		emulateAnInputLineFromTheConsoleForAClient(c2, "quit");
		sleep(100);
		emulateAnInputLineFromTheConsoleForAClient(c3, "quit");
		sleep(100);
		emulateAnInputLineFromTheConsoleForAClient(c4, "quit");
		sleep(100);
		emulateAnInputLineFromTheConsoleForAServer(s1, "quit");
		sleep(100);
		emulateAnInputLineFromTheConsoleForAServer(s2, "quit");
		sleep(100);
		emulateAnInputLineFromTheConsoleForAServer(s3, "quit");
		sleep(100);
		emulateAnInputLineFromTheConsoleForAServer(s4, "quit");
		sleep(100);
	}
}
