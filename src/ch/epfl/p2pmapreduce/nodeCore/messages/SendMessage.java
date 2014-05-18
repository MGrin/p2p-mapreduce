package ch.epfl.p2pmapreduce.nodeCore.messages;

public interface SendMessage extends Message {
	MessageType getType();
}
