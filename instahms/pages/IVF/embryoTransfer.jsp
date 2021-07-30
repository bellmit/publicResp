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
	<title>Embryo Transfer Details - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="ivf/cyclecompletion.js" />
	<insta:link type="script" file="ivf/ivfsessions.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
<script>
	var embryoDetails = ${embryoTransferDetails};
	function funValidateAndSubmit() {
		var method = document.CycleCompletion._method.value;
		var ivfCycleID = document.CycleCompletion.ivf_cycle_id.value;
		var mrNo = document.CycleCompletion.mr_no.value;;
		var patientID = document.CycleCompletion.patient_id.value;
		document.CycleCompletion.action=cpath+"/IVF/IVFCycleCompletion.do?_method=saveEmbryoTransferDetails&ivf_cycle_id="+ivfCycleID+"&mr_no="+mrNo+"&patient_id="+patientID;
		document.CycleCompletion.submit();
	}

</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body onload="fillembryotransferdetails();" class="yui-skin-sam">
<div class="pageHeader">Embryo Transfer Details</div>
<insta:feedback-panel/>
<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true"/>
<form name="CycleCompletion" method="post" action="${cpath}/IVF/IVFCycleCompletion.do" autocomplete="off">
<input type="hidden" name="_method" value="saveEmbryoTransferDetails"/>
<input type="hidden" name="ivf_cycle_id" id="ivf_cycle_id" value="${ifn:cleanHtmlAttribute(param.ivf_cycle_id)}"/>
<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
<input type="hidden" name="patient_id" id="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />
	<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Surgeon:</td>
				<td>
					<insta:selectdb name="em_transf_surgeon_id" id="em_transf_surgeon_id" table="doctors"
						valuecol="doctor_id" displaycol="doctor_name" dummyvalue="..Select.."
						value="${cycComplBean.em_transf_surgeon_id}"/>
				</td>
				<td class="formlabel">Anesthesiologist:</td>
				<td>
					<insta:selectdb name="em_transf_anesthesiologist_id" id="em_transf_anesthesiologist_id" table="doctors"
						valuecol="doctor_id" displaycol="doctor_name" dummyvalue="..Select.."
						value="${cycComplBean.em_transf_anesthesiologist_id}"/>
				</td>
				<td class="formlabel">Assistant:</td>
				<td><input type="text" name="em_transf_assistant_name" id="em_transf_assistant_name"
					value="${cycComplBean.em_transf_assistant_name}"/></td>
			</tr>
			<tr>
				<td class="formlabel">Transfer Date:</td>
				<td  style="white-space: nowrap"><fmt:formatDate value="${cycComplBean.embryo_transfer_date}" pattern="dd-MM-yyyy" var="et_dt" />
						<insta:datewidget id = "embryo_transfer_date" name="embryo_transfer_date" value="${et_dt}"/>
				</td>
			</tr>
		</table>
	</fieldset>
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Transfer Details</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Embryo Transfer Type</td>
				<td>
					<insta:selectoptions name="em_transf_type" id="em_transf_type" tabindex="310"
					opvalues=" ,D,E" optexts="--Select--,Difficult,Easy" value="${cycComplBean.em_transf_type}" />
				</td>
				<td class="formlabel">Blood Staining:</td>
				<td>
					<insta:selectoptions name="blood_stain" id="blood_stain" tabindex="310"
					opvalues=" ,P,A" optexts="--Select--,Present,Absent" value="${cycComplBean.blood_stain}" />
				</td>
				<td class="formlabel">Retained Embryo:</td>
				<td>
					<insta:selectoptions name="retained_emb" id="retained_emb" tabindex="310"
					opvalues=" ,P,A" optexts="--Select--,Present,Absent" value="${cycComplBean.retained_emb}" />
				</td>
			</tr>
			<tr>
				<td class="formlabel">Retransfer:</td>
				<td>
					<insta:selectoptions name="retrans_em" id="retrans_em" tabindex="310"
					opvalues=" ,D,N" optexts="--Select--,Done,Not Done" value="${cycComplBean.retrans_em}" />
				</td>
				<td class="formlabel">Endometrium :</td>
				<td>
					<input type="text" name="em_transf_endome_thickness" id="em_transf_endome_thickness"
							value="${cycComplBean.em_transf_endome_thickness}" onkeypress="return enterNumOnly(event)"
							onchange="return makeingDec(this.value,this);" class="number"/>  mm
				</td>
				<td class="formlabel">Blastocyst:</td>
				<td>
					<input type="text" name="blastocyst" id="blastocyst" value="${cycComplBean.blastocyst}"
					onkeypress="return enterNumOnlyzeroToNine(event)" class="number"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Cell:</td>
				<td>
					<input type="text" size="4" name="cell_two" id="cell_two"  value="${cycComplBean.cell_two}"
						class="number" onkeypress="return enterNumOnlyzeroToNine(event)"/>(2 to 4)
					<input type="text" size="4" name="cell_four" id="cell_four" value="${cycComplBean.cell_four}"
						class="number" onkeypress="return enterNumOnlyzeroToNine(event)"/>(6 to 8)
				</td>
			</tr>
		</table>
	</fieldset>
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Embryo Transfer</legend>
		<table class="formtable">
			<tr>
				<td>
					<table class="dashboard" id="ETTable" cellpadding="0" cellspacing="0" style="width:400px">
						<tr class="header">
							<td>Number</td>
							<td>Grade</td>
							<td>&nbsp;</td>
						</tr>
						<tr id="" style="display: none">
						</tr>
						<tr>
							<td colspan="2"></td>
							<td>
								<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="AddRowET(this)" >
									<img src="${cpath}/icons/Add.png" align="right"/>
								</button>
							</td>
						</tr>
					</table>
				</td>
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