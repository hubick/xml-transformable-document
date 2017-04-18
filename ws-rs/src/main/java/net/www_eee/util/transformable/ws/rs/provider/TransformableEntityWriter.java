/*
 * Copyright 2007-2017 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, a copy of which you should have received in the file LICENSE.txt.
 */

package net.www_eee.util.transformable.ws.rs.provider;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.nio.charset.*;
import java.util.*;
import java.util.logging.*;

import javax.activation.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import org.eclipse.jdt.annotation.*;

import net.www_eee.util.transformable.*;


/**
 * An {@link MessageBodyWriter} implementation {@link Provider} for {@link TransformableEntity} objects.
 * 
 * @see TransformableEntity#transform(Result)
 */
@Provider
@NonNullByDefault
public class TransformableEntityWriter implements MessageBodyWriter<TransformableEntity> {

  @Override
  public long getSize(final TransformableEntity transformableEntity, final Class<?> type, final Type genericType, final @NonNull Annotation[] annotations, final MediaType mediaType) {
    return -1;
  }

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final @NonNull Annotation[] annotations, final MediaType mediaType) {
    return TransformableEntity.class.isAssignableFrom(type);
  }

  @Override
  public void writeTo(final TransformableEntity transformableEntity, final Class<?> type, final Type genericType, final @NonNull Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String,Object> httpHeaders, final OutputStream entityStream) throws IOException, WebApplicationException {

    try {

      // Set the Content-Type header from the TransformableEntity.
      try {
        final @Nullable MimeType outputMimeType = transformableEntity.getOutputMediaType();
        if (outputMimeType != null) {
          @NonNull
          MediaType outputMediaType = MediaType.valueOf(outputMimeType.toString());
          final @Nullable Charset outputEncoding = transformableEntity.getOutputEncoding();
          if (outputEncoding != null) {
            final Map<String,String> outputMediaTypeParams = new HashMap<String,String>(outputMediaType.getParameters());
            outputMediaTypeParams.put("charset", outputEncoding.name());
            outputMediaType = new MediaType(outputMediaType.getType(), outputMediaType.getSubtype(), outputMediaTypeParams);
          }
          httpHeaders.putSingle("Content-Type", outputMediaType);
        } else {
          httpHeaders.remove("Content-Type");
        }
      } catch (MimeTypeParseException mtpe) {
        throw new WebApplicationException(mtpe);
      } catch (UnsupportedCharsetException uce) {
        throw new WebApplicationException(uce);
      }

      try {
        transformableEntity.transform(new StreamResult(entityStream));
      } catch (IOException ioe) {
        throw ioe;
      } catch (TransformerException te) {
        throw new WebApplicationException(te);
      }

    } catch (IOException ioe) {
      Logger.getLogger(TransformableEntityWriter.class.getName() + '.' + IOException.class.getSimpleName()).log(Level.FINER, ioe.getMessage(), ioe);
      throw ioe;
    } catch (WebApplicationException wae) {
      Logger.getLogger(TransformableEntityWriter.class.getName() + '.' + WebApplicationException.class.getSimpleName()).log(Level.FINER, wae.getMessage(), wae);
      throw wae;
    } catch (RuntimeException re) {
      Logger.getLogger(TransformableEntityWriter.class.getName() + '.' + RuntimeException.class.getSimpleName()).log(Level.FINER, re.getMessage(), re);
      throw re;
    }
    return;
  }

}
