<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Item Code - Insta HMS</title>
<script>
	function validateForm() {
		var itemCodes = document.getElementsByName('item_code');
		for(var i=0;i<itemCodes.length;i++) {
			if(empty(itemCodes[i].value)) {
				alert("item code is required.");
				itemCodes[i].focus();
				return false;
			}
		}
		return true;
	}
</script>
</head>
<body>
	 <h1>Edit Item Code</h1>
	 <div style="height: 5px;"></div>
	 <insta:feedback-panel/>
	 <form action="StoresItemMaster.do" name="editcodeform" method="post">
	 <input type="hidden" name="_method" value="saveItemCode"/>
	 <input type="hidden" name="medicine_id" value="${ifn:cleanHtmlAttribute(medicine_id)}"/>
	 	<table class="formtable">
	 		<tr>
	 			<td class="formlabel">Item Name:</td>
	 			<td style="width:1000px;">${itemBean.map.medicine_name}</td>
	 			<td style="width:500px;">&nbsp;</td>
	 		</tr>
	 	</table>
	 	<div style="height: 5px;"></div>
	 	<table class="resultList" cellpadding="0" cellspacing="0" width="100%">
	 		<tr>
	 			<th>Drug Code Type</th>
	 			<th>Drug Code</th>
	 		</tr>

	 		<c:forEach var="itemCode" items="${storeItemCodes}" varStatus="st">
	 			<tr>
	 				<td>
	 					<label id="code_tpe${st.index}">${itemCode.map.code_type}</label>
	 					<input type="hidden" name="code_id" id="code_id" value="${itemCode.map.code_id}">
	 					<input type="hidden" name="code_type" id="code_type" value="${itemCode.map.code_type}">
	 				</td>
	 				<td>
	 					<input type="text" name="item_code" value="${itemCode.map.item_code}" id="item_code${st.index}"/>
	 				</td>
	 			</tr>
	 		</c:forEach>

	 	</table>
	 	<div class="screenActions">
			<input type="submit" name="save" value="Save" class="button" onclick="return validateForm();"> |
			<a href="${cpath }/master/StoresItemMaster.do?_method=list&sortOrder=medicine_name&sortReverse=false&status=A">Item List</a> |
			<a href="${cpath }/master/StoresItemMaster.do?_method=show&medicine_id=${ifn:cleanURL(medicine_id)}">Edit Stores Item</a>
		</div>
	 </form>
</body>
</html>