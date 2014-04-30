package ch.epfl.p2pmapreduce.nodeCore.network;

import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;

/**
 * 
 * 
 * @author valerian
 *
 */
public interface IMessageSender {
	
	
	
	boolean send(GetChunkfield message, Neighbour receiver);
	boolean send(SendChunkfield message, Neighbour receiver);
	boolean send(GetChunk getChunk, Neighbour receiver);
	boolean send(SendChunk sendChunk, Neighbour receiver);
	// Should be removed with index messages done by miShell
	boolean send(SendIndex sendIndex, Neighbour receiver);
	// Should be removed with index messages done by miShell
	boolean send(GetIndex getIndex, Neighbour receiver);
}
