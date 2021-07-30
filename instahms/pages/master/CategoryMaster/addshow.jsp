<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Category Master - Insta HMS</title>

<script type="text/javascript">

	function doClose() {
		window.location.href = "${cpath}/master/CategoryMaster.do?_method=list&status=A&sortOrder=cat_name&sortReverse=false";
	}
</script>
</head>
<body>

<form action="CategoryMaster.do" >
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="cat_id" value="${bean.map.cat_id}"/>
	</c:if>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Category</h1>

	<insta:feedback-panel/>

		<fieldset class="fieldsetborder">

			<table class="formtable" >
				<tr>
					<td class="formlabel">Category Name:</td>
					<td class="formpg">
						<input type="text" name="cat_name" value="${bean.map.cat_name}" class="required field" title="Category Name is required">
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr>
					<td class="formlabel">Status:</td>
					<td class="formpg"><insta:selectoptions name="status" value="${bean.map.status}" class="dropdown" opvalues="A,I" optexts="Active,Inactive" /></td>
				</tr>
				<tr>
					<td valign="top" class="txtRt">Description:</td>
					<td><textarea name="cat_desc" cols="16" rows="3">${bean.map.cat_desc}</textarea>
				</tr>
			</table>

		</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
			<c:if test="${param._method=='show'}">
				<td>&nbsp;|&nbsp</td>
				<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/CategoryMaster.do?_method=add'">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Payment Category List</a></td>
		</tr>
	</table>
</form>

</body>
</html>
