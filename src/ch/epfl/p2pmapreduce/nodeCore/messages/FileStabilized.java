package ch.epfl.p2pmapreduce.nodeCore.messages;

public class FileStabilized extends IndexUpdate {

	private int from;
	private int fileId;
	
	public FileStabilized(int from, int fileId) {
		super(from);
		this.from = from;
		this.fileId = fileId;
	}
	
	public int file() { return fileId; }

	@Override
	public void visit(MessageReceiver messageReceiver) {
		messageReceiver.receive(this);
	}
	
	@Override
	public String toString() {
		return "file stabilized message from peer " + from;
	}

}
