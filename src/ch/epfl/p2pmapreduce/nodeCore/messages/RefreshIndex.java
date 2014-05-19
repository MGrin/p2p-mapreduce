package ch.epfl.p2pmapreduce.nodeCore.messages;
/**
 * Class RefreshIndex
 *
 */
public class RefreshIndex implements Message {

	private int senderId;
	
	public RefreshIndex(int senderId) {
		this.senderId = senderId;
	}
	
	@Override
	public int sender() { return senderId; }

	@Override
	public void visit(MessageReceiver messageReceiver) {
		// should not be received
	}
	
}
