<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.title"/></title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>

	<style type="text/css">

	</style>
	<script>
		function closeScreen() {

			window.location.href ="${cpath}/dialysis/DialysisMachineStatus.do?method=list";
		}
	</script>
</head>
<body>
<div class="pageHeader"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.pageheader"/></div>
<table align="center" width="97%" >
<tr>
<td>
<fieldset class="fieldsetborder">
	<table align="center" width="100%" border="0" class="formTable" >
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.machinename"/></td>
			<td ><b> ${bean['machine_name']}<b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.location"/></td>
			<td ><b>${bean['location_name']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.network.address"/></td>
			<td ><b>${bean['network_address']}</b></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.patient"/></td>
			<td ><b>${bean['patient_name']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.mrno"/></td>
			<td ><b>${bean['mr_no']}</b></td>
			<td class="formlabel"></td>
			<td ></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.machine.status"/></td>
			<c:set var="status">
	 				<c:choose>
	 					<c:when test="${bean['polled_status'] eq 'D'}"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.dialyzing"/></c:when>
	 					<c:when test="${bean['polled_status'] eq 'N'}"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.notdialyzing"/></c:when>
	  					<c:when test="${bean['polled_status'] eq 'R'}"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.notresponding"/></c:when>
	  					<c:when test="${bean['polled_status'] eq 'E'}"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.dataerror"/></c:when>
	  					<c:when test="${bean['polled_status'] eq 'X'}"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.cannotconnect"/></c:when>
	  					<c:otherwise><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.unknown"/></c:otherwise>
	 				</c:choose>
	 			</c:set>
			<td ><b>${ifn:cleanHtml(status)}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.lastpolled"/></td>
			<td ><b><fmt:formatDate value="${bean['last_polled_time']}" pattern="HH:mm:ss"/></b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.lastresult"/></td>
			<td ><b><fmt:formatDate value="${bean['last_results_time']}" pattern="HH:mm:ss"/></b></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.ufgoal"/></td>
			<td ><b>${bean['uf_goal']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.ufremoved"/></td>
			<td ><b>${bean['uf_removed']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.ufrate"/></td>
			<td ><b>${bean['uf_rate']}</b></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.bloodpumprate"/></td>
			<td ><b>${bean['blood_pump_rate']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.heparinrate"/> </td>
			<td ><b>${bean['heparin_rate']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.dialysate"/></td>
			<td ><b>${bean['dialysate_temp']}</b></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.dialysate.conductivity"/></td>
			<td ><b>${bean['dialysate_cond']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.venouspressure"/></td>
			<td ><b>${bean['venous_pressure']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.dialysatepressure"/></td>
			<td ><b>${bean['dialysate_pressure']}</b></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.tmp"/></td>
			<td ><b>${bean['tmp']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.dialysisflowrate"/> </td>
			<td ><b>${bean['blood_pump_rate']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.dialysistime"/></td>
			<td ><b>${bean['dialysis_time']}</b></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.bptime"/> </td>
			<td ><b><fmt:formatDate value="${bean['bp_time']}" pattern="HH:mm"/></b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.bphigh"/>  </td>
			<td ><b>${bean['bp_high']}</b></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.bplow"/></td>
			<td ><b>${bean['bp_low']}</b></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.pulserate"/></td>
			<td ><b>${bean['pulse_rate']}</b></td>
		</tr>

		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.alarms"/></td>
				<td ><font color="${bean['tmp_alarm'] eq '1' ? 'red' : ''}"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.tmp"/> ${bean['tmp_alarm'] eq '1' ? 'Alarm' : 'OK'}</font></td>

				<td ><font color="${bean['blood_leak_alarm'] eq '1' ? 'red' : ''}"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.bloodleak"/> ${bean['blood_leak_alarm'] eq '1' ? 'Alarm' : 'OK'}</font></td>

				<td ><font color="${bean['air_alarm'] eq '1' ? 'red' : ''}"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.air"/>  ${bean['air_alarm'] eq '1' ? 'Alarm' : 'OK'}</font></td>

				<td ><font color="${bean['bp_alarm'] eq '1' ? 'red' : ''}"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.bp"/>  ${bean['bp_alarm'] eq '1' ? 'Alarm' : 'OK'}</font></td>

				<td ><font color="${bean['other_alarm'] eq '1' ? 'red' : ''}"><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.other"/> ${bean['other_alarm'] eq '1' ? 'Alarm' : 'OK'}</font></td>
		</tr>

	</table>
</fieldset>
</td>
</tr>
</table>
<div class="screenActions">
	<button type="button" name="close" accesskey="C" onclick="closeScreen()"><b><u><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.c"/></u></b><insta:ltext key="patient.dialysis.machine.status.machstadetailsscreen.lose"/></button>
</div>
</body>
</html>
