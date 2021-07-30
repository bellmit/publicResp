<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="generalmasters.dialyzertypes.addoredit.dialyzertypeinstahms"/></title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function doClose() {
			window.location.href = "${cpath}/master/dialyses/list.htm?status=A&sortOrder=dialyzer_type_name&sortReverse=false";
		}

	</script>

	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
</head>
<body>
	<form action="create.htm" method="POST">
		<c:set var="addText"><insta:ltext key="generalmasters.dialysiscommon.addoredit.add"/></c:set>
		<h1>${addText} <insta:ltext key="generalmasters.dialyzertypes.addoredit.dialyzertype"/></h1>

		<insta:feedback-panel/>
<c:set var="status">
 <insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 <insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
</c:set>
		<input type="hidden" name="dialyzer_type_id" value="${bean.dialyzer_type_id}"/>

		<fieldset class="fieldsetborder">

			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialyzertypes.addoredit.dialyzertype"/>:</td>
					<td>
						 <input type="text" name="dialyzer_type_name" value="${bean.dialyzer_type_name}"
						 	class="required" title='<insta:ltext key="generalmasters.dialyzertypes.addoredit.dialyzertypeisrequired"/>' maxlength="30"/>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialyzertypes.addoredit.status"/></td>
					<td>
						 <insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="${status}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialyzertypes.addoredit.description"/></td>
					<td>
						 <textarea name="description" cols="16" rows="3"/>${bean.description}</textarea>
					</td>
				</tr>
			</table>

		</fieldset>

		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u><insta:ltext key="generalmasters.dialyzertypes.addoredit.s"/></u></b><insta:ltext key="generalmasters.dialyzertypes.addoredit.ave"/></button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="#" onclick="window.location.href='${cpath}/master/dialyses/add.htm?'"><insta:ltext key="generalmasters.dialyzertypes.addoredit.add"/></a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="generalmasters.dialyzertypes.addoredit.dialyzertypelist"/></a></td>
		</tr>
		</table>
	</form>
</body>
</html>
