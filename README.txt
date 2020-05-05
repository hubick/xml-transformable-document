Transformable Document
----------------------

A Java library for managing an XML document along with XSLT transformations.

This library provides you a single object for managing your XML content, that also includes the ability to configure and perform a streaming incremental serialization of that content, including applying a sequence of XSLT transformations, parameters to those transformations, media type, encoding, indentation, doctype, etc, without ever being required to deal directly with a TransformerFactory or linking any TransformerHandler's or SAXResult's yourself.

To use this library, create a TransformableDocument instance in order to manage your document:

TransformableDocument doc = new TransformableDocument();

You can then access the underlying org.w3c.dom.Document and use the standard DOM (Document Object Model) methods for creating and manipulating it's content:

doc.getDocument().appendChild(doc.getDocument().createElementNS(XMLConstants.NULL_NS_URI, "test"));
doc.getDocument().getDocumentElement().appendChild(doc.getDocument().createTextNode("Hello World"));

You can optionally provide a list of one or more transformations (javax.xml.transform.Templates) that will be linked in sequence and performed on the document when output:

doc.setTransformations(...);

There are also several serialization configuration parameters (corresponding to javax.xml.transform.OutputKeys, etc) which can be configured for the output, for example:

doc.setIndent(false);
doc.setOmitXMLDeclaration(true);

Once you are done creating and configuring your document, it's then simple to serialize it's output:

doc.transform(new StreamResult(System.out));

The default setup is to configure output to be generated via an incremental streaming transformation (see doc.setIncremental(boolean) for details).


Documentation and Examples
--------------------------

More detailed documentation is included in the Javadoc for each class, and there are complete executable usage examples in the 'examples' packages under the 'src/test/java/' folder.
