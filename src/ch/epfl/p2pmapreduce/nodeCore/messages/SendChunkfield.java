package ch.epfl.p2pmapreduce.nodeCore.messages;

import java.util.Map;

import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;

/**
 * Class implementing the message SendChunk that permits to send a chunk.
 */
public class SendChunkfield implements Message, SendMessage {
	
	private int from;
	private Map<String, Chunkfield> chunkfields;
	
	public SendChunkfield(int from, Map<String, Chunkfield> chunkfields) {
		this.from = from;
		this.chunkfields = chunkfields;
	}
	
	public int sender() { return from; }
	
	public Map<String, Chunkfield> chunkfields() { return chunkfields; }
	
	@Override
	public void visit(MessageReceiver messageVisitor) {
		messageVisitor.receive(this);
	}

	public String toString() {
		return "send chunkfield message from peer " + from;
	}

	@Override
	public MessageType getType() { return MessageType.SEND_CHUNKFIELD; }
}
