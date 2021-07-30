<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Department - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<c:set var="pagePath" value="<%=URLRoute.DEPARTMENT_PATH %>"/>
	<script>
		var chkDeptList =${ifn:convertListToJson(departments)};
		var hiddenDeptId = '${bean.dept_id}';
		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=dept_name" +
							"&sortReverse=false&status=A";
		}
		function checkduplicate(){
			var newDeptName = trimAll(document.editDepartmentForm.dept_name.value);
			for(var i=0;i<chkDeptList.length;i++){
				item = chkDeptList[i];
				if(hiddenDeptId!=item.dept_id){
				   var actualDeptName = item.dept_name;
				    if (newDeptName.toLowerCase() == actualDeptName.toLowerCase()) {
				    	alert(document.editDepartmentForm.dept_name.value+" already exists pls enter other name");
				    	document.editDepartmentForm.dept_name.value='';
				    	document.editDepartmentForm.dept_name.focus();
				    	return false;
				    }
			     }
			}
      }

	function focus(){
		document.editDepartmentForm.dept_name.focus();
	}
	</script>

</head>
<body onload="focus();">
<h1>Add Department</h1>

<form action="create.htm" name="editDepartmentForm" method="POST">
	
	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Department: </td>
			<td>
				<input type="text" name="dept_name"  value="${bean.dept_name}" onblur="capWords(dept_name);checkduplicate();"
					class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Department Type: </td>
			<td><select name="dept_type_id" class="dropdown">
				<option value="">---Select--</option>
				<c:forEach items="${departmenttypes}" var="departmentTypeList">
					<option value="${departmentTypeList.dept_type_id}" ${bean.dept_type_id == departmentTypeList.dept_type_id ? 'selected' : ''} >${departmentTypeList.dept_type_desc }</option>
				</c:forEach>
				</select>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status: </td>
			<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
		<tr>
			<td class="formlabel">Allowed Gender: </td>
			<td><insta:selectoptions name="allowed_gender" optexts="All,Male,Female" opvalues="ALL,M,F" value="${bean.allowed_gender}"/></td>
		</tr>
		<tr>
			<td class="formlabel">Cost Center Code: </td>
			<td><input type="text" name="cost_center_code" id="cost_center_code" value="${bean.cost_center_code}"/></td>
		</tr>
		<tr>
			<td class="formlabel">Send Referral Doctor as Ordering Clinician</td>
			<c:choose>
		 		<c:when test="${roleId==1||roleId==2}">
		 			<td><insta:radio name="is_referral_doc_as_ordering_clinician" radioValues="Y,N" value="N" radioText="Yes,No" /></td>
		 		</c:when>
		 		<c:otherwise>
		 			<td><insta:radio name="is_referral_doc_as_ordering_clinician" radioValues="Y,N" value="N" radioText="Yes,No" disabled="true"/></td>
		 		</c:otherwise>
		 	</c:choose>
		</tr>

	</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Department List</a></td>
		</tr>
	</table>

</form>
</body>
</html>
