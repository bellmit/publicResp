<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.struts.Globals" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.VITAL_PARAMETER_PATH %>"/>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

		<insta:link type="js" file="masters/vitalResultRanges.js" />
		<insta:link type="script" file="hmsvalidation.js"/>

		<title>Reference Ranges Master</title>
		<script type="text/javascript">
			var dialogState = 'Add';
			var cpath = '<%=request.getContextPath()%>';
		</script>
	</head>
	<body onload="init();" class="yui-skin-sam">
		<form method="POST" action="update.htm" name="resultrangesform">
		<input type="hidden" name="_method" id="_method" value="update"/>
		<input type="hidden" name="param_id" id="param_id" value="${vitalBean.param_id}"/>

		<h1>Reference Ranges Master</h1>
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Vital Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Name:</td>
					<td class="forminfo">${vitalBean.param_label}</td>
					<td class="formlabel">Vital Category</td>
					<td class="forminfo">${vitalBean.param_container eq 'V' ? 'Vital': (vitalBean.param_container eq 'I' ? 'Intake' : 'Output')}</td>
				</tr>
				<tr>
					<td class="formlabel">Visit Type:</td>
					<td class="forminfo">${vitalBean.visit_type eq 'I'?'Only for IP': (vitalBean.visit_type eq 'O' ? 'Only for OP' : 'All') }</td>
					<td class="formlabel">Status:</td>
					<td class="forminfo">${vitalBean.param_status == 'A' ?'Active':'Inactive'}</td>
				</tr>
			</table>
		</fieldset>

	<div class="resultList" >
		<table class="detailList dialog_displayColumns" id="resultsTable" width="100%">
			<tr>
				<th>Priority</th>
				<th>Applicable To All</th>
				<th>Min Age</th>
				<th>Max Age</th>
				<th>Gender</th>
				<th>Min Normal</th>
				<th>Max Normal</th>
				<th>Min Critical</th>
				<th>Max Critical</th>
				<th>Min Improbable</th>
				<th>Max Improbable</th>
				<th>Reference Ranges</th>
				<th></th>
			</tr>
			<c:forEach items="${resultRanges}" var="range" >
				<tr style="display: ">
					<td class="formlabel">${range.priority }
						<input type="hidden" name="priority" id="priority" value="${range.priority }"/>
						<input type="hidden" name="range_id" id="range_id" value="${range.range_id }"/>
					</td>
					<td class="formlabel">${range.range_for_all == 'N' ? 'No' : 'Yes'}
						<input type="hidden" name="range_for_all" id="range_for_all" value="${range.range_for_all }"/>
					</td>
					<td class="formlabel">${range.min_patient_age }${range.age_unit }
						<input type="hidden" name="min_patient_age" id="min_patient_age" value="${range.min_patient_age }"/>
					</td>
					<td class="formlabel">${range.max_patient_age }${range.age_unit == 'Y' ? 'Yrs' : 'Days' }
						<input type="hidden" name="max_patient_age" id="max_patient_age" value="${range.max_patient_age }"/>
						<input type="hidden" name="age_unit" id="age_unit" value="${range.age_unit }"/>
					</td>
					<td class="formlabel">${range.patient_gender eq 'M' ? 'Male' : range.patient_gender eq 'F' ? 'Female' : range.patient_gender eq 'O' ? 'Others' : ''}
						<input type="hidden" name="patient_gender" id="patient_gender" value="${range.patient_gender }"/>
					</td>
					<td class="formlabel">${range.min_normal_value }
						<input type="hidden" name="min_normal_value" id="min_normal_value" value="${range.min_normal_value }"/>
					</td>
					<td class="formlabel">${range.max_normal_value }
						<input type="hidden" name="max_normal_value" id="max_normal_value" value="${range.max_normal_value }"/>
					</td>
					<td class="formlabel">${range.min_critical_value }
						<input type="hidden" name="min_critical_value" id="min_critical_value" value="${range.min_critical_value }"/>
					</td>
					<td class="formlabel">${range.max_critical_value }
						<input type="hidden" name="max_critical_value" id="max_critical_value" value="${range.max_critical_value }"/>
					</td>
					<td class="formlabel">${range.min_improbable_value }
						<input type="hidden" name="min_improbable_value" id="min_improbable_value" value="${range.min_improbable_value }"/>
					</td>
					<td class="formlabel">${range.max_improbable_value }
						<input type="hidden" name="max_improbable_value" id="max_improbable_value" value="${range.max_improbable_value }"/>
					</td>
					<td class="formlabel"><insta:truncLabel value="${range.reference_range_txt }" length="16"/>
						<input type="hidden" name=reference_range_txt id="reference_range_txt" value="${fn:escapeXml(range.reference_range_txt)}"/>
					</td>
					<td style="text-align: center">
						<a href="javascript:void(0)" name="btnEditRanges" id="btnEditRanges"
						onclick="getAddDialog(this);"
						title="Edit Result Ranges" >
						<img src="${cpath}/icons/Edit.png" class="button" />
					</td>
				</tr>
			</c:forEach>
		</table>
		<div>
			<table width="100%" class="detailListAddBtn">
				<tr>
					<td width="100%"></td>
					<td align="right" style="width: 20px">
						<button type="button" name="btnAddRange" id="btnAddRange" title="Add New Result Range"
							onclick="getAddDialog(this)" accesskey="+" class="imgButton">
						<img src="${cpath}/icons/Add.png"/>
						</button>
					</td>
				</tr>
			</table>
		</div>
	</div>
	<div class="screenActions">
		<input type="submit" id="save" name="save" value="Save"/>
		|
		<a href="${cpath }/${pagePath}/show.htm?&param_id=${ifn:cleanURL(param.param_id)}">
					Vital Details
		</a>
	</div>
</form>
<form name="addRange">
	<input type="hidden" name="editedRow" id="editedRow"/>
	<div id="editDialog" style="visibility: hidden;">
	<div class="bd">
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Result Ranges </legend>
		<table class="formtable" width="100%">
			<tr>
				<td class="formlabel">Priority:</td>
				<td>
					<input type="text" name="ePriority" id="ePriority" onkeypress="return enterNumOnlyzeroToNine(event);"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Range applicable for all:</td>
				<td>
					<input type="checkbox" value="Y" name="eApplicableToAll" id="eApplicableToAll" onchange="disableFields(this.checked)"/>
				</td>
			</tr>
			<tr>
				<td  class="formlabel">Patient Gender:</td>
				<td>
					<select class=" dropdown"  name="ePatientGender" id="ePatientGender" tabindex="50">
							<option value="N">-- Select --</option>
							<option value="M">Male</option>
							<option value="F">Female</option>
							<option value="O">Others</option>
					</select>
				</td>
			</tr>
			<tr>
				<td  class="formlabel">Patient Age&nbsp;&nbsp;&nbsp;&nbsp; Min:
				</td>
				<td>
					<input type="text" name="eMinPatientAge" id="eMinPatientAge" onblur="compareMinMaxAge(this)"  onkeypress="return enterNumOnlyzeroToNine(event);"/>
				</td>
				<td style="width: 200px;">
					<label>to &nbsp;&nbsp;&nbsp;&nbsp;Max:&nbsp;&nbsp;&nbsp;&nbsp;</label>
					<input type="text" name="eMaxPatientAge" id="eMaxPatientAge" onblur="compareMinMaxAge(this)" onkeypress="return enterNumOnlyzeroToNine(event);"/>
				</td>
				<td>
					<insta:radio name="eAgeUnit" radioValues="Y,D" radioText="Years,Days" radioIds="eAgeUnitY,eAgeUnitD" value="Y"/>
				</td>
			</tr>
			<tr>
				<td  class="formlabel">Normal Range&nbsp;&nbsp;&nbsp;&nbsp; Min:</td>
				<td>
					<input type="text" name="eMinNormalValue" id="eMinNormalValue" onkeypress="return enterNumOnlyANDdot(event);"/>
				</td>
				<td style="width: 200px;">
					<label>to &nbsp;&nbsp;&nbsp;&nbsp;Max:&nbsp;&nbsp;&nbsp;&nbsp;</label>
					<input type="text" name="eMaxNormalValue" id="eMaxNormalValue" onkeypress="return enterNumOnlyANDdot(event);"/>
				</td>
			</tr>
			<tr>
				<td  class="formlabel">Abnormal Range&nbsp;&nbsp;&nbsp;&nbsp; Min:</td>
				<td>
					<input type="text" name="eMinCriticalValue" id="eMinCriticalValue" onkeypress="return enterNumOnlyANDdot(event);"/>
				</td>
				<td style="width: 200px;">
					<label>to &nbsp;&nbsp;&nbsp;&nbsp;Max:&nbsp;&nbsp;&nbsp;&nbsp;</label>
					<input type="text" name="eMaxCriticalValue" id="eMaxCriticalValue" onkeypress="return enterNumOnlyANDdot(event);"/>
				</td>
			</tr>
			<tr>
				<td  class="formlabel">Critical Range&nbsp;&nbsp;&nbsp;&nbsp; Min:</td>
				<td>
					<input type="text" name="eMinImprobableValue" id="eMinImprobableValue"  onkeypress="return enterNumOnlyANDdot(event);"/>
				</td>
				<td style="width: 200px;">
					<label>to &nbsp;&nbsp;&nbsp;&nbsp;Max:&nbsp;&nbsp;&nbsp;&nbsp;</label>
					<input type="text" name="eMaxImprobableValue" id="eMaxImprobableValue" onkeypress="return enterNumOnlyANDdot(event);"/>
				</td>
			</tr>
			<tr>
				<td  class="formlabel">Reference Ranges&nbsp;&nbsp;(Textual Representation):</td>
				<td>
					<textarea rows="3" cols="20" name="eRefernceRange" id="eRefernceRange" ></textarea>
				</td>
			</tr>
		</table>
	</fieldset>
		<input type="button" name="add" id="add" value="Ok" onclick="onDialogOK();" tabindex="4"/>
		<input type="button" name="close" id="close" value="Close" onclick="closeDialog();" tabindex="5"/>
</div>
</div>
		</form>
	</body>
</html>
