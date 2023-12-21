// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.commons.runtime.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.talend.utils.xml.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * DOC ggu class global comment. Detailled comment
 */
public final class XMLFileUtil {

    private static final TransformerFactory TRANSFORMER_FACTORY = XmlUtils.getXmlSecureTransformerFactory();

    private static final DocumentBuilderFactory DOCBUILDER_FACTORY = XmlUtils.getSecureDocumentBuilderFactory();

    static {
		DOCBUILDER_FACTORY.setNamespaceAware(true);
	}

	public static Document loadDoc(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
		try {
			DocumentBuilder db = DOCBUILDER_FACTORY.newDocumentBuilder();
			db.setErrorHandler(new ErrorHandler() {
                
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    throw exception;
                }
                
                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
                
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });
			return db.parse(stream);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				//
			}
		}
	}

	public static Document loadDoc(File file) throws ParserConfigurationException, SAXException, IOException {
		return loadDoc(new BufferedInputStream(new FileInputStream(file)));
	}

	public static void saveDoc(Document doc, OutputStream output) throws TransformerException {
		try {
			Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
			transformer.transform(new DOMSource(doc), new StreamResult(output));
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				//
			}
		}
	}

	public static void saveDoc(Document doc, File file) throws IOException, TransformerException {
		file.getParentFile().mkdirs();
		saveDoc(doc, new FileOutputStream(file));
	}

	public static void consoleDoc(Document doc) throws TransformerException {
		Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
		transformer.transform(new DOMSource(doc), new StreamResult(System.out));
	}

}
