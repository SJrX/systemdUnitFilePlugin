<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="xml"/>

  <!-- Convert citerefentries into HTML a tags -->
  <!-- This was adapted from systemd/man/rules/custom-html.xsl -->

  <xsl:template match="citerefentry">
    <a>
      <xsl:attribute name="href">
        <xsl:text>http://man7.org/linux/man-pages/man</xsl:text>
        <xsl:value-of select="manvolnum"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="refentrytitle"/>
        <xsl:text>.</xsl:text>
        <xsl:value-of select="manvolnum"/>
        <xsl:text>.html</xsl:text>
      </xsl:attribute>
      <xsl:value-of select="refentrytitle"/>(<xsl:value-of select="manvolnum"/>)
    </a>
  </xsl:template>

  <!-- Convert varname tags to be wrapped with <var> elements for formatting -->
  <xsl:template match="varname">
    <var><xsl:value-of select="."/></var>
  </xsl:template>

  <!-- Convert option tags to be wrapped with <var> elements for formatting -->
  <xsl:template match="option">
    <var><xsl:value-of select="."/></var>
  </xsl:template>

  <!-- Convert ulink elements to a tags -->
  <xsl:template match="ulink">
    <a>
      <xsl:attribute name="href">
        <xsl:value-of select="@url"/>
      </xsl:attribute>
      <xsl:value-of select="text()"/>
    </a>
  </xsl:template>

  <!-- Convert varname tags to be wrapped with <var> elements for formatting -->
  <xsl:template match="/">
    <parameterlist>
      <!-- For each varlist in source that is not environment variables (see comment in GenerateDataFromManPages.groovy) -->
    <xsl:for-each select="/refentry/refsect1/variablelist[not(contains(@class,'environment-variables'))]/varlistentry">
      <!-- for each term/varname underneath -->
      <xsl:for-each select="term/varname">
        <parameter>
          <name><xsl:value-of select="."/></name>

          <!-- We need to keep track of the section so we can know it is-->

          <section><xsl:value-of select="ancestor::refsect1/title[text()]"/></section>

          <description>
            <!-- For each paragraph apply templates -->
            <xsl:for-each select="../../listitem/para">
              <paragraph>
                <xsl:apply-templates/>
              </paragraph>
            </xsl:for-each>
          </description>
        </parameter>
      </xsl:for-each>
    </xsl:for-each>
    <!-- For each varlist in source that is not environment variables (see comment in GenerateDataFromManPages.groovy) -->
    <xsl:for-each select="/refentry/refsect1/refsect2/variablelist[not(contains(@class,'environment-variables'))]/varlistentry">
      <!-- for each term/varname underneath -->
      <xsl:for-each select="term/varname">
        <parameter>
          <name><xsl:value-of select="."/></name>

          <!-- We need to keep track of the section so we can know it is-->

          <section><xsl:value-of select="ancestor::refsect1/title[text()]"/></section>

          <description>
            <!-- For each paragraph apply templates -->
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



  <!-- Not sure if this matters anymore :( -->
  <xsl:template match="listitem/para">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>
