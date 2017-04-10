/*
 * Copyright 2007-2017 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, a copy of which you should have received in the file LICENSE.txt.
 */

package net.www_eee.util.transformable.sax;

import java.io.*;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;

import org.xml.sax.*;

import net.www_eee.util.transformable.*;

import org.eclipse.jdt.annotation.*;


/**
 * Encapsulate a SAX generated entity along with a set of (optional) {@linkplain #setTransformations(Iterable)
 * transformations}, the various objects needed to configure the serialized output, and the code to
 * {@linkplain #transform(Result) perform} it.
 */
@NonNullByDefault
public abstract class SAXEntity extends TransformableEntity {

  /**
   * Construct a new <code>SAXEntity</code>.
   */
  protected SAXEntity() {
    super();
    return;
  }

  /**
   * Write the content for this entity.
   * 
   * @param transformerHandler Content is generated within this method through calls to this {@link TransformerHandler}.
   * @throws SAXException If there was a problem while calling the <code>transformerHandler</code> to generate content.
   */
  protected abstract void writeSAXEntity(TransformerHandler transformerHandler) throws SAXException;

  @Override
  protected synchronized void transformImpl(final Result result) throws TransformerException, IOException {

    final TransformerHandler baseTransformerHandler = newTransformerHandler(null, incremental, ERROR_LISTENER);
    setupBaseTransformer(baseTransformerHandler.getTransformer());

    baseTransformerHandler.setResult(createTransformerHandlers(result));

    try {
      writeSAXEntity(baseTransformerHandler);
    } catch (SAXException saxe) {
      if (saxe.getCause() instanceof IOException) throw (IOException)saxe.getCause();
      throw new TransformerException(saxe);
    }

    return;
  }

}
