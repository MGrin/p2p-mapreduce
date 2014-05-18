package ch.epfl.p2pmapreduce.nodeCore.volume;


public class File {

	/* TODO determine persistence of metadata of files over disconnection
	 * will "isPeerResponsible" remain the same ?
	 */
	
	public final int chunkCount;
	public final String name;
	
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
	}
	
	/**
	 * create a new file that is a corresponding copy of the file parameter
	 * the uid of the new file is the same as the one of the parameter
	 * 
	 * @param f the file to copy
	 */
	public File(File f) {
		this(f.name, f.chunkCount);
		this.stabilized = f.stabilized;
	}
	
	@Override
	public int hashCode() {
		// arbitrary
		return name.hashCode();
	}
	
	public void stabilise() { stabilized = true; }
	
	public boolean isStabilized() { return stabilized; }
	
	public void setPeerResponsible() { isPeerResponsible = true; }
	
	/**
	 * Determines if the peer should be responsible for this file.
	 * @return true if peer is responsible or file is not stabilized
	 */
	public boolean isPeerResponsible() { return !stabilized || isPeerResponsible; }
	
	@Override
	public String toString() {
		return "(" + this.name + ", " + this.chunkCount + ")";
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof File)) return false;
		else {
			File that = (File) o;
			return this.name.equals(that.name);
		}
	}
}
