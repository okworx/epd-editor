package epd;

import javax.xml.namespace.QName;

/**
 * Utility class for XML vocabulary defined by the EPD editor.
 */
public final class EditorVocab {

	public static final String NAMESPACE = "http://greendelta.com/epd-editor";

	private EditorVocab() {
	}

	public static QName qNameOf(String localName) {
		return new QName(NAMESPACE, localName);
	}
}
