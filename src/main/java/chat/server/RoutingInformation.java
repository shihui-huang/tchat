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

import java.nio.channels.SelectionKey;
import java.util.Objects;

import chat.common.FullDuplexMsgWorker;

/**
 * This class defines the routing information to a remote entity (local client
 * or server).
 * 
 * @author Denis Conan
 * 
 */
public class RoutingInformation {
	/**
	 * the identity of the remote entity.
	 */
	private int indentityOfRemoteEntity;
	/**
	 * the length of the path to the remote entity. This is equal to 1 when the
	 * entity is a neighbouring server and is equal to -1 when the entity is a local
	 * client.
	 */
	private int lengthOfThePath;
	/**
	 * when a server, the identity of the neighbouring server in the path to the
	 * remote server, that is the identity of the first server of the path to the
	 * remote server. This attribute is equal to -1 when the entity is a local
	 * client.
	 */
	private int identityNeighbouringServer;
	/**
	 * the selection key to the neighbouring server or to the client.
	 */
	private SelectionKey selectionKey;
	/**
	 * the full duplex message worker that corresponds to the selection key.
	 */
	private FullDuplexMsgWorker worker;

	/**
	 * constructs an object.
	 * 
	 * @param indentityOfRemoteEntity    the identity of the remote entity.
	 * @param lengthOfThePath            the length of the path that has been found;
	 *                                   -1 in case of a local client.
	 * @param identityNeighbouringServer the identity of the server for the first
	 *                                   hop of the path ; -1 in case of local
	 *                                   client.
	 * @param selectionKey               the selection key of the first hop.
	 * @param worker                     the full duplex message worker.
	 */
	public RoutingInformation(final int indentityOfRemoteEntity, final int lengthOfThePath,
			final int identityNeighbouringServer, final SelectionKey selectionKey, final FullDuplexMsgWorker worker) {
		if (indentityOfRemoteEntity < 0) {
			throw new IllegalArgumentException("the identity of the remote entity cannot be negative");
		}
		Objects.requireNonNull(selectionKey, "selectionKeyOfNeighbouringServer cannot be null");
		Objects.requireNonNull(worker, "worker cannot be null");
		this.indentityOfRemoteEntity = indentityOfRemoteEntity;
		this.lengthOfThePath = lengthOfThePath;
		this.identityNeighbouringServer = identityNeighbouringServer;
		this.selectionKey = selectionKey;
		this.worker = worker;
	}

	/**
	 * gets the identity of the remote entity.
	 * 
	 * @return the identity.
	 */
	public int getIndentityOfRemoteEntity() {
		return indentityOfRemoteEntity;
	}

	/**
	 * gets the length of the path to the remote entity.
	 * 
	 * @return the length of the path ; -1 in case of a local client.
	 */
	public int getLengthOfThePath() {
		return lengthOfThePath;
	}

	/**
	 * the identity of the first server of the path to the remote entity.
	 * 
	 * @return the identity of the neighbouring server ; -1 in case of a local
	 *         client.
	 */
	public int getIdentityNeighbouringServer() {
		return identityNeighbouringServer;
	}

	/**
	 * gets the selection key.
	 * 
	 * @return the selection key.
	 */
	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	/**
	 * gets the full duplex message worker.
	 * 
	 * @return the worker.
	 */
	public FullDuplexMsgWorker getWorker() {
		return worker;
	}

	@Override
	public int hashCode() {
		return Objects.hash(indentityOfRemoteEntity);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RoutingInformation)) {
			return false;
		}
		RoutingInformation other = (RoutingInformation) obj;
		return indentityOfRemoteEntity == other.indentityOfRemoteEntity;
	}

	@Override
	public String toString() {
		return "RoutingInformation [indentityOfRemoteEntity=" + indentityOfRemoteEntity + ", lengthOfThePath="
				+ lengthOfThePath + ", identityNeighbouringServer=" + identityNeighbouringServer + ", selectionKey="
				+ selectionKey + ", worker=" + worker + "]";
	}
}
