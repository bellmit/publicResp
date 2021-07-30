<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.GENERIC_CLASSIFICATION_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
	<head>
		<title>Add Generic Classification - Insta HMS</title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<insta:link type="script" file="hmsvalidation.js"/>
		<script type="text/javascript">
			function Save(){
				var classificationName = document.genericclassForm.classification_name.value;
			    if(trimAll(classificationName)==""){
				    alert("Classification Name Should Not Be Empty");
				    document.genericclassForm.classification_name.value="";
				    document.genericclassForm.classification_name.focus();
				    return false;
			    }
			  	document.genericclassForm.submit();
			  	return true;
			 }
		</script>
	</head>
	<body class="yui-skin-sam" >
		<h1>Add Generic Classification Details</h1>
		<form method="POST" action="create.htm" name="genericclassForm">
			<insta:feedback-panel/>
			<fieldset  class="fieldSetBorder">
			<legend class="fieldSetLabel">Classification</legend>
			<table	class="formtable">
				<tr>
				     <td class="formlabel">Classification Name:</td>
				     <td><input type="text" name="classification_name" id="classification_name" value="${genclassdto.map.classification_name }"  maxlength="100"><span class="star">*</span></td>
				     <td>&nbsp;</td>
				     <td>&nbsp;</td>
				     <td>&nbsp;</td>
				     <td>&nbsp;</td>
				</tr>
			</table>
			</fieldset>
			<div class="screenActions">
				<button type="button" accesskey="S" name="save" class="button" onclick="return Save()"><b><u>S</u></b>ave</button>|
				<a href="${cpath}${pagePath}/list.htm?sortOrder=classification_name&sortReverse=false">Back To DashBoard</a>
			</div>
		</form>
	</body>
</html>