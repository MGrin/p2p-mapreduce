package ch.epfl.p2pmapreduce.nodeCore.messages;

import ch.epfl.p2pmapreduce.nodeCore.volume.File;
/**
 * Class implementing an object to create a message for getting a chunk
 *
 */
public class GetChunk implements Message {

	private int from;
	private String fName;
	private int chunkId;
	
	public GetChunk(int from, String fName, int chunkId) {
		this.from = from;
		this.fName = fName;
		this.chunkId = chunkId;
	}
	@Override
	public int sender() { return from; }

	@Override
	public void visit(MessageReceiver messageReceiver) {
		messageReceiver.receive(this);
	}
	
	public String fName() { return fName; }
	
	public int chunkId() { return chunkId; }


	public String toString() {
		return "get chunk message for file " + fName + ", chunk " + chunkId + " from peer " + from;
	}
}
