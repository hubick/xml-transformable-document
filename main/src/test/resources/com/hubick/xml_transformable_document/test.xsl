<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="exclaim.xsl" />

  <xsl:output indent="no" media-type="application/xhtml+xml" method="xml" omit-xml-declaration="yes" />

  <xsl:template match="@*|node()">
    <xsl:apply-templates select="@*|node()" />
  </xsl:template>

  <xsl:template match="/test">
    <xsl:element name="test">
      <xsl:call-template name="exclaim">
        <xsl:with-param name="input" select="." />
      </xsl:call-template>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
