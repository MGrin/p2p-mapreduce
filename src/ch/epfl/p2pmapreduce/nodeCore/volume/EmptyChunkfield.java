package ch.epfl.p2pmapreduce.nodeCore.volume;

/**
 * Class represents an EmptyChunkfield
 *
 */
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
		// nothing to do here;
	}
	
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
