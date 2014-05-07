package ch.epfl.p2pmapreduce.nodeCore.volume;

import java.util.Arrays;

public class Chunkfield {

	private boolean[] field;
	
	public Chunkfield(File f, boolean isOwned) {
		field = new boolean[f.chunkCount];
		for (int i = 0; i < field.length; i++) {
			field[i] = isOwned;
		}
//		this.f = f;
	}
	
	/**
	 * Used for creating a deep copy of a chunkfield.
	 * 
	 * @param c the chunkfield to copy.
	 */
	public Chunkfield(Chunkfield c) {
		this.field = Arrays.copyOf(c.field, c.field.length);
	}
	
	public Chunkfield(File f) {
		this(f, false);
	}
	public Chunkfield(boolean[] field){
		this.field = field;
	}
	
	public void putChunk(int i) {
		if (i < field.length) {
			field[i] = true;
		}
	}
	
	public void dropChunk(int i) {
		if (i < field.length) {
			field[i] = false;
		}
	}
	
	public boolean isEmpty() {
		for (boolean b : field) {
			if (b) return false;
		}
		return true;
	}
	
	/**
	 * The user must be careful not to mix fields of different files.
	 * Different chunkfield length may lead to out of bound exception.
	 * 
	 * @param globalChunkfield the array to increment with owned chunk indexes.
	 */
	public void fillWithChunks(int[] globalChunkfield) {
		for (int i = 0; i < field.length; i++) {
			if (field[i]) globalChunkfield[i]++;
		}
	}

	/**
	 * the user must be careful to provide chunkId in range of file's chunk count.
	 * @param chunkId the chunk to look for.
	 * @return true if this chunkfield contains the chunkId, false otherwise.
	 */
	public boolean hasChunk(int chunkId) {
		return field[chunkId];
	}
	
	@Override
	public String toString() {
		return Arrays.toString(field);
	}
	
	public String toBitString() {
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < field.length; i++) {
			if(field[i]) builder.append("1");
			else builder.append("0");
		}
		
		return builder.toString();
	}

}
