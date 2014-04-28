package ch.epfl.p2pmapreduce.nodeCore.volume;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileManager {

	private Map<File, Chunkfield> files = new HashMap<File, Chunkfield>();
	private Index index = new Index();
	public final int peerId;
	
	public FileManager(int peerId) {
		this.peerId = peerId;
	}
	
	// typically called on index update
	/**
	 * Puts a file in the index.
	 * @param f the file to add.
	 * @return true if the file was not already present
	 */
	public boolean addFile(File f) {
		if (!files.containsKey(f)) {
			files.put(f, new Chunkfield(f));
			index.put(f);
			return true;
		} else return false;
		
	}
	
	// typically called on put instruction
	
	/*
	 *  NOTE be careful, really important, not kidding at all !
	 *  files and index gets same file reference. Thus modifying a file in
	 *  one of both will modify in the other one as well.
	 */
	public void createFile(File f) {
		if (!files.containsKey(f)) {
			files.put(f, new Chunkfield(f, true));
			// triggering index update done in peer
			index.put(f);
		}
	}
	
	public void rmFile(File f) {
		files.remove(f);
	}

	public File getFile(int fileUid) {
		return index.getFile(fileUid);
	}
	
	public Index getIndex() {
		return index.copy();
	}
	
	public Chunkfield getChunkfield(File f) {
		return new Chunkfield(files.get(f));
	}
	
	/**
	 * For sending all chunkfields over network
	 * @return
	 */
	public Map<Integer, Chunkfield> getChunkfields() {
		Map<Integer, Chunkfield> result = new HashMap<Integer, Chunkfield>();
		for (File f : files.keySet()) {
			Chunkfield temp = files.get(f);
			if (! temp.isEmpty()) {
				result.put(f.uid, new Chunkfield(files.get(f)));
			}
		}
		return result;
	}
	
	public void replaceIndex(Index newIndex) {
		Map<File, Chunkfield> newFiles = new HashMap<File, Chunkfield>();
		
		
		for (File f : files.keySet()) {
			if (newIndex.contains(f)) {
				// keep old files
				newFiles.put(f, files.get(f));
			} else {
				// put new files
				newFiles.put(f, new Chunkfield(f, false));
			}
			
		}
		
		/*
		 * For concrete implementation :
		 * remove stored unused pieces from disk
		 */
		
		// rm unused files
		files = newFiles;
		index = newIndex;
	}


	public Set<File> getFiles() {
		return files.keySet();
	}


	public int filesCount() {
		return files.size();
	}
	
	public boolean containsChunk(int fileId, int chunkId) {
		Chunkfield cf = files.get(fileId);
		if (cf != null) {
			return cf.hasChunk(chunkId);
		} else return false ;
	}

	public void addChunk(int fileId, int chunkId) {
		files.get(index.getFile(fileId)).putChunk(chunkId);
	}

	public void stabilize(int file) {
		// as long as index and files rely on same reference of file, modifying index only is enough.
		index.stabilize(file);
	}
	
	public void print(String message) {
		System.out.println("peer_" + peerId + ": " + message);
	}
}
