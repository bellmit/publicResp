<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Department Unit - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>

	function doClose() {
		window.location.href = "${cpath}/master/DepartmentUnitMaster.do?_method=list&sortOrder=unit_name" +
						"&sortReverse=false&status=A";
	}

	<c:if test="${param._method != 'add'}">
	    Insta.masterData=${deptUnitsList};
	</c:if>
</script>
</head>

<body>
<c:choose>
    <c:when test="${param._method != 'add'}">
       <h1 style="float:left">Edit Unit</h1>
       <c:url var="searchUrl" value="/master/DepartmentUnitMaster.do"/>
       <insta:findbykey keys="unit_name,unit_id" fieldName="unit_id" method="show" url="${searchUrl}" />
    </c:when>
    <c:otherwise>
        <h1>Add Unit</h1>
    </c:otherwise>
</c:choose>
<form action="DepartmentUnitMaster.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="unit_id"  value="${bean.map.unit_id}"/>

	<center><insta:feedback-panel/></center>

	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Department:</td>
			<td>
				<insta:selectdb displaycol="dept_name" name="dept_id" value="${bean.map.dept_id}" table="department" valuecol="dept_id"/>
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="formlabel">Unit Name:</td>
			<td>
				<input type="text" name="unit_name"  value="${bean.map.unit_name}" class="required validate-length"
					length="100" title="Unit name is required and max length can be 100" />
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>

	</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
			<c:if test="${param._method=='show'}">
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/DepartmentUnitMaster.do?_method=add'">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Unit List</a></td>
		</tr>
	</table>

</form>
</body>
</html>
