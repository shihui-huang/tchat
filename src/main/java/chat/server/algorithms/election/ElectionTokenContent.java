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
package chat.server.algorithms.election;

import java.util.Arrays;

import chat.server.algorithms.ServerToServerMsgContent;

/**
 * This class defines the content of a token message of the election algorithm.
 *
 * TODO perhaps, add the methods equals and hashCode? (if necessary)
 *
 * @author Denis Conan
 * @author Shihui Huang
 * @author Bastien Sun
 *
 */
public class ElectionTokenContent extends ServerToServerMsgContent {
	/**
	 * version number for serialisation.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * identité du parent lors de la réception.
	 */
	private final int sender;
	/**
	 * identité de l'initiateur de la vague.
	 */
	private final int initiator;

	/**
	 * constructs the content of a leader election message.
	 *
	 * @param sender    the identity of the sender.
	 * @param initiator the initiator
	 */
	public ElectionTokenContent(final int sender, final int initiator) {
		super(sender, Arrays.asList(sender));
		this.sender = sender;
		this.initiator = initiator;
	}

	@Override
	public int getSender() {
		return sender;
	}

	/**
	 * Gets initiator.
	 *
	 * @return the initiator
	 */
	public int getInitiator() {
		return initiator;
	}

	@Override
	public String toString() {
		return "ElectionTokenContent{"
				+ "sender=" + sender
				+ ", initiator=" + initiator
				+ '}';
	}
}
