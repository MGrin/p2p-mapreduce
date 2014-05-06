package ch.epfl.p2pmapreduce.advertisement;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Element;
import net.jxta.document.Advertisement;
import net.jxta.document.Document;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.TextElement;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;

public class PutIndexAdvertisement extends Advertisement {
	private ID AdvertisementID = ID.nullID;

	public static final String Name = "PutIndexAdvertisement";
	public final static String AdvertisementType = "jxta:CustomizedAdvertisement";

	private String DFSFileName = "";
	private long fileSize = 0;
	private long fileCreationTime = 0;

	private final static String NameTag = "MyNameTag";
	private final static String IDTag = "MyIDTag";
	private final static String DateTag = "MyDateTag";
	// new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE).format(new
	// Date());
	private final static String SizeTag = "MySizeTag";

	private final static String[] IndexableFields = { NameTag, IDTag, DateTag };

	public PutIndexAdvertisement(Element Root) {
		// Retrieving the elements
		TextElement MyTextElement = (TextElement) Root;

		Enumeration TheElements = MyTextElement.getChildren();

		while (TheElements.hasMoreElements()) {

			TextElement TheElement = (TextElement) TheElements.nextElement();

			ProcessElement(TheElement);

		}
	}

	private void ProcessElement(TextElement TheElement) {
		String TheElementName = TheElement.getName();
		String TheTextValue = TheElement.getTextValue();

		if (TheElementName.compareTo(IDTag) == 0) {

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

		if (TheElementName.compareTo(NameTag) == 0) {

			DFSFileName = TheTextValue;
			return;

		}

		if (TheElementName.compareTo(DateTag) == 0) {

			fileCreationTime = Long.parseLong(TheTextValue);
			return;

		}
		
		//TODO: ADD SIZE TAG
	}

	@Override
	public Document getDocument(MimeMediaType TheMimeMediaType) {
		// Creating document
		StructuredDocument TheResult = StructuredDocumentFactory
				.newStructuredDocument(TheMimeMediaType, AdvertisementType);

		// Adding elements
		Element MyTempElement;

		MyTempElement = TheResult.createElement(DateTag, fileCreationTime);
		TheResult.appendChild(MyTempElement);

		MyTempElement = TheResult.createElement(SizeTag,
				Long.toString(fileSize));
		TheResult.appendChild(MyTempElement);

		MyTempElement = TheResult.createElement(NameTag, DFSFileName);
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

	public static String getAdvertisementType() {
		return AdvertisementType;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long size) {
		fileSize = size;
	}

	public long getFileCreationTime() {
		return fileCreationTime;
	}

	public void setFileCreationTime(long time) {
		fileCreationTime = time;
	}

	public String getDFSFileName() {
		return DFSFileName;
	}

	public void setDFSFileName(String name) {
		DFSFileName = name;
	}

	public static class Instantiator implements AdvertisementFactory.Instantiator {

		public String getAdvertisementType() {
			return RmIndexAdvertisement.getAdvertisementType();
		}

		public Advertisement newInstance() {
			return new RmIndexAdvertisement();
		}

		public Advertisement newInstance(net.jxta.document.Element root) {
			return new RmIndexAdvertisement(root);
		}

	}

}
