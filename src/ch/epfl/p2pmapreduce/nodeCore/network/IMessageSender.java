package ch.epfl.p2pmapreduce.nodeCore.network;

import ch.epfl.p2pmapreduce.advertisement.PutIndexAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.RmIndexAdvertisement;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.RefreshIndex;
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
	
	boolean send(SendIndex sendIndex, Neighbour receiver);
	boolean send(GetIndex getIndex, Neighbour receiver);
	
	boolean send(PutIndexAdvertisement putIndex);
	boolean send(RmIndexAdvertisement rmIndex);
	
	boolean send(RefreshIndex refreshIndex);
}
