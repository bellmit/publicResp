<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.GENERIC_CLASSIFICATION_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
	<head>
		<title>Edit Generic Classification - Insta HMS</title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="script" file="tableSearch.js"/>
		<script type="text/javascript">
			function saveDetails(){
				var classificationName = document.genericclassForm.classification_name.value;
			    if(trimAll(classificationName) == ""){
				    alert("Classification Name Should Not Be Empty");
				    document.genericclassForm.classification_name.value="";
				    document.genericclassForm.classification_name.focus();
				    return false;
			    }
			  	document.genericclassForm.submit();
			  	return true;
			 }
			Insta.masterData=${ifn:convertListToJson(genericClassificationsLists)};
		</script>
	</head>
	<body class="yui-skin-sam" >
		<h1 style="float:left">Edit Generic Classification Details</h1>
		<c:url var="searchUrl" value="${pagePath}/show.htm"/>
		<insta:findbykey keys="classification_name,classification_id" fieldName="classification_id" method="show" url="${searchUrl}"/>
		<form method="POST" action="update.htm" name="genericclassForm">
			<input type="hidden" name="_method" value="update"/>
			<input type="hidden" name="classification_id" value="${bean.classification_id}"/>
			<insta:feedback-panel/>
			<fieldset  class="fieldSetBorder">
			<legend class="fieldSetLabel"> Classification</legend>
			<table class="formtable" >
			  <tr>
			     <td class="formlabel">Classification Name:</td>
			     <td ><input type="text" name="classification_name" id="classification_name" value="${bean.classification_name }"  maxlength="100"><span class="star">*</span></td>
			     <td>&nbsp;</td>
			     <td>&nbsp;</td>
			     <td>&nbsp;</td>
			     <td>&nbsp;</td>
			 </tr>
			</table>
			</fieldset>
			<div class="screenActions">
			<button type="button" accesskey="S" name="save" class="button" onclick="return saveDetails()" ><b><u>S</u></b>ave</button>
			|
				<a href="javascript:void(0);" onclick="window.location.href='${cpath}${pagePath}/add.htm'">Add</a>
			|
			<a href="${cpath}${pagePath}/list.htm?sortOrder=classification_name&sortReverse=false">Back To DashBoard</a>
			</div>
		</form>
	</body>
</html>
