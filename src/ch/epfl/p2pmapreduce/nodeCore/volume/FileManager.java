package ch.epfl.p2pmapreduce.nodeCore.volume;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import ch.epfl.p2pmapreduce.nodeCore.utils.FileManagerConstants;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;

/**
 * 
 * Manages known files and chunks.
 * This class provides to the rest of the program access to stored data
 * (retrieving, storing) and chunkfield extraction.
 * 
 * 
 * @author vtpittet
 *
 */
public class FileManager {

	private final static String CHUNK_EXT = ".chk";
	
//	private Map<File, Chunkfield> files = new HashMap<File, Chunkfield>();
	private Index index = new Index();
	public final int peerId;
	
	// will restore file to os when all chunks are collected
	private HashMap<File, String> pendingGet = new HashMap<File, String>();
	
	public FileManager(int peerId) {
		this.peerId = peerId;
	}
	
	// typically called on index update (or put Instruction in simulation mode)
	/**
	 * Puts a file in the index.
	 * @param f the file to add.
	 * @return true if the file was not already present
	 */
	public boolean addFile(File f, boolean isOwned) {
		if (! isOwned) {
			java.io.File fileDir = new java.io.File(FileManagerConstants.DFS_DIR + java.io.File.separator + f.name);
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
		}
		
		return index.put(f, isOwned);
	}

	/**
	 * Loads, chunkify and creates file representation fo target osFile
	 * Note that chunksize is only a lower bound on chunkSize. In order to implement map/reduce
	 * it is required to keep lines in file intact. Thus a chunksize may vary.
	 * 
	 * @param osFullPath full file to load path using correct path separator.
	 * @param dfsFullPath internal representation, better use dot separator rather than actual file separator.
	 * @return the file representation of the loaded file, null if the file was not existing.
	 */
	public File loadFile(String osFullPath, String dfsFullPath) {
		// TODO Really load the file from os, duplicate in chunkfiles for local chunk storing
		String sep = java.io.File.separator;
		int chunkCount = 0;
		try {
			FileReader fr = new FileReader(osFullPath);
			BufferedReader in = new BufferedReader(fr);
			String destDir = /*System.getProperty("user.home") + sep +*/ FileManagerConstants.DFS_DIR + sep + dfsFullPath; // TODO Check path and rights
			java.io.File fileDir = new java.io.File(destDir);
			//check if we succeded in creating the dir
			if(fileDir.exists() || fileDir.mkdirs()){
				BufferedWriter out = null;
				String line = null;
				int chunkSize = -1;
				while ((line = in.readLine()) != null) {
					if (chunkSize == -1) {
						java.io.File dfsFile = new java.io.File(fileDir, chunkCount + CHUNK_EXT);
						
						out = new BufferedWriter(new FileWriter (dfsFile));
						chunkSize = 0;
					}
					out.write(line);
					out.newLine();
					chunkSize += line.length();
					if (chunkSize >= NetworkConstants.CHUNK_SIZE) {
						chunkCount ++;
						out.close();
						chunkSize = -1;
					}
				}
				// detects last unfinised chunk (smaller size)
				if (chunkCount < NetworkConstants.CHUNK_SIZE) {
					chunkCount ++;
					if(out != null){
						out.close();
					}else{
						System.err.println("the file " + osFullPath + " is empty");
					}

				}
			}else {
				System.err.println("the folder " + destDir + " couldn't be created"); 
				return null;
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("file " + osFullPath + " not found.");
			return null;
		} catch (IOException e) {
			System.err.println("something went wrong loading " + osFullPath + ".");
			clean(dfsFullPath);
			e.printStackTrace();
			return null;
		}
		
		System.out.println("--------- FILE LOADED WITH CHUNKCOUNT " + chunkCount);
		return new File(dfsFullPath, chunkCount);
	}
	
	public boolean rmFile(File f) {
		// TODO malicious injection possible here : filename = ../../../ => will remove the whole filesystem
				
		if (index.remove(f)) {
			clean(f.name);
			return true;
		} else return false;
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
		
		for (File oldF: oldFiles) {
			rmFile(oldF);
		}
		
		for (File newF: newIndex.files()) {
			if (! index.contains(newF)) this.addFile(newF, false);
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

	/**
	 * Stores chunkData on FS.
	 * @param fName full dfs path of file, using fake separator like dot.
	 * @param chunkId
	 * @param chunkData
	 */
	public void addChunk(String fName, int chunkId, byte[] chunkData) {
		try {
			FileWriter out = new FileWriter (new java.io.File(getChunkDir(fName), chunkId + CHUNK_EXT));
			char[] charData = new char[(int) Math.ceil(chunkData.length/2.0)];
			Arrays.fill(charData, (char) 0);
			for (int i = 0; i < chunkData.length; i++) {
				int data = chunkData[i];
				if (i%2 == 0) data = data << 8; // TODO Check correct implementation about bytes (last odd byte !!!!)
				charData[i/2] += data;
			}
			out.write(charData);
			out.close();
			index.putChunk(fName, chunkId);
			File target = getFile(fName);
			if (pendingGet.containsKey(target)
					&& index.hasAllChunks(target)) {
				restore(target);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not write chunk " + chunkId + " for file " + fName + ".");
		}
	}
	
	public byte[] getChunkData(String fName, int chunkId) {
		byte [] result = null;
		try {
			FileReader in = new FileReader (new java.io.File(getChunkDir(fName), chunkId + CHUNK_EXT));
			List<Integer> read = new ArrayList<Integer>();
			int r = 0;
			while ((r = in.read()) != -1) read.add(r);
			in.close();
			result = new byte[read.size()*2];
			for (int i = 0; i < read.size(); i++) {
				result[i*2] = (byte) ((read.get(i) >> 8) & 0xFF);
				result[i*2+1] = (byte) (read.get(i) & 0xFF);
			}
		} catch (FileNotFoundException e) {
			System.err.println("unable to load chunk " + chunkId + " for file " + fName + ".");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("unable to load chunk " + chunkId + " for file " + fName + ".");
			e.printStackTrace();
		}
		return result;
	}

	public void stabilize(String fName) {
		index.stabilize(fName);
	}
	
	public void addPendingGet(String fName, String osFullPath) {
		File target = getFile(fName);
		if (target == null) return;
		pendingGet.put(getFile(fName), osFullPath);
		if (index.hasAllChunks(getFile(fName))) {
			restore(target);
		}
	}
	
	public void print(String message) {
		System.out.println("peer_" + peerId + ": " + message);
	}
	
	public boolean containsFile(String fName) {
		return getFile(fName) != null;
	}
	
	// utility methods
	
	/*
	 * provides mapping between dfsFull path (full filename) and file system directory
	 * storing the chunkfiles.
	 */
	private java.io.File getChunkDir(String dfsFullPath) {
		//TODO: Get back to this!!
		return new java.io.File(/*System.getProperty("user.home") + java.io.File.separator +*/ FileManagerConstants.DFS_DIR + java.io.File.separator + dfsFullPath);
	}
	
	/*
	 * remove all chunks files associated to a filename and the storing directory
	 */
	private void clean(String dfsFullPath) {
		try {
			FileUtils.deleteDirectory(getChunkDir(dfsFullPath));
		} catch (IOException e) {
			System.err.println("cannot remove chunkfiles of file " + dfsFullPath);
		}
	}
	
	private void restore(File file) {
		String osFullPath = pendingGet.remove(file);
		java.io.File destFile = new java.io.File(osFullPath);
		// TODO create parent folders if don't exist
		if (!destFile.exists()) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter (destFile));
				BufferedReader in = null;
				for (int i = 0; i < file.chunkCount; i++) {
					in = new BufferedReader(new FileReader(new java.io.File(file.name, i + CHUNK_EXT)));
					String line = null;
					while ((line = in.readLine()) != null) {
						out.write(line);
						out.newLine();
					}
					in.close();
				}
				out.close();
			} catch (IOException e) {
				System.err.println("Something went wrong writing in " + osFullPath);
			}
		}
	}
}
