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
 * This class defines the content of a leader message of the election algorithm.
 * 
 * TODO add attributes.
 * 
 * TODO implement the constructor.
 * 
 * TODO add getters.
 * 
 * TODO perhaps, add the methods equals and hashCode? (if necessary)
 * 
 * TODO add the toString method.
 * 
 * TODO add your names in the list of authors.
 * 
 * @author Denis Conan
 *
 */
public class ElectionLeaderContent extends ServerToServerMsgContent {
	/**
	 * version number for serialisation.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * constructs the content of a leader election message.
	 * 
	 * @param sender         the identity of the sender.
	 */
	public ElectionLeaderContent(final int sender) {
		super(sender, Arrays.asList(sender));
		// TODO implement
	}
}
