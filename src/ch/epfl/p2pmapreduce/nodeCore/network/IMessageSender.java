package ch.epfl.p2pmapreduce.nodeCore.network;

import net.jxta.document.Advertisement;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.RefreshIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;

/**
 * Interface for the message sender
 *
 */
public interface IMessageSender {
	
	boolean send(GetChunkfield message, Neighbour receiver);
	boolean send(SendChunkfield message, Neighbour receiver);
	boolean send(GetChunk getChunk, Neighbour receiver);
	boolean send(SendChunk sendChunk, Neighbour receiver);
	
	boolean send(SendIndex sendIndex, Neighbour receiver);
	boolean send(GetIndex getIndex, Neighbour receiver);
	
	boolean send(Advertisement adv);
	
	boolean send(RefreshIndex refreshIndex);
}
