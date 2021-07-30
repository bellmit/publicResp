<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Embryo Freezing Details - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="ivf/cyclecompletion.js" />
	<insta:link type="script" file="ivf/ivfsessions.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
<script>
	var embryoDetails = ${embryoFrozenDetails};
	function funValidateAndSubmit() {
		var method = document.CycleCompletion._method.value;
		var ivfCycleID = document.CycleCompletion.ivf_cycle_id.value;
		var mrNo = document.CycleCompletion.mr_no.value;;
		var patientID = document.CycleCompletion.patient_id.value;
		document.CycleCompletion.action=cpath+"/IVF/IVFCycleCompletion.do?_method=saveEmbryoFreezingDetails&ivf_cycle_id="+ivfCycleID+"&mr_no="+mrNo+"&patient_id="+patientID;
		document.CycleCompletion.submit();
	}
</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body onload="fillembryofrozendetails();" class="yui-skin-sam">
<div class="pageHeader">Embryo Freezing Details</div>
<insta:feedback-panel/>
<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true"/>
<form name="CycleCompletion" method="post" action="${cpath}/IVF/IVFCycleCompletion.do" autocomplete="off">
<input type="hidden" name="_method" value="saveEmbryoFreezingDetails"/>
<input type="hidden" name="ivf_cycle_id" id="ivf_cycle_id" value="${ifn:cleanHtmlAttribute(param.ivf_cycle_id)}"/>
<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
<input type="hidden" name="patient_id" id="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Embryo Frozen</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Embryo Frozen Date:</td>
				<td  style="white-space: nowrap"><fmt:formatDate value="${cycComplBean.embryo_frozen_date}" pattern="dd-MM-yyyy" var="ef_dt" />
						<insta:datewidget id = "embryo_frozen_date" name="embryo_frozen_date" value="${ef_dt}"/>
				</td>
			</tr>
			<tr>
				<td>
					<table class="dashboard" id="EFTable" cellpadding="0" cellspacing="0" style="width:400px">
						<tr class="header">
							<td>Number</td>
							<td>Stage</td>
							<td>Grade</td>
							<td>&nbsp;</td>
						</tr>
						<tr id="" style="display: none">
						</tr>
						<tr>
							<td colspan="3"></td>
							<td>
								<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="AddRowEF(this)" >
									<img src="${cpath}/icons/Add.png" align="right"/>
								</button>
							</td>
						</tr>
					</table>
				</td>
				<td class="formlabel">No of Straws:</td>
				<td><input type="text" name="freeze_straws" id="freeze_straws" value="${cycComplBean.freeze_straws}" class="number"
					onkeypress="return enterNumOnlyzeroToNine(event)"/></td>
				<td class="formlabel">Cannister # :</td>
				<td><input type="text" name="freeze_can_num" id="freeze_can_num" value="${cycComplBean.freeze_can_num}"/></td>
			</tr>
		</table>
	</fieldset>
	<div class="screenActions">
		<input type="submit" value="Save" class="button" onclick="return funValidateAndSubmit();" />
		| <a href="${cpath}/IVF/IVFCycleCompletion.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}">Cycle Completion</a>
	</div>
</form>
</body>
</html>