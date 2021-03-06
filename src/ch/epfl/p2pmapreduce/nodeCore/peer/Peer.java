package ch.epfl.p2pmapreduce.nodeCore.peer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.jxta.document.AdvertisementFactory;
import net.jxta.id.ID;
import ch.epfl.p2pmapreduce.advertisement.PutIndexAdvertisement;
import ch.epfl.p2pmapreduce.advertisement.RmIndexAdvertisement;
import ch.epfl.p2pmapreduce.nodeCore.messages.FileStabilized;
import ch.epfl.p2pmapreduce.nodeCore.messages.FileStabilizedAdvertisement;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.Message;
import ch.epfl.p2pmapreduce.nodeCore.messages.NewFile;
import ch.epfl.p2pmapreduce.nodeCore.messages.RefreshIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;
import ch.epfl.p2pmapreduce.nodeCore.network.ConnectionManager;
import ch.epfl.p2pmapreduce.nodeCore.utils.NetworkConstants;
import ch.epfl.p2pmapreduce.nodeCore.utils.PeerConstants;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;
import ch.epfl.p2pmapreduce.nodeCore.volume.FileManager;
import ch.epfl.p2pmapreduce.nodeCore.volume.GlobalChunkfield;


/**
 * The class peer controls the main behavior of program.
 * It is responsible of choosing which message to send (except response message)
 * 
 * 
 * @author vtpittet
 *
 */

public class Peer implements Runnable, MessageBuilder{

	private String peerName;
	private Thread runner;

	public final int id;

	private StateManager state = new StateManager();
	private boolean running = false;

	private ConnectionManager cManager;
	private MessageHandler messages;
	private FileManager fManager;

	private final int x;
	private final int y;
	private boolean verbose = true;
	
	private boolean isReadyForAction = false;

	public Peer(String name, int id) {
		peerName = name;
		this.id = id;
		this.fManager = new FileManager(id);
		x = (int) (Math.random() * NetworkConstants.AREA_SIZE);
		y = (int) (Math.random() * NetworkConstants.AREA_SIZE);
		runner = new Thread(this);
		cManager = new ConnectionManager(name);
		messages = new MessageHandler(this, state, fManager, cManager);
		System.out.println("Hello world, I'm " + peerName + " with id " + id);

		verbose = id==0;
	}

	public void run() {
		running = true;
		print("running...");
		int waitTime = 0;
		long previousTs = 0;
		PeerState previous = null;
		while (running) {
			long ts = System.currentTimeMillis();
			if (ts%1000 == 0 && ts != previousTs) {
				waitTime++;
				previousTs = ts;
			}
			if (waitTime > PeerConstants.WAIT_TIMEOUT) {
				waitTime = 0;
				// this should only occur when a waiting too long for messages (sendIndex, send cf, send c)
				List<Integer> badPeers = new LinkedList<Integer>();
				if (state.get() == PeerState.WAITINGINDEX) {
					badPeers = messages.timeoutIndex();
					state.set(PeerState.GETINDEX);
				} else {
					badPeers = messages.timeoutChunk();
					badPeers.addAll(messages.timeoutChunkfield());
					state.set(PeerState.REFRESHINDEX);
				}
				if (badPeers.size() > 0 | cManager.neighborsCount() < NetworkConstants.N_OPT) {
					int nC = -1;
					if (badPeers.size() == 0) {
						nC = cManager.neighborsCount();
						System.out.println("trying to get more neighbors, currently have " + nC);
					}
					cManager.replaceNeighbors(badPeers);
					if (nC != -1) {
						nC = cManager.neighborsCount();
						System.out.println("now have " + nC + " peers.");
					}
				}
			}
			if (previous != state.get()) print(state.get() + "...");
			previous = state.get();
			switch (state.get()) {
			case BOOTING:
				print("fetching neighbors");

				cManager.init(messages);
				
				state.set(PeerState.GETINDEX);
				
				break;
			case GETINDEX:
				if (cManager.neighborsCount() <= 0) {
					cManager.replaceNeighbors(new ArrayList<Integer>());
					state.set(PeerState.GETINDEX);
				} else {
					cManager.send(getIndex());
					state.set(PeerState.WAITINGINDEX);
				}
				
				break;
			case WAITINGINDEX :
			case WAITING :
				if (!messages.isEmpty()) {
					print("handling a message...");
					if(messages.get() instanceof SendIndex) {
						isReadyForAction = true;
						System.out.println("peer is now ready to put/rm !");
					}
					
					messages.handleMessage();
				}
				break;
			case REFRESHINDEX :
				waitTime = 0;
				cManager.send(refreshIndex());
				state.set(PeerState.BUILDGLOBALCF); // not so good but simple implementation
				break;
			case BUILDGLOBALCF :
				waitTime = 0;
				if (fManager.filesCount() == 0) {
					print("No files in index !");
					state.set(PeerState.WAITING);
				} else {
					cManager.broadcast(getChunkfield());
					state.set(PeerState.WAITING);
				}
				break;
			case CHECKGLOBALCF :
				int sentRequestCount = checkGlobalCF();
				print("sent " + sentRequestCount + " chunk requests");
				if (sentRequestCount > 0 ) state.set(PeerState.WAITING);
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

		cManager.stop();
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
			//cManager.init(messages);
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
	 * Don't use this method !!!
	 * Use rootPut or remotePut instead
	 * 
	 * @param f the file to put in RAIDFS
	 */
	public void put(File f) {
	}

	/* miShell interface */

	/**
	 * Used by miShell to chunkify new file
	 * Will chunkify argument and update index and chunkfield in FileManager.
	 * 
	 * @param osFullPath os full path of file to load
	 * @param dfsFullPath dfsFullPath to store file (including fileName)
	 * @return file representation with metadata (chunk count, uID, name, dfsFull path)
	 */
	public File rootPut(String osFullPath, String dfsFullPath) {
		return fManager.loadFile(osFullPath, dfsFullPath);
	}

	/**
	 * Used by miShell to notify put index update
	 * 
	 * @param file The file representation of file to store in local index.
	 * @return true if the file was added (not already existing in local index) false otherwise
	 */
	public boolean remotePut(File file) {

		
		PutIndexAdvertisement putAdvertisement = (PutIndexAdvertisement) AdvertisementFactory.newAdvertisement(PutIndexAdvertisement.getAdvertisementType());
		
		putAdvertisement.setFileName(file.name);
		putAdvertisement.setFileCreationTime(System.currentTimeMillis());
		putAdvertisement.setFileSize(file.chunkCount * NetworkConstants.CHUNK_SIZE);
		putAdvertisement.setChunkCount(file.chunkCount);
		putAdvertisement.setID(ID.nullID);

		cManager.send(putAdvertisement);

		return fManager.addFile(file, true);
	}

	/**
	 * used by miShell to notify rm index update
	 * 
	 * @param file The file representation of file to remove from local index.
	 * @return true if the file existed false if no such file
	 */
	public boolean remoteRemove(File file) {
		
		RmIndexAdvertisement rmAdvertisement = (RmIndexAdvertisement) AdvertisementFactory.newAdvertisement(RmIndexAdvertisement.getAdvertisementType());
		rmAdvertisement.setFileName(file.name);
		rmAdvertisement.setFileDeletionTime(System.currentTimeMillis());
		rmAdvertisement.setID(ID.nullID);

		cManager.send(rmAdvertisement);

		return fManager.rmFile(file);
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
			print("GlobalChunkfield of file " + f.name + ": " + tempGC);
			tempLowChunks = tempGC.lowChunks();
			if (f.isPeerResponsible() && tempLowChunks.size() > 0) {
				// duplication required
				// selects only chunks that are not owned yet
				notOwnedLowChunks = new ArrayList<Integer>();
				for (Integer chunk : tempLowChunks) {
					if (! fManager.containsChunk(f.name, chunk)) {
						notOwnedLowChunks.add(chunk);
					}
				}
				// do not put empty chunklist for a file
				if (notOwnedLowChunks.size() > 0) {
					chunksToGet.put(f, notOwnedLowChunks);
				}
			}
			if (! f.isStabilized() && tempLowChunks.size() == 0) {
				// enough duplication achieved, file can be stabilized
				print("stabilize file and send notif");
				f.stabilise();
				
				FileStabilizedAdvertisement stabAdvertisement = (FileStabilizedAdvertisement) AdvertisementFactory.newAdvertisement(FileStabilizedAdvertisement.getAdvertisementType());
				
				stabAdvertisement.setID(ID.nullID);

				cManager.send(stabAdvertisement);
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
		} while (!cManager.send(getChunk(file.name, chunkId)));

		return true;
	}
	
	public boolean isReadyForActions() {
		return isReadyForAction;
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
	public NewFile newFile(String fileName, int chunkCount) {
		print("creating newFile message");
		return new NewFile(this.id, fileName, chunkCount);
	}

	@Override
	public GetChunk getChunk(String fName, int chunkId) {
		print("creating getChunk message for file " + fName + ", chunk " + chunkId);
		return new GetChunk(this.id, fName, chunkId);
	}

	@Override
	public SendChunk sendChunk(String fName, int chunkId) {
		print("creating sendChunk message for file " + fName + ", chunk " + chunkId);
		return new SendChunk(this.id, fName, chunkId, fManager.getChunkData(fName, chunkId));
	}

	@Override
	public FileStabilized fileStabilized(String fName) {
		print("creating fileStablilized message for file " + fName);
		return new FileStabilized(this.id, fName);
	}

	public MessageHandler getMessageHandler() {
		return messages;
	}
	public RefreshIndex refreshIndex() {
		return new RefreshIndex(id);
	}

	public void get(String fileName, String filePathOs) {
		File file = this.fManager.getFile(fileName);
		
		System.out.println("file to get is : " + file);
		
		fManager.addPendingGet(fileName, filePathOs);
		if (file != null) {
			for (int i = 0; i < file.chunkCount; i++) {
				if (!this.fManager.containsChunk(fileName, i)) {
					cManager.send(this.getChunk(fileName, i));
				}
			}
			
		}
	}


}
