<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:insta="http://www.instahealthsolutions.com/namespaces/xsl" 
	xmlns:func="http://exslt.org/functions" 
	xmlns:str= "http://exslt.org/strings" 
	extension-element-prefixes="func insta"
>

<xsl:output method="text"/>
<xsl:strip-space elements="*"/>

<!--
  To run this xslt, do the following in the srxml directory:
  for file in *.srxml ; do xsltproc -o ${file%%.srxml}.srjs  ../install/bin/srxml2js.xslt $file ; done ; sed -i -e 's/srxml/srjs/g' *.srjs ; mv *.srjs ../srjs
-->

 <xsl:variable name="nl">
<xsl:text>
</xsl:text>
  </xsl:variable>

	<xsl:template match="/report-desc">
		<xsl:text>{</xsl:text>
		<xsl:value-of select="$nl"/>

		<!-- write the attributes -->
		<xsl:for-each select="@*[name() != 'dateField']">
			<xsl:text>  "</xsl:text><xsl:value-of select="name()"/><xsl:text>": </xsl:text>
			<xsl:value-of select="insta:str-quote(., true())"/>
			<xsl:value-of select="$nl"/>
		</xsl:for-each>

		<!-- convert dateField comma separated to array -->
		<xsl:if test="@dateField">
			<xsl:text>  "dateFields": [</xsl:text>
			<xsl:for-each select="str:tokenize(@dateField, ',')">
				<xsl:value-of select="insta:str-quote(., position() != last())"/>
			</xsl:for-each>
			<xsl:text>],</xsl:text>
			<xsl:value-of select="$nl"/>
		</xsl:if>

		<xsl:if test="description">
			<xsl:text>  "description": </xsl:text>
			<xsl:value-of select="insta:str-quote(description)"/>
			<xsl:value-of select="$nl"/>
		</xsl:if>

		<xsl:if test="tableName">
			<xsl:text>  "tableName": [</xsl:text>
			<xsl:for-each select="tableName">
				<xsl:value-of select="insta:str-quote(., position() != last())"/>
			</xsl:for-each>
			<xsl:text>],</xsl:text>
			<xsl:value-of select="$nl"/>
		</xsl:if>

		<xsl:if test="query">
			<xsl:text>  "query": [</xsl:text>
			<xsl:for-each select="query">
				<xsl:value-of select="insta:str-quote(., position() != last())"/>
			</xsl:for-each>
			<xsl:text>],</xsl:text>
			<xsl:value-of select="$nl"/>
		</xsl:if>

		<xsl:if test="include">
			<xsl:text>  "includes": [</xsl:text>
			<xsl:for-each select="include">
				<xsl:value-of select="insta:str-quote(., position() != last())"/>
			</xsl:for-each>
			<xsl:text>],</xsl:text>
			<xsl:value-of select="$nl"/>
		</xsl:if>

		<xsl:text>  "defaultShowFields": [</xsl:text>
		<xsl:value-of select="$nl"/>
		<xsl:choose>
			<xsl:when test="default-show">
				<xsl:for-each select="default-show/field">
					<xsl:text>    </xsl:text><xsl:value-of select="insta:str-quote(., position() != last())"/>
					<xsl:value-of select="$nl"/>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="fields/field[not(@defaultShow='false')]">
					<xsl:text>    </xsl:text><xsl:value-of select="insta:str-quote(@name, position() != last())"/>
					<xsl:value-of select="$nl"/>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>  ],</xsl:text>
		<xsl:value-of select="$nl"/>

		<xsl:apply-templates select="fields"/>

		<xsl:text>}</xsl:text>
		<xsl:value-of select="$nl"/>
	</xsl:template>

	<!-- fields -->
	<xsl:template match="fields">
		<xsl:text>  "fields": {</xsl:text>
		<xsl:value-of select="$nl"/>
		<xsl:apply-templates select="field"/>
		<xsl:text>  }</xsl:text>
		<xsl:value-of select="$nl"/>
	</xsl:template>

	<!-- single field -->
	<xsl:template match="field">
		<xsl:text>    "</xsl:text><xsl:value-of select="@name"/><xsl:text>": {</xsl:text>

		<xsl:for-each select="@*[name() != 'defaultShow' and name() != 'name']">
			<xsl:value-of select="$nl"/>
			<xsl:text>      "</xsl:text><xsl:value-of select="name()"/><xsl:text>": </xsl:text>
			<xsl:value-of select="insta:str-quote(., position() != last())"/>
		</xsl:for-each>

		<xsl:if test="value-query">
			<xsl:text>,</xsl:text><xsl:value-of select="$nl"/>
			<xsl:text>      "allowedValuesQuery": </xsl:text>
			<xsl:value-of select="insta:str-quote(., false())"/>
		</xsl:if>

		<xsl:if test="value">
			<xsl:text>,</xsl:text><xsl:value-of select="$nl"/>
			<xsl:text>      "allowedValues": [</xsl:text>
			<xsl:for-each select="value">
				<xsl:value-of select="insta:str-quote(., position() != last())"/>
			</xsl:for-each>
			<xsl:text>]</xsl:text>
		</xsl:if>

		<xsl:value-of select="$nl"/>
		<xsl:text>    }</xsl:text>

		<xsl:if test="position() != last()">
			<xsl:text>,</xsl:text>
		</xsl:if>
		<xsl:value-of select="$nl"/>
	</xsl:template>

	<func:function name="insta:str-quote">
		<xsl:param name="string"/>
		<xsl:param name="comma" select="true()"/>

		<func:result>
			<xsl:if test="$string != 'true' and $string != 'false'"><xsl:text>"</xsl:text></xsl:if>
			<xsl:value-of select="normalize-space($string)"/>
			<xsl:if test="$string != 'true' and $string != 'false'"><xsl:text>"</xsl:text></xsl:if>
			<xsl:if test="$comma">,</xsl:if>
		</func:result>
	</func:function>

</xsl:stylesheet>

