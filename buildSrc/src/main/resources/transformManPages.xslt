<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="xml"/>
  <xsl:template match="citerefentry">
    <a>
      <xsl:attribute name="href">
      <!-- <citerefentry><refentrytitle>systemd.kill</refentrytitle><manvolnum>5</manvolnum></citerefentry> -->
        <xsl:text>http://man7.org/linux/man-pages/man</xsl:text>
        <xsl:value-of select="manvolnum"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="refentrytitle"/>
        <xsl:text>.</xsl:text>
        <xsl:value-of select="manvolnum"/>
        <xsl:text>.html</xsl:text>
      </xsl:attribute>
    <xsl:value-of select="refentrytitle"/>(<xsl:value-of select="manvolnum"/>)</a>
  </xsl:template>

  <xsl:template match="varname">
    <var><xsl:value-of select="."/></var>
  </xsl:template>

  <xsl:template match="option">
    <var><xsl:value-of select="."/></var>
  </xsl:template>


  <xsl:template match="/">
    <parameterlist>
    <xsl:for-each select="/refentry/refsect1/variablelist[not(contains(@class,'environment-variables'))]/varlistentry">


      <xsl:for-each select="term/varname">
        <parameter>
          <name><xsl:value-of select="."/></name>
          <section><xsl:value-of select="../../../../title[text()]"/></section>


          <description>
            <xsl:for-each select="../../listitem/para">
              <paragraph>
                <xsl:apply-templates/>
              </paragraph>
            </xsl:for-each>
          </description>


        </parameter>

      </xsl:for-each>

    </xsl:for-each>
    </parameterlist>
  </xsl:template>

  <xsl:template match="listitem/para">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>