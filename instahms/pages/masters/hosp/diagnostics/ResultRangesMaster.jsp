<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.struts.Globals" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

		<insta:link type="js" file="masters/resultRanges.js" />

		<title>Result Ranges Master</title>
		<script type="text/javascript">
			<c:if test="${ not empty resultLabels}">
				var resultLabels = ${resultLabels};
			</c:if>
			var dialogState = 'Add';
			var cpath = '<%=request.getContextPath()%>';
		</script>
	</head>
	<body onload="init();" class="yui-skin-sam">
		<form method="POST" action="resultranges.do" name="resultrangesform">
		<input type="hidden" name="_method" id="_method" value="save"/>
		<input type="hidden" name="test_id" id="test_id" value="${testDeatils.map.test_id }"/>
		<input type="hidden" name="orgId" id="orgId" value="${testDeatils.map.org_id }"/>
		<h1>Result Ranges Master</h1>
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Test Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Test Name:</td>
					<td class="forminfo">${testDeatils.map.test_name }</td>
					<td class="formlabel">Department</td>
					<td class="forminfo">${testDeatils.map.ddept_name }</td>
					<td class="formlabel">Status:</td>
					<td class="forminfo">${testDeatils.map.status == 'A' ?'Active':'Inactive'}</td>
				</tr>
			</table>
		</fieldset>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
			<tr>
				<td class="formlabel">Filter Result Labels:</td>
				<td align="left" colspan="2">
					<select name="fil_resultlabel_id" id="fil_resultlabel_id" class="dropdown" onchange="filterTabelByLabel();">
						<option value="">---Select---</option>
						<c:forEach items="${dynaResultLblList}" var="resultLblBean">
							<option value="${resultLblBean.map.resultlabel_id}" >${resultLblBean.map.resultlabel}${not empty resultLblBean.map.method_name ? ' (' : ''}${not empty resultLblBean.map.method_name ? resultLblBean.map.method_name : '' }${not empty resultLblBean.map.method_name ? ')' : ''}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
		</table>
	<div class="resultList" >
		<table class="detailList dialog_displayColumns" id="resultsTable" width="100%">
			<tr>
				<th>Result Name</th>
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
					<td class="formlabel"><insta:truncLabel value="${range.map.resultlabel }" length="30"/>${not empty range.map.method_name ? ' (' : ''}${not empty range.map.method_name ? range.map.method_name : ''}${not empty range.map.method_name ? ')' : ''}
						<input type="hidden" name="resultlabel_id" id="resultlabel_id" value="${range.map.resultlabel_id }"/>
						<input type="hidden" name="resultlabel" id="resultlabel" value="${range.map.resultlabel }"/>
						<input type="hidden" name="result_range_id" id="result_range_id" value="${range.map.result_range_id }"/>
					</td>
					<td class="formlabel">${range.map.priority }
						<input type="hidden" name="priority" id="priority" value="${range.map.priority }"/>
					</td>
					<td class="formlabel">${range.map.range_for_all == 'N' ? 'No' : 'Yes'}
						<input type="hidden" name="range_for_all" id="range_for_all" value="${range.map.range_for_all }"/>
					</td>
					<td class="formlabel">${range.map.min_patient_age }${range.map.age_unit }
						<input type="hidden" name="min_patient_age" id="min_patient_age" value="${range.map.min_patient_age }"/>
					</td>
					<td class="formlabel">${range.map.max_patient_age }${range.map.age_unit == 'Y' ? 'Yrs' : 'Days' }
						<input type="hidden" name="max_patient_age" id="max_patient_age" value="${range.map.max_patient_age }"/>
						<input type="hidden" name="age_unit" id="age_unit" value="${range.map.age_unit }"/>
					</td>
					<td class="formlabel">${range.map.gender }
						<input type="hidden" name="patient_gender" id="patient_gender" value="${range.map.patient_gender }"/>
					</td>
					<td class="formlabel">${range.map.min_normal_value }
						<input type="hidden" name="min_normal_value" id="min_normal_value" value="${range.map.min_normal_value }"/>
					</td>
					<td class="formlabel">${range.map.max_normal_value }
						<input type="hidden" name="max_normal_value" id="max_normal_value" value="${range.map.max_normal_value }"/>
					</td>
					<td class="formlabel">${range.map.min_critical_value }
						<input type="hidden" name="min_critical_value" id="min_critical_value" value="${range.map.min_critical_value }"/>
					</td>
					<td class="formlabel">${range.map.max_critical_value }
						<input type="hidden" name="max_critical_value" id="max_critical_value" value="${range.map.max_critical_value }"/>
					</td>
					<td class="formlabel">${range.map.min_improbable_value }
						<input type="hidden" name="min_improbable_value" id="min_improbable_value" value="${range.map.min_improbable_value }"/>
					</td>
					<td class="formlabel">${range.map.max_improbable_value }
						<input type="hidden" name="max_improbable_value" id="max_improbable_value" value="${range.map.max_improbable_value }"/>
					</td>
					<td class="formlabel"><insta:truncLabel value="${range.map.reference_range_txt }" length="16"/>
						<input type="hidden" name=reference_range_txt id="reference_range_txt" value="${fn:escapeXml(range.map.reference_range_txt)}"/>
					</td>
					<c:choose>
						<c:when test="${loggedInCenter == 0}">
							<td style="text-align: center">
								<a href="javascript:void(0)" name="btnEditRanges" id="btnEditRanges"
								onclick="getAddDialog(this);"
								title="Edit Result Ranges" >
								<img src="${cpath}/icons/Edit.png" class="button" />
							</td>
						</c:when>
						<c:otherwise>	
						<c:set var="centerIdsWithComma" value="${range.map.center_id_app}"/>
						<c:set var="centerIdsArray" value="${fn:split(centerIdsWithComma, ',')}" />
						<c:set var="noOfCentersApp" value="${fn:length(centerIdsArray)}" />					
							<c:choose>
								<c:when test="${noOfCentersApp > 1}">
									<td style="text-align: center">
										<a href="javascript:void(0)" name="btnEditRanges" id="btnEditRanges"
										onclick=""
										title="Edit Result Ranges" >
										<img src="${cpath}/icons/Edit1.png" class="button" />
									</td>
								</c:when>	
								<c:otherwise>
				  				<c:set var="centetId" value="${centerIdsArray[0]}"/>
					  				<c:choose>
					  					<c:when test="${centetId == loggedInCenter}">				  					
											<td style="text-align: center">
												<a href="javascript:void(0)" name="btnEditRanges" id="btnEditRanges"
												onclick="getAddDialog(this);"
												title="Edit Result Ranges" >
												<img src="${cpath}/icons/Edit.png" class="button" />
											</td>				  					
					  					</c:when>
					  					<c:otherwise>				  					
											<td style="text-align: center">
												<a href="javascript:void(0)" name="btnEditRanges" id="btnEditRanges"
												onclick=""
												title="Edit Result Ranges" >
												<img src="${cpath}/icons/Edit1.png" class="button" />
											</td>
					  					</c:otherwise>
					  				</c:choose>
								</c:otherwise>					
							</c:choose>					
						</c:otherwise>
					</c:choose>
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
						<img src="${cpath}/icons/Add.png">
					</button>
					</td>
				</tr>
			</table>
		</div>
	</div>
	<div>
		<table class="screenAction">
		<tr>
			<td>
				<input type="submit" id="save" name="save" value="Save"/>
			</td>
			<c:if test="${urlRightsMap.diag_edit_master_id == 'A'}">
				<td>|&nbsp;
					<a href="${cpath }/master/addeditdiagnostics/show.htm?&testid=${testDeatils.map.test_id }&orgId=${testDeatils.map.org_id }">
						Test Details
					</a>
				</td>
			</c:if>
		</tr>
		</table>
	</div>
</form>
<form name="addRange">
	<input type="hidden" name="editedRow" id="editedRow"/>
	<div id="editDialog" style="visibility: hidden;">
	<div class="bd">
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Result Ranges </legend>
		<table class="formtable" width="100%">
			<tr>
				<td class="formlabel">Result Label:</td>
				<td>
					<select name="eResultlabelId" id="eResultlabelId" class="dropdown">
						<option value="">-- Select --</option>
					</select>
				</td>
				<td class="formlabel">Priority:</td>
				<td>
					<input type="text" name="ePriority" id="ePriority" />
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
					<input type="text" name="eMinPatientAge" id="eMinPatientAge" onblur="compareMinMaxAge(this)" />
				</td>
				<td style="width: 200px;">
					<label>to &nbsp;&nbsp;&nbsp;&nbsp;Max:&nbsp;&nbsp;&nbsp;&nbsp;</label>
					<input type="text" name="eMaxPatientAge" id="eMaxPatientAge" onblur="compareMinMaxAge(this)" />
				</td>
				<td>
					<insta:radio name="eAgeUnit" radioValues="Y,D" radioText="Years,Days" radioIds="eAgeUnitY,eAgeUnitD" value="Y"/>
				</td>
			</tr>
			<tr>
				<td  class="formlabel">Normal Range&nbsp;&nbsp;&nbsp;&nbsp; Min:</td>
				<td>
					<input type="text" name="eMinNormalValue" id="eMinNormalValue" />
				</td>
				<td style="width: 200px;">
					<label>to &nbsp;&nbsp;&nbsp;&nbsp;Max:&nbsp;&nbsp;&nbsp;&nbsp;</label>
					<input type="text" name="eMaxNormalValue" id="eMaxNormalValue" />
				</td>
				<td style="width: 170px;">(Outside Range - Abnormal Low / Abnormal High)</td>
			</tr>
			<tr>
				<td  class="formlabel">Abnormal Range&nbsp;&nbsp;&nbsp;&nbsp; Min:</td>
				<td>
					<input type="text" name="eMinCriticalValue" id="eMinCriticalValue" />
				</td>
				<td style="width: 200px;">
					<label>to &nbsp;&nbsp;&nbsp;&nbsp;Max:&nbsp;&nbsp;&nbsp;&nbsp;</label>
					<input type="text" name="eMaxCriticalValue" id="eMaxCriticalValue" />
				</td>
				<td style="width: 170px;">(Outside Range - Critical Low / Critical High)</td>
			</tr>
			<tr>
				<td  class="formlabel">Critical Range&nbsp;&nbsp;&nbsp;&nbsp; Min:</td>
				<td>
					<input type="text" name="eMinImprobableValue" id="eMinImprobableValue" />
				</td>
				<td style="width: 200px;">
					<label>to &nbsp;&nbsp;&nbsp;&nbsp;Max:&nbsp;&nbsp;&nbsp;&nbsp;</label>
					<input type="text" name="eMaxImprobableValue" id="eMaxImprobableValue" />
				</td>

				<td style="width: 170px;">(Outside Range - Improbable Low/ Improbable High)</td>
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
