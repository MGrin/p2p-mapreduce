package ch.epfl.p2pmapreduce.nodeCore.volume;


public class EmptyChunkfield extends Chunkfield {
	
	
	
	public EmptyChunkfield() {
		super(new boolean[0]);
	}
	
	public void putChunk(int i) {
		throw new RuntimeException("putChunk in empty Chunkfield");
	}
	
	public void dropChunk(int i) {
		throw new RuntimeException("dropChunk in empty Chunkfield");
	}
	
	public boolean isEmpty() {
		return true;
	}
	
	public boolean isFull() {
		return false;
	}
	
	public void fillWithChunks(int[] globalChunkfield) {
		throw new RuntimeException("fillWithChunks in empty Chunkfield");
	}

	/**
	 * the user must be careful to provide chunkId in range of file's chunk count.
	 * @param chunkId the chunk to look for.
	 * @return true if this chunkfield contains the chunkId, false otherwise.
	 */
	public boolean hasChunk(int chunkId) {
		return false;
	}
	
	@Override
	public String toString() {
		return "[]";
	}
	
	public String toBitString() {
		return toString();
	}
}
