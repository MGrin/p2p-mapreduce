package ch.epfl.p2pmapreduce.nodeCore.messages;

import ch.epfl.p2pmapreduce.nodeCore.network.IMessageSender;
import ch.epfl.p2pmapreduce.nodeCore.network.Neighbour;

/**
 * Class that represents an object for the message that serve to update the index when 
 * a file is removed from the DFS
 */
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

