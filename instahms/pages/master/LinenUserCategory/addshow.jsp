<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Linen Category Master</title>
<script>
	function doClose() {
		window.location.href = "${cpath}/master/LinenUserCategory.do?_method=list";
	}
</script>
</head>

<body>
	<form name="LinenUserCategoryForm">
		
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
		<h1>${param._method == 'add' ? 'Add' : 'Edit'} Linen User Category</h1>
		<insta:feedback-panel/>
		<c:if test="${param._method == 'show'}">
			<input type="hidden" name="category_id" value="${bean.map.category_id}"/>
		</c:if>
		
		<table class="formtable" align="left">
			<tr>
				<td class="formlabel">Category:</td>
				<td><input type="text" name="category_name" value="${bean.map.category_name}"></td>
			</tr>
		</table>
		
		<table class="screenActions">
			<tr>
				<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
				<c:if test="${param._method=='show'}">
					<td>&nbsp;|&nbsp;</td>
					<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/LinenUserCategory.do?_method=add'">Add</a></td>
				</c:if>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0)" onclick="doClose();">Linen User Category List</a></td>
			</tr>
		</table>
		
	</form>
</body>
</html>
