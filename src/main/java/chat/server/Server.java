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
package chat.server;

import static chat.common.Log.COMM;
import static chat.common.Log.GEN;
import static chat.common.Log.LOG_ON;
import static chat.common.Log.TOPO;
import static chat.server.algorithms.election.ElectionAction.LEADER_MESSAGE;
import static chat.server.algorithms.election.ElectionAction.TOKEN_MESSAGE;
import static chat.server.algorithms.topology.TopologyAction.IDENTITY_MESSAGE;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import chat.client.algorithms.chat.ChatMsgContent;
import chat.common.Entity;
import chat.common.FullDuplexMsgWorker;
import chat.common.Log;
import chat.common.MsgContent;
import chat.common.ReadMessageStatus;
import chat.common.Scenario;
import chat.server.algorithms.ServerAlgorithm;
import chat.server.algorithms.election.ElectionLeaderContent;
import chat.server.algorithms.election.ElectionTokenContent;
import chat.server.algorithms.topology.IdentityContent;

/**
 * This class defines server object. The server object connects to existing chat
 * servers, waits for connections from other chat servers and from chat clients,
 * and forwards chat messages received from chat clients to other 'local' chat
 * clients and to the other chat servers.
 * 
 * The chat servers can be organised into a network topology forming cycles
 * since the method
 * {@link #sendToAllNeighbouringServersExceptOne(int, int, MsgContent)} is only
 * called when the message to forward has not already been received and
 * forwarded.
 * 
 * This class is provided as the starting point to implement distributed
 * algorithms. This explains why it is abstract.
 * 
 * @author Denis Conan
 * 
 */
public class Server implements Entity {
	/**
	 * the base of the port number for connecting to clients.
	 */
	public static final int BASE_PORTNB_LISTEN_CLIENT = 2050;
	/**
	 * the offset of the port number for connecting to servers.
	 */
	public static final int OFFSET_PORTNB_LISTEN_SERVER = 100;
	/**
	 * the number of clients that have opened a connection to this server till the
	 * beginning of its execution. Each client is assigned an identity in the form
	 * of an integer and this identity is provided by the server it is connected to:
	 * it is the current value of this integer.
	 */
	private int numberOfClientsSinceBeginning = 0;
	/**
	 * the selector.
	 */
	private final Selector selector;
	/**
	 * the runnable object of the server that receives the messages from the chat
	 * clients and the other chat servers.
	 */
	private final ReadMessagesFromNetwork runnableToRcvMsgs;
	/**
	 * the thread of the server that receives the messages from the chat clients and
	 * the other chat servers.
	 */
	private final Thread threadToRcvMsgs;
	/**
	 * identity of this server.
	 */
	private final int identity;
	/**
	 * collection of reachable entities, that is the already collected routing
	 * information data structures. The key of the identity is the remote entity
	 * (local client or remote server). <br>
	 * ASSUMPTION: no topology changes, except in the initialisation phase or in the
	 * termination phase. This is so because the method
	 * {@link #removeRoutingInformation} is not complete, i.e. the distributed
	 * algorithm for removing an entity from the routing information has not been
	 * written.
	 */
	private final Map<Integer, RoutingInformation> reachableEntities;
	/**
	 * the offset to compute the identity of the new client has a function of the
	 * identity of the server and the number of connected clients.
	 */
	public static final int OFFSET_ID_CLIENT = 100;
	/**
	 * since there may exist cycles in the topology of servers, chat messages can go
	 * through several path from the sender to this server, and hence a client
	 * message may receive the same chat message several times. This collection
	 * stores the last chat message received from each client in order to forward a
	 * chat message once to local clients. The key of the map is the client
	 * identifier and the value is the greatest sequence number.<br>
	 * It works till there is no loss (an assumption in our study) and messages are
	 * not reordered (an assumption of TCP network links).
	 */
	private final Map<Integer, Integer> sequenceNumberOfLocalClients;
	/**
	 * variables de l'algorithme
	 */
	private int caw;
	private int parent;
	private int win;
	private int rec;
	private int lrec;
	private String status;

	/**
	 * initialises the collection attributes and the state of the server, and
	 * creates the channels that are accepting connections from clients and servers.
	 * At the end of the constructor, the server opens connections to the other
	 * servers (hostname, identifier) that are provided in the command line
	 * arguments.
	 * 
	 * NB: after the construction of a client object, the thread for reading
	 * messages must be started using the method
	 * {@link #startThreadReadMessagesFromNetwork}.
	 * 
	 * @param args java command arguments.
	 */
	public Server(final String[] args) {
		Objects.requireNonNull(args, "args cannot be null");
		identity = Integer.parseInt(args[0]);
		if (identity <= 0) {
			throw new IllegalArgumentException("the identity of a server must be > 0");
		}
		int portnum = BASE_PORTNB_LISTEN_CLIENT + Integer.parseInt(args[0]);
		reachableEntities = new HashMap<>();
		sequenceNumberOfLocalClients = new HashMap<>();
		InetSocketAddress rcvAddressClient;
		InetSocketAddress rcvAddressServer;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new IllegalStateException("cannot create the selector");
		}
		ServerSocketChannel listenChanClient = null;
		ServerSocketChannel listenChanServer = null;
		try {
			listenChanClient = ServerSocketChannel.open();
			listenChanClient.configureBlocking(false);
		} catch (IOException e) {
			throw new IllegalStateException("cannot set the blocking option to a server socket");
		}
		try {
			listenChanServer = ServerSocketChannel.open();
		} catch (IOException e) {
			throw new IllegalStateException("cannot open the server socket" + " for accepting server connections");
		}
		try {
			rcvAddressClient = new InetSocketAddress(portnum);
			listenChanClient.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			rcvAddressServer = new InetSocketAddress(portnum + OFFSET_PORTNB_LISTEN_SERVER);
			listenChanServer.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		} catch (IOException e) {
			throw new IllegalStateException("cannot set the SO_REUSEADDR option");
		}
		try {
			listenChanClient.bind(rcvAddressClient);
			listenChanServer.bind(rcvAddressServer);
		} catch (IOException e) {
			throw new IllegalStateException("cannot bind to a server socket");
		}
		try {
			listenChanClient.configureBlocking(false);
			listenChanServer.configureBlocking(false);
		} catch (IOException e) {
			throw new IllegalStateException("cannot set the blocking option");
		}
		SelectionKey acceptClientKey = null;
		SelectionKey acceptServerKey = null;
		try {
			acceptClientKey = listenChanClient.register(selector, SelectionKey.OP_ACCEPT);
			acceptServerKey = listenChanServer.register(selector, SelectionKey.OP_ACCEPT);
		} catch (ClosedChannelException e) {
			throw new IllegalStateException("cannot register a server socket");
		}
		if (LOG_ON && COMM.isInfoEnabled()) {
			COMM.info(Log.computeServerLogMessage(this,
					", listenChanClient ok on port " + listenChanClient.socket().getLocalPort()));
			COMM.info(Log.computeServerLogMessage(this,
					", listenChanServer ok on port " + listenChanServer.socket().getLocalPort()));
		}
		runnableToRcvMsgs = new ReadMessagesFromNetwork(this, selector, acceptClientKey, listenChanClient,
				acceptServerKey, listenChanServer);
		threadToRcvMsgs = new Thread(runnableToRcvMsgs);
		for (int i = 1; i < args.length; i = i + 2) {
			try {
				connectToAServer(args[i], Integer.parseInt(args[i + 1]));
			} catch (IOException e) {
				COMM.error(e.getLocalizedMessage());
				return;
			}
		}
		// -1 since there is no neighbouring server to exclude
		sendToAllNeighbouringServersExceptOne(-1, ServerAlgorithm.getActionNumber(IDENTITY_MESSAGE),
				new IdentityContent(identity(), new ArrayList<>()));

		// initialisation des variables de l'algorithme
		this.caw = -1;
		this.parent = -1;
		this.win = -1;
		this.rec = 0;
		this.lrec = 0;
		this.status = "dormant";

		assert invariant();
	}

	/**
	 * connects socket, creates MsgWorker, and registers selection key of the remote
	 * server. This method is called when connecting to a remote server. Connection
	 * data are provided as arguments to the main.
	 * 
	 * @param host remote host's name.
	 * @param id   identity of the neighbouring server.
	 * @throws IOException the exception thrown in case of communication problem.
	 */
	private void connectToAServer(final String host, final int id) throws IOException {
		Objects.requireNonNull(host, "argument host cannot be null");
		Socket rwSock;
		SocketChannel rwChan;
		InetSocketAddress rcvAddress;
		int port = (BASE_PORTNB_LISTEN_CLIENT + id + OFFSET_PORTNB_LISTEN_SERVER);
		if (LOG_ON && COMM.isInfoEnabled()) {
			COMM.info(Log.computeServerLogMessage(this,
					"opening connection with server on host " + host + " on port " + port));
		}
		InetAddress destAddr = InetAddress.getByName(host);
		rwChan = SocketChannel.open();
		rwSock = rwChan.socket();
		// obtain the IP address of the target host
		rcvAddress = new InetSocketAddress(destAddr, port);
		// connect sending socket to remote port
		rwSock.connect(rcvAddress);
		FullDuplexMsgWorker worker = new FullDuplexMsgWorker(rwChan);
		worker.configureNonBlocking();
		SelectionKey serverKey = rwChan.register(selector, SelectionKey.OP_READ);
		synchronized (this) {
			reachableEntities.put(id, new RoutingInformation(id, 1, id, serverKey, worker));
			if (LOG_ON && COMM.isInfoEnabled()) {
				COMM.info(Log.computeServerLogMessage(this,
						", number of neighbouring servers = " + getNumberOfNeighbouringServers()));
			}
			if (LOG_ON && TOPO.isDebugEnabled()) {
				TOPO.debug(Log.computeServerLogMessage(this, ", " + toStringReachableEntities()));
			}
			worker.sendMsg(0, identity, identity);
		}
		assert invariant();
	}

	/**
	 * parses the path of the message content to update routing information. The
	 * path of the message is reversed in order to find at the beginning of the path
	 * the identity of the neighbour. Then, the path is parsed and for each remote
	 * server that is found in the path, the routing information is updated.
	 * Updating the path is as follows: either the remote server is not already
	 * "known" or the path to the remote server is shorter than what is already
	 * "known". <br>
	 * The method adds or updates the routing information to the server {@code id}.
	 * The information is updated only if server {@code id} is unknown to this
	 * server or the length of the path is less than the existing information. <br>
	 * When a new server is discovered or a shorter path to server is discovered, an
	 * identity message is sent to this remote server so that routing information is
	 * updated at the remote server. <br>
	 * When this method is called, it is already synchronised on the server object.
	 * 
	 * @param msg          the message to parse.
	 * @param selectionKey the current selection key of connection.
	 * @param worker       the current worker of the connection.
	 */
	synchronized void parsePathOfMsgToUpdateRoutingInformation(final MsgContent msg, final SelectionKey selectionKey,
			final FullDuplexMsgWorker worker) {
		int length = 0;
		List<Integer> reversePath = new ArrayList<>(msg.getPath());
		Collections.reverse(reversePath);
		for (Integer id : reversePath) {
			length++;
			if (id != identity && (!reachableEntities.containsKey(id) || (reachableEntities.containsKey(id)
					&& reachableEntities.get(id).getLengthOfThePath() > length))) {
				reachableEntities.put(id, new RoutingInformation(id, length, reversePath.get(0), selectionKey, worker));
				if (LOG_ON && TOPO.isDebugEnabled()) {
					TOPO.debug(Log.computeServerLogMessage(this, ", " + toStringReachableEntities()));
				}
				// the new server may not know me or may not know this shorter path
				sendToAServer(id, ServerAlgorithm.getActionNumber(IDENTITY_MESSAGE),
						new IdentityContent(identity(), new ArrayList<>()));
				if (LOG_ON && COMM.isDebugEnabled()) {
					COMM.debug(Log.computeServerLogMessage(this, ", send identity message to server " + id));
				}
			}
		}
		assert invariant();
	}

	@Override
	public synchronized int identity() {
		return identity;
	}

	/**
	 * computes the string version of the collection of reachable entities.
	 * 
	 * @return the string.
	 */
	public synchronized String toStringReachableEntities() {
		return reachableEntities.values().stream().map(Object::toString)
				.collect(Collectors.joining("\n", "reachableEntities=\n", "\n"));
	}

	/**
	 * gets the routing information of a given neighbouring servers or local client,
	 * i.e. the length of the path is less or equal to 1.
	 * 
	 * @param key the selection key to get the routing information.
	 * @return the given routing information.
	 */
	synchronized Optional<RoutingInformation> getRoutingInformation(final SelectionKey key) {
		Objects.requireNonNull(key, "key cannot be null");
		return reachableEntities.entrySet().stream()
				.filter(e -> key.equals(e.getValue().getSelectionKey()) && e.getValue().getLengthOfThePath() <= 1)
				.map(Entry::getValue).findFirst();
	}

	/**
	 * gets the identity of the neighbouring server that corresponds to the first
	 * hop of the path to a remote server. A negative value is returned when the
	 * remote server is unknown. <br>
	 * This method is used in JUnit tests.
	 * 
	 * @param identityOfReachableServer the identity of the remote server.
	 * @return the identity of the first server to reach the remote server or -1
	 *         when the remote server is unknown.
	 */
	public synchronized int getFirstHopToRemoteServer(final int identityOfReachableServer) {
		if (reachableEntities.get(identityOfReachableServer) != null) {
			return reachableEntities.get(identityOfReachableServer).getIdentityNeighbouringServer();
		} else {
			return -1;
		}
	}

	/**
	 * removes the routing information thats corresponds to a connection, i.e. to a
	 * selection key.
	 * 
	 * Be careful! the distributed algorithm for removing an entity of the topology
	 * from the routing information (at all the entities of the topology) has not
	 * been written.
	 * 
	 * @param key the SelectionKey of the routing information to remove.
	 */
	synchronized void removeRoutingInformation(final SelectionKey key) {
		Objects.requireNonNull(key, "argument key cannot be null");
		Set<Integer> toRemove = reachableEntities.entrySet().stream()
				.filter(e -> key.equals(e.getValue().getSelectionKey())).map(Entry::getKey).collect(Collectors.toSet());
		reachableEntities.keySet().removeAll(toRemove);
		if (LOG_ON && COMM.isDebugEnabled()) {
			// FIXME the routing information should be updated on other servers...
			COMM.debug(Log.computeServerLogMessage(this,
					", removeRoutingInformation: FIXME the routing information should be updated on other servers..."));
		}
		assert invariant();
	}

	/**
	 * computes the number of local clients, i.e. the length of the path is equal to
	 * {@code -1}.
	 * 
	 * @return the number of local clients.
	 */
	synchronized long getNumberOfLocalClients() {
		return reachableEntities.entrySet().stream().filter(e -> e.getValue().getLengthOfThePath() == -1).count();
	}

	/**
	 * computes the number of neighbouring servers, i.e. the length of the path is
	 * equal to {@code 1}.
	 * 
	 * @return the number of neighbouring servers.
	 */
	synchronized long getNumberOfNeighbouringServers() {
		return reachableEntities.entrySet().stream().filter(e -> e.getValue().getLengthOfThePath() == 1).count();
	}

	/**
	 * computes the number of servers, i.e. the length of the path is greater or
	 * equal to {@code 1}.
	 * 
	 * @return the number of neighbouring servers.
	 */
	public synchronized long getNumberOfServers() {
		return reachableEntities.entrySet().stream().filter(e -> e.getValue().getLengthOfThePath() >= 1).count();
	}

	/**
	 * checks the invariant of the class.
	 * 
	 * @return a boolean stating whether the invariant is maintained.
	 */
	private synchronized boolean invariant() {
		return identity >= 0 && numberOfClientsSinceBeginning >= 0 && selector != null && runnableToRcvMsgs != null
				&& threadToRcvMsgs != null;
	}

	/**
	 * starts the thread that is responible for reading messages from the clients
	 * and the other servers.
	 */
	public void startThreadReadMessagesFromNetwork() {
		threadToRcvMsgs.start();
	}

	/**
	 * treats an input line from the console. <br>
	 * Do not forget to synchronise with {@code synchronized}.
	 * 
	 * @param line the content of the message
	 */
	public synchronized void treatConsoleInput(final String line) {
		Objects.requireNonNull(line, "argument line cannot be null");
		if (LOG_ON && GEN.isDebugEnabled()) {
			GEN.debug(Log.computeServerLogMessage(this, ", new command line on console"));
		}
		if (line.equals("quit")) {
			threadToRcvMsgs.interrupt();
			// do not interrupt the main thread during the execution of a Scenario because
			// all the clients and all the servers are controlled by the same "main" thread
			if (!Scenario.isJUnitScenario()) {
				Thread.currentThread().interrupt();
			}
		}

		// LaunchElection
		this.status = "initiator";
		this.caw = this.identity;
		ElectionTokenContent tokenContent = new ElectionTokenContent(this.identity, this.identity);
		sendToAllNeighbouringServersExceptOne(-1, ServerAlgorithm.getActionNumber(TOKEN_MESSAGE), tokenContent);

		assert invariant();
	}

	/**
	 * accepts connection (socket level), creates MsgWorker, and registers selection
	 * key of the remote server. This method is called when accepting a connection
	 * from a remote server.
	 * 
	 * @param sc server socket channel.
	 * @throws IOException the exception thrown in case of communication problem.
	 */
	synchronized void acceptNewServer(final ServerSocketChannel sc) throws IOException {
		SocketChannel rwChan = sc.accept();
		if (rwChan != null) {
			try {
				FullDuplexMsgWorker worker = new FullDuplexMsgWorker(rwChan);
				worker.configureNonBlocking();
				synchronized (this) {
					SelectionKey newKey = rwChan.register(selector, SelectionKey.OP_READ);
					ReadMessageStatus msgState;
					int idNeigh;
					do {
						msgState = worker.readMessage();
					} while (msgState != ReadMessageStatus.READDATACOMPLETED);
					idNeigh = worker.getData().filter(data -> data instanceof Integer).map(data -> (Integer) data)
							.orElseThrow(() -> new IllegalStateException(
									Log.computeServerLogMessage(this, "communication problem while getting"
											+ " the identity of the chat server: was waiting for an integer")));
					reachableEntities.put(idNeigh, new RoutingInformation(idNeigh, 1, idNeigh, newKey, worker));
					if (LOG_ON && COMM.isInfoEnabled()) {
						COMM.info(Log.computeServerLogMessage(this,
								", number of neighbouring servers= " + getNumberOfNeighbouringServers()));
					}
					if (LOG_ON && TOPO.isDebugEnabled()) {
						TOPO.debug(Log.computeServerLogMessage(this, ", " + toStringReachableEntities()));
					}
					assert invariant();
				}
			} catch (ClosedChannelException e) {
				COMM.error(e.getLocalizedMessage());
			}
		}
	}

	/**
	 * accepts connection (socket level), creates MsgWorker, and registers selection
	 * key of the local client. This method is called when accepting a connection
	 * from a local client.
	 * 
	 * @param sc server socket channel.
	 */
	synchronized void acceptNewClient(final ServerSocketChannel sc) {
		SocketChannel rwChan = null;
		try {
			rwChan = sc.accept();
		} catch (IOException e) {
			COMM.error(Log.computeServerLogMessage(this, e.getLocalizedMessage()));
			return;
		}
		if (LOG_ON && COMM.isDebugEnabled()) {
			COMM.debug(Log.computeServerLogMessage(this, ", accepting a client connection"));
		}
		if (rwChan != null) {
			try {
				FullDuplexMsgWorker worker = new FullDuplexMsgWorker(rwChan);
				worker.configureNonBlocking();
				SelectionKey newKey = rwChan.register(selector, SelectionKey.OP_READ);
				int idClient = identity * OFFSET_ID_CLIENT + numberOfClientsSinceBeginning;
				numberOfClientsSinceBeginning++;
				worker.sendMsg(0, identity, idClient);
				reachableEntities.put(idClient, new RoutingInformation(idClient, -1, -1, newKey, worker));
				if (LOG_ON && COMM.isInfoEnabled()) {
					COMM.info(Log.computeServerLogMessage(this,
							", number of local clients = " + getNumberOfLocalClients()));
				}
				if (LOG_ON && TOPO.isDebugEnabled()) {
					TOPO.debug(Log.computeServerLogMessage(this, ", " + toStringReachableEntities()));
				}
				assert invariant();
			} catch (IOException e) {
				COMM.error(Log.computeServerLogMessage(this, e.getLocalizedMessage()));
			}
		}
	}

	/**
	 * sends a message to a particular server using the selection key of the
	 * neighbouring server from which that server is reachable. The method uses the
	 * routing information objects {@link RoutingInformation}. This is a utility
	 * method for implementing distributed algorithms in the servers' state machine:
	 * use this method when this server needs sending messages to a given remote
	 * server using the collection {@link #reachableEntities}.
	 * 
	 * @param remoteServerIdentity the identity of the remote server.
	 * @param type                 message's type.
	 * @param msg                  message as a serializable object.
	 */
	public synchronized void sendToAServer(final int remoteServerIdentity, final int type, final MsgContent msg) {
		if (remoteServerIdentity == identity()) {
			throw new UnsupportedOperationException("sending to \"myself\" ist not supported");
		}
		RoutingInformation ri = reachableEntities.get(remoteServerIdentity);
		Objects.requireNonNull(ri == null);
		if (!msg.getPath().contains(identity())
				|| (msg instanceof IdentityContent && Collections.frequency(msg.getPath(), identity()) < 2)) {
			msg.appendToPath(identity());
		}
		if (LOG_ON && COMM.isInfoEnabled()) {
			COMM.info(Log.computeServerLogMessage(this,
					"sends message of type " + type + " to server " + remoteServerIdentity));
		}
		try {
			ri.getWorker().sendMsg(type, identity(), msg);
		} catch (IOException e) {
			removeRoutingInformation(ri.getSelectionKey());
			COMM.error(Log.computeServerLogMessage(this, e.getLocalizedMessage()));
		}
		assert invariant();
	}

	/**
	 * sends a message to all the local clients, i.e. connected to this server,
	 * except one. This is a utility method for implementing distributed algorithms
	 * in the servers' state machine: use this method when this server needs sending
	 * messages to all its local clients, except one. Use a negative value for the
	 * first argument when there is a local client to exclude.
	 * 
	 * @param exceptId the identity of the client to exclude in the sending. Since
	 *                 valid identities are positive, a negative value stipulates
	 *                 that there is no local client to exclude.
	 * @param type     message's type.
	 * @param msg      message as a serializable object.
	 */
	public synchronized void sendToAllLocalClientsExceptOne(final int exceptId, final int type,
			final ChatMsgContent msg) {
		AtomicInteger nbClients = new AtomicInteger(0);
		if (LOG_ON && COMM.isTraceEnabled()) {
			COMM.trace(Log.computeServerLogMessage(this,
					sequenceNumberOfLocalClients.entrySet().stream().map(e -> e.getKey() + "/" + e.getValue())
							.collect(Collectors.joining("\n", "\nsequenceNumberOfLocalClients=", "\n"))));
		}
		if (msg.getSequenceNumber() > sequenceNumberOfLocalClients.getOrDefault(msg.getSender(), -1)) {
			reachableEntities.entrySet().stream().filter(e -> e.getKey() != exceptId
					&& e.getValue().getLengthOfThePath() == -1 && !msg.getPath().contains(e.getKey())).forEach(e -> {
						try {
							e.getValue().getWorker().sendMsg(type, identity(), msg);
						} catch (IOException e1) {
							removeRoutingInformation(e.getValue().getSelectionKey());
							COMM.error(Log.computeServerLogMessage(this, e1.getLocalizedMessage()));
							return;
						}
						nbClients.getAndIncrement();
						sequenceNumberOfLocalClients.put(msg.getSender(), msg.getSequenceNumber());
					});
		}
		assert invariant();
	}

	/**
	 * sends a message to all the neighbouring servers connected to this server,
	 * except one. This is a utility method for implementing distributed algorithms
	 * in the servers' state machine: use this method when this server needs sending
	 * messages to all its neighbours, except one. Use a negative value for the
	 * first argument when there is no neighbouring server to exclude.
	 * 
	 * The condition for sending is:
	 * <ol>
	 * <li>the identity of the neighbour is not equal to {@code exceptId};</li>
	 * <li>the server is a neighbour, i.e. the path is equal to 1;</li>
	 * <li>the server has not already received {@code msg}, i.e. its identity is not
	 * the path of the message. This is so to avoid loops.</li>
	 * </ol>
	 * 
	 * @param exceptId the identity of the server to exclude in the sending. Since
	 *                 valid identities are positive, a negative value stipulates
	 *                 that there is no neighbouring server to exclude.
	 * @param type     message's type.
	 * @param msg      message as a {@link MsgContent} object.
	 */
	public synchronized void sendToAllNeighbouringServersExceptOne(final int exceptId, final int type,
			final MsgContent msg) {
		AtomicInteger nbServers = new AtomicInteger(0);
		if (LOG_ON && COMM.isTraceEnabled()) {
			COMM.trace(Log.computeServerLogMessage(this, ", " + toStringReachableEntities()));
		}
		reachableEntities.entrySet().stream().filter(e -> e.getKey() != exceptId
				&& e.getValue().getLengthOfThePath() == 1 && !msg.getPath().contains(e.getKey())).forEach(e -> {
					sendToAServer(e.getKey(), type, msg);
					nbServers.getAndIncrement();
				});
		if (LOG_ON && COMM.isInfoEnabled()) {
			COMM.info(Log.computeServerLogMessage(this, "sends message to " + nbServers.get() + " server end points"));
		}
		assert invariant();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + identity;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Server)) {
			return false;
		}
		Server other = (Server) obj;
		return identity == other.identity;
	}

	@Override
	public String toString() {
		return "Server " + identity;
	}

	/**
	 * treats an identity message of the algorithm for discovering the topology of
	 * the overlay network of servers. <br>
	 * The routing information concerning the new server has already been stored in
	 * the collection {@link #reachableEntities} before entering this method.<br>
	 * The existence of the new server is transmitted by flooding.<br>
	 * The actions of an algorithm are atomic. So, this method is synchronised.
	 * 
	 * @param content the content of the message to treat.
	 */
	public synchronized void receiveIdentityContent(final IdentityContent content) {
		List<Integer> newPath = new ArrayList<>(content.getPath());
		newPath.add(identity());
		// -1 since there is no neighbouring server to exclude
		sendToAllNeighbouringServersExceptOne(-1, ServerAlgorithm.getActionNumber(IDENTITY_MESSAGE),
				new IdentityContent(identity(), newPath));
		assert invariant();
	}

	/**
	 * treats a token message of the election algorithm. <br>
	 * The actions of an algorithm are atomic. So, this method is synchronised.
	 * 
	 * @param content the content of the message to treat.
	 */
	public synchronized void receiveElectionTokenContent(final ElectionTokenContent content) {
        if (this.caw == -1 || content.getInitiator() < this.caw) {
			this.caw = content.getInitiator();
			this.rec = 0;
			this.parent = content.getSender();
			ElectionTokenContent tokenContent = new ElectionTokenContent(this.identity, content.getInitiator());
			sendToAllNeighbouringServersExceptOne(this.parent, ServerAlgorithm.getActionNumber(TOKEN_MESSAGE), tokenContent);
		}

		if (this.caw == content.getInitiator()) {
			this.rec++;
			if (this.rec == this.getNumberOfNeighbouringServers()) {
				if (this.caw == this.identity){
					ElectionLeaderContent leaderContent = new ElectionLeaderContent(this.identity,this.identity);
					sendToAllNeighbouringServersExceptOne(-1, ServerAlgorithm.getActionNumber(LEADER_MESSAGE), leaderContent);
				} else {
					ElectionTokenContent tokenContent = new ElectionTokenContent(this.identity, content.getInitiator());
					sendToAServer(this.parent, ServerAlgorithm.getActionNumber(TOKEN_MESSAGE), tokenContent);
				}
			}
		}

		assert invariant();
	}

	/**
	 * treats a leader message of the election algorithm.<br>
	 * The actions of an algorithm are atomic. So, this method is synchronised.
	 * 
	 * @param content the content of the message to treat.
	 */
	public synchronized void receiveElectionLeaderContent(final ElectionLeaderContent content) {
		if (this.lrec == 0 && this.identity != content.getInitiator()) {
			ElectionLeaderContent leaderContent = new ElectionLeaderContent(this.identity, content.getInitiator());
			sendToAllNeighbouringServersExceptOne(-1, ServerAlgorithm.getActionNumber(LEADER_MESSAGE), leaderContent);
		}

		this.lrec++;
		this.win = content.getInitiator();

		if (this.lrec == this.getNumberOfNeighbouringServers()) {
			if (this.win == this.identity) {
				this.status = "leader";
			} else {
				this.status = "non-leader";
			}
		}

		assert invariant();
	}
}
