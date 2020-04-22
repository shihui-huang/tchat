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
package chat.client;

import static chat.common.Log.CHAT;
import static chat.common.Log.COMM;
import static chat.common.Log.GEN;
import static chat.common.Log.LOG_ON;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Objects;

import chat.client.algorithms.ClientAlgorithm;
import chat.client.algorithms.chat.ChatAction;
import chat.client.algorithms.chat.ChatMsgContent;
import chat.common.Entity;
import chat.common.Log;
import chat.common.Scenario;
import chat.server.Server;

/**
 * This class contains the logic of a client of the chat application. It
 * configures the client, connects to a chat server, launches a thread for
 * reading chat messages from the chat server.
 * 
 * This class is provided as the starting point to implement distributed
 * algorithms and must be extended when new algorithms are implemented
 * 
 * @author Denis Conan
 * 
 */
public class Client implements Entity {
	/**
	 * the runnable object of the client that receives the messages from the chat
	 * server.
	 */
	private final ReadMessagesFromNetwork runnableToRcvMsgs;
	/**
	 * the thread of the client that receives the messages from the chat server.
	 */
	private final Thread threadToRcvMsgs;
	/**
	 * identity of this client. The identity is computed by the server as follows:
	 * {@code identity * OFFSET_ID_CLIENT + clientNumber}.
	 */
	private int identity;
	/**
	 * number of chat messages received.
	 */
	private int nbChatMsgContentReceived;
	/**
	 * number of chat messages sent.
	 */
	private int nbChatMsgContentSent;

	/**
	 * constructs a client with a connection to the chat server. The connection to
	 * the server is managed in a thread that is also a full message worker. Before
	 * creating the threaded full duplex message worker, the constructor check for
	 * the server host name and open a connection with the chat server.
	 * 
	 * NB: after the construction of a client object, the thread for reading
	 * messages must be started using the method
	 * {@link #startThreadReadMessagesFromNetwork}.
	 * 
	 * @param serverHostName the name of the host of the server.
	 * @param serverId       the identity of the server.
	 */
	public Client(final String serverHostName, final int serverId) {
		SocketChannel rwChan;
		InetAddress destAddr;
		try {
			destAddr = InetAddress.getByName(serverHostName);
		} catch (UnknownHostException e) {
			throw new IllegalStateException("unknown host name provided");
		}
		try {
			rwChan = SocketChannel.open();
		} catch (IOException e) {
			throw new IllegalStateException("cannot open a connection to the server");
		}
		try {
			int port = (Server.BASE_PORTNB_LISTEN_CLIENT + serverId);
			if (LOG_ON && GEN.isDebugEnabled()) {
				GEN.debug(Log.computeClientLogMessage(this,
						", connecting to server " + serverId + ", port number " + port));
			}
			rwChan.connect(new InetSocketAddress(destAddr, port));
		} catch (IOException e) {
			throw new IllegalStateException("cannot open a connection to the server");
		}
		runnableToRcvMsgs = new ReadMessagesFromNetwork(rwChan, this);
		threadToRcvMsgs = new Thread(runnableToRcvMsgs);
		assert invariant();
	}

	/**
	 * checks the invariant of the class.
	 * 
	 * NB: the method is final so that the method is not overridden in potential
	 * subclasses because it is called in the constructor.
	 * 
	 * @return a boolean stating whether the invariant is maintained.
	 */
	public final synchronized boolean invariant() {
		return runnableToRcvMsgs != null && threadToRcvMsgs != null && nbChatMsgContentReceived >= 0
				&& nbChatMsgContentSent >= 0;
	}

	@Override
	public synchronized int identity() {
		return identity;
	}

	/**
	 * sets the identity of the client. The invariant is not asserted at the end of
	 * the method since the method is called in the constructor of the class
	 * {@link ReadMessagesFromNetwork} in the assignment of the attribute
	 * {@link #runnableToRcvMsgs}.
	 * 
	 * @param identity the new identity.
	 */
	synchronized void setIdentity(final int identity) {
		this.identity = identity;
	}

	/**
	 * gets the number of chat messages received.
	 * 
	 * @return the number.
	 */
	public synchronized int getNbChatMsgContentReceived() {
		return nbChatMsgContentReceived;
	}

	/**
	 * gets the number of chat messages sent.
	 * 
	 * @return the number.
	 */
	public synchronized int getNbChatMsgContentSent() {
		return nbChatMsgContentSent;
	}

	/**
	 * starts the thread that is responsible for reading messages from the server.
	 */
	public synchronized void startThreadReadMessagesFromNetwork() {
		threadToRcvMsgs.start();
		assert invariant();
	}

	/**
	 * treats an input line from the console. For now, it sends the input line as a
	 * chat message to the server.
	 * 
	 * @param line the content of the message
	 */
	public synchronized void treatConsoleInput(final String line) {
		Objects.requireNonNull(line, "argument line cannot be null");
		if (LOG_ON && GEN.isDebugEnabled()) {
			GEN.debug(Log.computeClientLogMessage(this, ", new command line on console"));
		}
		if (line.equals("quit")) {
			threadToRcvMsgs.interrupt();
			// do not interrupt the main thread during the execution of a Scenario because
			// all the clients and all the servers are controlled by the same "main" thread
			if (!Scenario.isJUnitScenario()) {
				Thread.currentThread().interrupt();
			}
		} else {
			synchronized (this) {
				ChatMsgContent msg = new ChatMsgContent(identity(), getNbChatMsgContentSent(), line);
				if (LOG_ON && CHAT.isInfoEnabled()) {
					CHAT.info(Log.computeClientLogMessage(this, " sending chat message: " + msg));
				}
				try {
					long sent = runnableToRcvMsgs.sendMsg(ClientAlgorithm.getActionNumber(ChatAction.CHAT_MESSAGE),
							identity(), msg);
					nbChatMsgContentSent++;
					if (LOG_ON && COMM.isDebugEnabled()) {
						COMM.debug(Log.computeClientLogMessage(this, ", " + sent + " bytes sent."));
					}
				} catch (IOException e) {
					COMM.error(
							Log.computeClientLogMessage(this, ", pb with channel (" + e.getLocalizedMessage() + ")."));
					return;
				}
			}
			assert invariant();
		}
	}

	/**
	 * treats the reception of a chat message: the message is displayed in the
	 * console.
	 * 
	 * @param content the content of the message.
	 */
	public synchronized void receiveChatMsgContent(final ChatMsgContent content) {
		Objects.requireNonNull(content, "argument content cannot be null");
		synchronized (this) {
			// due to the forwarding of messages through multiple paths, a client may
			// receive its own message => exclude it
			if (content.getSender() != identity()) {
				if (LOG_ON && CHAT.isInfoEnabled()) {
					CHAT.info(Log.computeClientLogMessage(this, " receives " + content));
				}
				nbChatMsgContentReceived++;
			}
			assert invariant();
		}
	}

	@Override
	public synchronized String toString() {
		return "Client " + identity;
	}
}
