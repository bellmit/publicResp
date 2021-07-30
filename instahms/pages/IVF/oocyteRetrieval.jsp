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
	<title>Oocyte Pickup Details - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="ivf/oocyteRetrieval.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>

<script>
var retrievalDetails = ${OOCyteDetails};
</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body onload="init();" class="yui-skin-sam">
<div class="pageHeader">Oocyte Pickup Details</div>
<insta:feedback-panel/>
<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true"/>
<form name="CycleCompletion" method="post" action="${cpath}/IVF/IVFCycleCompletion.do" autocomplete="off">
<input type="hidden" name="_method" value="saveOoCyteDetails"/>
<input type="hidden" name="ivf_cycle_id" id="ivf_cycle_id" value="${ifn:cleanHtmlAttribute(param.ivf_cycle_id)}"/>
<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
<input type="hidden" name="patient_id" id="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />

	<fieldset class="fieldSetBorder">
	<table class="formtable" cellpadding="0" cellspacing="0">
	<tr>
		<td class="formlabel">Surgeon:</td>
		<td>
			<insta:selectdb name="opu_surgeon_id" id="opu_surgeon_id" table="doctors"
				valuecol="doctor_id" displaycol="doctor_name" dummyvalue="..Select.."
				value="${cycComplbean.opu_surgeon_id}"/>
		</td>
		<td class="formlabel">Anesthesiologist:</td>
		<td>
			<insta:selectdb name="opu_anesthesiologist_id" id="opu_anesthesiologist_id" table="doctors"
				valuecol="doctor_id" displaycol="doctor_name" dummyvalue="..Select.."
				value="${cycComplbean.opu_anesthesiologist_id}"/>
		</td>
		<td class="formlabel">Assistant:</td>
		<td><input type="text" name="opu_assistant_name" id="opu_assistant_name"
			value="${cycComplbean.opu_assistant_name}"/></td>
	</tr>
	<tr>
		<td class="formlabel">OPU Date:</td>
		<td>
			<fmt:formatDate var="opudt" pattern="dd-MM-yyyy" value="${cycComplbean.opu_start_time}"/>
			<insta:datewidget name="opuDate" id="opuDate" value="${opudt}" btnPos="left" />
		</td>
		<td class="formlabel">OPU Start Time:</td>
		<td>
			<fmt:formatDate var="stime" pattern="HH:mm" value="${cycComplbean.opu_start_time}"/>
			<input type="text" size="4" id="startTime" name="startTime" value="${stime}"
				class="timefield" onchange="calDuration();"/>
		</td>
		<td class="formlabel">OPU End Time:</td>
		<td>
			<fmt:formatDate var="etime" pattern="HH:mm" value="${cycComplbean.opu_end_time}"/>
			<input type="text" size="4" id="endTime" name="endTime" value="${etime}"
				class="timefield" onchange="calDuration();"/>
		</td>
	</tr>
	<tr>
		<td class="formlabel">Duration :</td>
		<td>
			<label id="opu_duration">${cycComplbean.opu_duration} hrs</label>
		</td>
		<td class="formlabel">Anesthesia Type:</td>
		<td>
			<insta:selectoptions name="opu_anesthesia_type" id="opu_anesthesia_type" tabindex="310"
					opvalues="I,G" optexts="IVS,GA" value="${cycComplbean.opu_anesthesia_type}" />
		</td>
		<td class="formlabel"> Needle Used:</td>
		<td>
			<insta:selectoptions name="opu_needle_used" id="opu_needle_used" tabindex="310"
					opvalues="S,D" optexts="Single Lumen,Double Lumen" value="${cycComplbean.opu_needle_used}" />
		</td>
	</tr>
	</table>
	</fieldset>
	<fieldset>
		<table class="dashboard">
			<tr>
				<td>Ovary</td>
				<td>Follicles</td>
				<td>Oocytes</td>
			</tr>
			<tr>
				<td>Right</td>
				<td>
					<input type="text" name="opu_rt_ovary_follicles" id="opu_rt_ovary_follicles"
						value="${cycComplbean.opu_rt_ovary_follicles}" onkeypress="return enterNumOnlyzeroToNine(event)"/>
				</td>
				<td>
					<input type="text" name="opu_rt_ovary_oocytes" id="opu_rt_ovary_oocytes"
						value="${cycComplbean.opu_rt_ovary_oocytes}" onkeypress="return enterNumOnlyzeroToNine(event)"/>
				</td>
			</tr>
			<tr>
				<td>Left</td>
				<td>
					<input type="text" name="opu_lt_ovary_follicles" id="opu_lt_ovary_follicles"
						value="${cycComplbean.opu_lt_ovary_follicles}" onkeypress="return enterNumOnlyzeroToNine(event)"/>
				</td>
				<td>
					<input type="text" name="opu_lt_ovary_oocytes" id="opu_lt_ovary_oocytes"
						value="${cycComplbean.opu_lt_ovary_oocytes}" onkeypress="return enterNumOnlyzeroToNine(event)"/>
				</td>
			</tr>
		</table>
	</fieldset>
	<fieldset class="fieldsetBorder"><legend class="fieldsetLabel">Oocyte Assessment Data:</legend>
		<table class="formtable">
			<tr>
				<td>
					<table class="dashboard" style="width:200px" id="OTABLE">
						<tr>
							<td>Type</td>
							<td>Numbers</td>
							<td></td>
						</tr>
						<tr id="" style="display: none">
						</tr>
						<tr>
							<td colspan="2"></td>
							<td>
								<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="AddRowOocyte(this)" >
									<img src="${cpath}/icons/Add.png" align="right"/>
								</button>
							</td>
						</tr>
					</table>
				</td>
				<td class="formlabel">Total Oocyte:</td>
				<td><label id="total_oocyte"></label></td>
				<td class="formlabel">Remarks:</td>
				<td><textarea name="opu_remarks">${cycComplbean.opu_remarks}</textarea></td>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<input type="submit" name="Save" value="Save" />
		| <a href="${cpath}/IVF/IVFCycleCompletion.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}">Cycle Completion</a>
	</div>
</form>
</body>
</html>