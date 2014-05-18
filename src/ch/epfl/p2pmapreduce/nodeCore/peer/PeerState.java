package ch.epfl.p2pmapreduce.nodeCore.peer;

public enum PeerState {
	BOOTING,
	GETINDEX,
	REFRESHINDEX,
	BUILDGLOBALCF,
	CHECKGLOBALCF,
	WAITING,
	EXITING;
}
