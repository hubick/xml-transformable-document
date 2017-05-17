/*
 * Copyright 2012-2017 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package net.www_eee.util.transformable.dom;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.eclipse.jdt.annotation.*;

import static org.junit.Assert.*;

import org.junit.*;


/**
 * JUnit tests for {@link TransformableDocument}.
 */
@NonNullByDefault
public class TransformableDocumentTest {

  /**
   * Load and parse the {@link Templates} from the given {@link URL}.
   * 
   * @param templatesURL The URL to load the templates from.
   * @return The parsed Templates.
   * @throws IOException If there was an I/O problem loading the templates data.
   * @throws SAXException If there was a problem creating the XML parser.
   * @throws TransformerConfigurationException If there was a problem creating the Templates.
   */
  public static final Templates loadTemplates(final URL templatesURL) throws IOException, SAXException, TransformerConfigurationException {
    final InputSource templatesInputSource = new InputSource(templatesURL.openStream());
    final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
    xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
    final Source templatesSource = new SAXSource(xmlReader, templatesInputSource);
    templatesSource.setSystemId(templatesURL.toString());
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    return transformerFactory.newTemplates(templatesSource);
  }

  /**
   * Load test.xsl.
   * 
   * @return The compiled test.xsl Templates.
   * @throws IOException If there was an I/O problem loading the templates data.
   * @throws SAXException If there was a problem creating the XML parser.
   * @throws TransformerConfigurationException If there was a problem creating the Templates.
   */
  public static final Templates loadTestTemplates() throws IOException, SAXException, TransformerConfigurationException {
    final URL testTemplatesURL = TransformableDocumentTest.class.getResource("/net/www_eee/util/transformable/test.xsl");
    assertNotNull("test.xsl not found", testTemplatesURL);
    return loadTemplates(testTemplatesURL);
  }

  /**
   * {@linkplain TransformableDocument#transform(Result) Transform} the supplied document into a String.
   * 
   * @param testDocument The document to be transformed.
   * @return The String output from the test document.
   * @throws Exception If there was a problem.
   */
  protected static final String toString(final TransformableDocument testDocument) throws Exception {
    final StringWriter transformResultWriter = new StringWriter();
    testDocument.transform(new StreamResult(transformResultWriter));
    return transformResultWriter.toString();
  }

  /**
   * Test the {@link TransformableDocument}.
   * 
   * @throws Exception If something went wrong.
   */
  @Test
  public void testTransformableDocument() throws Exception {

    final TransformableDocument testDocument = new TransformableDocument();
    testDocument.setIndent(false);
    testDocument.setOmitXMLDeclaration(true);
    final List<@Nullable Templates> testTransformations = new ArrayList<@Nullable Templates>(2);
    testDocument.setTransformations(testTransformations);

    final Element testElement = testDocument.getDocument().createElementNS(XMLConstants.NULL_NS_URI, "test");
    testDocument.getDocument().appendChild(testElement);
    testElement.appendChild(testDocument.getDocument().createTextNode("Hello World"));

    // Test output with no transformations.
    assertEquals("<test>Hello World</test>", toString(testDocument));

    // Test output with a single transform.
    testTransformations.add(loadTestTemplates());
    assertEquals("<test>Hello World!</test>", toString(testDocument));

    // Test output with two chained transforms.
    testTransformations.add(loadTestTemplates());
    assertNotSame(testTransformations.get(0), testTransformations.get(1));
    assertEquals("<test>Hello World!!</test>", toString(testDocument));

    // Test output with a trailing null transform.
    testTransformations.add(null);
    assertEquals("<test>Hello World!!</test>", toString(testDocument));

    // Test output with a null transform in the middle.
    testTransformations.add(loadTestTemplates());
    assertEquals("<test>Hello World!!!</test>", toString(testDocument));

    // Test output with a leading null transform.
    testTransformations.set(0, null);
    assertEquals("<test>Hello World!!</test>", toString(testDocument));

    return;
  }

}
