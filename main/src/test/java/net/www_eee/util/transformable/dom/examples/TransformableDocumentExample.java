/*
 * Copyright 2013-2017 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, a copy of which you should have received in the file LICENSE.txt.
 */

package net.www_eee.util.transformable.dom.examples;

import java.util.*;

import javax.xml.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

import org.eclipse.jdt.annotation.*;

import net.www_eee.util.transformable.dom.*;


/**
 * A simple example of using a {@link TransformableDocument}.
 */
@NonNullByDefault
public class TransformableDocumentExample {

  /**
   * Execute this example.
   * 
   * @param args The arguments provided to this example program.
   * @throws Exception If there was a problem executing the program.
   */
  public static void main(final @NonNull String[] args) throws Exception {

    // Create an example document...
    final TransformableDocument doc = new TransformableDocument();

    // Add some simple content...
    final Element testElement = doc.getDocument().createElementNS(XMLConstants.NULL_NS_URI, "test");
    doc.getDocument().appendChild(testElement);
    testElement.appendChild(doc.getDocument().createTextNode("Hello World"));

    // Add an example transformation that will simply add an exclamation mark to the content of the root 'test' element...
    doc.setTransformations(Arrays.asList(TransformableDocumentTest.loadTestTemplates()));

    // Set the output options...
    doc.setIndent(false);
    doc.setOmitXMLDeclaration(true);

    // Perform the transformations and print the document to System.out...
    doc.transform(new StreamResult(System.out));

    return;
  }

}
