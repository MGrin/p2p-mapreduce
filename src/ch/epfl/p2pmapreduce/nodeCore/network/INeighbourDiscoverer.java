package ch.epfl.p2pmapreduce.nodeCore.network;

import java.util.List;

/**
 * Interface neighbour discoverer
 *
 */
public interface INeighbourDiscoverer {
	List<Neighbour> getNeighbors();
}
