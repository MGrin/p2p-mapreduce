package ch.epfl.p2pmapreduce.index;
import org.jdom2.*;
import org.jdom2.output.*;

public class Metadata {
	static Element racine = new Element("root");
	static org.jdom2.Document document = new Document(racine);
	public Metadata(){
		racine = new Element("root");
		document = new Document(racine);
	}
	public static void metaPut(){
		
	}
	public static void metaRm(){
		
	}
}
