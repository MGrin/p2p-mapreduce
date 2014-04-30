package ch.epfl.p2pmapreduce.nodeCore.volume;

import ch.epfl.p2pmapreduce.nodeCore.utils.UidGenerator;

public class File {

	/* TODO determine persistence of metadata of files over disconnection
	 * will "isPeerResponsible" remain the same ?
	 */
	
	public final int chunkCount;
	public final String name;
	public final int uid;
	
	private boolean stabilized;
	private boolean isPeerResponsible = false;
	
	/**
	 * create a new file with auto-generated uid
	 * 
	 * @param name file name
	 * @param chunkCount file chunkcount
	 */
	public File(String name, int chunkCount) {
		this.chunkCount = chunkCount;
		this.name = name;
		this.uid = UidGenerator.freshId();
	}
	
	/**
	 * create a new file with provided uid
	 * 
	 * @param name
	 * @param chunkCount
	 * @param uid
	 */
	public File(String name, int chunkCount, int uid) {
		this.chunkCount = chunkCount;
		this.name = name;
		this.uid = uid;
		stabilized = false;
	}
	
	/**
	 * create a new file that is a corresponding copy of the file parameter
	 * the uid of the new file is the same as the one of the parameter
	 * 
	 * @param f the file to copy
	 */
	public File(File f) {
		this(f.name, f.chunkCount, f.uid);
		this.stabilized = f.stabilized;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof File)) return false;
		else {
			File that = (File) o;
			return
					this.uid == that.uid
					&& this.chunkCount == that.chunkCount
					&& this.name.equals(that.name);
		}
	}
	
	@Override
	public int hashCode() {
		// arbitrary
		return name.hashCode() + chunkCount - uid;
	}
	
	public void stabilise() { stabilized = true; }
	
	public boolean isStabilized() { return stabilized; }
	
	public void setPeerResponsible() { isPeerResponsible = true; }
	
	/**
	 * Determines if the peer should be responsible for this file.
	 * @return true if peer is responsible or file is not stabilized
	 */
	public boolean isPeerResponsible() { return !stabilized || isPeerResponsible; }
}