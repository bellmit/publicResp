<?xml version="1.0" encoding="UTF-8"?>
<!--
  FindBugs - Find bugs in Java programs
  Copyright (C) 2004,2005 University of Maryland
  Copyright (C) 2005, Chris Nappin
  Copyright (C) 2015, 2017, Brahim Djoudi (modifications)

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
-->
<xsl:stylesheet version="2.0"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output
	method="xml"
	omit-xml-declaration="yes"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	indent="yes"
	encoding="UTF-8"/>

<xsl:variable name="bugTableHeader">
	<tr class="tableheader">
		<th align="center">Priority</th>
		<th align="left">Details</th>
	</tr>
</xsl:variable>

<xsl:template match="/">
	<html>
	<head>
		<title>SpotBugs Report</title>
		<style type="text/css">
            body {
            	font-family: Candara, Arial, Helvetica, sans-serif;
                padding: 10px;
                margin: 0;
                width: 100%;
                box-sizing: border-box;
                font-size: 11pt;
                word-break: break-all;
            }
            p {
                font-size: 11pt;
                margin: 10px 0;
            }
            table.summary p{
                margin: 5px 0;
                font-size: 9pt;
            }
            tt {
                display: inline;
            }
            dl {
                margin: 0;
            }
            h2 {
                font-size: 16pt;
                margin: 30px 0 5px 0;
            }
            h1 {
                font-size: 20pt;
                margin: 30px 0 5px 0;
            }
            table {
                padding: 0;
                margin: 0;
                border: 1px solid black;
                width: 100%;
                border-collapse: collapse;
            }

            table.summary {
                border: 0;
            }
            table.summary td.metrics-cell {
                border-right: 25px solid white;
                vertical-align: top;
                width: 50%;
            }
            table.summary td.summary-cell {
                border-left: 25px solid white;
                vertical-align: top;
                width: 50%;
            }
            table.severity-summary td, 
            table.classification-summary td,
            table.severity-summary th, 
            table.classification-summary th,
            table.warningtable th {
                padding: 5px 10px;
            } 

            table.severity-summary tr, 
            table.classification-summary tr, 
            table.warningtable tr {
                border: 1px solid;
                border-left: 3px solid;
                width: 100%;
            }

            div.short-message {
                padding: 10px;
                font-weight: bold;
                font-size: 12pt;
            }            
            div.long-message {
                padding: 0px 10px;
                font-size: 11pt;
                display: inline-block;
            }            
            div.bug-details {
                padding: 10px 10px 10px 50px;
                font-size: 9pt;
                line-height: 1.5;
            }            
            table.warningtable tr td:nth-child(2){
                background: white;
                font-size:9pt;
                width: 80%;
                box-sizing: border-box;
            }
            table.warningtable tr td:nth-child(1){
                font-size:12pt;
                padding: 10px 0;
                font-weight: bold;
                text-align: center;
                width:10%;
                box-sizing: border-box;
            }

            .tablerow0 {
                background: #EEEEEE;
            }

            .tablerow1 {
                background: white;
            }

            .detailrow0 {
                background: #EEEEEE;
            }

            .detailrow1 {
                background: white;
            }

            .tableheader {
                font-size: 11pt;
                background: #D7D7D7;
            }

            .high {
                background: #e60000;
            }

            .medium {
            	background: #ffa500;
            }

            .low {
                background: #00b300;
            }

            pre {
                font-size: 9pt;
                font-family: Monospace !important;
                box-shadow: 0 0;
                width: 100%;
                color: black;
                border-width: 1px 1px 1px 6px;
                border-style: solid;
                padding: 2ex;
                margin: 2ex 2ex 2ex 2ex;
                overflow: auto;
                box-sizing:border-box;
                border-radius: 0px;
                border-color: #996666;
                background: rgb(232, 239, 244);
            }
        </style>
	</head>
    <script>
    	var analysisTimestamp=new Date(<xsl:value-of select="/BugCollection/@analysisTimestamp" />);
    	function updateAnalysisTimestamp() {
    		var dtEl = document.getElementById("analysisDate");
            var tmEl = document.getElementById("analysisTime");
            dtEl.innerHTML = analysisTimestamp.toDateString();
    		tmEl.innerHTML = analysisTimestamp.toTimeString();
    	}
    </script>
	<xsl:variable name="unique-catkey" select="/BugCollection/BugCategory/@category"/>
	<!--xsl:variable name="unique-catkey" select="/BugCollection/BugInstance[generate-id() = generate-id(key('bug-category-key',@category))]/@category"/-->
	<body onload="updateAnalysisTimestamp();">

	<h1>SpotBugs Report</h1>
		<p>Produced using <a href="https://spotbugs.github.io">SpotBugs </a> <xsl:value-of select="/BugCollection/@version"/>.</p>
		<p>Project: <b><xsl:value-of select="/BugCollection/Project/@projectName" /></b></p>
		<p>Scan performed on <b id="analysisDate">-</b> at <b id="analysisTime">-</b></p>
		<table class="summary">
			<tr>
				<td class="metrics-cell">
					<h2>Metrics</h2>
					<xsl:apply-templates select="/BugCollection/FindBugsSummary"/>
				</td>
				<td class="summary-cell">
					<h2>Summary</h2>
					<table class="classification-summary">
					    <tr class="tableheader">
							<th align="left">Warning Type</th>
							<th align="right">Total</th>
						</tr>

					<xsl:for-each select="$unique-catkey">
						<xsl:sort select="." order="ascending"/>
						<xsl:variable name="catkey" select="."/>
						<xsl:variable name="catdesc" select="/BugCollection/BugCategory[@category=$catkey]/Description"/>
						<xsl:variable name="styleclass">
							<xsl:choose><xsl:when test="position() mod 2 = 1">tablerow0</xsl:when>
								<xsl:otherwise>tablerow1</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>

						<tr class="{$styleclass}">
							<td><a href="#Warnings_{$catkey}"><xsl:value-of select="$catdesc"/> Warnings</a></td>
							<td align="right"><xsl:value-of select="count(/BugCollection/BugInstance[@category=$catkey])"/></td>
						</tr>
					</xsl:for-each>

					<xsl:variable name="styleclass">
						<xsl:choose><xsl:when test="count($unique-catkey) mod 2 = 0">tablerow0</xsl:when>
							<xsl:otherwise>tablerow1</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
						<tr class="{$styleclass}">
						    <td><b>Total</b></td>
						    <td align="right"><b><xsl:value-of select="count(/BugCollection/BugInstance)"/></b></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>

	<h1>Warnings</h1>

	<p>Click on each warning link to see a full description of the issue, and
	    details of how to resolve it.</p>

	<xsl:for-each select="$unique-catkey">
		<xsl:sort select="." order="ascending"/>
		<xsl:variable name="catkey" select="."/>
		<xsl:variable name="catdesc" select="/BugCollection/BugCategory[@category=$catkey]/Description"/>

		<xsl:call-template name="generateWarningTable">
			<xsl:with-param name="warningSet" select="/BugCollection/BugInstance[@category=$catkey]"/>
			<xsl:with-param name="sectionTitle"><xsl:value-of select="$catdesc"/> Warnings</xsl:with-param>
			<xsl:with-param name="sectionId">Warnings_<xsl:value-of select="$catkey"/></xsl:with-param>
		</xsl:call-template>
	</xsl:for-each>

    <p><br/><br/></p>
	<h1><a name="Details">Warning Types</a></h1>

	<xsl:apply-templates select="/BugCollection/BugPattern">
		<xsl:sort select="@abbrev"/>
		<xsl:sort select="ShortDescription"/>
	</xsl:apply-templates>

	</body>
	</html>
</xsl:template>

<xsl:template match="BugInstance">
	<xsl:variable name="warningId"><xsl:value-of select="generate-id()"/></xsl:variable>

	<tr>
		<!-- class="tablerow{position() mod 2}" -->
		<xsl:choose>
			<xsl:when test="@priority = 1"><xsl:attribute name="class">high</xsl:attribute></xsl:when>
			<xsl:when test="@priority = 2"><xsl:attribute name="class">medium</xsl:attribute></xsl:when>
			<xsl:when test="@priority = 3"><xsl:attribute name="class">low</xsl:attribute></xsl:when>
			<xsl:otherwise><xsl:attribute name="bgcolor">#fdfdfd</xsl:attribute></xsl:otherwise>
		</xsl:choose>
		<td width="20%" valign="top" align="center">
			<xsl:choose>
				<xsl:when test="@priority = 1"><strong>High</strong></xsl:when>
				<xsl:when test="@priority = 2">Medium</xsl:when>
				<xsl:when test="@priority = 3">Low</xsl:when>
				<xsl:otherwise>Unknown</xsl:otherwise>
			</xsl:choose>
		</td>
		<td width="80%">
			<div class="short-message"><a href="#{@type}"><xsl:value-of select="ShortMessage"/></a></div>
			<div class="long-message"><xsl:value-of select="LongMessage"/></div>
			<div class="bug-details">
				<!--  add source filename and line number(s), if any -->
				<xsl:if test="SourceLine">
					In file <tt><strong><xsl:value-of select="SourceLine/@sourcefile"/></strong></tt>,
					<xsl:choose>
						<xsl:when test="SourceLine/@start = SourceLine/@end">
						line <xsl:value-of select="SourceLine/@start"/>
						</xsl:when>
						<xsl:otherwise>
						lines <xsl:value-of select="SourceLine/@start"/>
						    to <xsl:value-of select="SourceLine/@end"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>

				<xsl:for-each select="./*/Message">
					<br/><xsl:value-of select="text()"/>
				</xsl:for-each>				
			</div>
		</td>
	</tr>
</xsl:template>

<xsl:template match="BugPattern">
	<h2><a name="{@type}"><xsl:value-of select="ShortDescription"/></a></h2>
	<xsl:value-of select="Details" disable-output-escaping="yes"/>
	<p><br/><br/></p>
</xsl:template>

<xsl:template name="generateWarningTable">
	<xsl:param name="warningSet"/>
	<xsl:param name="sectionTitle"/>
	<xsl:param name="sectionId"/>

	<h2><a name="{$sectionId}"><xsl:value-of select="$sectionTitle"/></a></h2>
	<table class="warningtable">
		<xsl:copy-of select="$bugTableHeader"/>
		<xsl:choose>
		    <xsl:when test="count($warningSet) &gt; 0">
				<xsl:apply-templates select="$warningSet">
					<xsl:sort select="@priority"/>
					<xsl:sort select="@abbrev"/>
					<xsl:sort select="Class/@classname"/>
				</xsl:apply-templates>
		    </xsl:when>
		    <xsl:otherwise>
		        <tr><td colspan="2"><p><i>None</i></p></td></tr>
		    </xsl:otherwise>
		</xsl:choose>
	</table>
	<p><br/><br/></p>
</xsl:template>

<xsl:template match="FindBugsSummary">
    <xsl:variable name="kloc" select="@total_size div 1000.0"/>
    <xsl:variable name="format" select="'#######0.00'"/>

	<table class="severity-summary">
	    <tr class="tableheader">
			<th align="left">Metric</th>
			<th align="right">Total</th>
			<th align="right">Density*</th>
		</tr>
		<tr class="high" >
			<td>High Priority Warnings</td>
			<td align="right"><xsl:value-of select="@priority_1"/></td>
			<td align="right"><xsl:value-of select="format-number(@priority_1 div $kloc, $format)"/></td>
		</tr>
		<tr class="medium">
			<td>Medium Priority Warnings</td>
			<td align="right"><xsl:value-of select="@priority_2"/></td>
			<td align="right"><xsl:value-of select="format-number(@priority_2 div $kloc, $format)"/></td>
		</tr>

    <xsl:choose>
		<xsl:when test="@priority_3">
			<tr class="low">
				<td>Low Priority Warnings</td>
				<td align="right"><xsl:value-of select="@priority_3"/></td>
				<td align="right"><xsl:value-of select="format-number(@priority_3 div $kloc, $format)"/></td>
			</tr>
		</xsl:when>
	</xsl:choose>

		<tr bgcolor="#f0f0f0">
			<td><b>Total Warnings</b></td>
			<td align="right"><b><xsl:value-of select="@total_bugs"/></b></td>
			<td align="right"><b><xsl:value-of select="format-number(@total_bugs div $kloc, $format)"/></b></td>
		</tr>
	</table>
	<p><xsl:value-of select="@total_size"/> lines of code analysed,
	in <xsl:value-of select="@total_classes"/> classes,
	in <xsl:value-of select="@num_packages"/> packages.</p>
	<p><i>(* Defects per thousand lines of non-commenting source statements)</i></p>
</xsl:template>

</xsl:stylesheet>