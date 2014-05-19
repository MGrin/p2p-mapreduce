package ch.epfl.p2pmapreduce.nodeCore.messages;

/**
 * Message for asking the index (up-to-date or almost) to a neighbor
 *
 */
public class GetIndex implements Message {

	private final int from;
	
	public GetIndex(int from) {
		this.from = from;
	}
	
	@Override
	public int sender() { return from; }

	@Override
	public void visit(MessageReceiver messageVisitor) {
		messageVisitor.receive(this);
	}

	public String toString() {
		return "get index message from peer " + from;
	}
	
}
