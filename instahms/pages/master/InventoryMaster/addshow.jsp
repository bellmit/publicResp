<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Inventory Category - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>

		function doClose() {
			window.location.href = "";
		}

      function focus(){
      	document.forms[0].category.focus();
      }
	</script>
</head>
<body onload="focus()">
	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Inventory Category</h1>
	<insta:feedback-panel/>

<form action="InventoryMaster.do" method="GET">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="category_id" value="${bean.map.category_id}"/>
	</c:if>


	<table class="formtable" >
		<tr>
			<td class="formlabel">Category Name:</td>
			<td>
				<input type="text" name="category" value="${bean.map.category}"	onblur="capWords(category);" class="required validate-length" length="50" title="Category name is required and max length of name can be 50" />
				<span class="star">&nbsp;*</span>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Identification:</td>
			<td><insta:selectoptions name="identification" value="${bean.map.identification}"  opvalues="S,B" optexts="Serial NO,Batch No" /></td>

		</tr>

		<tr>
			<td class="formlabel">Issue Type:</td>
			<td><insta:selectoptions name="issue_type" value="${bean.map.issue_type}"
				opvalues="P,C,L" optexts="Permanent, Consumable, Reusable"  /></td>
		</tr>

		<tr>
			<td class="formlabel">Billable:</td>
			<td><insta:selectoptions name="billable" value="${bean.map.billable}" opvalues="true,false" optexts="Yes, No" />
			<script>
				<c:if test="${not empty dsabled}" >
					document.forms[0].identification.disabled = ${dsabled};
					document.forms[0].issue_type.disabled = ${dsabled};
					document.forms[0].billable.disabled = ${dsabled};
				</c:if>
			</script>
            </td>
		</tr>

		<tr>
			<td class="formlabel">Status:</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active, Incative" /></td>
		</tr>
		</table>
		<div style="display:${dsabled == true ? 'block':'none' }">
			 *** (This Category is connected to item you can't
			        update Identification,Issue Type & Billable fields) ***
		</div>
		<div class="screenActions">
			<input type="submit" value="Save"/>
			<a href="${cpath}/master/InventoryMaster.do?_method=list">Back To Dash Board</a>
		</div>



</form>

</body>
</html>
