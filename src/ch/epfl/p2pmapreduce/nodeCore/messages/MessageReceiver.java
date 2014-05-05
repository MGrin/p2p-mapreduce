package ch.epfl.p2pmapreduce.nodeCore.messages;

public interface MessageReceiver {
	
	void receive(GetChunkfield message);
	void receive(SendChunkfield message);
	void receive(GetIndex message);
	void receive(SendIndex sendIndex);
	void receive(NewFile updateIndex);
	void receive(GetChunk getChunk);
	void receive(SendChunk sendChunk);
	void receive(FileStabilized fileSabilized);
}
