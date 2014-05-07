package ch.epfl.p2pmapreduce.nodeCore.peer;

import java.util.LinkedList;

import ch.epfl.p2pmapreduce.nodeCore.messages.FileRemoved;
import ch.epfl.p2pmapreduce.nodeCore.messages.FileStabilized;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.Message;
import ch.epfl.p2pmapreduce.nodeCore.messages.MessageReceiver;
import ch.epfl.p2pmapreduce.nodeCore.messages.NewFile;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;
import ch.epfl.p2pmapreduce.nodeCore.network.ConnectionManager;
import ch.epfl.p2pmapreduce.nodeCore.volume.File;
import ch.epfl.p2pmapreduce.nodeCore.volume.FileManager;


public class MessageHandler implements MessageReceiver {
	// TODO implement a list of pending request to know when answer are received
	
	private LinkedList<Message> messages = new LinkedList<Message>();
	private MessageBuilder builder;
	private StateManager state;
	private FileManager files;
	private ConnectionManager cManager;
	
	private int pendingChunkRequest = 0;
	
	public MessageHandler(MessageBuilder builder, StateManager state, FileManager files, ConnectionManager cManager) {
		this.builder = builder;
		this.state = state;
		this.files = files;
		this.cManager = cManager;
	}
	
	public synchronized void enqueue(Message m) {
		messages.addLast(m);
	}
	
	public synchronized Message get() {
		if (!messages.isEmpty()) {
			return messages.peekFirst(); 
		} else {
			return null;
		}
	}
	
	public boolean isEmpty() {
		return messages.isEmpty();
	}
	
	private synchronized Message readHead() {
		if (!messages.isEmpty()) return messages.removeFirst();
		else return null;
	}
	
	/**
	 * Takes one message from message queue and handles it
	 * uses visitor pattern
	 */
	public void handleMessage() {
		Message m = readHead();
		m.visit(this);
	}
	
	public void err(String message) {
		System.err.println("Message handler : " + message);
	}

	public void addPendingChunkRequest(int sentRequestCount) {
		pendingChunkRequest += sentRequestCount;
	}

	
	// visitor patter methods

	@Override
	public void receive(GetChunkfield message) {
		cManager.send(builder.sendChunkfield(), message.sender());
	}

	@Override
	public void receive(SendChunkfield message) {
		for (String fName : message.chunkfields().keySet()) {
			cManager.update(message.sender(), files.getFile(fName), message.chunkfields().get(fName));
		}
		state.set(PeerState.CHECKGLOBALCF);
	}

	@Override
	public void receive(GetIndex message) {
		cManager.send(builder.sendIndex(), message.sender());
	}

	@Override
	public void receive(SendIndex sendIndex) {
		
		// simply ignore sendIndex when not requested
		if(state.get().equals(PeerState.WAITINGINDEX)) {
			
			files.replaceIndex(sendIndex.index);
			state.set(PeerState.BUILDGLOBALCF);
		}
	}

	@Override
	public void receive(NewFile newfile) {
		if (files.addFile(new File(newfile.name(), newfile.chunkCount()))) {
			state.set(PeerState.BUILDGLOBALCF);
		}
	}
	
	@Override
	public void receive(FileRemoved updateIndex) {
		
		//TODO: Implement | But same problem here! we only have the name so cannot instantiate
		
		
	}

	@Override
	public void receive(GetChunk getChunk) {
		cManager.send(builder.sendChunk(getChunk.fName(), getChunk.chunkId()), getChunk.sender());
	}

	@Override
	public void receive(SendChunk sendChunk) {
		// only consider receiving chunks when actually requesting.
		if (state.get() == PeerState.WAITINGCHUNKS) {
			files.addChunk(sendChunk.fName(), sendChunk.chunkId(), sendChunk.getChunkData());
			// may not be necessary because of build global cf
			cManager.resetGlobalChunkfield(files.getFile(sendChunk.fName()));
			pendingChunkRequest--;
			if (pendingChunkRequest <= 0) {
				pendingChunkRequest = 0;
				state.set(PeerState.BUILDGLOBALCF);
			}
		}
	}

	@Override
	public void receive(FileStabilized fileSabilized) {
		files.stabilize(fileSabilized.file());
	}
}
