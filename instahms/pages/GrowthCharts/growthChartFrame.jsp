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
<title><insta:ltext key="growthchart.frame.title"/></title>
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="script" file="widgets.js" />

<script type="text/javascript">
function printPdf() {
	var mrNo = document.getElementById('mrNo').value;
	var chartType = document.getElementById('chartType').value;
	window.open(cpath+"/GrowthCharts/GrowthChartsAction.do?method=createCharts&mr_no="+mrNo+"&chart_type="+chartType);
}
</script>
<insta:js-bundle prefix="growthchart.frame"/>
</head>
<body onload="">
<h1><insta:ltext key="growthchart.frame.heading"/></h1>
	<insta:feedback-panel/>
	<insta:patientdetails  visitid="${patient_id}" showClinicalInfo="true"/>

	<form action="${cpath}/GrowthCharts/GrowthChartsAction.do?method=createCharts"	method="POST" name="growthChart" autocomplete="off">
		<input type="hidden" id="chartType" name="chartType" value="${ifn:cleanHtmlAttribute(chart_type)}" />
		<input type="hidden" id="mrNo" name="mrNo" value="${patient.mr_no}" />
		<table width="100%" height="100%" border="0">
			<tr>
				<td>
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel"><insta:ltext key="growthchart.frame.fieldlabel"/></legend>
						<table border="0" width="100%">
							<tr>
								<td>
									<table width="100%" >
										<tr>
											<td valign="top">
												<table id="documentSummary" class="formtable">
												</table>
											</td>
										</tr>
										<tr>
											<td valign="top">
												<table border="0" width="100%" height="685px">
													<tr>
														<td valign="top">
															<iframe  id="growthChartFrame" name="growthChartFrame"
																src="${cpath}/GrowthCharts/GrowthChartsAction.do?method=createCharts&chart_type=${ifn:cleanURL(chart_type)}&patient_id=${patient.patient_id}&mr_no=${patient.mr_no}"
																height="99%" width="100%" frameborder="1" >
															</iframe>
														</td>
													</tr>
												</table>
											</td>
										</tr>
										<tr>
											<td>
												<table id="navigate" class="formtable" style="margin-top: 0px">
													<tr><c:set var="print"><insta:ltext key="growthchart.frame.print"/></c:set>
														<td><input type="button" name="chartPrintBtn" id="chartPrintBtn" value="${ifn:cleanHtmlAttribute(print)}" onclick="return printPdf();"/></td>
														<td></td>
														<td></td>
														<td></td>
														<td></td>
														<td></td>
													</tr>
												</table>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
		</table>
	</form>
</body>
</html>