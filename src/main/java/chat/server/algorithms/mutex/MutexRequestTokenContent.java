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
package chat.server.algorithms.mutex;

import chat.server.algorithms.ServerToServerMsgContent;

import java.util.Arrays;

/**
 * This class defines the content of a leader message of the election algorithm.
 * 
 * @author Shihui Huang
 * @author Bastien Sun
 *
 */
public class MutexRequestTokenContent extends ServerToServerMsgContent {
	/**
	 * clock.
	 */
	private int ns;

	/**
     * constructs the content of a request mutex token message.
	 *
	 * @param sender            the identity of the sender
	 * @param intendedRecipient the intended recipient
	 * @param ns                the clock
	 */
	public MutexRequestTokenContent(final int sender, final int intendedRecipient, final int ns) {
		super(sender, intendedRecipient, Arrays.asList(sender));
		this.ns = ns;
	}

	/**
	 * Gets clock.
	 *
	 * @return the clock
	 */
	public int getNs() {
		return ns;
	}

	@Override
	public String toString() {
		return "MutexRequestTokenContent{"
				+ "sender=" + getSender()
				+ ", recipient=" + getIntendedRecipient()
				+ ", ns=" + ns
				+ '}';
	}
}
