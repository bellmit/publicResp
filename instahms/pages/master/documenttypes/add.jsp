<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Document Type - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function focus(){
			document.docTypeForm.doc_type_name.focus();
		}
	</script>

</head>
<body onload="focus()">

<form action="create.htm" method="POST" name="docTypeForm">
	<h1>Add DocumentType</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetbrder">

	<table class="formtable">
		<tr>
			<td class="formlabel">DocumentType:</td>
			<td><input type="text" name="doc_type_name" onblur="capWords(doc_type_name)" class="required validate-length" length="100" title="Name is required and max length of name can be 100" /></td>
	<td >&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
		</tr>

	<tr>
	<td class="formlabel">Status:</td>
	<td><insta:selectoptions name="status" value="A" opvalues="A,I" optexts="Active,Inactive" /></td>
	</tr>
	<tr>
		<td class="formlabel">Prefix:</td>
		<td><input type="text" maxlength="3" size="3" name="prefix" class="required validate-length" length="3" title="Prefix is required and max length of prefix can be 3" /></td>
	</tr>

	<tr>
      <td class="formlabel">Document Category:</td>
    	    <td>
    				<select name="selectedCategories" multiple class="listbox" style="width: 200px">
    					<c:forEach items="${docTypeCategories}" var="option">
    						<c:set var="value" value="${option.get('doc_category_id')}"/>
    							<c:choose>
    								<c:when test="${ifn:arrayFind(mappedCategories,value) ne -1}">
    									<c:set var="attr" value="selected='true'"/>
    							  </c:when>
    							  <c:otherwise>
    							   <c:set var="attr" value=""/>
    							  </c:otherwise>
    							</c:choose>
    						<option value='<c:out value="${value}"/>' ${attr}>
    								${option.get("doc_category_name")}
    						</option>
    					</c:forEach>
    				</select>
    			</td>
    	</tr>
	<tr>
    		<td class="formlabel">Allow patient sharing to external systems:</td>
    	  <td>
    	  <insta:selectoptions name="isshareabletopatient" value="${bean.isshareabletopatient}"
            opvalues="Y,N" optexts="Yes,No" />
    	  </td>
    </tr>

	</table>

	</fieldset>

	<table class="screenActions">
	<tr>
		<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
		<td>&nbsp;|&nbsp;</td>
		<td><a href="add.htm" >Add</a>
		<td>&nbsp;|&nbsp;</td>
		<td><a href="list.htm?status=A&sortReverse=false&sortOrder=system_type">Document Type List</a></td>
	</tr>
	</table>

</form>

</body>
</html>
