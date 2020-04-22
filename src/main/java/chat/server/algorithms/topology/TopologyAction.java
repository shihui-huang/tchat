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
package chat.server.algorithms.topology;

import java.util.Objects;
import java.util.function.BiConsumer;

import chat.common.Action;
import chat.common.Entity;
import chat.common.MsgContent;
import chat.server.Server;

/**
 * This Enumeration type declares the actions of the algorithm for discovering
 * the topology of the overlay network of servers.
 * 
 * @author Denis Conan
 * 
 */
public enum TopologyAction implements Action {
	/**
	 * the enumerator for the action of the token message of the election algorithm.
	 */
	IDENTITY_MESSAGE(IdentityContent.class,
			(Entity server, MsgContent content) -> ((Server) server).receiveIdentityContent((IdentityContent) content));

	/**
	 * the type of the content.
	 */
	private final Class<? extends MsgContent> contentClass;

	/**
	 * the lambda expression of the action.
	 */
	private final BiConsumer<Entity, MsgContent> actionFunction;

	/**
	 * is the constructor of message type object.
	 * 
	 * @param contentClass   the type of the content.
	 * @param actionFunction the lambda expression of the action.
	 */
	TopologyAction(final Class<? extends MsgContent> contentClass,
			final BiConsumer<Entity, MsgContent> actionFunction) {
		Objects.requireNonNull(contentClass, "argument contentClass cannot be null");
		Objects.requireNonNull(actionFunction, "argument actionFunction cannot be null");
		this.contentClass = contentClass;
		this.actionFunction = actionFunction;
	}

	/**
	 * gets the type of the content.
	 * 
	 * @return the type of the content.
	 */
	public Class<? extends MsgContent> contentClass() {
		return contentClass;
	}

	/**
	 * gets the lambda expression of the action.
	 * 
	 * @return the lambda expression.
	 */
	public BiConsumer<Entity, MsgContent> actionFunction() {
		return actionFunction;
	}
}
