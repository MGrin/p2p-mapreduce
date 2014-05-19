package ch.epfl.p2pmapreduce.advertisement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.TextElement;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
/**
 * Class representing an IndexAdvertisement
 *
 */
public class IndexAdvertisement extends Advertisement {

	public static final String Name = "IndexAdvertisement";

	// Advertisement elements, tags and indexables
	public final static String AdvertisementType = "jxta:CustomizedAdvertisement";

	private ID AdvertisementID = ID.nullID;

	private String XMLINdex = "";

	private final static String IDTag = "MyIDTag";
	private final static String XMLIndexTag = "MyXMLIndexTag";

	private final static String[] IndexableFields = { IDTag, XMLIndexTag };

	public IndexAdvertisement() {

		// Accepting default values
	}

	public IndexAdvertisement(Element Root) {

		// Retrieving the elements
		TextElement MyTextElement = (TextElement) Root;

		Enumeration TheElements = MyTextElement.getChildren();

		while (TheElements.hasMoreElements()) {

			TextElement TheElement = (TextElement) TheElements.nextElement();

			ProcessElement(TheElement);

		}        

	}

	public void ProcessElement(TextElement TheElement) {

		String TheElementName = TheElement.getName();
		String TheTextValue = TheElement.getTextValue();

		if (TheElementName.compareTo(IDTag)==0) {

			try {

				URI ReadID = new URI(TheTextValue);
				AdvertisementID = IDFactory.fromURI(ReadID);
				return;

			} catch (URISyntaxException Ex) {

				// Issue with ID format
				Ex.printStackTrace();

			} catch (ClassCastException Ex) {

				// Issue with ID type
				Ex.printStackTrace();

			}

		}

		if (TheElementName.compareTo(XMLIndexTag) == 0) {

			XMLINdex = TheTextValue;
			return;

		}

	}

	@Override
	public Document getDocument(MimeMediaType TheMimeMediaType) {

		// Creating document
		StructuredDocument TheResult = StructuredDocumentFactory.newStructuredDocument(
				TheMimeMediaType, AdvertisementType);

		// Adding elements
		Element MyTempElement;

		MyTempElement = TheResult.createElement(XMLIndexTag, XMLINdex);
		TheResult.appendChild(MyTempElement);

		return TheResult;

	}

	public void SetID(ID TheID) {
		AdvertisementID = TheID;
	}

	@Override
	public ID getID() {
		return AdvertisementID;
	}

	@Override
	public String[] getIndexFields() {
		return IndexableFields;
	}

	public void setXMLIndex(String newIndex) {
		this.XMLINdex = newIndex;
	}

	public String getXMLIndex() {
		return XMLINdex;	
	}

	@Override
	public IndexAdvertisement clone() throws CloneNotSupportedException {

		IndexAdvertisement Result =
				(IndexAdvertisement) super.clone();

		Result.AdvertisementID = this.AdvertisementID;
		Result.XMLINdex = this.XMLINdex;

		return Result;

	}

	@Override
	public String getAdvType() {
		return IndexAdvertisement.class.getName();
	}

	public static String getAdvertisementType() {
		return AdvertisementType;
	}    

	public static class Instantiator implements AdvertisementFactory.Instantiator {

		public String getAdvertisementType() {
			return IndexAdvertisement.getAdvertisementType();
		}

		public Advertisement newInstance() {
			return new IndexAdvertisement();
		}

		public Advertisement newInstance(net.jxta.document.Element root) {
			return new IndexAdvertisement(root);
		}

	}

}