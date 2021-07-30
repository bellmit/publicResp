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
<title><insta:ltext key="patient.dialysismachineshift.addshow.title"/></title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
	<script>
		var originalMachineId = '${original_machine_id}';
		function init(){
			document.machineShift.originalMachineId.disabled = true;
		}
		function funCancel(){
			document.machineShift.machine_id.value = "";

			document.machineShift.action = "DialysisCurrentSessions.do?_method=list";
			document.machineShift.submit();
		}
		function Validate(){
			var machineId = document.machineShift.machine_id.value ;
			if(machineId == null || machineId == "") {
				showMessage("js.dialysismodule.commonvalidations.select.machine");
				document.getElementById("machine_id").focus();
				return false;
			}
			if(originalMachineId == machineId){
				showMessage("js.dialysismodule.commonvalidations.machineshift.notsameas.currentmachine");
				return false;
			}
			return true;
		}
	</script>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
	<insta:js-bundle prefix="widgets.commonvalidations"/>

</head>
<body onload="init();">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<div class="pageHeader"><insta:ltext key="patient.dialysismachineshift.addshow.pageheader"/></div>
<insta:feedback-panel/>
<insta:patientgeneraldetails  mrno="${mr_no}" addExtraFields="true"/>
<form name="machineShift" method="post" action="${cpath}/dialysis/DialysisMachineShift.do">
<input type="hidden" name="_method" value="shift">
<input type="hidden" name="order_id" value="${ifn:cleanHtmlAttribute(order_id)}"/>
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}"/>
<input type="hidden" name="dialysis_presc_id" value="${ifn:cleanHtmlAttribute(dialysis_presc_id)}"/>
<input type="hidden" name="original_machine_id" value="${ifn:cleanHtmlAttribute(original_machine_id)}">

<fieldset class="fieldSetBorder">
	<table class="formtable" align="left">
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysismachineshift.addshow.currentmachine"/></td>
			<td >
				<insta:selectdb name="originalMachineId" table="dialysis_machine_master" valuecol="machine_id"
								displaycol="machine_name" filtered="true" value="${original_machine_id}" dummyvalue="${dummyvalue}" /><span class="star">*</span>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysismachineshift.addshow.shiftto"/> </td>
			<td >
				<select name="machine_id" id="machine_id" class="dropdown">
					<option value=""><insta:ltext key="patient.dialysismachineshift.addshow.select"/></option>
					<c:forEach items="${machines}" var="machine">
						<option value="${machine.map.machine_id}">${machine.map.machine_name}</option>
					</c:forEach>
				</select>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysismachineshift.addshow.discard"/></td>
			<td>
				<insta:radio name="discard_session_details" radioText="Yes,No"
						radioValues="Y,N" radioIds="discardYes,discardNo" value="Y"/>
			</td>
		</tr>
	</table>
</fieldset>
	<c:url value="PreDialysisSessions.do" var="preurl">
		<c:param name="_method" value="show"/>
		<c:param name="mr_no" value="${mr_no}"/>
		<c:param name="dialysisprescId" value="${dialysis_presc_id}"/>
		<c:param name="order_id" value="${order_id}"/>
	</c:url>
	<c:url value="IntraDialysisSessions.do" var="intraurl">
		<c:param name="_method" value="show"/>
		<c:param name="mr_no" value="${mr_no}"/>
		<c:param name="order_id" value="${order_id}"/>
	</c:url>
	<div class="screenActions">
		<input type="submit" name="save" id="save" value="Save" onclick="return Validate();"> |
		<a href="javascript:void(0)" onclick="funCancel();"><insta:ltext key="patient.dialysismachineshift.addshow.dialysissessions"/></a> |
		<a href='<c:out value="${preurl}"/>'><insta:ltext key="patient.dialysismachineshift.addshow.predialysis"/></a> |
		<a href='<c:out value="${intraurl}"/>'><insta:ltext key="patient.dialysismachineshift.addshow.intradialysis"/></a>
	</div>
</body>
</html>
