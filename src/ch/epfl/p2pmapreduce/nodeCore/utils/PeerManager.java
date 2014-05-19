package ch.epfl.p2pmapreduce.nodeCore.utils;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.p2pmapreduce.nodeCore.messages.IndexUpdate;
import ch.epfl.p2pmapreduce.nodeCore.peer.Peer;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;

/**
 * To manage the peers
 *
 */
public enum PeerManager {
	PEERMANAGER;
	
	public static PeerManager getInstance() {
		return PEERMANAGER;
	}
	private ArrayList<Peer> peers = new ArrayList<Peer>();
	private int peerCount = 0;
	
	public Peer get(int peerId) {
		if (peerId < peers.size()) return peers.get(peerId);
		else return null;
	}
	
	/**
	 * returns a random peer different from the caller.
	 * 
	 * @param callerId the unique id
	 * @return random selected peer
	 */
	public Peer getPeer(int callerId) {
		if (peerCount <= 1) {
			return null;
		}
		List<Peer> alive = new ArrayList<Peer>();
		for (Peer p: peers) {
			if (p.isRunning()) alive.add(p);
		}
		int aliveCount = alive.size();
		int pId = (int)(Math.random() * (aliveCount-1));
		if (pId == callerId) pId ++;
		return alive.get(pId);
	}
	
	public Peer newPeer(String name) {
		Peer p = new Peer(name, peerCount);
		peers.add(p);
		peerCount ++;
		return p;
	}
	
	public void broadcast(IndexUpdate updateIndex) {
		for (Peer p : peers) {
			if (p.id != updateIndex.sender()) {
				p.enqueue(updateIndex);
			}
		}
	}
	
	public void start(int peerId) {
		if (peerId < peerCount) {
			peers.get(peerId).start();
		}
	}
	
	public void startAll() {
		for (Peer peer : peers) {
			peer.start();
		}
	}
	
	public void connect(int peerId) {
		if (peerId < peerCount) {
			peers.get(peerId).connect();
		}
	}
	
	public void killAll() {
		for (Peer peer : peers) {
			peer.kill();
		}
	}
	
	public void kill(int peerId) {
		if (peerId < peerCount) {
			peers.get(peerId).kill();
		}
	}

	public void neighbors(int id) {
		if (id < peerCount) {
			peers.get(id).neighbors();
		}
	}

	public void init() {
		for (Peer peer: peers) {
			peer.init();
		}
	}

	public void neighborsAll() {
		for (Peer peer: peers) {
			peer.neighbors();
		}
	}

	public void setVerbose(int peerId, boolean b) {
		if (peerId < peerCount) {
			peers.get(peerId).setVerbose(b);
		}
	}
	
	public void put(int peerId, String fileName, int chunkCount) {
		if (peerId < peerCount) {
			peers.get(peerId).put(new File(fileName, chunkCount));
		}
	}
}
