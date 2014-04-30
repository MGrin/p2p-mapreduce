package ch.epfl.p2pmapreduce.nodeCore.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.IndexUpdate;
import ch.epfl.p2pmapreduce.nodeCore.messages.Message;
import ch.epfl.p2pmapreduce.nodeCore.peer.Peer;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;
import ch.epfl.p2pmapreduce.nodeCore.utils.PeerManager;
import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;
import ch.epfl.p2pmapreduce.nodeCore.volume.GlobalChunkfield;


public class ConnectionManager {

	private final static PeerManager PM = PeerManager.getInstance();
	
	private int peerId;
	private List<Neighbour> neighbors = new ArrayList<Neighbour>();
	// TODO think of resetting globalChunkfields entries when getting chunk !!
	private Map<File, GlobalChunkfield> globalChunkfields = new HashMap<File, GlobalChunkfield>();
	
	public ConnectionManager(int peerId) {
		this.peerId = peerId;
	}

	public void init() {
		neighbors = new ArrayList<Neighbour>();
		getNeighbors();
	}
	
	private void getNeighbors() {
		List<Peer> tempNeighbors = new ArrayList<Peer>();
		Peer p1 = null;
		while (tempNeighbors.size() < NetworkConstants.CANDIDATE_SIZE) {
			p1 = PM.getPeer(peerId);
			if (! tempNeighbors.contains(p1)) {
				tempNeighbors.add(p1);
			}
		}
		Collections.sort(tempNeighbors, new Comparator<Peer>() {
			@Override
			public int compare(Peer a, Peer b) {
				Peer thisPeer = PM.get(peerId);
				int result = (int) Math.signum(thisPeer.dist(a) - thisPeer.dist(b));
				if (result == 0) result = (int) Math.signum(a.id - b.id);
				return result;
			}
		});
		
		tempNeighbors = tempNeighbors.subList(0, NetworkConstants.N_OPT);
		
		// resets neighbors with chunkfields
		// TODO discuss about usefulness of keeping known chunkfields (fetched periodically anyway)
		neighbors = new ArrayList<Neighbour>();
		for (Peer p: tempNeighbors) {
			neighbors.add(new Neighbour(p.id));
		}
	}

	public void printNeighbors() {
		System.out.println(neighborsToString());
	}
	
	public String neighborsToString() {
		StringBuilder sb = new StringBuilder("[");
		Peer temPeer = null;
		for (Neighbour n: neighbors) {
			temPeer = PM.get(n.id);
			if (temPeer.isRunning()) {
				sb.append(n.id + ", ");
			}
		}
		return sb.substring(0, sb.length()-2)+"]";
	}
	
	/**
	 * returns the global chunkfield of all neighbors including the peers data passed
	 * as parameter.
	 * 
	 * @param file the file to which the global chunkfield correspond.
	 * @param peerId id of peer add to global chunkfield (usually, local peer)
	 * @param c the chunkfield of the peer with peerId
	 * @return the global chunkfield corresponding to file.
	 */
	public GlobalChunkfield getGlobalChunkfield(File file, int peerId, Chunkfield c) {
		GlobalChunkfield result = globalChunkfields.get(file);
		if (globalChunkfields.get(file) == null) {
			result = new GlobalChunkfield(peerId, c, file.chunkCount);
			for (Neighbour n : neighbors) {
				result.update(n.id, neighbors.get(n.id).getChunkfield(file));
			}
		}
		return result;
	}
	
	public void update(int peerId, File file, Chunkfield c) {
		for (Neighbour n: neighbors) {
			if (n.id == peerId) n.setChunkfield(file, c);
		}
	}

	/**
	 * reset global chunkfield for safe new global chunkfield computation.
	 * @param file target reference of chunkfield
	 */
	public void resetGlobalChunkfield(File file) {
		// entry for file in globalChunkfields will have to be recomputed
		globalChunkfields.put(file, null);
	}
	
	public void remove(int peerId) {
		neighbors.remove(peerId);
	}

	// message sending methods
	
	/**
	 * Sends the message to all memorized peers (in neighbors)
	 * 
	 * @param message
	 */
	public void broadcast(Message message) {
		for (Neighbour n : neighbors) {
			PM.get(n.id).enqueue(message);
		}
	}

	public void send(GetIndex getIndex) {
		PM.get(neighbors.iterator().next().id).enqueue(getIndex);
	}

	/**
	 * Sends request getChunk, only if finds some neighbor having the chunk.
	 * @param getChunk the request to send.
	 * @return true if a peer was found to send the request.
	 */
	public boolean send(GetChunk getChunk) {
		int ownerId = -1;
		// finds a neighbor owning the selected chunk
		for (Neighbour n : neighbors) {
			if (n.getChunkfield(getChunk.file()).hasChunk(getChunk.chunkId())) {
				ownerId = peerId;
				break;
			}
		}
		if (ownerId != -1) {
			PM.get(ownerId).enqueue(getChunk);
			return true;
		} else return false;
	}
	
	public void broadcastAll(IndexUpdate updateIndex) {
		// trick here !!! should rely on index dissemination system
		PM.broadcast(updateIndex);
	}
}

