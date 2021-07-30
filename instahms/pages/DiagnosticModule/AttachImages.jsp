<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="laboratory.testconduction.list.titleimages"/></title>

<script>
function validate(){
		var form = document.diagcenterform;

		if( trim(form.formFile.value) == '' ){
	    	alert("Browse any Image....");
	    	form.formFile.focus();
	    	return false;
	    }

		var title = trim(form.imageTitle.value);
		if(title == ''){
			alert('Image title is required');
			form.imageTitle.focus();
			return false;
		}
		var i=0;
		for(i=0;i<image_list.length;i++){
			if(title == image_list[i].title) {
				alert("Duplicate Title - Image Title already in use");
				form.imageTitle.value = " ";
				form.imageTitle.focus();
				return false;
			}
		}
		return true;
}
</script>

</head>
<body>
<h1><insta:ltext key="laboratory.testconduction.list.attachimages"/></h1>
 <c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:choose>
	<c:when test="${isIncomingPatient}">
		<insta:incomingpatientdetails incomingVisitId="${testdetails.pat_id}" />
	</c:when>
	<c:when test="${not empty testdetails.pat_id}">
		<insta:patientdetails  visitid="${testdetails.pat_id}" showClinicalInfo="true"/>
	</c:when>
	<c:otherwise>
		<insta:patientgeneraldetails  mrno="${testdetails.mr_no}" showClinicalInfo="true"/>
	</c:otherwise>
</c:choose>
<fieldset class="fieldSetBorder" style="margin-bottom: 5px;">
	<legend class="fieldSetLabel"><insta:ltext key="patient.header.fieldset.otherdetails"/>:</legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">

		<tr>
			<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.test"/>: </td>
			<td class="forminfo">
				<div title="${testdetails.test_name}">${testdetails.test_name}</div>
			</td>
			<td class="formlabel"></td>
			<td></td>
			<td class="formlabel"></td>
			<td></td>
		</tr>
	</table>
</fieldset>
<html:form method="POST" enctype="multipart/form-data"
  action="${category == 'DEP_LAB' ? 'Laboratory' : 'Radiology' }/AddTestImages.do"  onsubmit="return validate() ">
 <input type="hidden" name="_method" value="uploadImages">
 <input type="hidden" name="prescribedid" value="${ifn:cleanHtmlAttribute(param.prescribedId)}"/>



<div >${uploadStatus}</div>
	<table class="formtable" >
		<tr>
			<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.file"/>: </td>
			<td ><html:file property="formFile"/></td>
			<td class="formlabel"></td>
			<td></td>
			<td class="formlabel"></td>
			<td></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="laboratory.testconduction.list.titlelabel"/>: </td>
			<td><input type="text" name="imageTitle"></td>
		</tr>
	</table>
	<div class="screenActions">
		<button type="submit"  accesskey="U"><b><u><insta:ltext key="laboratory.testconduction.list.u"/></u></b><insta:ltext key="laboratory.testconduction.list.ploadfile"/></button>
	</div>
	<table class="dashboard" style="margin-top: 10px" cellspacing="0" cellpadding="0">
		<tr>
			<th><insta:ltext key="laboratory.testconduction.list.image"/></th>
			<th><insta:ltext key="laboratory.testconduction.list.view"/></th>
			<th><insta:ltext key="laboratory.testconduction.list.delete"/></th>
		</tr>
		<c:forEach items="${imagesList}" var="row">
		<tr>
			<td>${ifn:cleanHtml(row.map.title)}</td>
			<c:url value="AddTestImages.do" var="displayURL">
				<c:param name="_method" value="getImage"/>
				<c:param name="titleName" value="${row.map.title}"/>
				<c:param name="prescribedId" value="${param.prescribedId}"/>
			</c:url>
			<td><a href="<c:out value='${displayURL}'/>"><insta:ltext key="laboratory.testconduction.list.view"/></a></td>
			<c:url var="DeleteURL" value="AddTestImages.do">
				<c:param  name="_method" value="deleteImage" />
				<c:param  name="titleName" value="${row.map.title}" />
				<c:param  name="prescribedId" value="${param.prescribedId}"/>
			</c:url>
			<td><a href="<c:out value='${DeleteURL}'/>"><insta:ltext key="laboratory.testconduction.list.delete"/></a></td>
		</tr>
		</c:forEach>
	</table>
	<div class="clear: both"/>
	<div style="display: ${not empty generatedURL ? 'block' : 'none'}">
		<img src="${cpath}/${generatedURL}" width="150" height="100"/>
	</div>

	<div class="screenActions">
		<insta:screenlink screenId="${category == 'DEP_LAB' ? 'lab_edit_results' : 'rad_edit_results'}"
			addPipe="false" label="Edit Test Results" extraParam="?_method=getBatchConductionScreen
			&reportId=${empty param.reportId ? testPrescDetails.map.report_id : param.reportId}&prescId=${param.prescribedId}
			&visitid=${testdetails.pat_id}&category=${category}"/>
		<insta:screenlink screenId="${category == 'DEP_LAB' ? 'lab_unfinished_tests' : 'rad_unfinished_tests'}"
			addPipe="true" label="Pending Tests" extraParam="?_method=unfinishedTestsList&conducted=N&conducted=P&conducted=NRN&sortOrder=pres_date&patient_id=${testdetails.pat_id}"/>
		<insta:screenlink screenId="${category == 'DEP_LAB' ? 'lab_schedules_list' : 'rad_schedules_list'}"
			addPipe="true" label="Schedules" extraParam="?_method=getScheduleList&patient_id=${testdetails.pat_id}"/>
	</div>
   <script>
  var image_list = ${imagesListjson};
  </script>
</html:form>
</body>
</html>
