package ch.epfl.p2pmapreduce.nodeCore.messages;
/**
 * Interface for the Message Receiver.
 * implemented by MessageHandler
 */
public interface MessageReceiver {
	
	void receive(GetChunkfield message);
	void receive(SendChunkfield message);
	void receive(GetIndex message);
	void receive(SendIndex sendIndex);
	void receive(NewFile updateIndex);
	void receive(FileRemoved updateIndex);
	void receive(GetChunk getChunk);
	void receive(SendChunk sendChunk);
	void receive(FileStabilized fileSabilized);
}
