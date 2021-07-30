<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.STORE_ITEM_CONTROL_TYPE_PATH %>"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Store Item Control Type- Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function keepBackUp(){
				backupName = document.controltypemasterform.control_type_name.value;
		}

		function doClose() {
			window.location.href = "${cpath}/${pagePath}.htm?&sortOrder=control_type_name" +
						"&sortReverse=false";
		}
		function focus() {
			document.controltypemasterform.control_type_name.focus();
		}

          Insta.masterData=${ifn:convertListToJson(storeitemcontroltypedetails)};
	</script>
</head>
<body onload= "keepBackUp();" >
        <h1 style="float:left">Edit Control Type</h1>
        <insta:findbykey keys="control_type_name,control_type_id" fieldName="control_type_id" method="show" url="${searchUrl}"/>
	<form action="update.htm"  name="controltypemasterform" method="POST">
	<input type="hidden" name="control_type_id" value="${bean.control_type_id}"/>

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Control Type Name:</td>
				<td>
					<input type="text" name="control_type_name" value="${bean.control_type_name}" onblur="capWords(control_type_name)"
						class="required validate-length" length="100"
						title="Name is required and max length of name can be 100" />
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
		|<a href="${cpath}/${pagePath}/add.htm?">Add</a>
		|<a href="javascript:void(0)" onclick="doClose();">Control Type List</a>
	</div>

</form>
</body>
</html>
