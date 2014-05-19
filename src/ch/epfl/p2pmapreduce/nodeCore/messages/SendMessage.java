package ch.epfl.p2pmapreduce.nodeCore.messages;
/**
 * Interface for sending a message
 *
 */
public interface SendMessage extends Message {
	MessageType getType();
}
