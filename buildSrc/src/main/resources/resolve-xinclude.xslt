<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xi="http://www.w3.org/2001/XInclude">

  <!-- Identity template: Copies everything as-is -->
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Process XInclude -->
  <xsl:template match="xi:include">
    <xsl:variable name="file" select="@href"/>
    <xsl:apply-templates select="document($file)/*"/>
  </xsl:template>

</xsl:stylesheet>
