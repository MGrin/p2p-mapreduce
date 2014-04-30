package ch.epfl.p2pmapreduce.nodeCore.messages;

import ch.epfl.p2pmapreduce.nodeCore.volume.File;

public class GetChunk implements Message {

	private int from;
	private File file;
	private int chunkId;
	
	public GetChunk(int from, File file, int chunkId) {
		this.from = from;
		this.file = file;
		this.chunkId = chunkId;
	}
	@Override
	public int sender() { return from; }

	@Override
	public void visit(MessageReceiver messageReceiver) {
		messageReceiver.receive(this);
	}
	
	public File file() { return file; }
	
	public int chunkId() { return chunkId; }


	public String toString() {
		return "get chunk message for file " + file.uid + ", chunk " + chunkId + " from peer " + from;
	}
}
