<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Linen User Master</title>
<script>
	function doClose() {
		window.location.href = "${cpath}/master/LinenUserMaster.do?_method=list";
	}
</script>
</head>

<body>
	<form name="LinenUserMasterForm">
		
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
		<h1>${param._method == 'add' ? 'Add' : 'Edit'} Linen User</h1>
		<insta:feedback-panel/>
		<c:if test="${param._method == 'show'}">
			<input type="hidden" name="category_user_id" value="${bean.map.category_user_id}"/>
		</c:if>
		
		<table class="formtable" align="left">
			<tr>
				<td class="formlabel">Category:</td>
				<td>
					<insta:selectdb id="category_id" name="category_id" value="${bean.map.category_id}"  table="linen_user_category"
						class="dropdown" style="width:140px;" valuecol="category_id" filtered="false"
						displaycol="category_name"  dummyvalue="-- Select --" /></td>
			</tr>
			<tr>
				<td class="formlabel">Category User Name:</td>
				<td><input type="text" name="category_user_name" id="category_user_name" 
						value="${bean.map.category_user_name }" class="field"></td>
			</tr>
		</table>
		
		<table class="screenActions">
			<tr>
				<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
				<c:if test="${param._method=='show'}">
					<td>&nbsp;|&nbsp;</td>
					<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/LinenUserMaster.do?_method=add'">Add</a></td>
				</c:if>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0)" onclick="doClose();">Linen User Master List</a></td>
			</tr>
		</table>
			
	</form>
</body>

</html>
