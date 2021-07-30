<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title><insta:ltext key="growthchart.addshow.title"/></title>
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="widgets.js" />

	<script type="text/javascript">
	</script>
	<insta:js-bundle prefix="growthchart.addshow"/>
</head>
<body onload="">
	<h1><insta:ltext key="growthchart.addshow.heading"/></h1>
	<insta:feedback-panel/>
	<insta:patientdetails  visitid="${param.patient_id}" showClinicalInfo="true"/>

	<form action="${cpath}/GrowthCharts/GrowthChartsAction.do?method=list"	method="POST" name="growthChartForm" autocomplete="off">
		<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />
		<input type="hidden" name="paramType" value="${paramType}"/>
		<input type="hidden" name="visit_type" id="visit_type" value="${patient.visit_type}"/>
		<input type="hidden" name="patient_gender" id="patient_gender" value="${patient.patient_gender}"/>

		<table class="formtable">
			<tr>
				<td><c:set var="lwa"><insta:ltext key="growthchart.addshow.length.weight.for.age"/></c:set>
					<insta:screenlink screenId="growth_chart" label="${lwa}" target="_blank"
				extraParam="?method=getScreen&chart_type=L,WA&patient_id=${patient.patient_id}&mr_no=${patient.mr_no}"/>
				</td>
			</tr>
			<tr>
				<td><c:set var="hc"><insta:ltext key="growthchart.addshow.head.circumference.for.age"/></c:set>
					<insta:screenlink screenId="growth_chart" label="${hc}" target="_blank"
				extraParam="?method=getScreen&chart_type=HC&patient_id=${patient.patient_id}&mr_no=${patient.mr_no}"/>
				</td>
			</tr>
			<tr>
				<td><c:set var="wl"><insta:ltext key="growthchart.addshow.weight.for.length"/></c:set>
					<insta:screenlink screenId="growth_chart" label="${wl}" target="_blank"
				extraParam="?method=getScreen&chart_type=WL&patient_id=${patient.patient_id}&mr_no=${patient.mr_no}"/>
				</td>
			</tr>
			<tr>
				<td><c:set var="swa"><insta:ltext key="growthchart.addshow.stature.weight.for.age"/></c:set>
					<insta:screenlink screenId="growth_chart" label="${swa}" target="_blank"
				extraParam="?method=getScreen&chart_type=S,WA&patient_id=${patient.patient_id}&mr_no=${patient.mr_no}"/>
				</td>
			</tr>
			<tr>
				<td><c:set var="bmi"><insta:ltext key="growthchart.addshow.bmi.for.age"/></c:set>
					<insta:screenlink screenId="growth_chart" label="${bmi}" target="_blank"
				extraParam="?method=getScreen&chart_type=BMI&patient_id=${patient.patient_id}&mr_no=${patient.mr_no}"/>
				</td>
			</tr>
			<tr>
				<td><c:set var="ws"><insta:ltext key="growthchart.addshow.weight.stature.for.age"/></c:set>
					<insta:screenlink screenId="growth_chart" label="${ws}" target="_blank"
				extraParam="?method=getScreen&chart_type=WS&patient_id=${patient.patient_id}&mr_no=${patient.mr_no}"/>
				</td>
			</tr>
		</table>
</form>
</body>
</html>