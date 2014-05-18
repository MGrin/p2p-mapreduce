package ch.epfl.p2pmapreduce.nodeCore.messages;

public abstract class ExpectedSend implements SendMessage {

	private int senderId;
	
	public ExpectedSend(int senderId) {
		this.senderId = senderId;
	}
	
	@Override
	public int sender() {
		return senderId;
	}

	@Override
	public void visit(MessageReceiver messageReceiver) {
		// no subject to visitor
	}

	
	public static class Index extends ExpectedSend {
		public Index(int senderId) { super(senderId); }
		@Override
		public MessageType getType() { return MessageType.SEND_INDEX; }
	}
	
	public static class Chunk extends ExpectedSend {
		public Chunk(int senderId) { super(senderId); }
		@Override
		public MessageType getType() { return MessageType.SEND_CHUNK; }
	}
	
	public static class Chunkfield extends ExpectedSend {
		public Chunkfield(int senderId) { super(senderId); }
		@Override
		public MessageType getType() { return MessageType.SEND_CHUNKFIELD; }
	}
}
