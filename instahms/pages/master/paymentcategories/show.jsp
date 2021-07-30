<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.PAYMENT_CATEGORY_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Category Master - Insta HMS</title>

<script type="text/javascript">

	function doClose() {
		window.location.href = "${cpath}/${pagePath}/list.htm?status=A&sortOrder=cat_name&sortReverse=false";
	}
</script>
</head>
<body>

<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>

<form action="${actionUrl}" method="POST">
		<input type="hidden" name="cat_id" value="${bean.cat_id}"/>

	<h1>Edit Category</h1>

	<insta:feedback-panel/>

		<fieldset class="fieldsetborder">

			<table class="formtable" >
				<tr>
					<td class="formlabel">Category Name:</td>
					<td class="formpg">
						<input type="text" name="cat_name" value="${bean.cat_name}" class="required field" title="Category Name is required">
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr>
					<td class="formlabel">Status:</td>
					<td class="formpg"><insta:selectoptions name="status" value="${bean.status}" class="dropdown" opvalues="A,I" optexts="Active,Inactive" /></td>
				</tr>
				<tr>
					<td valign="top" class="txtRt">Description:</td>
					<td><textarea name="cat_desc" cols="16" rows="3">${bean.cat_desc}</textarea>
				</tr>
			</table>

		</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
				<td>&nbsp;|&nbsp</td>
				<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/${pagePath}/add.htm'">Add</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Payment Category List</a></td>
		</tr>
	</table>
</form>

</body>
</html>
