<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Bank Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function keepBackUp(){
			if(document.bankmasterform._method.value == 'update'){
				backupName = document.bankmasterform.bank_name.value;
			}
		}

		function doClose() {
			window.location.href = "${cpath}/master/BankMaster.do?_method=list&sortOrder=bank_name" +
						"&sortReverse=false";
		}


		<c:if test="${param._method != 'add'}">
           Insta.masterData=${BankDetails};
       </c:if>

	</script>
</head>
<body onload= "keepBackUp();" >
<c:choose>
    <c:when test="${param._method != 'add'}">
        <h1 style="float:left">Edit Bank Details</h1>
        <c:url var="searchUrl" value="/master/BankMaster.do"/>
        <insta:findbykey keys="bank_name,bank_id" fieldName="bank_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Bank Name </h1>
    </c:otherwise>
</c:choose>
<form action="BankMaster.do"  name="bankmasterform"method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="bank_id" value="${bean.map.bank_id}"/>
	</c:if>

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Bank Name:</td>
			<td>
				<input type="text" name="bank_name" value="${bean.map.bank_name}" />
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="formlabel">Status</td>
			<td>
				<insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,InActive"/>
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|
		 <c:if test="${param._method != 'add'}">
			<a href="${cpath}/master/BankMaster.do?_method=add">Add</a>
		|
		</c:if>
			<a href="javascript:void(0)" onclick="doClose();">Bank List</a>
	</div>

</form>
</body>
</html>
