package ch.epfl.p2pmapreduce.nodeCore.messages;

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
import ch.epfl.p2pmapreduce.advertisement.PutIndexAdvertisement;

public class FileStabilizedAdvertisement extends Advertisement{
	public static final String name = "FileStabilizedAdvertisement";
	public final static String advertisementType = "jxta:FileStabilizedAdvertisement";

	private ID advertisementID = ID.nullID;
	
	//informations with a put : name of the added file, its size, its creation date
	private final String identifier = "indexAdvertisement:put";
	private String fileName = "";

	private final static String identifierTag = "MyIdentifierTag";
	private final static String fileNameTag = "MyFileNameTag";
	private final static String idTag = "MyIDTag";

	private final static String[] indexableFields = { identifierTag, idTag };

	public FileStabilizedAdvertisement(Element Root) {

		// Retrieving the elements
		TextElement MyTextElement = (TextElement) Root;

		Enumeration TheElements = MyTextElement.getChildren();

		while (TheElements.hasMoreElements()) {

			TextElement TheElement = (TextElement) TheElements.nextElement();

			processElement(TheElement);
		}
	}

	public FileStabilizedAdvertisement() {

		// Accepting default values
	}

	private void processElement(TextElement TheElement) {
		String theElementName = TheElement.getName();
		String theTextValue = TheElement.getTextValue();

		if (theElementName.compareTo(idTag) == 0) {
			try {

				URI ReadID = new URI(theTextValue);
				advertisementID = IDFactory.fromURI(ReadID);
				return;
			} catch (URISyntaxException Ex) {

				// Issue with ID format
				Ex.printStackTrace();
			} catch (ClassCastException Ex) {

				// Issue with ID type
				Ex.printStackTrace();
			}
		}

		if (theElementName.compareTo(fileNameTag) == 0) {
			fileName = theTextValue;
			return;
		}
		
	}

	@Override
	public Document getDocument(MimeMediaType theMimeMediaType) {

		// Creating document
		StructuredDocument theResult = StructuredDocumentFactory
				.newStructuredDocument(theMimeMediaType, advertisementType);

		// Adding elements
		Element myTempElement;

		myTempElement = theResult.createElement(fileNameTag, fileName);
		theResult.appendChild(myTempElement);
		
		myTempElement = theResult.createElement(identifierTag, identifier);
		theResult.appendChild(myTempElement);

		return theResult;
	}

	public void setID(ID theID) {
		advertisementID = theID;
	}

	@Override
	public ID getID() {
		return advertisementID;
	}

	@Override
	public String[] getIndexFields() {
		return indexableFields;
	}

	public static String getAdvertisementType() {
		return advertisementType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String name) {
		fileName = name;
	}

	public static class Instantiator implements
			AdvertisementFactory.Instantiator {

		public String getAdvertisementType() {
			return PutIndexAdvertisement.getAdvertisementType();
		}

		public Advertisement newInstance() {
			return new PutIndexAdvertisement();
		}

		public Advertisement newInstance(net.jxta.document.Element root) {
			return new PutIndexAdvertisement(root);
		}
	}

	@Override
	public FileStabilizedAdvertisement clone() throws CloneNotSupportedException {
		FileStabilizedAdvertisement result = (FileStabilizedAdvertisement) super.clone();

		result.advertisementID = this.advertisementID;
		result.fileName = this.fileName;
		return result;
	}

	@Override
	public String getAdvType() {
		return PutIndexAdvertisement.class.getName();
	}

}
