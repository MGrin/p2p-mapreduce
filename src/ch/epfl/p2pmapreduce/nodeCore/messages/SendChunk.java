package ch.epfl.p2pmapreduce.nodeCore.messages;

import ch.epfl.p2pmapreduce.nodeCore.volume.File;

public class SendChunk implements Message, SendMessage {

	private int from;
	private String fName;
	private int chunkId;
	
	private byte[] chunkData;
	
	
	public SendChunk(int from, String fName, int chunkId, byte[] chunkData) {
		this.from = from;
		this.fName = fName;
		this.chunkId = chunkId;
		
		//TODO: Copy?
		this.chunkData = chunkData;
	}
	
	public SendChunk(int from, String fName, int chunkId) {
		this(from, fName, chunkId, null);
	}
	@Override
	public int sender() { return from; }

	@Override
	public void visit(MessageReceiver messageReceiver) {
		messageReceiver.receive(this);
	}

	public String toString() {
		return "get chunk message for file " + fName + ", chunk " + chunkId + " from peer " + from;
	}
	
	public String fName() { return fName; }
	
	public int chunkId() { return chunkId; }
	
	public byte[] getChunkData() {
		return chunkData;
	}

}
