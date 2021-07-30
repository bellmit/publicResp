<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Other ID Type Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function keepBackUp(){
			if(document.otheridtypemasterform._method.value == 'update'){
				backupName = document.otheridtypemasterform.other_identification_doc_name.value;
			}
		}

		function doClose() {
			window.location.href = "${cpath}/master/otheridentifiers/list.htm?_method=list&status=A&sortOrder=other_identification_doc_name" +
						"&sortReverse=false";
		}
		function focus() {
			document.otheridtypemasterform.other_identification_doc_name.focus();
		}

		<c:if test="${param._method != 'add'}">
    	   Insta.masterData=${ifn:convertListToJson(identifierTypeDetails)};
       </c:if>

	</script>
</head>
<body onload= "keepBackUp();" >   
        <h1 style="float:left">Edit Other ID Type</h1>
        <c:url var="searchUrl" value="show.htm"/>
        <insta:findbykey keys="other_identification_doc_name,other_identification_doc_id" fieldName="other_identification_doc_id" method="show" url="${searchUrl}"/>
 
<form action="update.htm"  name="otheridtypemasterform" method="POST">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="other_identification_doc_id" value="${bean.other_identification_doc_id}"/>
	
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Document Type:</td>
			<td>
				<input type="text" maxlength="100" name="other_identification_doc_name" value="${bean.other_identification_doc_name}" />
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,InActive"/>
			</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|
		 <c:if test="${param._method != 'add'}">
			<a href="${cpath}/master/otheridentifiers/add.htm?_method=add">Add</a>
		|
		</c:if>
			<a href="javascript:void(0)" onclick="doClose();">Other ID Type List</a>
	</div>

</form>
</body>
</html>
