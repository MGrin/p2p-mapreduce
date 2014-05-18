package ch.epfl.p2pmapreduce.nodeCore.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.p2pmapreduce.advertisement.PutIndexAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.RmIndexAdvertisement;
import ch.epfl.p2pmapreduce.networkCore.JxtaCommunicator;
import ch.epfl.p2pmapreduce.nodeCore.messages.ExpectedSend;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.RefreshIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;
import ch.epfl.p2pmapreduce.nodeCore.peer.MessageExpecter;
import ch.epfl.p2pmapreduce.nodeCore.peer.MessageHandler;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;
import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;
import ch.epfl.p2pmapreduce.nodeCore.volume.GlobalChunkfield;


public class ConnectionManager {

	private final INeighbourDiscoverer nD ;
	private IMessageSender sender ;


	private MessageExpecter expecter = MessageExpecter.INSTANCE;

	private final JxtaCommunicator communicator;

	private List<Neighbour> neighbors = new ArrayList<Neighbour>();
	// TODO think of resetting globalChunkfields entries when getting chunk !!
	private Map<File, GlobalChunkfield> globalChunkfields = new HashMap<File, GlobalChunkfield>();

	public ConnectionManager(String peerName) {
		this.communicator = new JxtaCommunicator(peerName, NetworkConstants.generatePortNumber());
		this.nD = communicator.new JxtaNeighbourDiscoverer();
	}

	public boolean init(MessageHandler handler) {

		boolean couldStart = communicator.start();

		if(couldStart) {

			initMessageListening(handler);

			neighbors = nD.getNeighbors();

			if(neighbors == null || neighbors.size() == 0) return false;
			// cuts the list when too many peers
			for (int i = NetworkConstants.N_OPT; i < neighbors.size(); i++) {
				neighbors.remove(i);
			}
		} else {
			System.err.println("Could not start JXTA network.. Exiting");
			System.exit(-1);
		}

		return true;
	}

	public void stop() {
		communicator.stop();
	}

	public void initMessageListening(MessageHandler handler) {

		communicator.initMessageListener(handler, communicator.netPeerGroup);
		this.sender = new JxtaMessageSender(communicator);
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
				result.update(n.id, neighbors.get(n.id).getChunkfield(file.name));
			}
		}
		return result;
	}

	public void update(int peerId, File file, Chunkfield c) {
		for (Neighbour n: neighbors) {
			System.out.println("neighbour " + n + " has chunkfield " + c + " for file " + file);
			if (n.id == peerId) n.setChunkfield(file.name, c);
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

	// fetches a list of fresh neighbors and replace neigbors with peerID in peerIds
	public void replaceNeighbors(List<Integer> peerIds) {
		List<Neighbour> freshNeighbors = nD.getNeighbors();
		List<Neighbour> partingNeighbors = removeNeighbors(peerIds);
		freshNeighbors.removeAll(partingNeighbors);
		for (Neighbour n : freshNeighbors) {
			if (neighbors.size() < NetworkConstants.N_OPT && !neighbors.contains(n)) {
				neighbors.add(n);
			}
		}

	}

	private List<Neighbour> removeNeighbors(List<Integer> peerIds) {
		List<Neighbour> stayingPeers = new ArrayList<Neighbour>();
		List<Neighbour> quittingPeers = new ArrayList<Neighbour>();
		for (Neighbour n : neighbors) {
			if (!peerIds.contains(n.id)) stayingPeers.add(n);
			else quittingPeers.add(n);
		}
		neighbors = stayingPeers;
		return quittingPeers;
	}
	// message sending methods

	/**
	 * Sends request getChunk, only if finds some neighbor having the chunk.
	 * @param getChunk the request to send.
	 * @return true if a peer was found to send the request.
	 */
	public boolean send(GetChunk getChunk) {
		Neighbour owner = null;
		// finds a neighbor owning the selected chunk
		for (Neighbour n : neighbors) {
			if (n.getChunkfield(getChunk.fName()).hasChunk(getChunk.chunkId())) {
				owner = n;
				break;
			}
		}
		if (owner != null) {
			expecter.expect(new ExpectedSend.Chunk(owner.id));
			return sender.send(getChunk, owner);
		} else return false;
	}

	public boolean send(SendChunk sendChunk, int receiverId) {
		Neighbour receiver = new Neighbour(receiverId);
		if (receiver != null) {
			return sender.send(sendChunk, receiver);
		} else return false;

	}

	public boolean send(SendChunkfield sendChunkfield, int receiverId) {
		Neighbour receiver = new Neighbour(receiverId);
		if (receiver != null) {
			return sender.send(sendChunkfield, receiver);
		} else return false;
	}

	public void broadcast(GetChunkfield getChunkfield) {

		for (Neighbour n: neighbors) {
			expecter.expect(new ExpectedSend.Chunkfield(n.id));
			sender.send(getChunkfield, n);
		}
	}

	public boolean send(SendIndex sendIndex, int receiverId) {
		System.out.println("receiver id is " + receiverId);
		Neighbour receiver = new Neighbour(receiverId);
		System.out.println("receiver for index will be " + receiver);
		if (receiver != null) {
			return sender.send(sendIndex, receiver);
		} else return false;
	}

	public boolean send(GetIndex getIndex) {
		expecter.expect(new ExpectedSend.Index(neighbors.get(0).id));
		return sender.send(getIndex, neighbors.get(0));
	}

	public void send(RefreshIndex refreshIndex) {
		// TODO add method in IMessageSender
		// sender.send(refreshIndex);
	}

	public void send(RmIndexAdvertisement rmAdvertisement) {
		sender.send(rmAdvertisement);
	}

	public void send(PutIndexAdvertisement putAdvertisement) {
		sender.send(putAdvertisement);
	}



	// utilities

//	private Neighbour getFromId(int neighbourId) {
//
//		System.out.println("getting neighbour for id " + neighbourId);
//		System.out.println(neighbors.size() + " neighbours discovered so far");
//
//		for (Neighbour n: neighbors) {
//			System.out.println("neighbours id is " + n.id);
//			if (n.id == neighbourId) return n;
//		}
//		return null;
//	}

	public String neighborsToString() {
		StringBuilder sb = new StringBuilder("[");
		for (Neighbour n: neighbors) {
			sb.append(n.id + ", ");
		}
		return sb.substring(0, sb.length()-2)+"]";
	}

	public void initIndexUpdateDiscovery(MessageHandler handler) {
		communicator.initIndexUpdateDiscovery(handler);
		
		
	}

	public int neighborsCount() {
		if (neighbors == null) {
			return 0;
		} else {
			return neighbors.size();
		}
	}
}

