<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<title>Optometrist Examination Screen</title>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link type="script" file="opthalmology/optometristexam.js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

</head>
	<body onload="initDialog(); ">
	<h1>Optometrist Examination Screen</h1>
	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${mr_no}" addExtraFields="true" />
	<form name="OpthalmologyForm" action="${cpath}/opthalmology/OpthalmologyTestsList.do" method="post">

		<input type="hidden" name="_method" value="saveTestValues" />
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}" />
		<input type="hidden" name="doctor_id" value="${ifn:cleanHtmlAttribute(param.doctor_id)}" />
		<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}">
		<input type="hidden" name="complaint"value="${ifn:cleanHtmlAttribute(param.complaint)}" />
		<input type="hidden" name="opthalId" value="${opthal_id}" />

	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Registration Details</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Optometrist Name: </td>
			<td class="forminfo">${ifn:cleanHtml(doctor_name)}</td>
			<td class="formlabel">Complaint: </td>
			<td class="forminfo">${ifn:cleanHtml(complaint)}</td>
		</tr>
	</table>
	<c:set var="historyExist" value="${not empty patientHistory || not empty patientEyeHistory}" />
	</fieldset>
	<table class="formtable">
		<tr>
			<td style="padding-left: 0px">
				<div id="CollapsiblePanel1" class="CollapsiblePanel" style="padding-bottom:9px;">
					<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
						<div class="fltL " style="width: 230px; margin:5px 0 0 10px;"><font color="${not empty patientHistory ? 'green' : ''}" >Patient History >></font></div>
							<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
								<img src="${cpath}/images/down.png" />
							</div>
						<div class="clrboth"></div>
					</div>
					<fieldset class="fieldSetBorder">
						<table class="formtable">
							<tr>
								<td class="formlabel">Family History:</td>
								<td><textarea name="familyHistory"  style="width: 275px" >${patientHistory.map.family_history}</textarea></td>
								<td class="formlabel">Past Ocular History:</td>
								<td><textarea name="pastOcularHistory"  style="width: 275px">${patientEyeHistory.map.patient_eye_history}</textarea></td>
							</tr>
							<tr>
								<td class="formlabel">Medical/Surgery History:</td>
								<td><textarea name="medicalHistory"  style="width: 275px">${patientHistory.map.past_surgery}</textarea></td>
							</tr>
						</table>
					</fieldset>
				</div>
			</td>
		</tr>
	</table>


		<fieldset  class="fieldSetBorder">
		 <legend class="fieldSetLabel">Exam Details</legend>
		 <div id="dialog" style="visibility:hidden">
			<div class="bd">
				<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Values</legend>
				<table>
				<input type="hidden" id="fromId" value=""/>
				<tr>
					<c:set var="i" value="1"/>
					<input type="hidden" id="fromId" value=""/>
					<td style="text-align:right;">+</td>
					<td style="text-align:right;">&nbsp;</td><td style="text-align:right;">&nbsp;</td>
					<c:forTokens items="${valuelistpicker}" var="valuelist" delims=",">
						<td style="text-align:right;width:4px"><a onclick='selectValue(this,"plus");'><label id='selectVal'>${ifn:cleanHtml(valuelist)}</label></a></td><td style="text-align:right;">&nbsp;</td><td style="text-align:right;">&nbsp;</td>
						${(i) % 6 eq 0 ? '<tr><td style="text-align:right;">+</td><td style="text-align:right;">&nbsp;</td><td style="text-align:right;">&nbsp;</td>' : ''}
						<c:set var="i" value="${i+1}"/>
					</c:forTokens>
				</tr>
				<tr><td>&nbsp;</td></tr>
				<tr>
					<c:set var="i" value="1"/>
					<td style="text-align:right;">-</td>
					<td style="text-align:right;">&nbsp;</td><td style="text-align:right;">&nbsp;</td>
					<c:forTokens items="${valuelistpicker}" var="valuelist" delims=",">
						<td style="text-align:right;width:4px"><a onclick='selectValue(this,"minus");'><label id='selectVal'>${ifn:cleanHtml(valuelist)}</label></a></td><td style="text-align:right;">&nbsp;</td><td style="text-align:right;">&nbsp;</td>
						${(i) % 6 eq 0 ? '<tr><td style="text-align:right;">-</td><td style="text-align:right;">&nbsp;</td><td style="text-align:right;">&nbsp;</td>' : ''}
						<c:set var="i" value="${i+1}"/>
					</c:forTokens>
				</tr>
				</table>
				</fieldset>
			</div>
		</div>
		<table id="valuesTab" style="border:solid 1px" align="center" width="100%">
			<tr>
			<c:set var="indexx" value="0"/>
			<c:set var="newOrUpdate" value="0" />
			<c:forEach items="${eyeTestList}" var="testList" varStatus="status">

					<td valign="top" style="border:solid 1px">
						<h1 align="center">${testList.map.test_name}</h1>
						<table align="center" >
							<c:set var="displayName" value=""/>

							<c:forEach items="${parametersList}" var="attributes" varStatus="index">

								<c:if test="${attributes.test_id eq testList.map.test_id }">

								<c:set var="indexx" value="${indexx+1}" />

									<input type="hidden" name="testIds" value="${testList.map.test_id}" />
								<c:if test="${displayName ne attributes.display_name}" >
								<tr>
									<td align="left" style="padding-bottom: 3px;">
										${attributes.display_name}
									</td>

								</c:if>
										<c:set var="eachValue" />
										<c:forEach items="${testValues}" var="testValues">
											<c:if test="${(testValues.test_id eq testList.map.test_id && testValues.attribute_id eq attributes.attribute_id)}">
												<c:set var="newOrUpdate" value="${newOrUpdate+1}" />
												<c:set var="eachValue" value="${testValues.test_values}" />
												<input type="hidden" name="key" value="${testValues.value_id}" />

											</c:if>
										</c:forEach>

										<c:if test="${indexx != newOrUpdate}">
											<input type="hidden" name="key" value="_" />
											<c:set var="newOrUpdate" value="${indexx}" />
										</c:if>

										<c:if test="${fn:contains(attributes.attribute_name, 'RE')==true}">
											<td class="formlabel" style="padding-bottom: 5px;padding-left: 3px">RE: </td>
												<input type="hidden" name="attributeIds" value="${attributes.attribute_id}" />
											<td style="padding-bottom: 5px;">
												<c:choose>
												<c:when test="${attributes.field_type eq 'dropdown'}">
													<select name="attribValues" id="${attributes.test_attrib_id}" class="dropdown" style="width: 65px">
														<option value="" selected></option>
														<c:forTokens items="${attributes.default_values}" var="values" delims=",">
															 <option value="${values}" ${eachValue == values?'selected':'' }>${values}</option>
														</c:forTokens>
													</select>
												</c:when>
												<c:when test="${attributes.field_type eq 'valuepicker' }">
													<input type="text" name="attribValues"
														value="${eachValue}" class="number" id="${attributes.test_attrib_id}"/><img class="button" name="choose" id="img_${attributes.test_attrib_id}" style="cursor:pointer;"  onclick="openDialogBox(this)" src="../icons/tearoff.png"/>
												</c:when>
												<c:otherwise>
													<input type="text" name="attribValues" value="${eachValue}" class="number" id="${attributes.test_attrib_id}" onblur="return enterNumbersAndPlusMinus(this);"/>
												</c:otherwise>
												</c:choose>
											</td>
										</c:if>
										<c:if test="${fn:contains(attributes.attribute_name, 'LE')==true}">
											<input type="hidden" name="attributeIds" value="${attributes.attribute_id}" />
										<td class="formlabel" style="padding-bottom: 5px;padding-left: 3px">LE: </td>
										<td style="padding-bottom: 5px;padding-right: 3px">
											<c:choose>
											<c:when test="${attributes.field_type eq 'dropdown'}">
												<select name="attribValues" id="${attributes.test_attrib_id}" class="dropdown" style="width: 65px">
													<option value="" selected></option>
													<c:forTokens items="${attributes.default_values}" var="values" delims=",">
														 <option value="${values}" ${eachValue == values?'selected':'' }>${values}</option>
													</c:forTokens>
												</select>
											</c:when>
											<c:when test="${attributes.field_type eq 'valuepicker' }">
												<input type="text" name="attribValues" value="${eachValue}" class="number" id="${attributes.test_attrib_id}"/><img class="button" name="choose" id="img_${attributes.test_attrib_id}" style="cursor:pointer;"  onclick="openDialogBox(this)" src="../icons/tearoff.png"/>
											</c:when>
											<c:otherwise>
											<input type="text" name="attribValues" class="number" value="${eachValue}" id="${attributes.test_attrib_id}" onblur="return enterNumbersAndPlusMinus(this)";/>
											</c:otherwise>
											</c:choose>
										</td>
										</c:if>
									<c:choose>
										<c:when test="${displayName ne ''}">
									</tr>
									<c:set var="displayName" value=''/>
									</c:when>
									<c:otherwise>
										<c:set var="displayName" value="${attributes.display_name}" />
									</c:otherwise>
									</c:choose>
								</c:if>
							</c:forEach>
						</table>
					</td>
						${(status.index+1) % 3 eq 0 ? '<tr>' : ''}

			</c:forEach>

		 	</tr>

		</table>
		<table>
			<tr><td>&nbsp;</td></tr>
			<tr><td>Consultation Complete:</td>
				<c:set var="statusValue" value="${opthalTestmain.map.status}" />
				<td><input type="checkbox" name="statusCheckbox"  id="statusCheckbox" ${(statusValue == 'D' || statusValue == 'S') ? 'checked' : ''} onclick="setStatus(this, 'D');"
						${(statusValue == 'D' || statusValue == 'S') ? 'disabled' : ''}/></td>
				<input type="hidden" name="status" id="status" value="${opthalTestmain.map.status}" />
			</tr>
		</table>
		<table width="100%">
			<tr><td>&nbsp;</td></tr>
			<tr>
				<td class="formlabel">Notes</td>
			</tr>
			<tr>
				<td style="padding-top: 1px"><textarea id="notes" name="notes" style="width:100%;height: 60px">${opthalTestmain.map.test_notes}</textarea></td>
			</tr>
		</table>
		</fieldset>
		<div class="screenActions">
			<input type="submit" value="Save" />
			|
			<input type="button" value="Cancel">
			|
			<a href="${cpath }/opthalmology/OpthalmologyTestsList.do?_method=list&status=">Patient List</a>
			| <a href="${cpath}/opthalmology/DoctorEyeExam.do?_method=showDoctorEyeExamScreen&patient_id=${ifn:cleanURL(param.patient_id)}&mr_no=${ifn:cleanURL(mr_no)}&doctor_id=${ifn:cleanURL(param.doctor_id)}">Doctor Exam</a>
			| <a href="${cpath}/emr/EMRMainDisplay.do?_method=list&mr_no=${ifn:cleanURL(mr_no)}&VisitId=${ifn:cleanURL(param.patient_id)}">Latest test Reports</a>
			| <a href="${cpath}/emr/EMRMainDisplay.do?_method=list&mr_no=${ifn:cleanURL(mr_no)}">EMR View</a>
		</div>

	</form>
	<script>
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:${!historyExist}});
	</script>
	</body>
</html>