package ch.epfl.p2pmapreduce.exchanger;

import net.jxta.endpoint.Message;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;

public class ChunkSender extends Message implements MessageStruct{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int from;
	private byte[] chunkData;
	private int fileId;
	private int chunkId;
	
	public ChunkSender(SendChunk sendChunk) {
		super();
		this.name = "CHUNKSENDER";
		this.setFrom(sendChunk.sender());
		this.setChunkData(sendChunk.getChunkData());
		this.setFileId(sendChunk.chunkId());
		this.setChunkId(sendChunk.chunkId());
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}


	public void setFrom(int from) {
		this.from = from;
	}


	public int getFrom() {
		return from;
	}


	public void setChunkData(byte[] chunkData) {
		this.chunkData = chunkData;
	}


	public byte[] getChunkData() {
		return chunkData;
	}


	public void setFileId(int fileId) {
		this.fileId = fileId;
	}


	public int getFileId() {
		return fileId;
	}


	public void setChunkId(int chunkId) {
		this.chunkId = chunkId;
	}


	public int getChunkId() {
		return chunkId;
	}


	@Override
	public void accept(MessageVisitor visitor) {
		visitor.visit(this);
	}
}
