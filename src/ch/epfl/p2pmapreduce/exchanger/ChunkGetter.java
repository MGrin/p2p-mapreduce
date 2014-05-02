package ch.epfl.p2pmapreduce.exchanger;

import net.jxta.endpoint.Message;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;

public class ChunkGetter extends Message {
	private static final long serialVersionUID = 1L;

	private String name;
	private int from;
	private int chunkId;
	private File file;

	public ChunkGetter(GetChunk getChunk) {
		super();
		this.name = "CHUNKGETTER";
		this.setFrom(getChunk.sender());
		this.setChunkId((getChunk.chunkId()));
		this.setFile(getChunk.file());
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

	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public void setChunkId(int chunkId) {
		this.chunkId = chunkId;
	}

	public int getChunkId() {
		return chunkId;
	}
}
