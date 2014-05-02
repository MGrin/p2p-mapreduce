package ch.epfl.p2pmapreduce.nodeCore.network;

import java.util.HashMap;
import java.util.Map;

import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;

public class Neighbour {
	
	public final int id;
	//private PeerID peerID;
	
	private final Map<File, Chunkfield> chunkfields = new HashMap<File, Chunkfield>();
	
	public Neighbour(int peerId) {
		id = peerId;
	}
	
	public Chunkfield getChunkfield(File f) {
		return chunkfields.get(f);
	}

	public void setChunkfield(File file, Chunkfield c) {
		chunkfields.put(file, c);
	}
}
