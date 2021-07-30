<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ page import="com.insta.hms.common.Encoder" %>

<html>
<head>
<meta name="viewport" content="initial-scale=1">
<title>Graphical Report - Insta HMS</title>
<insta:link type="css" file="c3.min.css" />
<insta:link type="css" file="magicsuggest.css" />
<insta:link type="css" file="graphicalReport.css" />
<insta:link type="script" file="jquery-2.2.4.min.js" />
<insta:link type="script" file="reports/magicsuggest.js" />
<insta:link type="script" file="reports/jspdf.min.js" />
<insta:link type="script" file="reports/d3.v3.js" />
<insta:link type="script" file="reports/c3.min.js" />
<insta:link type="script" file="analytics/tracking.js" />
</head>

<body>
	<div id="container">
		<div id="loading">
		<span class="helper"></span>
		<insta:link type="image" file="Preloader_2-128px.gif" id="loading-image" alt="Loading..."/>
		</div>
		<div id="title">
			<div><span id="titlename"> </span>
				 <span id="dateRange" style="float:right"></span>
			</div>
		</div>
		<div id="filters">
			<div id="filter-by">
				<label style="color: #4a4a4a">FILTER BY</label>
			</div>
			<label class="filter-label"> <span id="filterName1"></span>
				<div id="filter1"></div>
			</label> <label class="filter-label"> <span id="filterName2"></span>
				<div id="filter2"></div>
			</label>
		</div>
		<div id="button-row">
			<div class="button" onclick="zoom('in')">
				<span class="zoom-icon zoom-in-icon"></span> Zoom In
			</div>
			<div class="button disabled" onclick="zoom('out')">
				<span class="zoom-icon zoom-out-icon"></span> Zoom Out
			</div>
			<div class="button disabled" onclick="zoom('reset')">
				<span class="zoom-icon zoom-reset-icon"></span> Reset
			</div>

			<div id="export" class="button ms-ctn">
				<span> Export Graph
					<div class="ms-trigger">
						<div class="arrow-icon"></div>
					</div>
				</span>
				<div class="dropdown-menu">
					<div onclick="exportGraph('pdf')">PDF</div>
					<div onclick="exportGraph('png')">PNG</div>
					<div onclick="exportGraph('jpeg')">JPEG</div>
				</div>
			</div>
		<div id="chart"></div>
		</div>
		<canvas id="svg-canvas" style="display: none"></canvas>
	</div>
	<script type="text/javascript">
		var url = '${url}';
		var schema = '${ifn:cleanJavaScript(hospital)}';
	</script>
	<insta:link type="script" file="reports/graphicalReport.js" />
</body>
</html>