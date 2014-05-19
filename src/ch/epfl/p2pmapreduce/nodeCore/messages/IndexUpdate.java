package ch.epfl.p2pmapreduce.nodeCore.messages;
/**
 * Message IndexUpdate
 *
 */
public abstract class IndexUpdate implements Message {

	private int from;
	
	public IndexUpdate(int from) {
		this.from = from;
	}
	
	@Override
	public int sender() { return from; }

}
