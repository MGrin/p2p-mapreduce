package ch.epfl.p2pmapreduce.nodeCore.peer;

import java.util.LinkedList;
import java.util.List;

import ch.epfl.p2pmapreduce.index.Metadata;
import ch.epfl.p2pmapreduce.nodeCore.messages.FileRemoved;
import ch.epfl.p2pmapreduce.nodeCore.messages.FileStabilized;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.GetIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.Message;
import ch.epfl.p2pmapreduce.nodeCore.messages.MessageReceiver;
import ch.epfl.p2pmapreduce.nodeCore.messages.MessageType;
import ch.epfl.p2pmapreduce.nodeCore.messages.NewFile;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunk;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendChunkfield;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendIndex;
import ch.epfl.p2pmapreduce.nodeCore.messages.SendMessage;
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
	
	private MessageExpecter expecter = MessageExpecter.INSTANCE;
	
	public MessageHandler(MessageBuilder builder, StateManager state, FileManager files, ConnectionManager cManager) {
		this.builder = builder;
		this.state = state;
		this.files = files;
		this.cManager = cManager;
	}
	
	public synchronized void enqueue(Message m) {
		messages.addLast(m);
	}
	
	private boolean isExpected(SendMessage m) {
		return expecter.isExpected(m);
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
		System.out.println("message is of type " + m.getClass());
		m.visit(this);
	}
	
	public void err(String message) {
		System.err.println("Message handler : " + message);
	}
	
	public List<Integer> timeoutAll() {
		List<Integer> result = timeoutIndex();
		result.addAll(timeoutChunk());
		result.addAll(timeoutChunkfield());
		return result;
	}
	
	public List<Integer> timeoutIndex() {
		return waitTimeout(MessageType.SEND_INDEX);
	}
	
	public List<Integer> timeoutChunk() {
		return waitTimeout(MessageType.SEND_CHUNK);
	}
	
	public List<Integer> timeoutChunkfield() {
		return waitTimeout(MessageType.SEND_CHUNKFIELD);
	}
	
	private List<Integer> waitTimeout(MessageType type) {
		return expecter.timeOut(type);
	}

	
	// visitor patter methods

	@Override
	public void receive(GetChunkfield message) {
		cManager.send(builder.sendChunkfield(), message.sender());
	}

	@Override
	public void receive(SendChunkfield message) {
		
		if (! isExpected(message)) return;
		
		for (String fName : message.chunkfields().keySet()) {
			cManager.update(message.sender(), files.getFile(fName), message.chunkfields().get(fName));
		}
		
		if (!expecter.waitingChunkfield()) state.set(PeerState.CHECKGLOBALCF);
	}

	@Override
	public void receive(GetIndex message) {
		cManager.send(builder.sendIndex(), message.sender());
	}

	@Override
	public void receive(SendIndex sendIndex) {
		
		if (!isExpected(sendIndex)) return;
		
		cManager.initIndexUpdateDiscovery(this);
		
		files.replaceIndex(sendIndex.index);
		
		if (!expecter.waitingIndex()) state.set(PeerState.BUILDGLOBALCF);
	}

	@Override
	public void receive(NewFile newfile) {
				
		if (files.addFile(new File(newfile.name(), newfile.chunkCount()), false)) {
			Metadata.metaPut(newfile.getFileInfos());
			state.set(PeerState.BUILDGLOBALCF);
		}
	}
	
	@Override
	public void receive(FileRemoved updateIndex) {
				
		if(files.rmFile(files.getFile(updateIndex.name()))) {
			//Not a directory be default.. But Metadata should actually now!
			System.out.println("removing from local xml file!");
			Metadata.metaRm(updateIndex.name(), false);
			
		}
		
	}

	@Override
	public void receive(GetChunk getChunk) {
		if (!files.containsFile(getChunk.fName())) return;
		cManager.send(builder.sendChunk(getChunk.fName(), getChunk.chunkId()), getChunk.sender());
	}

	@Override
	public void receive(SendChunk sendChunk) {
		
		if (! isExpected(sendChunk)) return;
		if (!files.containsFile(sendChunk.fName())) return;
		
		files.addChunk(sendChunk.fName(), sendChunk.chunkId(), sendChunk.getChunkData());
		// may not be necessary because of build global cf
		cManager.resetGlobalChunkfield(files.getFile(sendChunk.fName()));
		
		if (!expecter.waitingChunk()) state.set(PeerState.REFRESHINDEX);
	}

	@Override
	public void receive(FileStabilized fileSabilized) {
		files.stabilize(fileSabilized.file());
	}
}
