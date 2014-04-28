package ch.epfl.p2pmapreduce.nodeCore.peer;

public class StateManager {

	private PeerState currentState = PeerState.EXITING;
	
	public StateManager() {}
	
	public synchronized void set(PeerState nextState) {
		currentState = nextState;
	}
	
	public PeerState get() { return currentState; }
}
