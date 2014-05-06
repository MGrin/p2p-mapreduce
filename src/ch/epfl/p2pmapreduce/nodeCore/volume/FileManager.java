package ch.epfl.p2pmapreduce.nodeCore.volume;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileManager {

//	private Map<File, Chunkfield> files = new HashMap<File, Chunkfield>();
	private Index index = new Index();
	public final int peerId;
	
	public FileManager(int peerId) {
		this.peerId = peerId;
	}
	
	// typically called on index update (or put Instruction in simulation mode)
	/**
	 * Puts a file in the index.
	 * @param f the file to add.
	 * @return true if the file was not already present
	 */
	public boolean addFile(File f) {
		return index.put(f);
	}

	/**
	 * Loads, chunkify and creates file representation fo target osFile
	 * 
	 * 
	 * @param osFullPath
	 * @param dfsFullPath
	 * @return the file representation of the loaded file.
	 */
	public File loadFile(String osFullPath, String dfsFullPath) {
		// TODO Really load the file from os, duplicate in chunkfiles for local chunk storing
		
		return new File(dfsFullPath, 100);
	}
	
	public boolean rmFile(File f) {
		// TODO erase all chunkfiles stored in OS.
		return index.remove(f);
	}

	public File getFile(String fName) {
		return index.getFile(fName);
	}
	
	public Index getIndex() {
		return index.copy();
	}
	
	public Chunkfield getChunkfield(File f) {
		return new Chunkfield(index.getChunkfield(f));
	}
	
	/**
	 * For sending all chunkfields over network
	 * @return map of filesUid to chunkfields
	 */
	public Map<String, Chunkfield> getChunkfields() {
		return index.getChunkfields();
	}
	
	public void replaceIndex(Index newIndex) {
		
		List<File> oldFiles = new ArrayList<File>();
		for (File oldF: index.files()) {
			if (!newIndex.contains(oldF)) oldFiles.add(oldF);
		}
		// TODO rm oldfiles from filesystem
		
		for (File oldF: oldFiles) {
			index.remove(oldF);
		}
		
		for (File newF: newIndex.files()) {
			if (! index.contains(newF)) index.put(newF);
		}
	}


	public Set<File> getFiles() {
		return index.files();
	}


	public int filesCount() {
		return index.size();
	}
	
	public boolean containsChunk(String fName, int chunkId) {
		Chunkfield cf = index.getChunkfield(fName);
		if (cf != null) {
			return cf.hasChunk(chunkId);
		} else return false ;
	}

	// TODO add byte array as parameter for chunkfield data and store that data in os.
	public void addChunk(String fName, int chunkId/*, byte[] chunkData*/) {
		// TODO store chunkData
		index.putChunk(fName, chunkId);
	}

	public void stabilize(String fName) {
		index.stabilize(fName);
	}
	
	public void print(String message) {
		System.out.println("peer_" + peerId + ": " + message);
	}
}
