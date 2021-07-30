<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit MRD Case File User Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

	<script>
		var backupName = '';

		function keepBackUp(){
			if(document.forms[0]._method.value == 'update'){
				backupName = document.forms[0].file_user_name.value;
			}
		}

		function doClose() {
			window.location.href = "${cpath}/master/MRDCaseFileUsers.do?_method=list&sortOrder=file_user_name" +
							"&sortReverse=false&status=A";
		}

		var usersList = <%= request.getAttribute("mrdCasefileUsersList") %>;
		var fileuser_id = '${bean.map.file_user_id}';

		function checkduplicate(){
			var newFileUser = trimAll(document.forms[0].file_user_name.value);
			for(var i=0;i<usersList.length;i++){
				item = usersList[i];
				if(fileuser_id!=item["file_user_id"]){
				   var actualUserName = item["file_user_name"];
				    if (newFileUser == actualUserName) {
				    	alert(document.forms[0].file_user_name.value+" already exists pls enter other name");
				    	document.forms[0].file_user_name.focus();
				    	return false;
				    }
			     }
			}
      }
	</script>

</head>
<body onload="keepBackUp();">

<form action="MRDCaseFileUsers.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="file_user_id" value="${bean.map.file_user_id}"/>
	</c:if>

	<div class="pageHeader">${param._method == 'add' ? 'Add' : 'Edit'} MRD Case File User</div>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">MRD File User Name:</td>
			<td>
				<input type="text" name="file_user_name" value="${bean.map.file_user_name}" class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Status:</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>

	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkduplicate();"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/MRDCaseFileUsers.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();">MRD Files List</a>
	</div>

</form>

</body>
</html>
