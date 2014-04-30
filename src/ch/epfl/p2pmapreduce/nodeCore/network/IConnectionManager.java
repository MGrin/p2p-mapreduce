package ch.epfl.p2pmapreduce.nodeCore.network;

import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.Message;

public interface IConnectionManager {
	
	public void init();

	public void stop();
	
	// mesage sending methods
	
	public void  broadcast(Message message);
		
	public void send(GetIndex getIndex);
	
	public boolean send(GetChunk getChunk);
}
