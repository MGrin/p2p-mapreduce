package ch.epfl.p2pmapreduce.nodeCore.messages;

import java.util.Date;

import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;


public class NewFile extends IndexUpdate {

	private String fileName;
	private int chunkCount;
	private int from;

	private long fileSize = -1;

	public NewFile(int from, String fileName, int chunkCount) {
		super(from);
		this.from = from;
		this.fileName = fileName;
		this.chunkCount = chunkCount;
	}

	public NewFile(int from, long fileSize, String fileName) {
		super(from);
		this.fileSize = fileSize;
		this.fileName = fileName;
		this.chunkCount = (int) Math.ceil(1.0 * fileSize / NetworkConstants.CHUNK_SIZE);
	}

	public String name() { return fileName; }
	public int chunkCount() { 
		return chunkCount;
	}
	
	public String getFileInfos() {
		if(fileSize == -1) System.out.println("FileSize not initialized!!"); System.exit(-1);
		return fileName + "," + fileSize + "," + new Date();
	}

	@Override
	public void visit(MessageReceiver messageVisitor) {
		messageVisitor.receive(this);
	}

	@Override
	public String toString() {
		return "new file message from peer " + from;
	}

}
