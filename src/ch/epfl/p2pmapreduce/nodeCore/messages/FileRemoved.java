package ch.epfl.p2pmapreduce.nodeCore.messages;


public class FileRemoved extends IndexUpdate {

	private int fileId;
	private String fileName;
	private int from;
	
	public FileRemoved(int from, int fileId, String fileName) {
		super(from);
		this.fileId = fileId;
		this.from = from;
		this.fileName = fileName;
	}
	
	public int uid() { return fileId; }
	public String name() { return fileName; }

	@Override
	public void visit(MessageReceiver messageVisitor) {
		messageVisitor.receive(this);
	}
	
	@Override
	public String toString() {
		return "new file message from peer " + from;
	}

}

