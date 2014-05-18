package ch.epfl.p2pmapreduce.nodeCore.messages;

public class Get implements Message {
	private int from;
	private String fName;
	
	public Get(int from, String fName) {
		this.from = from;
		this.fName = fName;
	}
	
	@Override
	public int sender() {
		return from;
	}

	@Override
	public void visit(MessageReceiver messageReceiver) {
		
	}
	
	public String toString() {
		return "get message for file " + fName + " from peer " + from;
	}
}
