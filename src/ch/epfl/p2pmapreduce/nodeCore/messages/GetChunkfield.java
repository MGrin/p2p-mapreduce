package ch.epfl.p2pmapreduce.nodeCore.messages;

/**
 * Message to request all chunkfields from a peer
 *
 */
public class GetChunkfield implements Message {

	private final int from;
	
	public GetChunkfield(int from) {
		this.from = from;
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
		return "get chunkfield message from peer " + from;
	}

}
