package ch.epfl.p2pmapreduce.nodeCore.network;

import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;

public class JxtaMessageSender implements IMessageSender{

	private JxtaCommunicator communicator;
	
	@Override
	public boolean send(GetChunkfield message, Neighbour receiver) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean send(SendChunkfield message, Neighbour receiver) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean send(GetChunk getChunk, Neighbour receiver) {

		
		
		return false;
	}

	@Override
	public boolean send(SendChunk sendChunk, Neighbour receiver) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean send(SendIndex sendIndex, Neighbour receiver) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean send(GetIndex getIndex, Neighbour receiver) {
		// TODO Auto-generated method stub
		return false;
	}

}
