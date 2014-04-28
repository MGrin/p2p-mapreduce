package ch.epfl.p2pmapreduce.nodeCore.volume;

public class ChunkToGet {

	public final File container;
	public final int id;
	public final int ownerId;
	
	public ChunkToGet(File container, int chunkId, int ownerId) {
		this.container = container;
		this.id = chunkId;
		this.ownerId = ownerId;
	}
}
