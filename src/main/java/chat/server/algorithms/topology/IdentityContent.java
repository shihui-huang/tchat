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

import java.util.List;

import chat.server.algorithms.ServerToServerMsgContent;

/**
 * This class defines the content of a message containing the identity of the
 * sender. This message is used each time a topology change is detected.
 * 
 * @author Denis Conan
 *
 */
public class IdentityContent extends ServerToServerMsgContent {
	/**
	 * serialisation number.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * constructs a message.
	 * 
	 * @param sender            the identity of the sender.
	 * @param intendedRecipient the identity of the intended recipient, which is a
	 *                          reachable server.
	 * @param path              the path of servers.
	 */
	public IdentityContent(final int sender, final int intendedRecipient, final List<Integer> path) {
		super(sender, intendedRecipient, path);
		assert invariantMsgContent();
	}

	/**
	 * constructs a message with no intended recipient (irrelevant intended
	 * recipient).
	 * 
	 * @param sender the identity of the sender.
	 * @param path   the path of servers.
	 */
	public IdentityContent(final int sender, final List<Integer> path) {
		this(sender, NO_INTENDED_RECIPIENT, path);
	}
}
