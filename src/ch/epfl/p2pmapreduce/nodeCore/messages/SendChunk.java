package ch.epfl.p2pmapreduce.nodeCore.messages;

public class SendChunk implements Message {

	private int from;
	private int fileId;
	private int chunkId;
	
	private byte[] chunkData;
	
	public SendChunk(int from, int fileId, int chunkId, byte[] chunkData) {
		this.from = from;
		this.fileId = fileId;
		this.chunkId = chunkId;
		
		//TODO: Copy?
		this.chunkData = chunkData;
	}
	@Override
	public int sender() { return from; }

	@Override
	public void visit(MessageReceiver messageReceiver) {
		messageReceiver.receive(this);
	}

	public String toString() {
		return "get chunk message for file " + fileId + ", chunk " + chunkId + " from peer " + from;
	}
	
	public int fileId() { return fileId; }
	
	public int chunkId() { return chunkId; }

}
