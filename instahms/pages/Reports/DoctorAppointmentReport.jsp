<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>

<head>
	<title>Doctor's Appointment Report - Insta HMS</title>
	<insta:link type="style"  file="hmsNew.css"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<script>
	function selectordeselectAll(){
				var length = document.forms[0].allDoctorNames.length;
				var typeAll = document.forms[0].all.checked;
				var disabled = typeAll;
				for (var i=0;i<length;i++){
					document.forms[0].allDoctorNames[i].selected = disabled;
				}
			}

			function validatedoctorNames(){
				var temp="";
				var doctorSelected = false;
				var len = document.forms[0].allDoctorNames.length;
				var options = document.forms[0].allDoctorNames;

				for (var i=0;i<len;i++){
					if (options[i].selected == true){
						doctorSelected=true;
						if(temp==""){
							temp = options[i].value;
							doctor_id = "'"+temp+"'";
						}else{
							temp = options[i].value;
							doctor_id = ','+"'"+temp+"'";
						}
					}
				}
				if(!doctorSelected){
					alert("Select atleast one doctor name for report");
					return false;
				}
			}

			function deselectAll(){
				document.forms[0].all.checked = false;
			}
	</script>
</head>
	<body onload="selectordeselectAll()">
		<form action="DoctorsAppointment.do" method="get" target="_blank">
			<input type="hidden" name="method" value="getDoctorsAppointmentReport">
			<input type="hidden" name="doctor_id">
			<input type="hidden" name="onDate">
		<div class="pageHeader">Doctor's Appointment Report</div>


			<div class="tipText" align="center">
				This report lists the doctor's appointments.
			</div>
          <center>
			<label>Filter Options:</label>
			<div class="stwMain" style="width:30%;">
			<table width="100%" height="15%" class="search">
			<tr>
			<td align="right">On Date:</td>
			<td><insta:datewidget name="onDate" value="today"/></td>
            </tr>
			<tr>
			<td>Select the Doctor Name: <input type="checkbox" name="all" onclick="selectordeselectAll()" checked>All </td>
			</tr>
			<tr>
			  <td colspan="2">
					<select name="allDoctorNames" multiple="multiple" size="5" onclick="deselectAll()" style="width: 15em;">
						<c:forEach var="doctorNames" items="${doctorNameList}">
							<option value="'${doctorNames.DOCTOR_ID}'">${doctorNames.DOCTOR_NAME}</option>
						</c:forEach>
					</select>
				</td>
				</tr>
			</table>
			</div>
			</center>
			<table align="center" style="margin-top: 1em">
			<tr>
				<td>
					<button type="submit" accesskey="G" onclick="return validatedoctorNames()"><b><u>G</u></b>enerate Report</button>
				</td>
			  </tr>
			</table>
			</form>
	</body>
</html>
