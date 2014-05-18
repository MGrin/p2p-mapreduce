package ch.epfl.p2pmapreduce.nodeCore.peer;

import java.util.List;

import ch.epfl.p2pmapreduce.nodeCore.messages.FileStabilized;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.NewFile;
import ch.epfl.p2pmapreduce.nodeCore.messages.RefreshIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;

public interface MessageBuilder {

	GetIndex getIndex();
	SendIndex sendIndex();
	
	GetChunkfield getChunkfield();
	SendChunkfield sendChunkfield();
	
	GetChunk getChunk(String fileName, int chunkId);
	SendChunk sendChunk(String fileName, int chunkId);
	
	List<GetChunk> get(String fileName);
	
	NewFile newFile(String fileName, int chunkCount);
	FileStabilized fileStabilized(String fName);
	
	RefreshIndex refreshIndex();
}
