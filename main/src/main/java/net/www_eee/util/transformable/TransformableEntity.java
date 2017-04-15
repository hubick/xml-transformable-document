/*
 * Copyright 2007-2017 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, a copy of which you should have received in the file LICENSE.txt.
 */

package net.www_eee.util.transformable;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

import javax.activation.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.eclipse.jdt.annotation.*;


/**
 * Encapsulate an entity (document) along with a set of (optional) {@linkplain #setTransformations(Iterable)
 * transformations}, the various objects needed to configure the serialized output, and the code to
 * {@linkplain #transform(Result) perform} it.
 */
@NonNullByDefault
public abstract class TransformableEntity {
  /**
   * A shared {@link ErrorListener} implementation for internal use.
   */
  protected static final ErrorListener ERROR_LISTENER = new ErrorListener() {

    @Override
    public void warning(TransformerException exception) throws TransformerException {
      return;
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
      return;
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
      throw exception;
    }

  };
  /**
   * A shared {@link SAXTransformerFactory}.
   */
  private static final SAXTransformerFactory SAX_TRANSFORMER_FACTORY = (SAXTransformerFactory)TransformerFactory.newInstance();
  /**
   * A shared {@link SAXTransformerFactory} which supports
   * <a href="http://xml.apache.org/xalan-j/features.html#incremental">incremental</a> transforms, or <code>null</code>
   * if not available.
   */
  private static final @Nullable SAXTransformerFactory SAX_TRANSFORMER_FACTORY_INCREMENTAL;
  static {
    SAXTransformerFactory stf = null;
    try {
      stf = (SAXTransformerFactory)TransformerFactory.newInstance();
      stf.setAttribute("http://xml.apache.org/xalan/features/incremental", Boolean.TRUE);
    } catch (IllegalArgumentException iae) {
      stf = null;
    }
    SAX_TRANSFORMER_FACTORY_INCREMENTAL = stf;
  }
  /**
   * Does the {@link #SAX_TRANSFORMER_FACTORY} support the <a href="http://www.w3.org/TR/xslt20/">XSLT 2.0</a>
   * &quot;xhtml&quot; <a href="http://www.w3.org/TR/xslt20/#serialization">output serialization</a>
   * {@linkplain OutputKeys#METHOD method}?
   */
  private static final boolean SAX_TRANSFORMER_FACTORY_SUPPORTS_XHTML_OUTPUT;
  static {
    Exception e = null;
    try {
      final Transformer transformer = SAX_TRANSFORMER_FACTORY.newTransformer();
      transformer.setErrorListener(ERROR_LISTENER);
      transformer.setOutputProperty(OutputKeys.METHOD, "xhtml"); // JDK (1.6) does _not_ throw here.
      // With "xhtml" JDK (1.6) throws NullPointerException via TransformerImpl.transform(Source, Result) -> TransformerImpl.getOutputHandler(Result) -> TransletOutputHandlerFactory.getSerializationHandler().
      transformer.transform(new StreamSource(new StringReader("<html><head><title>Test</title></head><body></body></html>")), new StreamResult(new OutputStream() {

        @Override
        public void write(final int b) throws IOException {
          return;
        }

      }));
    } catch (Exception e2) {
      e = e2;
    }
    SAX_TRANSFORMER_FACTORY_SUPPORTS_XHTML_OUTPUT = (e == null) ? true : false;
  }
  /**
   * The {@link MimeType} Object for the <code>"text/html"</code> mime type.
   */
  private static final MimeType TEXT_HTML_MIME_TYPE = newMimeType("text", "html");
  /**
   * @see #setMediaType(MimeType)
   */
  protected @Nullable MimeType mediaType = null;
  /**
   * @see #setEncoding(Charset)
   */
  protected @Nullable Charset encoding = null;
  /**
   * @see #setSystemID(URI)
   */
  protected @Nullable URI systemID = null;
  /**
   * @see #setPublicID(String)
   */
  protected @Nullable String publicID = null;
  /**
   * @see #setTransformations(Iterable)
   */
  protected @Nullable Iterable<? extends @Nullable Templates> transformations = null;
  /**
   * @see #setTransformationParameters(Map)
   */
  protected @Nullable Map<?,?> transformationParameters = null;
  /**
   * @see #setIncremental(boolean)
   */
  protected boolean incremental = false;
  /**
   * @see #setIndent(boolean)
   */
  protected boolean indent = false;
  /**
   * @see #setOmitXMLDeclaration(boolean)
   */
  protected boolean omitXMLDeclaration = false;


  /**
   * Construct a new <code>TransformableEntity</code>.
   */
  protected TransformableEntity() {
    return;
  }

  /**
   * Get the {@linkplain OutputKeys#MEDIA_TYPE media type} to be
   * {@linkplain Transformer#setOutputProperty(String, String) configured} for this entity during
   * {@linkplain #transform(Result) output}.
   * 
   * @return A {@link MimeType} containing the {@linkplain OutputKeys#MEDIA_TYPE media type}.
   */
  public synchronized @Nullable MimeType getMediaType() {
    return mediaType;
  }

  /**
   * Set the {@linkplain OutputKeys#MEDIA_TYPE media type} to be
   * {@linkplain Transformer#setOutputProperty(String, String) configured} for this entity during
   * {@linkplain #transform(Result) output}.
   * 
   * @param mediaType A {@link MimeType} containing the {@linkplain OutputKeys#MEDIA_TYPE media type}.
   */
  public synchronized void setMediaType(final @Nullable MimeType mediaType) {
    this.mediaType = mediaType;
    return;
  }

  /**
   * Get the {@linkplain OutputKeys#ENCODING encoding} to be {@linkplain Transformer#setOutputProperty(String, String)
   * configured} for this entity during {@linkplain #transform(Result) output}.
   * 
   * @return A {@link Charset} containing the {@linkplain OutputKeys#ENCODING encoding}.
   */
  public synchronized @Nullable Charset getEncoding() {
    return encoding;
  }

  /**
   * Set the {@linkplain OutputKeys#ENCODING encoding} to be {@linkplain Transformer#setOutputProperty(String, String)
   * configured} for this entity during {@linkplain #transform(Result) output}.
   * 
   * @param encoding A {@link Charset} containing the {@linkplain OutputKeys#ENCODING encoding}.
   */
  public synchronized void setEncoding(final @Nullable Charset encoding) {
    this.encoding = encoding;
    return;
  }

  /**
   * Get the {@linkplain OutputKeys#DOCTYPE_SYSTEM system identifier} to be
   * {@linkplain Transformer#setOutputProperty(String, String) configured} for this entity during
   * {@linkplain #transform(Result) output}.
   * 
   * @return A {@link URI} containing the {@linkplain OutputKeys#DOCTYPE_SYSTEM system identifier}.
   */
  public synchronized @Nullable URI getSystemID() {
    return systemID;
  }

  /**
   * Set the {@linkplain OutputKeys#DOCTYPE_SYSTEM system identifier} to be
   * {@linkplain Transformer#setOutputProperty(String, String) configured} for this entity during
   * {@linkplain #transform(Result) output}.
   * 
   * @param systemID A {@link URI} containing the {@linkplain OutputKeys#DOCTYPE_SYSTEM system identifier}.
   */
  public synchronized void setSystemID(final @Nullable URI systemID) {
    this.systemID = systemID;
    return;
  }

  /**
   * Get the {@linkplain OutputKeys#DOCTYPE_PUBLIC public identifier} to be
   * {@linkplain Transformer#setOutputProperty(String, String) configured} for this entity during
   * {@linkplain #transform(Result) output}.
   * 
   * @return A String containing the {@linkplain OutputKeys#DOCTYPE_PUBLIC public identifier}.
   */
  public synchronized @Nullable String getPublicID() {
    return publicID;
  }

  /**
   * Set the {@linkplain OutputKeys#DOCTYPE_PUBLIC public identifier} to be
   * {@linkplain Transformer#setOutputProperty(String, String) configured} for this entity during
   * {@linkplain #transform(Result) output}.
   * 
   * @param publicID A String containing the {@linkplain OutputKeys#DOCTYPE_PUBLIC public identifier}.
   */
  public synchronized void setPublicID(final @Nullable String publicID) {
    this.publicID = publicID;
    return;
  }

  /**
   * Get the transformations which will be performed on this entity during {@linkplain #transform(Result) output}.
   * 
   * @return A list of {@link Templates}.
   */
  public synchronized @Nullable Iterable<? extends @Nullable Templates> getTransformations() {
    return transformations;
  }

  /**
   * Set the transformations which will be performed on this entity during {@linkplain #transform(Result) output}.
   * 
   * @param transformations A list of {@link Templates}.
   */
  public synchronized void setTransformations(final @Nullable Iterable<? extends @Nullable Templates> transformations) {
    this.transformations = transformations;
    return;
  }

  /**
   * Get the parameters which will be {@linkplain Transformer#setParameter(String, Object) supplied} to any
   * {@linkplain #setTransformations(Iterable) configured transformations} during {@linkplain #transform(Result) output}
   * .
   * 
   * @return A Map of parameters.
   */
  public synchronized @Nullable Map<?,?> getTransformationParameters() {
    return transformationParameters;
  }

  /**
   * Set the parameters which will be {@linkplain Transformer#setParameter(String, Object) supplied} to any
   * {@linkplain #setTransformations(Iterable) configured transformations} during {@linkplain #transform(Result) output}
   * .
   * 
   * @param transformationParameters A Map of parameters.
   */
  public synchronized void setTransformationParameters(final @Nullable Map<?,?> transformationParameters) {
    this.transformationParameters = transformationParameters;
    return;
  }

  /**
   * Will <a href="http://xml.apache.org/xalan-j/features.html#incremental">incremental</a> transformations be used if
   * available?
   * 
   * @return <code>true</code> if incremental transforms are enabled.
   */
  public synchronized boolean getIncremental() {
    return incremental;
  }

  /**
   * Set if <a href="http://xml.apache.org/xalan-j/features.html#incremental">incremental</a> transformations should be
   * used if available.
   * 
   * @param incremental Should incremental transforms be enabled?
   */
  public synchronized void setIncremental(final boolean incremental) {
    this.incremental = incremental;
    return;
  }

  /**
   * Get the {@linkplain OutputKeys#INDENT indenting policy} to be
   * {@linkplain Transformer#setOutputProperty(String, String) configured} for this entity during
   * {@linkplain #transform(Result) output}.
   * 
   * @return A boolean containing the {@linkplain OutputKeys#INDENT indenting policy}.
   */
  public synchronized boolean getIndent() {
    return indent;
  }

  /**
   * Set the {@linkplain OutputKeys#INDENT indenting policy} to be
   * {@linkplain Transformer#setOutputProperty(String, String) configured} for this entity during
   * {@linkplain #transform(Result) output}.
   * 
   * @param indent A boolean containing the {@linkplain OutputKeys#INDENT indenting policy}.
   */
  public synchronized void setIndent(final boolean indent) {
    this.indent = indent;
    return;
  }

  /**
   * Get the {@linkplain OutputKeys#OMIT_XML_DECLARATION XML declaration policy} to be
   * {@linkplain Transformer#setOutputProperty(String, String) configured} for this entity during
   * {@linkplain #transform(Result) output}.
   * 
   * @return A boolean containing the {@linkplain OutputKeys#OMIT_XML_DECLARATION XML declaration policy}.
   */
  public synchronized boolean getOmitXMLDeclaration() {
    return omitXMLDeclaration;
  }

  /**
   * Set the {@linkplain OutputKeys#OMIT_XML_DECLARATION XML declaration policy} to be
   * {@linkplain Transformer#setOutputProperty(String, String) configured} for this entity during
   * {@linkplain #transform(Result) output}.
   * 
   * @param omitXMLDeclaration A boolean containing the {@linkplain OutputKeys#OMIT_XML_DECLARATION XML declaration
   * policy}.
   */
  public synchronized void setOmitXMLDeclaration(final boolean omitXMLDeclaration) {
    this.omitXMLDeclaration = omitXMLDeclaration;
    return;
  }

  /**
   * Get the final {@linkplain OutputKeys#MEDIA_TYPE media type} which will be {@linkplain #transform(Result) output} by
   * this entity and it's {@linkplain #setTransformations(Iterable) configured transformations}.
   * 
   * @return A {@link MimeType} containing the final {@linkplain OutputKeys#MEDIA_TYPE media type}.
   * @throws MimeTypeParseException If there was a problem {@linkplain MimeType#MimeType(String) constructing} the
   * result.
   */
  public synchronized @Nullable MimeType getOutputMediaType() throws MimeTypeParseException {
    final Optional<? extends @Nullable Templates> lastTransformation = last(transformations, true);
    if (!lastTransformation.isPresent()) return mediaType;
    final String mediaType = lastTransformation.get().getOutputProperties().getProperty(OutputKeys.MEDIA_TYPE);
    return (mediaType != null) ? new MimeType(mediaType) : null;
  }

  /**
   * Get the final {@linkplain OutputKeys#ENCODING encoding} which will be {@linkplain #transform(Result) output} by
   * this entity and it's {@linkplain #setTransformations(Iterable) configured transformations}.
   * 
   * @return A {@link Charset} containing the final {@linkplain OutputKeys#ENCODING encoding}.
   * @throws UnsupportedCharsetException If there was a problem {@linkplain Charset#forName(String) constructing} the
   * result.
   */
  public synchronized @Nullable Charset getOutputEncoding() throws UnsupportedCharsetException {
    final Optional<? extends @Nullable Templates> lastTransformation = last(transformations, true);
    if (!lastTransformation.isPresent()) return encoding;
    final String encoding = lastTransformation.get().getOutputProperties().getProperty(OutputKeys.ENCODING);
    return (encoding != null) ? Charset.forName(encoding) : null;
  }

  /**
   * {@linkplain SAXResult#SAXResult(org.xml.sax.ContentHandler) Create} a new {@link SAXResult} from the supplied
   * <code>transformerHandler</code>.
   * 
   * @param transformerHandler The {@link TransformerHandler} to use to create the SAXResult.
   * @return The {@link SAXResult} that was {@linkplain SAXResult#SAXResult(org.xml.sax.ContentHandler) created} using
   * the supplied <code>transformerHandler</code>.
   * @see SAXResult#SAXResult(org.xml.sax.ContentHandler)
   */
  protected static final SAXResult newSAXResult(final TransformerHandler transformerHandler) {
    final SAXResult saxResult = new SAXResult(transformerHandler);
    saxResult.setLexicalHandler(transformerHandler); // do this explicitly, shouldn't be necessary
    if (transformerHandler.getSystemId() != null) saxResult.setSystemId(transformerHandler.getSystemId());
    return saxResult;
  }

  /**
   * Configure the {@link Transformer} which will drive the {@linkplain #transform(Result) output} of this entity.
   * 
   * @param baseTransformer If there are {@linkplain #getTransformations() configured transformations}, this
   * {@link Transformer} is the one which will feed the chain of {@link TransformerHandler}'s, else it's output will be
   * returned directly.
   */
  protected void setupBaseTransformer(final Transformer baseTransformer) {

    final MimeType mediaType = this.mediaType;
    if (mediaType != null) {

      baseTransformer.setOutputProperty(OutputKeys.MEDIA_TYPE, mediaType.toString());

      // Use the media type to set a default output method as best we can...
      if (isXML(mediaType)) {
        if ((SAX_TRANSFORMER_FACTORY_SUPPORTS_XHTML_OUTPUT) && (isHTML(mediaType))) {
          baseTransformer.setOutputProperty(OutputKeys.METHOD, "xhtml");
        } else {
          baseTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
        }
      } else if (TEXT_HTML_MIME_TYPE.match(mediaType)) {
        baseTransformer.setOutputProperty(OutputKeys.METHOD, "html");
      } else if (mediaType.getPrimaryType().equals("text")) {
        baseTransformer.setOutputProperty(OutputKeys.METHOD, "text");
      }

    }

    if (encoding != null) {
      baseTransformer.setOutputProperty(OutputKeys.ENCODING, encoding.name());
    }

    if (systemID != null) {
      baseTransformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemID.toString());
    }
    if (publicID != null) {
      baseTransformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicID);
    }

    baseTransformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
    baseTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXMLDeclaration ? "yes" : "no");

    return;
  }

  /**
   * Construct the chain of {@link TransformerHandler}'s required to {@linkplain #transform(Result) output} this entity.
   * 
   * @param result The final {@link Result} the chain of handlers should output to.
   * @return A {@link Result} wrapping the head of the created {@link TransformerHandler} chain, or the supplied
   * <code>result</code> if there are no <code>transformations</code>.
   * @throws TransformerConfigurationException If there was a problem
   * {@linkplain SAXTransformerFactory#newTransformerHandler(Templates) constructing} a handler.
   */
  protected Result createTransformerHandlers(final Result result) throws TransformerConfigurationException {
    final Iterable<? extends @Nullable Templates> transformations = this.transformations;
    if ((transformations == null) || (!first(transformations, true).isPresent())) return result;

    final ArrayList<TransformerHandler> transformerHandlers = new ArrayList<TransformerHandler>();
    TransformerHandler lastHandler = null;
    for (Templates template : transformations) {
      if (template == null) continue;

      final TransformerHandler transformerHandler = newTransformerHandler(template, incremental, ERROR_LISTENER);

      final Map<?,?> transformationParameters = this.transformationParameters;
      if (transformationParameters != null) {
        for (Object key : transformationParameters.keySet()) {
          if (key == null) continue;
          final Object value = transformationParameters.get(key);
          if (value == null) continue;
          transformerHandler.getTransformer().setParameter(key.toString(), value);
        }
      }

      if (lastHandler != null) lastHandler.setResult(newSAXResult(transformerHandler)); // Set the previous handler's result to the one we just created.

      transformerHandlers.add(transformerHandler);
      lastHandler = transformerHandler;
    }

    if (lastHandler == null) return result;
    lastHandler.setResult(result);

    final Optional<TransformerHandler> firstHandler = first(transformerHandlers, false);
    return (firstHandler.isPresent()) ? newSAXResult(firstHandler.get()) : result;
  }

  /**
   * Output this entity to the given <code>result</code> while performing any {@linkplain #setTransformations(Iterable)
   * configured transformations}.
   * 
   * @param result The {@link Result} the serialized output should be sent to.
   * @throws TransformerException If there was a problem constructing the transformer(s).
   * @throws IOException If there was an I/O problem writing to the <code>result</code>.
   */
  protected abstract void transformImpl(Result result) throws TransformerException, IOException;

  /**
   * Output this entity to the given <code>result</code> while performing any {@linkplain #setTransformations(Iterable)
   * configured transformations}.
   * 
   * @param result The {@link Result} the serialized output should be sent to.
   * @throws TransformerException If there was a problem constructing the transformer(s).
   * @throws IOException If there was an I/O problem writing to the <code>result</code>.
   */
  public void transform(final Result result) throws TransformerException, IOException {
    try {
      transformImpl(result);
    } catch (TransformerException te) {
      Logger.getLogger(TransformableEntity.class.getName() + ".transform." + TransformerException.class.getSimpleName()).log(Level.FINER, te.getMessage(), te);
      throw te;
    } catch (IOException ioe) {
      Logger.getLogger(TransformableEntity.class.getName() + ".transform." + IOException.class.getSimpleName()).log(Level.FINER, ioe.getMessage(), ioe);
      throw ioe;
    } catch (RuntimeException re) {
      Logger.getLogger(TransformableEntity.class.getName() + ".transform." + RuntimeException.class.getSimpleName()).log(Level.FINER, re.getMessage(), re);
      throw re;
    }
    return;
  }

  /**
   * Return the first item of the supplied <code>iterable</code>.
   * 
   * @param <T> The type of the <code>list</code> items.
   * @param iterable The collection to retrieve the first element of.
   * @param skipNulls Should <code>null</code> values within the collection be ignored?
   * @return The first (optionally non-<code>null</code>) item in <code>iterable</code>.
   */
  protected static final <T> Optional<@NonNull T> first(final @Nullable Iterable<T> iterable, final boolean skipNulls) {
    return Optional.ofNullable(iterable).flatMap((i) -> StreamSupport.stream(i.spliterator(), false).filter((obj) -> !skipNulls || Objects.nonNull(obj)).findFirst());
  }

  /**
   * Return the last item of the supplied <code>iterable</code>.
   * 
   * @param <T> The type of the <code>iterable</code> items.
   * @param iterable The collection to retrieve the last element of.
   * @param skipNulls Should <code>null</code> values within the collection be ignored?
   * @return The last (optionally non-<code>null</code>) item in <code>iterable</code>.
   */
  protected static final <T> Optional<@NonNull T> last(final @Nullable Iterable<T> iterable, final boolean skipNulls) {
    return Optional.ofNullable(iterable).flatMap((i) -> StreamSupport.stream(i.spliterator(), false).filter((obj) -> !skipNulls || Objects.nonNull(obj)).reduce((p, c) -> c));
  }

  /**
   * Is the supplied {@link MimeType} that of an XML document?
   * 
   * @param mimeType The {@link MimeType} in question
   * @return <code>true</code> if the <code>mimeType</code> argument is an XML type.
   */
  protected static final boolean isXML(final @Nullable MimeType mimeType) {
    if (mimeType == null) return false;
    final String subType = mimeType.getSubType();
    if (subType == null) return false;
    if (subType.endsWith("+xml")) return true;
    final String primaryType = mimeType.getPrimaryType();
    if (primaryType == null) return false;
    if (primaryType.equals("application")) {
      if (subType.equals("xml")) return true;
    } else if (primaryType.equals("text")) {
      if (subType.equals("xml")) return true;
    }
    return false;
  }

  /**
   * Is the supplied {@link MimeType} that of an <a href="http://www.w3.org/TR/html/">HTML</a> document?
   * 
   * @param mimeType The {@link MimeType} in question
   * @return <code>true</code> if the <code>mimeType</code> argument is an HTML type.
   */
  protected static final boolean isHTML(final @Nullable MimeType mimeType) {
    if (mimeType == null) return false;
    final String primaryType = mimeType.getPrimaryType();
    final String subType = mimeType.getSubType();
    if ((primaryType == null) || (subType == null)) return false;
    if (primaryType.equals("application")) {
      if (subType.equals("html")) return true;
      if (subType.equals("xhtml+xml")) return true;
    } else if (primaryType.equals("text")) {
      if (subType.equals("html")) return true;
    }
    return false;
  }

  /**
   * Construct a {@linkplain MimeType#MimeType(String, String) new MimeType}, converting any
   * {@link MimeTypeParseException} into a {@link RuntimeException}. This method is intended for creating MimeType
   * constants with known-valid values.
   * 
   * @param primary The primary MIME type.
   * @param sub The MIME sub-type.
   * @return The new MimeType.
   * @see MimeType#MimeType(String, String)
   */
  protected static final MimeType newMimeType(final String primary, final String sub) {
    try {
      return new MimeType(primary, sub);
    } catch (MimeTypeParseException mtpe) {
      throw new RuntimeException(mtpe.getClass().getName() + ": " + mtpe.getMessage(), mtpe);
    }
  }

  /**
   * Create a new {@link Transformer}.
   * 
   * @param incremental Use <a href="http://xml.apache.org/xalan-j/features.html#incremental">incremental</a>
   * transformations if available.
   * @param errorListener The {@link ErrorListener} to {@linkplain Transformer#setErrorListener(ErrorListener) set}.
   * @return The new {@link Transformer}.
   * @throws TransformerConfigurationException If there was a problem creating the handler.
   * @see TransformerFactory#newTransformer()
   * @see Transformer#setErrorListener(ErrorListener)
   */
  protected static final Transformer newTransformer(final boolean incremental, final @Nullable ErrorListener errorListener) throws TransformerConfigurationException {
    final SAXTransformerFactory saxTransformerFactory = ((incremental) && (SAX_TRANSFORMER_FACTORY_INCREMENTAL != null)) ? SAX_TRANSFORMER_FACTORY_INCREMENTAL : SAX_TRANSFORMER_FACTORY;
    synchronized (saxTransformerFactory) {
      final Transformer transformer = saxTransformerFactory.newTransformer();
      if (errorListener != null) transformer.setErrorListener(errorListener);
      return transformer;
    }
  }

  /**
   * Create a new {@link TransformerHandler}.
   * 
   * @param templates Optional {@link Templates} to use when creating the handler.
   * @param incremental Use <a href="http://xml.apache.org/xalan-j/features.html#incremental">incremental</a>
   * transformations if available.
   * @param errorListener The {@link ErrorListener} to {@linkplain Transformer#setErrorListener(ErrorListener) set}.
   * @return The new {@link TransformerHandler}.
   * @throws TransformerConfigurationException If there was a problem creating the handler.
   * @see SAXTransformerFactory#newTransformerHandler(Templates)
   * @see SAXTransformerFactory#newTransformerHandler()
   * @see Transformer#setErrorListener(ErrorListener)
   */
  protected static final TransformerHandler newTransformerHandler(final @Nullable Templates templates, final boolean incremental, final @Nullable ErrorListener errorListener) throws TransformerConfigurationException {
    final SAXTransformerFactory saxTransformerFactory = ((incremental) && (SAX_TRANSFORMER_FACTORY_INCREMENTAL != null)) ? SAX_TRANSFORMER_FACTORY_INCREMENTAL : SAX_TRANSFORMER_FACTORY;
    synchronized (saxTransformerFactory) {
      final TransformerHandler transformerHandler = (templates != null) ? saxTransformerFactory.newTransformerHandler(templates) : saxTransformerFactory.newTransformerHandler();
      if (errorListener != null) transformerHandler.getTransformer().setErrorListener(errorListener);
      return transformerHandler;
    }
  }

}
