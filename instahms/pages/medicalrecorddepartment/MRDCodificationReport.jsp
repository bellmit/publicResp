<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
	<head>
		<insta:link type="css" file="widget.css"/>
		<insta:link type="script" file="widget.js"/>
		<insta:link type="script" file="hmsvalidation.js"/>

		<script type="text/javascript">

			function onSubmit(option) {
				document.forms[0].format.value = option;
				if (option == 'pdf'){
					document.forms[0].target = "_blank";
				}else {
					document.forms[0].target = "";
				}
				return true;
			}
		</script>
	</head>
	<body>
		<form action="${pageContext.request.contextPath}/pages/medicalrecorddepartment/MRDUpdate.do" >
			<input type="hidden" name="_method" value="print">
			<input type="hidden" name="format" value="screen">
			<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}">

			<h1>MRD Codification Report </h1>

			<table class="searchFormTable" width="20%">
				<tr>
					<td>
						<div class="sfLabel">Category</div>
						<div class="sfField">
							<insta:checkgroup name="category" selValues="${paramValues.reg_time}" opvalues="diagnosis,encounter,eandmcodes,treatment,drugs,observations,drgcodes"
							optexts="Diagnosis,Encounter,E&MCodes,Treatment,Drug,Observations,DRGCodes"/>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Code Type</div>
						<div class="sfField">
							<input type="text" name="code_type"/>
						</div>
					</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
						<td>
							<input type="submit" value="View" onclick="return onSubmit('screen')">
						</td>
						<td>
							<input type="submit" value="Print" onclick="return onSubmit('pdf')">
						</td>
				  </tr>
			</table>
		</form>
	</body>
</html>