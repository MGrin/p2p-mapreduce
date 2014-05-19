package ch.epfl.p2pmapreduce.nodeCore.volume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ch.epfl.p2pmapreduce.nodeCore.peer.Peer;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;

/**
 * Class implementing the global chunkfield
 *
 */
public class GlobalChunkfield {

	private List<Chunkfield> chunkfields = new LinkedList<Chunkfield>();
	public final int fileSize;
	
	public GlobalChunkfield(int fileSize) {
		this.fileSize = fileSize;
	}
	
//	public GlobalChunkfield(int peerId, Chunkfield cf, int fileSize) {
//		this(fileSize);
//		update(peerId, cf);
//	}
	
//	public void update(Peer p, Chunkfield c) {
//		chunkfields.put(p.id, c);
//	}
	public void update(Chunkfield c) {
		chunkfields.add(c);
	}
	
//	public void remove(Peer p) {
//		chunkfields.remove(p.id);
//	}
	
	private int[] chunkCounts() {
		int [] result = new int[fileSize];
		Arrays.fill(result, 0);
		for (Chunkfield c : chunkfields) {
			if (c != null) c.fillWithChunks(result);
		}
		return result;
	}

	/**
	 * Selects chunks which replication value is below GC_MIN.
	 * GC_MIN denotes the minimal value of duplication seen per one peer to become
	 * responsible for the file.
	 * @return The list of under replicated chunks indexes. List() if none.
	 */
	public List<Integer> underMinChunks() {
		return chunksBelowBound(NetworkConstants.GC_OPT);
	}

	/**
	 * Selects chunks which replication value is below GC_OPT.
	 * GC_OPT denotes the optimal value of duplication seen per one responsible peer.
	 * @return The list of under replicated chunks indexes. List() if none.
	 */
	public List<Integer> lowChunks() {
		return chunksBelowBound(NetworkConstants.GC_OPT);
	}
	
	private List<Integer> chunksBelowBound(int bound) {
		List<Integer> result = new ArrayList<Integer>();
		int[] globalChunks = chunkCounts();
		for (int i = 0; i < globalChunks.length; i++) {
			if (globalChunks[i] < bound) {
				result.add(i);
			}
		}
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for (int gcc : chunkCounts()) {
			sb.append(gcc + ", ");
		}
		return sb.substring(0, sb.length()-2) + "]";
	}
}
