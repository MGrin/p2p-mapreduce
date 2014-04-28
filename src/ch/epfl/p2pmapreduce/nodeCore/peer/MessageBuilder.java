package ch.epfl.p2pmapreduce.nodeCore.peer;

import ch.epfl.p2pmapreduce.nodeCore.messages.FileStabilized;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.NewFile;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;

public interface MessageBuilder {

	GetIndex getIndex();
	SendIndex sendIndex();
	
	GetChunkfield getChunkfield();
	SendChunkfield sendChunkfield();
	
	GetChunk getChunk(int fileId, int chunkId);
	SendChunk sendChunk(int fileId, int chunkId);
	
	NewFile newFile(int fileId, String fileName, int chunkCount);
	FileStabilized fileStabilized(int fileId);
}
