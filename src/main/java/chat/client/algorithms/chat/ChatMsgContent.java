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
package chat.client.algorithms.chat;

import static chat.common.Log.CHAT;
import static chat.common.Log.LOG_ON;

import java.util.Objects;

import chat.common.MsgContent;
import chat.common.VectorClock;

/**
 * This class defines the content of a chat message.
 * 
 * @author Denis Conan
 *
 */
public class ChatMsgContent extends MsgContent {
	/**
	 * version number for serialization.
	 */
	private static final long serialVersionUID = 2L;
	/**
	 * the content of the message.
	 */
	private final String content;
	/**
	 * the sequence number.
	 */
	private final int sequenceNumber;
	/**
	 * the vectorClock.
	 */
	private VectorClock vectorClock;

	/**
	 * constructs the message.
	 *
	 * @param idSender    the identifier of the sender.
	 * @param seqNumber   the sequence number of the message.
	 * @param content     the content of the message.
	 * @param vectorClock the vector clock
	 */
	public ChatMsgContent(final int idSender, final int seqNumber, final String content, final VectorClock vectorClock) {
		super(idSender);
		Objects.requireNonNull(content, "argument content cannot be null");
		this.sequenceNumber = seqNumber;
		this.content = content;
		this.vectorClock = vectorClock;
		assert invariant();
	}

	/**
	 * checks the invariant of the class.
	 * 
	 * NB: the method is final so that the method is not overridden in potential
	 * subclasses because it is called in the constructor.
	 * 
	 * @return the boolean stating the invariant is maintained.
	 */
	public final boolean invariant() {
		return content != null;
	}

	/**
	 * the content of the message.
	 * 
	 * @return the content of the message as a string.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * the sequence number of the message.
	 * 
	 * @return the sequence number of the message.
	 */
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * Gets vector clock.
	 *
	 * @return the vector clock
	 */
	public VectorClock getVectorClock() {
		return vectorClock;
	}

	@Override
	public String toString() {
		if (LOG_ON && CHAT.isDebugEnabled()) {
			return "sender = " + getSender() + ", sequence number = " + sequenceNumber + ", path = ["
					+ toStringPath() + "], content = " + content;
		} else {
			return content;
		}
	}
}
