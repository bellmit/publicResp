<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<html>
<head>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Complaints Log- Insta HMS</title>

	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>

	<script>
		function doClose() {
			window.location.href = "${cpath}/master/ComplaintsLog.do?_method=list&sortOrder=complaint_id&sortReverse=true&complaint_status=Open&complaint_status=Clarify";
		}

		function keepBackUp(){
			if(document.forms[0]._method.value == 'update'){
					backupName = document.forms[0].display_complaint_id.value;
			}
		}
		function checkLength(e, obj, limit) {
			var key=0;
			if(window.event){
				key = e.keyCode;
			}
			else{
				key = e.which;
			}
			if (obj.value.toString().length > limit && key != 8 && key != 0)
				return false;
			else
				return true;
		}
		function validate() {
			var phoneNo = document.getElementById('phone_no').value;
			if (phoneNo == '') {
				alert("Please enter the Phone No.");
				document.getElementById('phone_no').focus();
				return false;
			} else {
				if (!validatePhoneNo(phoneNo)) {
					document.getElementById('phone_no').focus();
					return false;
				}
			}

		}
	</script>

</head>

<body onload="keepBackUp();">

	<form action="ComplaintsLog.do" method="POST">

		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">

		<c:if test="${param._method == 'show'}">
			<input type="hidden" name="complaint_id" value="${bean.map.complaint_id}"/>
		</c:if>

		<h1>${param._method == 'add' ? 'Add' : 'Edit'} Complaint</h1>

		<insta:feedback-panel/>

		<div class="infoPanel">
			<div class="img"><img src="${cpath}/images/information.png"/></div>
			<div class="txt">Note: No validations or restrictions on changes to log is enforced. <br> Please be careful when modifying comments entered by others.</div>
			<div style="clear: both"></div>
		</div>

		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Complaint Id: </td>
					<td><input type="text" name="display_complaint_id" id="display_complaint_id"
							 value="${complaintDetails[0].COMPLAINT_ID}" readonly /></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr>
					<td class="formlabel">Module: </td>
					<td><input type ="text"  name="complaint_module" id="complaint_module"
							 value="${complaintDetails[0].COMPLAINT_MODULE}" /></td>
				</tr>
				<tr>
					<fmt:message key="insta.software.version" var="version"/>
					<td class="formlabel">Version: </td>
					<td><input type ="text"  name="version_no" id="version_no"
							 value="${param._method == 'add' ? version : complaintDetails[0].VERSION_NO}" readonly/></td>
				</tr>
				<tr>
					<td class="formlabel">Phone No.: </td>
					<td><input type="text" name="phone_no" id="phone_no" value="${bean.map.phone_no}"></td>
				</tr>
				<tr>
					<td class="formlabel">Summary:</td>
					<td>
					<textarea rows="1" cols="30" name="complaint_summary" class="required"
					title="Please enter Summary upto 120 characters only." onkeypress="return checkLength(event, this, 120);">${complaintDetails[0].COMPLAINT_SUMMARY}</textarea>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Description:</td>
					<td>
					<textarea rows="10" cols="50" name="complaint_desc" class="required" title="Please enter Description">${complaintDetails[0].COMPLAINT_DESC}</textarea>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Status:</td>
					<td>
						<insta:selectoptions name="complaint_status" id="complaint_status"
							opvalues="Open,Clarify,Pending,Fixed,NotInScope,ProdEnh"
							 optexts="Open,Clarify,Pending,Fixed,Not In Scope,Prod Enh"
							value="${complaintDetails[0].COMPLAINT_STATUS}" />
					</td>
				</tr>
				<tr>
					<td class="formlabel">Closure Note:</td>
					<td>
						<textarea rows="10" cols="50" name="complaint_closure_note" <c:if test="${roleId ne '1'}" >disabled</c:if>  >${complaintDetails[0].COMPLAINT_CLOSURE_NOTE}</textarea>
					</td>
				</tr>
			</table>
		</fieldset>

		<table class="screenActions">
			<tr>
				<td><button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button></td>
				<c:if test="${param._method=='show'}">
					<td>&nbsp;|&nbsp;</td>
					<td><a href="#" onclick="window.location.href='${cpath}/master/ComplaintsLog.do?_method=add'">Add</a></td>
				</c:if>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0)" onclick="doClose();"/>Complaint Logs</a></td>
			</tr>
		</table>

	</form>

</body>
</html>
