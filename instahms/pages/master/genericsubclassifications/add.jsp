<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.GENERIC_SUB_CLASSIFICATION_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<title>Generic Sub Classification - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<script>
	function Save(){
	   var field1 = document.genericsubclassForm.sub_classification_name.value;
	   var classi = document.getElementById("classification_id").value;
	   document.genericsubclassForm.classification_id.value = classi;
	   	if (classi == ""){
	   		alert("Select one of the Classification Name");
	   		document.getElementById("classification_id").focus();
	   		return false;
	   	}
	    if(trimAll(field1)==""){
		    alert("Sub Classification Name Should Not Be Empty");
		    document.genericsubclassForm.sub_classification_name.value="";
		    document.genericsubclassForm.sub_classification_name.focus();
		    return false;
	    }
	  	document.genericsubclassForm.submit();
	  	return true;
	}
</script>
</head>
<body class="yui-skin-sam" >
	<h1>Add Sub Classification Details</h1>
	<insta:feedback-panel/>
	<form method="POST" action="create.htm" name="genericsubclassForm">
	<fieldset  class="fieldSetBorder">
		<legend class="fieldSetLabel">Sub Classification</legend>
		<table class="formtable">
		  <tr>
		    <td class="formlabel">Sub Classification Name:</td>
			<td><input type="text" name="sub_classification_name" id="sub_classification_name" value="${sClassfication.sub_classification_name}"  maxlength="100"><span class="star">*</span></td>
		  	<td class="formlabel">Classification Name:</td>
		    <td>
		    	<select name="classification_id" id="classification_id"  class="dropdown">
					<option value="">--select--</option>
					<c:forEach var="classi" items="${classificationdetails}">
						<option value="${classi.classification_id}"
							${sClassfication.classification_id == classi.classification_id ? 'selected' : ''}>
							${classi.classification_name}
						</option>
					</c:forEach>
				</select>
			</td>
		  	<td>&nbsp;</td>
		  	<td>&nbsp;</td>
		 </tr>
		</table>
		</fieldset>
		<div class="screenActions">
			<button type="button" accesskey="S" name="save" class="button" onclick="return Save()" >
			<b><u>S</u></b>ave</button>
			|
			<a href="${cpath}${pagePath}/list.htm?sortOrder=sub_classification_name&sortReverse=false">Back To DashBoard</a>
		</div>
	</form>
</body>
</html>