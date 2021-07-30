<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.dialysisaccesstypeinstahms"/></title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function doClose() {
			window.location.href = "${cpath}/master/dialysisaccesstypes/list.htm?status=A&sortOrder=access_type&sortReverse=false";
		}
	</script>
	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
</head>
<body>
	<form action="create.htm" method="POST">
		<c:set var="addText"><insta:ltext key="generalmasters.dialysiscommon.addoredit.add"/></c:set>
		<h1> ${addText} <insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.dialysisaccesstype"/></h1>
		<insta:feedback-panel/>

		<c:set var="status">
 			<insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 			<insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
		</c:set>
		
		<c:set var="createText">
 			<insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 			<insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
		</c:set>

		<c:set var="amode">
 			<insta:ltext key="generalmasters.dialysisaccesstypes.list.permanent"/>,
 			<insta:ltext key="generalmasters.dialysisaccesstypes.list.temporary"/>
		</c:set>

		<c:set var="acategory">
 			<insta:ltext key="generalmasters.dialysisaccesstypes.list.avg"/>,
 			<insta:ltext key="generalmasters.dialysisaccesstypes.list.cvc"/>,
 			<insta:ltext key="generalmasters.dialysisaccesstypes.list.avf"/>,
 			<insta:ltext key="generalmasters.dialysisaccesstypes.list.other"/>
		</c:set>
		<fieldset class="fieldsetborder">

			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.accesstype"/></td>
					<td>
						 <input type="text" name="access_type" value="${bean.access_type}"
						 	class="required" title='<insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.dialysisaccesstype"/>' maxlength="30"/>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.status"/></td>
					<td>
						 <insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="${status}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.accessmode"/></td>
					<td>
						<insta:selectoptions name="access_mode" value="${bean.access_mode}" opvalues="P,T" optexts="${amode}" />
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.accesscategory"/></td>
					<td>
						 <insta:selectoptions name="access_category" value="${bean.access_category}" opvalues="AVG,CVC,AVF,OTHER" optexts="${acategory}" />
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.description"/></td>
					<td>
						 <textarea name="description" cols="16" rows="3"/>${bean.description}</textarea>
					</td>
				</tr>
			</table>

		</fieldset>

		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u><insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.s"/></u></b><insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.ave"/></button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="generalmasters.dialysisaccesstypes.addoredit.dialysisaccesstype"/></a></td>
		</tr>
	</table>
	</form>
</body>
</html>
