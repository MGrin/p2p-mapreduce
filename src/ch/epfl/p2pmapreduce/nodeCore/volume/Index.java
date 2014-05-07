package ch.epfl.p2pmapreduce.nodeCore.volume;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Index {
	
	private HashMap<File, Chunkfield> files = new HashMap<File, Chunkfield>();
	
	public Index() {
		
	}
	
	// creates index initialized from files with empty chunkfields
	private Index(List<File> files) {
		for (File f : files) {
			this.files.put(f, new Chunkfield(f));
		}
	}
	
	public Index copy() {
		List<File> copied = new ArrayList<File>();
		for (File f: files.keySet()) {
			copied.add(new File(f));
		}
		return new Index(copied);
	}
	
	public List<File> lsUnstab() {
		List<File> unstab = new ArrayList<File>();
		for (File f: files.keySet()) {
			if (!f.isStabilized()) {
				unstab.add(f);
			}
		}
		
		return unstab;
	}
	
	public boolean remove(File f) {
		boolean existF = files.containsKey(f);
		files.remove(f);

		// TODO: push notif rmIndex
		return existF;
	}
	
	public boolean put(File f) {
		if (!files.containsKey(f)) {
			files.put(f, new Chunkfield(f));
			return true;
		} else return false;
		//TODO: push notif putIndex done by peer
	}
	
	public boolean contains(File f) {
		return files.containsKey(f);
	}
	
	public Set<File> files() {
		return files.keySet();
	}

	public File getFile(String fName) {
		for (File f : files.keySet()) {
			if (f.name.equals(fName)) return f;
		}
		return null;
	}

	public void stabilize(String fName) {
		File toStabilize = getFile(fName);
		if (toStabilize != null) {
			toStabilize.stabilise();
		}
	}

	public Chunkfield getChunkfield(File f) {
		return files.get(f);
	}
	public Chunkfield getChunkfield(String fName) {
		return files.get(getFile(fName));
	}

	public Map<File, Chunkfield> filesWithChunkfields() {
		return files;
	}

	public Map<String, Chunkfield> getChunkfields() {
		// TODO check implementation/ utility after reconsidering use of uID
		Map<String, Chunkfield> result = new HashMap<String, Chunkfield>();
		for (File f : files.keySet()) {
			Chunkfield temp = files.get(f);
			if (! temp.isEmpty()) {
				result.put(f.name, new Chunkfield(files.get(f)));
			}
		}
		return result;
	}

	public int size() {
		return files.size();
	}

	public void putChunk(String fName, int chunkId) {
		files.get(getFile(fName)).putChunk(chunkId);
	}

	public void dropChunk(String fName, int chunkId) {
		files.get(getFile(fName)).dropChunk(chunkId);
	}
}
