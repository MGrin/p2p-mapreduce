package ch.epfl.p2pmapreduce.nodeCore.volume;

import java.util.ArrayList;
import java.util.List;

public class Index {
	
	private List<File> files = new ArrayList<File>();
	
	public Index() {
		
	}
	
	private Index(List<File> files) {
		this.files = files;
	}
	
	public Index copy() {
		List<File> copied = new ArrayList<File>();
		for (File f: files) {
			copied.add(new File(f));
		}
		return new Index(copied);
	}
	
	public List<File> lsUnstab() {
		List<File> unstab = new ArrayList<File>();
		for (File f: files) {
			if (!f.isStabilized()) {
				unstab.add(f);
			}
		}
		
		return unstab;
	}
	
	public void rm(File f) {
		files.remove(f);
		// TODO: push notif rmIndex
	}
	
	public void put(File f) {
		files.add(f);
		//TODO: push notif putIndex done by peer
	}
	
	public boolean contains(File f) {
		return files.contains(f);
	}
	
	public List<File> files() {
		return new ArrayList<File>(files);
	}

	public File getFile(int fileUid) {
		for (File f : files) {
			if (f.uid == fileUid) return f;
		}
		return null;
	}

	public void stabilize(int fileUid) {
		File toStabilize = getFile(fileUid);
		if (toStabilize != null) {
			toStabilize.stabilise();
		}
	}

}
