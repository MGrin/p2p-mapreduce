package ch.epfl.p2pmapreduce.nodeCore.network;

import java.util.HashMap;
import java.util.Map;

import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;
import ch.epfl.p2pmapreduce.nodeCore.volume.EmptyChunkfield;
/**
 * Class reprenting a neighbour
 *
 */
public class Neighbour {
	
	public final int id;
	//private PeerID peerID;
	
	private final Map<String, Chunkfield> chunkfields = new HashMap<String, Chunkfield>();
	
	public Neighbour(int peerId) {
		id = peerId;
	}
	
	public Chunkfield getChunkfield(String fName) {
		if (! chunkfields.containsKey(fName)) {
			return new EmptyChunkfield();
		} else return chunkfields.get(fName);
	}

	public void setChunkfield(String fName, Chunkfield c) {
		chunkfields.put(fName, c);
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(Object that) {
		if (that != null && this.getClass().isInstance(that)) {
			return this.id == ((Neighbour) that).id;
		} else return false;
	}
}
