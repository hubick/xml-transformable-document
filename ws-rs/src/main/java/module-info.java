module com.hubick.util.xml_transformable_document.ws.rs {
  requires transitive org.eclipse.jdt.annotation;
  requires transitive java.ws.rs;
  requires transitive com.hubick.util.xml_transformable_document;
  requires java.logging;

  exports com.hubick.xml_transformable_document.ws.rs.provider;
}
