package ch.epfl.p2pmapreduce.nodeCore.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.p2pmapreduce.advertisement.PutIndexAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.RmIndexAdvertisement;
import ch.epfl.p2pmapreduce.networkCore.JxtaCommunicator;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;
import ch.epfl.p2pmapreduce.nodeCore.peer.MessageHandler;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;
import ch.epfl.p2pmapreduce.nodeCore.volume.Chunkfield;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;
import ch.epfl.p2pmapreduce.nodeCore.volume.GlobalChunkfield;


public class ConnectionManager {

	private final INeighbourDiscoverer nD ;
	private final IMessageSender sender ;

	private final JxtaCommunicator communicator;

	private int peerId;
	private List<Neighbour> neighbors = new ArrayList<Neighbour>();
	// TODO think of resetting globalChunkfields entries when getting chunk !!
	private Map<File, GlobalChunkfield> globalChunkfields = new HashMap<File, GlobalChunkfield>();

	public ConnectionManager(int peerId) {
		this.peerId = peerId;
		this.communicator = new JxtaCommunicator("Peer" + peerId, NetworkConstants.generatePortNumber());
		this.nD = communicator.new JxtaNeighbourDiscoverer();
		this.sender = new JxtaMessageSender(communicator);
	}

	public void init() {

		boolean couldStart = communicator.start();

		if(couldStart) {
			neighbors = nD.getNeighbors();
			
		} else {
			System.err.println("Could not start JXTA network.. Exiting");
			System.exit(-1);
		}
	}

	public void stop() {
		// TODO Auto-generated method stub

	}
	
	public void initMessageListening(MessageHandler handler) {
		
		communicator.initMessageListener(handler, communicator.netPeerGroup);
	
		
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

	public void remove(int peerId) {
		neighbors.remove(peerId);
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
			return sender.send(getChunk, owner);
		} else return false;
	}

	public boolean send(SendChunk sendChunk, int receiverId) {
		Neighbour receiver = getFromId(receiverId);
		if (receiver != null) {
			return sender.send(sendChunk, receiver);
		} else return false;

	}

	public boolean send(SendChunkfield sendChunkfield, int receiverId) {
		Neighbour receiver = getFromId(receiverId);
		if (receiver != null) {
			return sender.send(sendChunkfield, receiver);
		} else return false;
	}

	public void broadcast(GetChunkfield getChunkfield) {

		for (Neighbour n: neighbors) {
			sender.send(getChunkfield, n);
		}
	}

	// temporary, until index messages gestion in miShell is integrated 
	public boolean send(SendIndex sendIndex, int receiverId) {
		Neighbour receiver = getFromId(receiverId);
		if (receiver != null) {
			return sender.send(sendIndex, receiver);
		} else return false;
	}

	// temporary, until index messages gestion in miShell is integrated
	public boolean send(GetIndex getIndex) {
		return sender.send(getIndex, neighbors.get(0));
	}
	
	public void send(RmIndexAdvertisement rmAdvertisement) {
		sender.send(rmAdvertisement);
	}
	
	public void send(PutIndexAdvertisement putAdvertisement) {
		sender.send(putAdvertisement);
	}

	// utilities

	private Neighbour getFromId(int neighbourId) {
		for (Neighbour n: neighbors) {
			if (n.id == neighbourId) return n;
		}
		return null;
	}

	public String neighborsToString() {
		StringBuilder sb = new StringBuilder("[");
		for (Neighbour n: neighbors) {
			sb.append(n.id + ", ");
		}
		return sb.substring(0, sb.length()-2)+"]";
	}
}

