package epd.io.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.google.common.base.Strings;

import epd.model.Amount;
import epd.model.EpdProfile;

class AmountConverter {

	static List<Amount> readAmounts(Other other, EpdProfile profile) {
		if (other == null)
			return Collections.emptyList();
		List<Amount> amounts = new ArrayList<>();
		for (Object any : other.any) {
			if (!(any instanceof Element)) {
				continue;
			}
			Element element = (Element) any;
			if (!isValid(element))
				continue;
			amounts.add(fromElement(element, profile));
		}
		return amounts;
	}

	private static boolean isValid(Element element) {
		if (element == null)
			return false;
		String nsUri = element.getNamespaceURI();
		if (!Objects.equals(nsUri, Vocab.NS_EPD))
			return false;
		return Objects.equals(element.getLocalName(), "amount");
	}

	static Amount fromElement(Element element, EpdProfile profile) {
		Amount amount = new Amount();
		amount.value = getValue(element);
		NamedNodeMap attributes = element.getAttributes();
		for (int m = 0; m < attributes.getLength(); m++) {
			String attName = attributes.item(m).getLocalName();
			String attVal = attributes.item(m).getNodeValue();
			switch (attName) {
				case "module" -> amount.module = profile.module(attVal);
				case "scenario" -> amount.scenario = attVal;
			}
		}
		return amount;
	}

	private static Double getValue(Element element) {
		String text = element.getTextContent();
		if (Strings.isNullOrEmpty(text))
			return null;
		try {
			return Double.parseDouble(text);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(AmountConverter.class);
			log.warn("amount {} is not numeric", text);
			return null;
		}
	}

	static void writeAmounts(List<Amount> amounts, Other extension,
			Document doc) {
		if (amounts == null || extension == null || doc == null)
			return;
		for (Amount amount : amounts) {
			Element element = toElement(amount, doc);
			if (element == null)
				continue;
			extension.any.add(element);
		}
	}

	private static Element toElement(Amount amount, Document doc) {
		try {
			String nsUri = Vocab.NS_EPD;
			Element element = doc.createElementNS(nsUri, "epd:amount");
			if (amount.module != null)
				element.setAttributeNS(nsUri, "epd:module",
						amount.module.name);
			if (amount.scenario != null)
				element.setAttributeNS(nsUri, "epd:scenario",
						amount.scenario);
			if (amount.value != null)
				element.setTextContent(amount.value.toString());
			return element;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(AmountConverter.class);
			log.error("failed to convert amount to DOM element", e);
			return null;
		}
	}
}
