<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Service Department - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
function doClose()
{
	window.location.href="${cpath}/master/ServiceDepartmentMaster.do?_method=list&dep.status=A&sortOrder=department&sortReverse=false";
}
   <c:if test="${param._method != 'add'}">
       Insta.masterData=${serviceLists};
   </c:if>
</script>
</head>
<body>
<c:choose>
    <c:when test="${param._method != 'add'}">
        <h1 style="float:left">Edit Service Department</h1>
        <c:url var="searchUrl" value="/master/ServiceDepartmentMaster.do"/>
        <insta:findbykey keys="department,serv_dept_id" fieldName="serv_dept_id" url="${url}" method="show"/>
    </c:when>
    <c:otherwise>
       <h1>Add Service Department</h1>
    </c:otherwise>
</c:choose>
<form action="ServiceDepartmentMaster.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="serv_dept_id" value="${bean.map.serv_dept_id}"/>
	</c:if>


	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Service Department:</td>
			<td>
				<input type="text" name="department"  value="${bean.map.department}" onblur="capWords(department);"
					class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Department Type: </td>
			<td><insta:selectdb table="department_type_master" name="dept_type_id" displaycol="dept_type_desc"
				valuecol="dept_type_id" dummyvalue="-- Select --" dummyvalueid="" value="${bean.map.dept_type_id}"/>
			</td>
		</tr>
		<tr>
				<td class="formlabel">Store:</td>
				<td>
				 <insta:selectdb name="store_id" id="store_id" table="stores" value="${bean.map.store_id}"
				 	dummyvalue="-- Select --" valuecol="dept_id" displaycol="dept_name" style="width: 13em"/>
				</td>
         </tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>

	</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
			<c:if test="${param._method != 'add'}">
				<td>&nbsp;|&nbsp;</td>
				<td><a href="${cpath}/master/ServiceDepartmentMaster.do?_method=add&store_id=-2">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Service Dept List</a></td>
		</tr>
	</table>

</form>
</body>
</html>
