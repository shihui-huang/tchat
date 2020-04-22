// CHECKSTYLE:OFF
/**
This file is part of the CSC4509 teaching unit.

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
along with the CSC4509 teaching unit. If not, see <http://www.gnu.org/licenses/>.

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
import static chat.common.Log.LOGGER_NAME_TOPO;
import static chat.common.Log.LOG_ON;
import static chat.common.Log.TEST;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import chat.client.Client;
import chat.common.Log;
import chat.common.Scenario;
import chat.server.Server;
import util.TestPointToPointMessage;

public class TestScenarioOssatureDeDepart extends Scenario {

	@Test
	@Override
	public void constructAndRun() throws Exception {
		Scenario.setIsJUnitScenario(); // mandatory
		Log.configureALogger(LOGGER_NAME_CHAT, Level.WARN);
		Log.configureALogger(LOGGER_NAME_INTERCEPT, Level.WARN);
		Log.configureALogger(LOGGER_NAME_COMM, Level.WARN);
		Log.configureALogger(LOGGER_NAME_TOPO, Level.WARN);
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
		Server s5 = instanciateAServer("5 localhost 2 localhost 4");
		sleep(1500);
		Server s6 = instanciateAServer("6 localhost 5");
		sleep(2000);
		if (LOG_ON && TEST.isInfoEnabled()) {
			TEST.info("all the servers are started...");
		}
		Assert.assertTrue(s1.getFirstHopToRemoteServer(s1.identity()) == -1);
		Assert.assertTrue(s1.getFirstHopToRemoteServer(s2.identity()) == s2.identity());
		Assert.assertTrue(s1.getFirstHopToRemoteServer(s3.identity()) == s3.identity());
		Assert.assertTrue(s1.getFirstHopToRemoteServer(s4.identity()) == s3.identity());
		Assert.assertTrue(s1.getFirstHopToRemoteServer(s5.identity()) == s2.identity());
		Assert.assertTrue(s1.getFirstHopToRemoteServer(s6.identity()) == s2.identity());
		Assert.assertTrue(s2.getFirstHopToRemoteServer(s1.identity()) == s1.identity());
		Assert.assertTrue(s2.getFirstHopToRemoteServer(s2.identity()) == -1);
		Assert.assertTrue(s2.getFirstHopToRemoteServer(s3.identity()) == s3.identity());
		Assert.assertTrue(s2.getFirstHopToRemoteServer(s4.identity()) == s3.identity()
				|| s2.getFirstHopToRemoteServer(s4.identity()) == s5.identity());
		Assert.assertTrue(s2.getFirstHopToRemoteServer(s5.identity()) == s5.identity());
		Assert.assertTrue(s2.getFirstHopToRemoteServer(s6.identity()) == s5.identity());
		Assert.assertTrue(s3.getFirstHopToRemoteServer(s1.identity()) == s1.identity());
		Assert.assertTrue(s3.getFirstHopToRemoteServer(s2.identity()) == s2.identity());
		Assert.assertTrue(s3.getFirstHopToRemoteServer(s3.identity()) == -1);
		Assert.assertTrue(s3.getFirstHopToRemoteServer(s4.identity()) == s4.identity());
		Assert.assertTrue(s3.getFirstHopToRemoteServer(s5.identity()) == s2.identity()
				|| s3.getFirstHopToRemoteServer(s5.identity()) == s4.identity());
		Assert.assertTrue(s3.getFirstHopToRemoteServer(s6.identity()) == s2.identity()
				|| s3.getFirstHopToRemoteServer(s6.identity()) == s4.identity());
		Assert.assertTrue(s4.getFirstHopToRemoteServer(s1.identity()) == s3.identity());
		Assert.assertTrue(s4.getFirstHopToRemoteServer(s2.identity()) == s3.identity()
				|| s4.getFirstHopToRemoteServer(s2.identity()) == s5.identity());
		Assert.assertTrue(s4.getFirstHopToRemoteServer(s3.identity()) == s3.identity());
		Assert.assertTrue(s4.getFirstHopToRemoteServer(s4.identity()) == -1);
		Assert.assertTrue(s4.getFirstHopToRemoteServer(s5.identity()) == s5.identity());
		Assert.assertTrue(s4.getFirstHopToRemoteServer(s6.identity()) == s5.identity());
		Assert.assertTrue(s5.getFirstHopToRemoteServer(s1.identity()) == s2.identity());
		Assert.assertTrue(s5.getFirstHopToRemoteServer(s2.identity()) == s2.identity());
		Assert.assertTrue(s5.getFirstHopToRemoteServer(s3.identity()) == s2.identity()
				|| s5.getFirstHopToRemoteServer(s3.identity()) == s4.identity());
		Assert.assertTrue(s5.getFirstHopToRemoteServer(s4.identity()) == s4.identity());
		Assert.assertTrue(s5.getFirstHopToRemoteServer(s5.identity()) == -1);
		Assert.assertTrue(s5.getFirstHopToRemoteServer(s6.identity()) == s6.identity());
		Assert.assertTrue(s6.getFirstHopToRemoteServer(s1.identity()) == s5.identity());
		Assert.assertTrue(s6.getFirstHopToRemoteServer(s2.identity()) == s5.identity());
		Assert.assertTrue(s6.getFirstHopToRemoteServer(s3.identity()) == s5.identity());
		Assert.assertTrue(s6.getFirstHopToRemoteServer(s4.identity()) == s5.identity());
		Assert.assertTrue(s6.getFirstHopToRemoteServer(s5.identity()) == s5.identity());
		Assert.assertTrue(s6.getFirstHopToRemoteServer(s6.identity()) == -1);
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
		sleep(500);
		Client c5 = instanciateAClient(s5.identity());
		sleep(500);
		Client c6 = instanciateAClient(s6.identity());
		sleep(1000);
		if (LOG_ON && TEST.isInfoEnabled()) {
			TEST.info("starting the test of the algorithms...");
		}
		// the first part of the algorithm is the sending of client chat messages
		emulateAnInputLineFromTheConsoleForAClient(c0, "message 0 from c0");
		emulateAnInputLineFromTheConsoleForAClient(c1, "message 1 from c1");
		emulateAnInputLineFromTheConsoleForAClient(c2, "message 2 from c2");
		emulateAnInputLineFromTheConsoleForAClient(c3, "message 3 from c3");
		emulateAnInputLineFromTheConsoleForAClient(c4, "message 4 from c4");
		emulateAnInputLineFromTheConsoleForAClient(c5, "message 5 from c5");
		emulateAnInputLineFromTheConsoleForAClient(c6, "message 6 from c6");
		sleep(2000);
		System.out.flush();
		if (LOG_ON && TEST.isInfoEnabled()) {
			TEST.info("end of the scenario.");
		}
		// wait and then flush stdout (necessary for IDEs such as Eclipse)
		// without flush, no output in Eclipse for instance
		// without sleep, not all outputs
		sleep(3000);
		System.out.flush();
		// the first part of the scenario has ended, check the execution
		Assert.assertEquals(1, c0.getNbChatMsgContentSent());
		Assert.assertEquals(1, c1.getNbChatMsgContentSent());
		Assert.assertEquals(1, c2.getNbChatMsgContentSent());
		Assert.assertEquals(1, c3.getNbChatMsgContentSent());
		Assert.assertEquals(1, c4.getNbChatMsgContentSent());
		Assert.assertEquals(1, c5.getNbChatMsgContentSent());
		Assert.assertEquals(1, c6.getNbChatMsgContentSent());
		Assert.assertEquals(6, c0.getNbChatMsgContentReceived());
		Assert.assertEquals(6, c1.getNbChatMsgContentReceived());
		Assert.assertEquals(6, c2.getNbChatMsgContentReceived());
		Assert.assertEquals(6, c3.getNbChatMsgContentReceived());
		Assert.assertEquals(6, c4.getNbChatMsgContentReceived());
		Assert.assertEquals(6, c5.getNbChatMsgContentReceived());
		Assert.assertEquals(6, c6.getNbChatMsgContentReceived());
		sleep(500);
		// the second part of the scenario, point-to-point message communication between
		// servers ; the message type is voluntarily set to -1 in order to be ignored by
		// the intended recipient ; there is no algorithm for the following messages,
		// then use logging for visually checking the execution ; the usage of the
		// method Server::sendToAServer is not recommended in other test scenarios than
		// this one!
		// Log.configureALogger(LOGGER_NAME_COMM, Level.INFO);
		// the following point-to-point message should go through servers 1 and 4
		s1.sendToAServer(s6.identity(), -1, new TestPointToPointMessage(s1.identity(), s6.identity()));
		sleep(100);
		System.out.flush();
		// the following point-to-point message should go through server 2
		s1.sendToAServer(s4.identity(), -1, new TestPointToPointMessage(s1.identity(), s4.identity()));
		sleep(100);
		System.out.flush();
		// the following point-to-point message is sent to a neighbouring server
		s1.sendToAServer(s3.identity(), -1, new TestPointToPointMessage(s1.identity(), s3.identity()));
		sleep(100);
		System.out.flush();
		// the following point-to-point message should go through servers 4 and 1
		s6.sendToAServer(s1.identity(), -1, new TestPointToPointMessage(s6.identity(), s1.identity()));
		sleep(100);
		System.out.flush();
		// end of the scenario, finish properly
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
		emulateAnInputLineFromTheConsoleForAClient(c5, "quit");
		sleep(100);
		emulateAnInputLineFromTheConsoleForAClient(c6, "quit");
		sleep(100);
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
