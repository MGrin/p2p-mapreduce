package ch.epfl.p2pmapreduce.exchanger;

import ch.epfl.p2pmapreduce.index.Metadata;


public class Receive {
	
	public Receive(){
		//appeler la méthode correspondante
	}
	
	public static void receivePut() {
		Metadata.metaPut();
	}
	
	public static void receiveRm() {
		Metadata.metaRm();
	}
	
	public static void receiveConnect() {
		//envoyer le fichier metadata
	}
}
