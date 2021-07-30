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
	<title>Cycle Completion Details - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="ivf/cyclecompletion.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
<script>
	var embryoDetails = ${embryoDetails};
	var OOCyteDetails = ${OOCyteDetails};
</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body onload="init();" class="yui-skin-sam">
<div class="pageHeader">Cycle Completion Details</div>
<insta:feedback-panel/>
<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true"/>
<form name="CycleCompletion" method="post" action="${cpath}/IVF/IVFCycleCompletion.do" autocomplete="off">
<input type="hidden" name="_method" value="update"/>
<input type="hidden" name="ivf_cycle_id" id="ivf_cycle_id" value="${ifn:cleanHtmlAttribute(param.ivf_cycle_id)}"/>
<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
<input type="hidden" name="patient_id" id="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />

	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Cycle Completion</legend>
		<table border="0" width="100%" class="formtable">
			<tr>
				<td class="formlabel">Treatment Cycle:</td>
				<td>
					<insta:selectoptions name="final_treatment_cycle" id="final_treatment_cycle" tabindex="310"
						opvalues="IU,IV,VC,VN" optexts="IUI,IVF,IVF-ICSI,IVF-IMSI"
						value="${CycCompDetails.final_treatment_cycle}" onchange="OnChangeTreatCycle();"/>
				</td>
				<td class="formlabel">Protocol:</td>
				<td>
					<insta:selectoptions name="protocol" id="protocol" tabindex="310"
						opvalues="I,A,M,U,S" optexts="Ibg,antag,MF,UL,SBG"
						value="${CycCompDetails.protocol}"/>
				</td>
			</tr>
			<tr>
				<td>
					<div id="IUIdiv" style="display: none;white-space:nowrap;">
					<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">IUI</legend>
						<table class="formtable" border="0" width="100%">
							<tr>
								<td class="formlable">Pre-wash SA:</td>
								<td>
									<textarea name="pre_wash_sa">${CycCompDetails.pre_wash_sa}</textarea>
								</td>
							</tr>
							<tr>
								<td class="formlable">Post-wash SA :</td>
								<td>
									<textarea name="post_wash_sa">${CycCompDetails.post_wash_sa}</textarea>
								</td>
							</tr>
							<tr>
								<td class="formlable">IUI Technique:</td>
								<td>
									<insta:selectoptions name="iui_technique" id="iui_technique" tabindex="310"
										opvalues="E,D,B" optexts="Easy,Difficult,Blood Stained" value="${CycCompDetails.iui_technique}" />
								</td>
							</tr>
						</table>
					</fieldset>
					</div>
				</td>
			</tr>
		</table>
	</fieldset>
	<div id="IVFdiv" style="display: none;white-space:nowrap;">
		<fieldset class="fieldSetBorder">
			<table  class="formtable"  cellspacing="0" cellpadding="0">
				<tr>
					<td class="formlabel">Injection RHCG/UHCG Date:</td>
					<td>
						<fmt:formatDate var="rhcg_uhcg_dt" pattern="dd-MM-yyyy" value="${CycCompDetails.rhcg_uhcg_date}"/>
						<fmt:formatDate var="rhcg_uhcg_tm" pattern="HH:mm" value="${CycCompDetails.rhcg_uhcg_date}"/>
						<insta:datewidget name="rhcguhcg_date" id="rhcguhcg_date" value="${rhcg_uhcg_dt}" btnPos="left"/>
						<input type="text" size="4" id="rhcguhcg_time" name="rhcguhcg_time" value="${rhcg_uhcg_tm}"
							class="timefield" />
					</td>

					<td class="formlabel">Total dose of Gonadotrophin:</td>
					<td>
						<input type="text" name="gndtropin_dose" id="gndtropin_dose" value="${CycCompDetails.gndtropin_dose }"
							onkeypress="return enterNumOnlyzeroToNine(event);" style="width:60px"/>
					</td>
				</tr>
			</table>
		</fieldset>
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">OOCyte</legend>
		 	<table class="formtable" cellpadding="0" cellspacing="0">
				<tr>
					<td class="formlabel">Fertilized:</td>
					<td><input type="text" name="fertilization_rate_number" id="fertilization_rate_number" style="width:60px"
						value="${CycCompDetails.fertilization_rate_number}" onchange="getFertilizedRateperc();"
						onkeypress="return enterNumOnlyzeroToNine(event);" class="number"/></td>
					<td class="formlabel">Cleaved:</td>
					<td><input type="text" name="cleavage_rate_number" id="cleavage_rate_number" style="width:60px"
						value="${CycCompDetails.cleavage_rate_number}" onchange="getCleavageRateperc();"
						onkeypress="return enterNumOnlyzeroToNine(event);" class="number"/></td>
					<td class="formlabel">Total Oocyte:</td>
					<td><label id="total_Oocyte"></label></td>
				</tr>
				<tr>
					<td class="formlabel">Fertilization %:</td>
					<td>
						<label id="fertilizationRatePerc">${CycCompDetails.fertilization_rate_perc}</label>
						<input type="hidden" name="fertilization_rate_perc" id="fertilization_rate_perc" value="${CycCompDetails.fertilization_rate_perc}"/>
					</td>
					<td class="formlabel">Cleavage %:</td>
					<td>
						<label id="cleavageRatePerc">${CycCompDetails.cleavage_rate_perc}</label>
						<input type="hidden" name="cleavage_rate_perc" id="cleavage_rate_perc" value="${CycCompDetails.cleavage_rate_perc}"/>
					</td>
				</tr>
		 	</table>
		</fieldset>
	</div>
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Final Result</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Cycle Treatment Outcome:</td>
				<td>
				<insta:selectoptions name="cycle_treatment_outcome" id="cycle_treatment_outcome"
					tabindex="310" opvalues=" ,X,F,CM,CL,H,MP"
					optexts="...Select...,Cancelled,Failed to Conceive,Chemical Pregnancy,Clinical Pregnancy, Heterotropic Pregnancy,Molar Pregnancy"
					value="${CycCompDetails.cycle_treatment_outcome}" />
				</td>
				<td class="formlabel">Remarks:</td>
				<td>
					<textarea name="completion_remarks" style="width:350px;height:30px">${CycCompDetails.completion_remarks}</textarea>
				</td>
			</tr>
		</table>
	</fieldset>
	<div class="screenActions">
		<input type="checkbox" name="completed" id="completed" ${CycCompDetails.cycle_status == 'C' ? 'checked' : ''}
	 	${CycCompDetails.cycle_status == 'C' ? 'disabled' : ''}/> Mark Completed
		<input type="submit" value="Save" class="button" onclick="return funValidateAndSubmit();" />
		| <a href="${cpath}/IVF/IVFPreCycle.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}&start_date=${ifn:cleanURL(param.start_date)}">Pre Cycle</a>
		| <a href="${cpath}/IVF/IVFDailyTreatment.do?_method=list&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}&start_date=${ifn:cleanURL(param.start_date)}">Daily Treatment</a>
		| <a href="${cpath}/IVF/IVFCycleCompletion.do?_method=getOocyteDetails&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}">Oocyte Retrieval</a>
		| <a href="${cpath}/IVF/IVFCycleCompletion.do?_method=getEmbryoTransferDetails&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}">Embryo Transfer</a>
		| <a href="${cpath}/IVF/IVFCycleCompletion.do?_method=getEmbryoFreezingDetails&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}">Embryo Freezing</a>
		| <a href="${cpath}/IVF/IVFCycleCompletion.do?_method=getLutealDetails&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}">Luteal Support</a>

	</div>
</form>
</body>
</html>