<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.STORE_TYPE_PATH %>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Store Type- Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>

		function doClose() {
			window.location.href = "${cpath}${pagePath}/list.htm?sortOrder=store_type_name" +
						"&sortReverse=false";
		}
		function focus() {
			document.storetypemasterform.store_type_name.focus();
		}

           
	</script>
</head>
<body>
    
      <h1>Add Store Type </h1>

<form action="${cpath}${pagePath}/create.htm"  name="storetypemasterform"method="POST">

	<insta:feedback-panel/>
	
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Store Type Name:</td>
				<td>
					<input type="text" name="store_type_name" value="${bean.store_type_name}" onblur="capWords(store_type_name)"
						class="required validate-length" length="100"
						title="Name is required and max length of name can be 100" />
				</td>
				</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|
		<a href="javascript:void(0)" onclick="doClose();">Store Type List</a>
	</div>

</form>
</body>
</html>
