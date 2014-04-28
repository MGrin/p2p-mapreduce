package ch.epfl.p2pmapreduce.nodeCore.messages;


public class NewFile extends IndexUpdate {

	private int fileId;
	private String fileName;
	private int chunkCount;
	private int from;
	
	public NewFile(int from, int fileId, String fileName, int chunkCount) {
		super(from);
		this.fileId = fileId;
		this.from = from;
		this.fileName = fileName;
		this.chunkCount = chunkCount;
	}
	
	public int uid() { return fileId; }
	public String name() { return fileName; }
	public int chunkCount() { return chunkCount; }

	@Override
	public void visit(MessageReceiver messageVisitor) {
		messageVisitor.receive(this);
	}
	
	@Override
	public String toString() {
		return "new file message from peer " + from;
	}

}
