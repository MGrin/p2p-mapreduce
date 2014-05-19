package ch.epfl.p2pmapreduce.nodeCore.messages;

import ch.epfl.p2pmapreduce.nodeCore.volume.Index;
/**
 * Message to send the index
 *
 */
public class SendIndex implements Message, SendMessage {

	private final int from;
	public final Index index;

	public SendIndex(int from, Index index) {
		this.from = from;
		this.index = index;
	}

	@Override
	public int sender() {
		return from;
	}

	@Override
	public void visit(MessageReceiver messageVisitor) {
		messageVisitor.receive(this);
	}

	public String toString() {
		return "send index message from peer " + from;
	}

	@Override
	public MessageType getType() { return MessageType.SEND_INDEX; }
}
