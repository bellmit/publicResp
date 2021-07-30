<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title> Edit / Issue MRD Case File - Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>

<script>
	var mrdUserNameList = <%= request.getAttribute("mrdUserNameList") %>;

	function doClose() {
		var screen = '${ifn:cleanJavaScript(param.mrdscreen)}';
		if(screen == 'issue') {
			window.location.href = '${cpath}/medicalrecorddepartment/MRDCaseFileIssue.do?_method=list&mrdscreen='+screen+'&case_status=&visit_status=A&sortReverse=false&sortOrder=mr_no';
		}else {
			window.location.href = '${cpath}/medicalrecorddepartment/MRDCaseFileReturn.do?_method=list&mrdscreen='+screen+'&case_status=&visit_status=A&sortReverse=false&sortOrder=mr_no';
		}
	}

	function changeApprovalReject(value,id) {
		document.getElementById("approve_reject"+id).value = value;
	}

	function validateFields() {
		if (!doValidateDateField(document.forms[0].issued_date))
			return false;

		if (document.forms[0].issued_date.value == "")  {
			alert("Issue Date is required");
			document.forms[0].issued_date.focus();
			return false;
		}
		if(!doValidateDateField(document.forms[0].issued_date, "past")){
			return false;
	  	}
		if (trim(document.forms[0].issued_to.value) == "")  {
			alert("Issued To is required");
			document.forms[0].issued_to.focus();
			return false;
		}
		if (document.forms[0].purpose.value == "")  {
			alert("Purpose is required");
			document.forms[0].purpose.focus();
			return false;
		}
		if(document.forms[0].mrd_available != null &&
			document.forms[0].mrd_available.checked &&
			document.forms[0].remarks.value == "") {
			alert("Please enter remarks");
			document.forms[0].remarks.focus();
			return false;
		}
		return true;
	}

	function onSaveValidate() {
		if(document.forms[0].caseStatus != null && document.forms[0].caseStatus.checked) {
			document.forms[0].case_status.value = "I";
		}else document.forms[0].case_status.value = "";

		if(document.forms[0].issued_date.value != ''
			&& !document.forms[0].mrd_available) {
			if(trim(document.forms[0].issued_to.value) == '') {
				alert("Issued to is required");
				document.forms[0].issued_to.value = trim(document.forms[0].issued_to.value);
				document.forms[0].issued_to.focus();
				return false;
			}
			if (!doValidateDateField(document.forms[0].issued_date))
			return false;

			if (document.forms[0].issued_date.value == "")  {
				alert("Issue Date is required");
				document.forms[0].issued_date.focus();
				return false;
			}
			if(!doValidateDateField(document.forms[0].issued_date, "past")){
				return false;
		  	}
		  	if(document.forms[0].issued_time.value == '') {
		  		alert("Issue Time is required");
				document.forms[0].issued_time.focus();
				return false;
		  	}
		  	if(!doValidateTimeField(document.forms[0].issued_time)){
		  		return false;
		  	}
			document.forms[0].issued_on.value = document.forms[0].issued_date.value +" "+document.forms[0].issued_time.value;
		}
		document.forms[0].submit();
	}

	function initCaseFileUserNameAutoComplete() {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(mrdUserNameList);
			oAutoComp = new YAHOO.widget.AutoComplete('issued_to', 'issuedToDropdown', dataSource);
			oAutoComp.maxResultsDisplayed = 5;
			oAutoComp.allowBrowserAutocomplete = false;
			oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			oAutoComp.typeAhead = false;
			oAutoComp.useShadow = false;
			oAutoComp.minQueryLength = 0;
			oAutoComp.forceSelection = false;
		}
	}

	function setMRDReturn() {
		if(document.forms[0].mrd_available.checked) {
			document.forms[0].mrdReturn.value = "Y";
		} else {
			document.forms[0].mrdReturn.value = "N";
		}
	}
</script>
</head>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Available"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>
<c:set target="${statusDisplay}" property="L" value="Lost"/>
<c:set target="${statusDisplay}" property="U" value="Issued"/>

<body class="yui-skin-sam" onload="initCaseFileUserNameAutoComplete();">
<c:choose>
	<c:when test="${mrdscreen == 'issue'}">
		<c:set var="actionurl" value="MRDCaseFileIssue.do"/>
	</c:when>
	<c:otherwise>
		<c:set var="actionurl" value="MRDCaseFileReturn.do"/>
	</c:otherwise>
</c:choose>
<h1> Edit / Issue MRD Case File </h1>

<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${mrdfile.map.mr_no}" />
<form action="${actionurl}" method="POST">
	<input type="hidden" name="_method" value="create">
	<input type="hidden" name="mr_no" value="${mrdfile.map.mr_no}">
	<input type="hidden" name="mrdReturn" value="N"/>
	<input type="hidden" name="case_status" value="${mrdfile.map.case_status}"/>
	<input type="hidden" name="issued_on" value=""/>
	<input type="hidden" name="mrdscreen" value="${ifn:cleanHtmlAttribute(mrdscreen)}"/>


	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Other Details</legend>
			<table class="formtable" cellpadding="0" cellspacing="0">
			<c:choose>
				<c:when test="${not empty inActiveVisitId}">
					<tr>
						<td class="formlabel">Last Visit No:</td><td class="forminfo">${inactivePatient.map.visit_id}</td>
						<td class="formlabel">Last Dept:</td><td class="forminfo">${inactivePatient.map.dept_name}</td>
						<td class="formlabel">Last Doctor:</td><td class="forminfo">${inactivePatient.map.doctor_name}</td>
					</tr>
					<tr>
						<td class="formlabel">Last Ward:</td>
						<td class="forminfo">${inactivePatient.map.reg_ward_name == null || inactivePatient.map.reg_ward_name == ""  ?inactivePatient.map.alloc_ward_name:inactivePatient.map.reg_ward_name}</td>
						<td class="formlabel">Last Bed Type:</td>
						<td class="forminfo">${inactivePatient.map.alloc_bed_type == null || inactivePatient.map.alloc_bed_type == ""  ?inactivePatient.map.bill_bed_type:inactivePatient.map.alloc_bed_type}</td>
						<td class="formlabel">Last Bed Name:</td>
						<td class="forminfo">${inactivePatient.map.alloc_bed_name}:</td>
					</tr>
					<tr>
						<td class="formlabel">Last Date of Admission:</td><td class="forminfo"><fmt:formatDate value="${inactivePatient.map.reg_date}" pattern="dd-MM-yyyy"/></td>
						<td class="formlabel">Last Date of Discharge:</td><td class="forminfo"><fmt:formatDate value="${inactivePatient.map.discharge_date}" pattern="dd-MM-yyyy"/></td>
					</tr>
				</c:when>
				<c:when test="${not empty activeVisitId}">
					<tr>
						<td class="formlabel">Visit No:</td><td class="forminfo">${activePatient.map.visit_id}</td>
						<td class="formlabel">Dept:</td><td class="forminfo">${activePatient.map.dept_name}</td>
						<td class="formlabel">Doctor:</td><td class="forminfo">${activePatient.map.doctor_name}</td>
					</tr>
					<tr>
						<td class="formlabel">Ward:</td>
						<td class="forminfo">${activePatient.map.reg_ward_name == null || activePatient.map.reg_ward_name == ""  ?activePatient.map.alloc_ward_name:activePatient.map.reg_ward_name}</td>
						<td class="formlabel">Bed Type:</td>
						<td class="forminfo">${activePatient.map.alloc_bed_type == null || activePatient.map.alloc_bed_type == ""  ?activePatient.map.bill_bed_type:activePatient.map.alloc_bed_type}</td>
						<td class="formlabel">Bed Name:</td>
						<td class="forminfo">${activePatient.map.alloc_bed_name}:</td>
					</tr>
					<tr>
						<td class="formlabel">Date of Admission:</td><td class="forminfo"><fmt:formatDate value="${activePatient.map.reg_date}" pattern="dd-MM-yyyy"/></td>
						<td class="formlabel">Date of Discharge:</td><td class="forminfo"><fmt:formatDate value="${activePatient.map.discharge_date}" pattern="dd-MM-yyyy"/></td>
					</tr>
				</c:when>
			</c:choose>
			<tr>
				<td class="formlabel">Old MR No:</td><td class="forminfo">${patient.oldmrno}</td>
				<td class="formlabel">Case File No:</td><td class="forminfo">${patient.casefile_no}
					<c:if test="${mrdfile.map.recreated}">(Recreated)</c:if>
				</td>
			</tr>
			</table>
	</fieldset>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Issue Details</legend>
		<table class="formtable">
			<input type="hidden" name="issuedID" value="${mrdissueBean.map.issued_id}">
			<tr>
				<td class="formlabel">Date of Issue:</td>
				<td><jsp:useBean id="now" class="java.util.Date"/>
					<c:set var="issueDt" value="${empty mrdissueBean.map.issued_on ? now : mrdissueBean.map.issued_on}"/>
					<fmt:formatDate value="${issueDt}" pattern="HH:mm" var="issueTime"/>
					<fmt:formatDate value="${issueDt}" pattern="dd-MM-yyyy" var="issueDt"/>
					<table>
						<tr>
							<td style="white-space:nowrap;"><insta:datewidget name="issued_date" valid="past" value="${issueDt}"/></td>
							<td><input type="text" size="5" name="issued_time"  value="${issueTime}"/></td>
						</tr>
					</table>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel" >Issued to:</td>
				<td valign="top" style="height:3em;">
					<div id="issuedTo_wrapper">
						<input type="text" name="issued_to" id="issued_to" value="${mrdissueBean.map.issued_to}"/>
						<div id="issuedToDropdown"  style="padding-top:5em;padding-bottom:5em"></div>
					</div>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Purpose:</td><td><input name="purpose" type="text" size="50" value="${mrdissueBean.map.purpose}"/></td>
			</tr>
			<c:choose>
				<c:when test="${empty mrdissueBean.map.issued_id}">
				</c:when>
				<c:otherwise>
					<tr>
						<td class="formlabel">Return to MRD:</td>
						<td><input name="mrd_available" type="checkbox" onclick="setMRDReturn();"></td>
					</tr>
					<tr>
						<td class="formlabel">Remarks:</td>
						<td><input name="remarks" type="text" size="50"/></td>
					</tr>
				</c:otherwise>
			</c:choose>
			<tr>
				<td class="formlabel">Case Status:</td>
				<td><input type="checkbox" name="caseStatus" <c:if test="${mrdissueBean.map.case_status == 'I'}">checked</c:if>/>Inactive</td>
			</tr>
			<tr>
				<td class="formlabel">File Status:</td><td><insta:selectoptions name="file_status" optexts="--Select File Status--,Available With MRD,Lost,Issued to User"
					opvalues=" ,A,L,U" value="${mrdissueBean.map.file_status}" style="width:15em;"/></td>
			</tr>
		</table>
	</fieldset>
	<table class="screenActions">
		<tr>
			<td><input type="button" value="Save" name="save" onclick="return onSaveValidate();"/></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();return true;">MRD Case Files</a></td>
		</tr>
	</table>

</form>
</body>
</html>
