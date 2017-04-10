<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template name="exclaim">
    <xsl:param name="input" />

    <xsl:value-of select="$input" />    
    <xsl:text>!</xsl:text>
  </xsl:template>

</xsl:stylesheet>
