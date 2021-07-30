<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.ISSUE_USER_MASTER_PATH %>"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<insta:js-bundle prefix="sales.issues.user"/>
	<title><insta:ltext key="sales.issues.user.title"/></title>
    <insta:link type="script" file="hmsvalidation.js" />
	<script>
		var issueUserList = ${ifn:convertListToJson(lookupListMap)};
		var userName = '';
		function init() {
			userName = document.getElementById("hosp_user").value.trim();
		}

		function checkName() {
			var Name = document.getElementById("hosp_user_name").value.trim();
			for (var i = 0; i < issueUserList.length; i++) {
				if (userName == Name) {
					return true;
				} else {
					if (Name == issueUserList[i].hosp_user_name) {
						showMessage("js.sales.issues.user.dup.issueuser");
						document.getElementById("hosp_user_name").value = '';
						document.getElementById("hosp_user_name").focus();
						return false;
					}
				}
			}
		}

		function validateForm() {
			var hosp_user_name = document.getElementById("hosp_user_name").value.trim();
			document.getElementById("hosp_user_name").value=hosp_user_name;
			if (!hosp_user_name) {
				showMessage("js.sales.issues.user.selectissueuser");
				document.getElementById("hosp_user_name").focus();
				return false;
			}
			document.IssueUserMasterForm.submit();
			return true;
		}
	</script>
</head>

<body onload="init()">
<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
<form action="${actionUrl}" name="IssueUserMasterForm" method="POST">
		
		<input type="hidden" name="_method" id="_method" value="update"/>
		<input type="hidden" name="hosp_user_id" value="${bean.hosp_user_id}"/>
		<input type="hidden" name="validation_hosp_user" id="hosp_user" value="${bean.hosp_user_name}"/>
		<table width="100%">
				<tr>
					<td width="100%"><h1><insta:ltext key="sales.issues.user.editdetails"/></h1></td>
					<td>&nbsp; &nbsp;</td>
				</tr>
		</table>
<insta:feedback-panel/>
		<fieldset class="fieldSetBorder" id="corporateInsu1">
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%" >
				<tr>
					<td class="formlabel" ><insta:ltext key="sales.issues.user.issueusername"/>: </td>
					<td class="forminfo" >
						<input type="text" name="hosp_user_name" id="hosp_user_name"  
						value="${bean.hosp_user_name}" onkeypress="return enterAlphanNumericals(event);" 
						onchange="checkName();" maxlength="60"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel" width="100px" ><insta:ltext key="sales.issues.user.status"/>: </td>
					<td class="forminfo" >
						<insta:selectoptions name="status" id="status" value="${bean.status}" opvalues="A,I"
						optexts="Active,Inactive" />
					</td>
					<td></td>
					<td></td>
				</tr>
			 </table>
		 </fieldset>

		<div class="screenActions">
			<button type="button" accesskey="S" onclick="return validateForm();"><b><u><insta:ltext key="sales.issues.user.s"/></u></b><insta:ltext key="sales.issues.user.ave"/></button>
			|
			<a href="javascript:void(0);" onclick="window.location.href='${cpath}/${pagePath}/add.htm?'"><insta:ltext key="sales.issues.user.add"/></a>
			|
		 	<a href="${cpath}/${pagePath}/list.htm?sortOrder=hosp_user_name&sortReverse=false&status=A"><insta:ltext key="sales.issues.user.issueuserlist"/></a>
		</div>
	</form>
</body>
</html>
