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
	<title>Pre Cycle Details - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="ivf/ivfsessions.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>

	<script>
		var allergies = ${allergieslist};

		function init() {
			var form = document.IVFpreCycle;
			var index=0;
			var opt = form.allergy_id;
			for(var i=0; i<opt.options.length; i++) {
				var opt_value = opt.options[i].value;
				for (var j=0; j<allergies.length; j++) {
					if (opt_value == allergies[j].allergy_id) {
					opt.options[index].selected = true;
					}
				}
				index++;
			}
		}

		function onChangeStatus() {
			document.IVFpreCycle.hiv_status.value=document.IVFpreCycle._hiv_status.checked?'Y':'N';
			document.IVFpreCycle.hbsag_status.value=document.IVFpreCycle._hbsag_status.checked?'Y':'N';
			document.IVFpreCycle.hcv_status.value=document.IVFpreCycle._hcv_status.checked?'Y':'N';
		}

		function validateAll() {
			if(!validateTime(document.IVFpreCycle.test_time)){
				document.IVFpreCycle.test_time.focus();
				return false;
			}
			document.forms.IVFpreCycle.submit();
		}
	</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body class="yui-skin-sam" onload="init();">
<div class="pageHeader">Pre Cycle Details</div>
<insta:feedback-panel/>
<insta:patientgeneraldetails  mrno="${param.mr_no}" />
<form name="IVFpreCycle" method="post" action="${cpath}/IVF/IVFPreCycle.do" autocomplete="off">
<input type="hidden" name="_method" value="update"/>
<input type="hidden" name="ivf_cycle_id" id="ivf_cycle_id" value="${preCycDetails.ivf_cycle_id}"/>
<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
<input type="hidden" name="patient_id" id="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />
	<fieldSet class="fieldSetBorder"> <legend class="fieldSetLabel">Patient Cycle Details</legend>
		<table border="0"  width="100%" class="formtable">
			<tr>
				<td class="formlabel">Cycle Number:</td>
				<td>
					<input type="text" name="cycle_number" id="cycle_number" value="${preCycDetails.cycle_number}"
						class="number" onkeypress="return enterNumOnlyzeroToNine(event)"/>
				</td>
				<td class="formlabel">LMP:</td>
				<td>
					<fmt:formatDate value="${preCycDetails.lmp}" pattern="dd-MM-yyyy" var="lmpdt" />
								<insta:datewidget id = "lmp" name="lmp" value="${lmpdt}"/>
				</td>
				<td class="formlabel">Married Life:</td>
				<td>
					<input type="text" name="married_life_yrs" id="married_life_yrs" value="${preCycDetails.married_life_yrs}"
						class="number" onkeypress="return enterNumOnly(event)"/>yrs
					<input type="text" name="married_life_months" id="married_life_months" value="${preCycDetails.married_life_months}"
					class="number" onkeypress="return enterNumOnly(event)"/>mnts
				</td>
			</tr>
			<tr>
				<td class="formlabel">Treatment Cycle:</td>
				<td>
					<insta:selectoptions name="pre_treatment_cycle" id="pre_treatment_cycle"
								opvalues="U,V" optexts="IUI,IVF" value="${preCycDetails.pre_treatment_cycle}"/>
				</td>
				<td class="formlabel">Height:</td>
				<td><input type="text" name="height" id="height" value="${preCycDetails.height}"
					class="number" onkeypress="return enterNumAndDot(event);" onchange="CalculateBMI(this,this.value);" onclick=""/>cms</td>
				<td class="formlabel">Weight:</td>
				<td><input type="text" name="weight" id="weight" value="${preCycDetails.weight}"
					class="number" onkeypress="return enterNumAndDot(event);"  onchange="CalculateBMI(this,this.value);"/>kgs</td>
			</tr>
			<tr>
				<td class="formlabel">BMI:</td>
				<td>
					<input type="hidden" name="bmi" id="bmi" value="${preCycDetails.bmi}"/>
					<label id="bmiLbl">${preCycDetails.bmi}</label>
				</td>
				<td class="formlabel">Previous BOH:</td>
				<td><textarea name="previous_obh" >${preCycDetails.previous_obh}</textarea></td>
				<td class="formlabel">Medical Conditions:</td>
				<td><textarea name="medical_condition" >${preCycDetails.medical_condition}</textarea></td>
			</tr>
			<tr>
				<td class="formlabel">Allergies:</td>
				<td>
					<insta:selectdb name="allergy_id" table="allergy_master" valuecol="allergy_id" class="listbox"  multiple="multiple"
						displaycol="allergy_name" filtered="true"/>
				</td>
			</tr>
			</table>
		</fieldSet>

		<fieldSet class="fieldSetBorder"><legend class="fieldSetLabel">Pre Cycle Details</legend>
			<table border="0"  width="100%" class="formtable">
			<tr>
				<td class="formlabel">Date:</td>
				<td>
					<fmt:formatDate var="test_dt" pattern="dd-MM-yyyy" value="${preCycDetails.test_datetime}"/>
					<fmt:formatDate var="test_tm" pattern="HH:mm" value="${preCycDetails.test_datetime}"/>
						<insta:datewidget name="test_date" id="test_date" value="${test_dt}" btnPos="left"/>
						<input type="text" size="4" id="test_time" name="test_time" value="${test_tm}"
							class="timefield" />
				</td>
				<td class="formlabel">FSH:</td>
				<td><input type="text" name="fsh" id="fsh" value="${preCycDetails.fsh}" class="number"
					onkeypress="return enterNumOnly(event)" onchange="return makeingDec(this.value,this);"/></td>
				<td class="formlabel">LH:</td>
				<td><input type="text" name="lh" id="lh" value="${preCycDetails.lh}" class="number"
					onkeypress="return enterNumOnly(event)" onchange="return makeingDec(this.value,this);"/></td>
			</tr>
			<tr>
				<td class="formlabel">TSH:</td>
				<td><input type="text" name="tsh" id="tsh" value="${preCycDetails.tsh}" class="number"
					onkeypress="return enterNumOnly(event)" onchange="return makeingDec(this.value,this);"/></td>
				<td class="formlabel">PRL:</td>
				<td><input type="text" name="prl" id="prl" value="${preCycDetails.prl}" class="number"
					onkeypress="return enterNumOnly(event)" onchange="return makeingDec(this.value,this);"/></td>
				<td class="formlabel">AMH:</td>
				<td><input type="text" name="amh" id="amh" value="${preCycDetails.amh}" class="number"
					onkeypress="return enterNumOnly(event)" onchange="return makeingDec(this.value,this);"/></td>
			</tr>
			<tr>
				<td class="formlabel">HIV:</td>
				<td><insta:selectoptions name="hiv_status" id="hiv_status"
								opvalues="N,P" optexts="Negative,Positive" value="${preCycDetails.hiv_status}"/>
				</td>
				<td class="formlabel">HBsAg:</td>
				<td><insta:selectoptions name="hbsag_status" id="hbsag_status"
								opvalues="N,P" optexts="Negative,Positive" value="${preCycDetails.hbsag_status}"/>
				</td>
				<td class="formlabel">HCV:</td>
				<td><insta:selectoptions name="hcv_status" id="hcv_status"
								opvalues="N,P" optexts="Negative,Positive" value="${preCycDetails.hcv_status}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"></td>
				<td>
					<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">HSG</legend>
						<table>
							<tr>
								<td class="formlabel">Uterus:</td>
								<td>
									<insta:selectoptions name="hsg_uterus" id="hsg_uterus"
												opvalues=" ,N,A" optexts="-- Select --,Normal,Abnormal" value="${preCycDetails.hsg_uterus}"/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Right Tube:</td>
								<td>
									<insta:selectoptions name="hsg_rt_tube" id="hsg_rt_tube"
												opvalues=" ,P,B" optexts="-- Select --,Patent,Blocked" value="${preCycDetails.hsg_rt_tube}"/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Left Tube:</td>
								<td>
									<insta:selectoptions name="hsg_lt_tube" id="hsg_lt_tube"
												opvalues=" ,P,B" optexts="-- Select --,Patent,Blocked" value="${preCycDetails.hsg_lt_tube}"/>
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
				<td class="formlabel"></td>
				<td>
					<fieldset><legend class="fieldSetLabel">Diagnosis LAP</legend>
						<table>
							<tr>
								<td class="formlabel">Uterus:</td>
								<td>
									<insta:selectoptions name="diaglap_uterus" id="diaglap_uterus"
												opvalues=" ,N,A" optexts="-- Select --,Normal,Abnormal" value="${preCycDetails.diaglap_uterus}"/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Right Tube:</td>
								<td>
									<insta:selectoptions name="diaglap_rt_tube" id="diaglap_rt_tube"
												opvalues=" ,P,B" optexts="-- Select --,Patent,Blocked" value="${preCycDetails.diaglap_rt_tube}"/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Left Tube:</td>
								<td>
									<insta:selectoptions name="diaglap_lt_tube" id="diaglap_lt_tube"
												opvalues=" ,P,B" optexts="-- Select --,Patent,Blocked" value="${preCycDetails.diaglap_lt_tube}"/>
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
				<td class="formlabel"></td>
				<td>
					<fieldset><legend class="fieldSetLabel">Diagnosis Hyst</legend>
						<table>
							<tr>
								<td class="formlabel">Cavity:</td>
								<td>
									<insta:selectoptions name="diaghyst_cavity" id="diaghyst_cavity"
												opvalues=" ,N,A" optexts="-- Select --,Normal,Abnormal" value="${preCycDetails.diaghyst_cavity}"/>
								</td>
							</tr>
							<tr>
								<td class="formlabel"> Right Ostea:</td>
								<td>
									<insta:selectoptions name="diaghyst_rt_ostea" id="diaghyst_rt_ostea"
												opvalues=" ,N,A" optexts="-- Select --,Normal,Abnormal" value="${preCycDetails.diaghyst_rt_ostea}"/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Left Ostea:</td>
								<td>
									<insta:selectoptions name="diaghyst_lt_ostea" id="diaghyst_lt_ostea"
												opvalues=" ,N,A" optexts="-- Select --,Normal,Abnormal" value="${preCycDetails.diaghyst_lt_ostea}"/>
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
			<tr>
				<td class="formlabel"></td>
				<td>
					<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Semen Analysis</legend>
						<table style="width:200px">
							<tr>
								<td class="formlabel">Count:</td>
								<td><input type="text" name="sa_count" id="sa_count" value="${preCycDetails.sa_count}" class="number"
									onChange="return makeingDec(this.value,this);"/>millions
								</td>
							</tr>
							<tr>
								<td class="formlabel">Total Mobility:</td>
								<td><input type="text" name="sa_total_mobility" id="sa_total_mobility" value="${preCycDetails.sa_total_mobility}" class="number"
									onChange="return makeingDec(this.value,this);"/>%
								</td>
							</tr>
							<tr>
								<td class="formlabel">Rapid:</td>
								<td><input type="text" name="sa_rapid" id="sa_rapid" value="${preCycDetails.sa_rapid}" class="number"
									onChange="return makeingDec(this.value,this);"/>%
								</td>
							</tr>
							<tr>
								<td class="formlabel">Normal:</td>
								<td><input type="text" name="sa_normal" id="sa_normal" value="${preCycDetails.sa_normal}" class="number"
									onChange="return makeingDec(this.value,this);"/>%
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
				<td class="formlabel">Remarks:</td>
				<td>
					<textarea name="precyle_remarks">${preCycDetails.precyle_remarks}</textarea>
				</td>
			</tr>
			</table>
		</fieldSet>
	<div class="screenActions" align="left">
	<input type="checkbox" name="precycleCompleted" id="precycleCompleted" ${preCycDetails.cycle_status != 'O' ? 'checked' : ''}
	 ${preCycDetails.cycle_status != 'O' ? 'disabled' : ''}/> PreCycle Completed
	| <input type="button" name="save" id="save" value="Save" onclick="validateAll();"/>
	| <a href="${cpath}/IVF/IVFDailyTreatment.do?_method=list&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}&start_date=${ifn:cleanURL(param.start_date)}">Daily Treatment</a>
	</div>
</form>
</html>