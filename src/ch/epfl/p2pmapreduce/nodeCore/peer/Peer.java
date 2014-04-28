package ch.epfl.p2pmapreduce.nodeCore.peer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.epfl.p2pmapreduce.nodeCore.messages.FileStabilized;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.Message;
import ch.epfl.p2pmapreduce.nodeCore.messages.NewFile;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;
import ch.epfl.p2pmapreduce.nodeCore.network.SimConnectionManager;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;
import ch.epfl.p2pmapreduce.nodeCore.utils.PeerConstants;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;
import ch.epfl.p2pmapreduce.nodeCore.volume.FileManager;
import ch.epfl.p2pmapreduce.nodeCore.volume.GlobalChunkfield;


public class Peer implements Runnable, MessageBuilder{

	private String peerName;
	private Thread runner;
	
	
	public final int id;
	
	private StateManager state = new StateManager();
	private boolean running = false;
	
	private SimConnectionManager cManager;
	private MessageHandler messages;
	private FileManager fManager;
	
	private final int x;
	private final int y;
	private boolean verbose = false;
	
	public Peer(String name, int id) {
		peerName = name;
		this.id = id;
		this.fManager = new FileManager(id);
		x = (int) (Math.random() * NetworkConstants.AREA_SIZE);
		y = (int) (Math.random() * NetworkConstants.AREA_SIZE);
		runner = new Thread(this);
		cManager = new SimConnectionManager(this.id);
		messages = new MessageHandler(this, state, fManager, cManager);
		System.out.println("Hello world, I'm " + peerName + " with id " + id);
		
		
		verbose = id==0;
	}
	
	public void run() {
		running = true;
		print("running...");
		int waitTimeout = 0;
		PeerState previous = null;
		while (running) {
			if (previous != state.get()) print(state.get() + "...");
			previous = state.get();
			switch (state.get()) {
			case BOOTING:
				print("fetching neighbors");
				cManager.init();
				state.set(PeerState.GETINDEX);
				break;
			case GETINDEX:
				cManager.send(getIndex());
				state.set(PeerState.WAITINGINDEX);
				break;
			case WAITINGGLOBALCF :
				// TODO set timeout for continuing re-requesting global chunkfield
			case WAITINGCHUNKS :
				// TODO set timeout for continuing requesting chunks
			case WAITINGINDEX :
				// TODO set timeout for re-requesting index
			case WAITING :
				if (!messages.isEmpty()) {
					print("handling a message...");
					messages.handleMessage();
				}
				long ts = System.currentTimeMillis();
				if (ts%1000 == 0) {
					waitTimeout++;
					while (ts == System.currentTimeMillis());
				}
				if (waitTimeout > PeerConstants.WAIT_TIMEOUT) {
					waitTimeout = 0;
					state.set(PeerState.BUILDGLOBALCF);
				}
				break;
			case BUILDGLOBALCF :
				if (fManager.filesCount() == 0) {
					print("No files in index !");
					state.set(PeerState.WAITING);
				} else {
					cManager.broadcast(getChunkfield());
					state.set(PeerState.WAITINGGLOBALCF);
				}
				break;
			case CHECKGLOBALCF :
				int sentRequestCount = checkGlobalCF();
				print("sent " + sentRequestCount + " chunk requests");
				messages.addPendingChunkRequest(sentRequestCount);
				if (sentRequestCount > 0 ) state.set(PeerState.WAITINGCHUNKS);
				else state.set(PeerState.WAITING);
				break;
			case EXITING :
				running = false;
				break;
			default :
				err("unimplemented state : " + state.get());
				running = false;
				break;
			}
		}
		
		print("disconnected");
	}
	
	private void print(String message) {
		if (verbose) System.out.println(peerName + ": " + message);
	}
	
	private void err(String message) {
		System.err.println(peerName + ": " + message);
	}
	
	public void enqueue(Message m) {
		print("enqueud " + m);
		messages.enqueue(m);
	}
	
	public double dist(Peer that) {
		return Math.sqrt((that.x-this.x) * (that.x-this.x) + (that.y-this.y) * (that.y-this.y));
	}
	
	public boolean isRunning() {
		return running;
	}
	
	
	private void sleep(int sec) {
		if (running) {
			try {
				Thread.sleep(sec * 1000);
			} catch (InterruptedException e) {
				err("sleeping interrupted.");
			}
		}
	}
	
	// control methods
	
	public void start() {
		if (!running) {
			running = true;
			state.set(PeerState.BOOTING);
			runner = new Thread(this);
			runner.start();
		}
	}
	
	public void kill() {
		if (running) {
			print("killing...");
			state.set(PeerState.EXITING);
		}
	}
	
	public void connect() {
		if (running) {
			print("connecting...");
			cManager.init();
		}
	}
	
	public void neighbors() {
		if (running) {
			print(cManager.neighborsToString());
		}
	}

	public void init() {
		connect();
	}
	
	public void setVerbose(boolean v) {
		verbose = v;
	}
	
	/**
	 * Caller must guarantee that new file is completely new (unique id).
	 * this method will not overwrite oldVersions of a file. Must use rm first.
	 * @param f the file to put in RAIDFS
	 */
	public void put(File f) {
		fManager.createFile(f);
		cManager.broadcastAll(newFile(f.uid, f.name, f.chunkCount));
	}

	
	// state machine methods
	
	private int checkGlobalCF() {
		Map<File, List<Integer>> chunksToGet = new HashMap<File, List<Integer>>();
		GlobalChunkfield tempGC = null;
		List<Integer> tempLowChunks = null;
		List<Integer> notOwnedLowChunks = null;
		for (File f : fManager.getFiles()) {
			tempGC = cManager.getGlobalChunkfield(f, this.id, fManager.getChunkfield(f));
			print("current local chunkfield: " + fManager.getChunkfield(f));
			print("GlobalChunkfield of file " + f.uid + ": " + tempGC);
			tempLowChunks = tempGC.lowChunks();
			if (f.isPeerResponsible() && tempLowChunks.size() > 0) {
				// duplication required
				// selects only chunks that are not owned yet
				notOwnedLowChunks = new ArrayList<Integer>();
				for (Integer chunk : tempLowChunks) {
					if (! fManager.containsChunk(f.uid, chunk)) {
						notOwnedLowChunks.add(chunk);
					}
				}
				// do not put empty chunklist for a file
				if (notOwnedLowChunks.size() > 0) {
					chunksToGet.put(f, notOwnedLowChunks);
				}
			}
			if (! f.isStabilized() && tempLowChunks.size() == 0) {
				// enough duplication archieved, file can be stabilized
				f.stabilise();
				cManager.broadcastAll(fileStabilized(f.uid));
			}
			if (tempGC.underMinChunks().size() == 0) {
				// peer will become responsible for the file
				f.setPeerResponsible();
			}
		}
		// send request for chunks to get
		int requestCounter = 0;
		while (requestCounter < NetworkConstants.CHUNK_REQUEST_COUNT
					&& requestRandomChunk(chunksToGet)) {
			requestCounter ++;
		}
		
		return requestCounter;
	}
	/*
	 * Takes a random chunk from the chunksToGet map,
	 * sends a request and removes it from the map.
	 * The owning file is removed from the map if no chunks are left for this
	 * file.
	 * 
	 * returns false if there is no chunk to request, true otherwise.
	 */
	private boolean requestRandomChunk(Map<File, List<Integer>> chunksToGet) {
		File file = null;
		Integer chunkId = null;
		/*
		 * selects a random chunk to get and tries to send it until
		 * cManager find a neighbor owning this piece or the 
		 * chunksToGet map gets empty.
		 */
		do {
			file = null;
			if (chunksToGet.size() == 0) return false;
			int fileIndex = (int) (Math.random() * chunksToGet.size());
			Iterator<File> filesIterator = chunksToGet.keySet().iterator();
			for (int i = 0; i <= fileIndex && filesIterator.hasNext(); i++) {
				file = filesIterator.next();
			}
			List<Integer> chunksOfFile = chunksToGet.get(file);
			int chunkIndex = (int) (Math.random() * chunksOfFile.size());
			chunkId = chunksOfFile.remove(chunkIndex);
			if (chunksOfFile.size() == 0) chunksToGet.remove(file);
		} while (!cManager.send(getChunk(file.uid, chunkId)));
		
		return true;
	}
	
	// MessageBuilder methods
	@Override
	public GetIndex getIndex() {
		print("creating getIndex message");
		return new GetIndex(this.id);
	}

	@Override
	public SendIndex sendIndex() {
		print("creating sendIndex message");
		return new SendIndex(this.id, fManager.getIndex());
	}

	@Override
	public GetChunkfield getChunkfield() {
		print("creating getChunkfield message");
		return new GetChunkfield(this.id);
	}

	@Override
	public SendChunkfield sendChunkfield() {
		print("creating sendChunkfield message");
		return new SendChunkfield(this.id, fManager.getChunkfields());
	}

	@Override
	public NewFile newFile(int fileId, String fileName, int chunkCount) {
		print("creating newFile message");
		return new NewFile(this.id, fileId, fileName, chunkCount);
	}

	@Override
	public GetChunk getChunk(int fileId, int chunkId) {
		print("creating getChunk message for file " + fileId + ", chunk " + chunkId);
		return new GetChunk(this.id, fileId, chunkId);
	}

	@Override
	public SendChunk sendChunk(int fileId, int chunkId) {
		print("creating sendChunk message for file " + fileId + ", chunk " + chunkId);
		return new SendChunk(this.id, fileId, chunkId);
	}

	@Override
	public FileStabilized fileStabilized(int fileId) {
		print("creating fileStablilized message for file " + fileId);
		return new FileStabilized(this.id, fileId);
	}

}
