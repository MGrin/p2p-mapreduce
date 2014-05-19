package ch.epfl.p2pmapreduce.nodeCore.peer;
/**
 * Enum of the states of a peer
 *
 */
public enum PeerState {
	BOOTING,
	GETINDEX,
	REFRESHINDEX,
	BUILDGLOBALCF,
	CHECKGLOBALCF,
	WAITINGINDEX,
	WAITING,
	EXITING;
}
