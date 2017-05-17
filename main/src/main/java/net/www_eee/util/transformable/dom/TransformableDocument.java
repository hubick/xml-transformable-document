/*
 * Copyright 2007-2017 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package net.www_eee.util.transformable.dom;

import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;

import org.w3c.dom.*;

import org.xml.sax.*;

import net.www_eee.util.transformable.*;

import org.eclipse.jdt.annotation.*;


/**
 * Encapsulate a DOM {@link Document} entity along with a set of (optional) {@linkplain #setTransformations(Iterable)
 * transformations}, the various objects needed to configure the serialized output, and the code to
 * {@linkplain #transform(Result) perform} it.
 */
@NonNullByDefault
public class TransformableDocument extends TransformableEntity {
  /**
   * A shared {@linkplain DocumentBuilderFactory#isNamespaceAware() namespace-aware}
   * {@linkplain DocumentBuilderFactory#isValidating() non-validating} {@link DocumentBuilderFactory}. Access to this
   * factory should be synchronized for thread safety.
   */
  private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
  static {
    DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
  }
  /**
   * The DOM {@link Document} encapsulated by this object.
   */
  protected final Document document;

  /**
   * Construct a new <code>TransformableDocument</code>.
   */
  public TransformableDocument() {
    super();
    document = newDocument();
    return;
  }

  /**
   * Get the DOM {@link Document} encapsulated by this object.
   * 
   * @return The encapsulated {@link Document}.
   */
  public Document getDocument() {
    return document;
  }

  @Override
  protected synchronized void transformImpl(final Result result) throws TransformerException, IOException {

    final Transformer baseTransformer = newTransformer(incremental, ERROR_LISTENER);
    setupBaseTransformer(baseTransformer);

    final Result transformResult = createTransformerHandlers(result);

    final DOMSource documentDOMSource = new DOMSource(document);

    try {
      synchronized (document) {
        baseTransformer.transform(documentDOMSource, transformResult);
      }
    } catch (TransformerException te) {
      if (te.getCause() instanceof IOException) throw (IOException)te.getCause();
      if ((te.getCause() instanceof SAXException) && (te.getCause().getCause() instanceof IOException)) throw (IOException)te.getCause().getCause();
      throw te;
    }

    return;
  }

  /**
   * Create a new DOM {@link Document}.
   * 
   * @return The new {@link Document}.
   */
  protected static final Document newDocument() {
    final DocumentBuilder documentBuilder;
    try {
      synchronized (DOCUMENT_BUILDER_FACTORY) {
        documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
      }
    } catch (ParserConfigurationException pce) { // Should never happen with our internal factory.
      throw new RuntimeException(pce);
    }
    return documentBuilder.newDocument();
  }

}
