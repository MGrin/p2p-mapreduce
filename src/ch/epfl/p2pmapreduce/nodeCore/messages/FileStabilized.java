package ch.epfl.p2pmapreduce.nodeCore.messages;

public class FileStabilized extends IndexUpdate {

	private int from;
	private String fName;
	
	public FileStabilized(int from, String fname) {
		super(from);
		this.from = from;
		this.fName = fName;
	}
	
	public String file() { return fName; }

	@Override
	public void visit(MessageReceiver messageReceiver) {
		messageReceiver.receive(this);
	}
	
	@Override
	public String toString() {
		return "file stabilized message from peer " + from;
	}

}
