module com.hubick.util.xml_transformable_document {
  requires transitive org.eclipse.jdt.annotation;
  requires transitive jakarta.activation;
  requires transitive java.xml;
  requires java.logging;

  exports com.hubick.xml_transformable_document;
  exports com.hubick.xml_transformable_document.dom;
  exports com.hubick.xml_transformable_document.sax;
}
