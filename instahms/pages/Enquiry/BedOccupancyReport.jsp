<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page isELIgnored="false"%>
<%
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-store");
response.setHeader("Expires", "0");
%>

<html>
	<head>
		<title>Bed Occupancy Report - Insta HMS</title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="script" file="date_go.js"/>

		<script language="javascript" type="text/javascript">

 			function selectordeselectAll(){
				var length = document.forms[0].allWardNames.length;
				var typeAll = document.forms[0].all.checked;
				var disabled = typeAll;
				for (var i=0;i<length;i++){
					document.forms[0].allWardNames[i].selected = disabled;
				}
			}

			function validateWardNames(){
				var temp="";
				var wardSelected = false;
				var len = document.forms[0].allWardNames.length;
				var options = document.forms[0].allWardNames;

				for (var i=0;i<len;i++){
					if (options[i].selected == true){
						wardSelected=true;
						if(temp==""){
							temp = options[i].value;
							wardId = "'"+temp+"'";
						}else{
							temp = options[i].value;
							wardId = ','+"'"+temp+"'";
						}
					}
				}
				if(!wardSelected){
					alert("Select atleast one ward name for report");
					return false;
				}
				if(!document.forms[0].occupied.checked && !document.forms[0].vacant.checked){
					alert("Select bed status");
					return false;
				}
			}

			function deselectAll(){
				document.forms[0].all.checked = false;
			}

		</script>

	</head>
	<body onload="selectordeselectAll()">
		<form action="bedoccupancyreport.do" method="GET" target="_blank">
			<input type="hidden" name="method" value="getBedOccupancyReport">
			<input type="hidden" name="wardId">
		<div class="pageHeader">Bed Occupancy Report</div>

		<table align="center">

			<tr>
				<td>Select bed status:</td>
				<td><input type="checkbox" name="occupied" value="'Y'" checked>Occupied</td>
				<td><input type="checkbox" name="vacant" value="'N'" checked>Vacant</td>
			</tr>

			<tr>
				<td>Select the ward names:</td>
				<td> <input type="checkbox" name="all" onclick="selectordeselectAll()" checked>All </td>
			</tr>
			<tr>
				<td>Order By:</td>
				<td><input type="radio" name="orderby" id="orderbyPatientname" checked value="patient_name"/>Patient Name</td>
				<td><input type="radio" name="orderby" id="orderbyBedname" value="bed_id" />Bed Name</td>
			</tr>
			<tr>
				<td colspan="2">
					<select name="allWardNames" multiple="multiple" size="5" onclick="deselectAll()" style="width: 15em;">
						<c:forEach var="wardNames" items="${wardNameList}">
							<option value="'${wardNames.WARD_NO}'">${wardNames.WARD_NAME}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td>
				&nbsp;&nbsp;
				</td>
			</tr>
			<tr>
				<td>
					<button type="submit" accesskey="G" onclick="return validateWardNames()"><b><u>G</u></b>enerate Report</button>
				</td>

			</tr>
		</table>
		</form>
	</body>

</html>
