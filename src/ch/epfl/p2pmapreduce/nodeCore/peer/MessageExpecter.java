package ch.epfl.p2pmapreduce.nodeCore.peer;

import java.util.LinkedList;
import java.util.List;

import ch.epfl.p2pmapreduce.nodeCore.messages.MessageType;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendMessage;

public enum MessageExpecter {

	INSTANCE;
	
	private List<SendMessage> expected = new LinkedList<SendMessage>();
	
	
	public synchronized void expect(SendMessage newMessage) {
		expected.add(newMessage);
	}
	
	public synchronized boolean isExpected(SendMessage message) {
		boolean result = false;
		List<SendMessage> newExpected = new LinkedList<SendMessage>();
		for (SendMessage e : expected) {
			// could do better checking correct chunk was received but ...
			if (e.getType() == message.getType() && e.sender() == message.sender() && !result) {
				result = true;
			} else newExpected.add(e);
		}
		expected = newExpected; // removes found message;
		return result;
	}
	
	public synchronized boolean waitingChunkfield() {
		return waitingX(MessageType.SEND_CHUNKFIELD);
	}
	
	public synchronized boolean waitingChunk() {
		return waitingX(MessageType.SEND_CHUNK);
	}
	
	public synchronized boolean waitingIndex() {
		return waitingX(MessageType.SEND_INDEX);
	}
	
	private boolean waitingX(MessageType type) {
		for (SendMessage e : expected) if (e.getType() == type) return true;
		return false;
	}

	/**
	 * Eliminates from the expected list a specified type of message.
	 * 
	 * 
	 * @param type the type of message to timeout
	 * @return the list of peerIds that did not respond to their message.
	 */
	public List<Integer> timeOut(MessageType type) {
		List<Integer> result = new LinkedList<Integer>();
		List<SendMessage> keptMessage = new LinkedList<SendMessage>();
		for (SendMessage e : expected) {
			if (e.getType() == type) result.add(e.sender());
			else keptMessage.add(e);
		}
		expected = keptMessage;
		return result;
	}
}
