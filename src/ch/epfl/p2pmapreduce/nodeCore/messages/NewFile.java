package ch.epfl.p2pmapreduce.nodeCore.messages;

import java.text.SimpleDateFormat;
import java.util.Date;

import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;

/**
 * Class NewFile instantiates a new File for updating the index
 *
 */
public class NewFile extends IndexUpdate {

	private String fileName;
	private int chunkCount;
	private int from;

	private long fileSize = -1;
	
	private String fileCurrentDate;

	public NewFile(int from, String fileName, int chunkCount) {
		super(from);
		this.from = from;
		this.fileName = fileName;
		this.chunkCount = chunkCount;
		
		fileCurrentDate = getCurrentDate();
	}

	public String name() { return fileName; }
	public int chunkCount() { 
		return chunkCount;
	}
	
	public String getFileInfos() {
		
		return fileName + "," + chunkCount + "," + fileCurrentDate;
	}
	
	public long getFileSize() {
		return fileSize;
	}
	
	private String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		String currentDate = sdf.format(System.currentTimeMillis());
		
		return currentDate;
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
