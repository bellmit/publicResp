<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="generalmasters.dialysisaccesssites.addoredit.dialysisaccesssiteinstahms"/></title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="script" file="hmsvalidation.js"/>

	<script>
		function doClose() {
			window.location.href = "${cpath}/master/dialysisAccessSite.do?_method=list&status=A&sortOrder=access_site&sortReverse=false";
		}

	</script>

	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>

</head>
<body>
	<form action="dialysisAccessSite.do" method="POST">
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>

		<c:set var="addText"><insta:ltext key="generalmasters.dialysiscommon.addoredit.add"/></c:set>
		<c:set var="editText"><insta:ltext key="generalmasters.dialysiscommon.addoredit.edit"/></c:set>
		<h1>${param._method == 'add' ? addText : editText} <insta:ltext key="generalmasters.dialysisaccesssites.addoredit.dialysisaccesssite"/></h1>
		<insta:feedback-panel/>
		<c:if test="${param._method == 'show'}">
			<input type="hidden" name="access_site_id" value="${bean.map.access_site_id}"/>

		</c:if>
		<c:set var="status">
 			<insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 			<insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
		</c:set>
		<fieldset class="fieldsetborder">

			<table class="formTable">
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysisaccesssites.addoredit.accesssite"/></td>
					<td>
						 <input type="text" name="access_site" value="${bean.map.access_site}"
						 	class="required" title='<insta:ltext key="generalmasters.dialysisaccesssites.addoredit.dialysisaccesssiteisrequired"/>' maxlength="30"/>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysisaccesssites.addoredit.status"/></td>
					<td>
						 <insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="${status}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysisaccesssites.addoredit.description"/></td>
					<td>
						 <textarea name="description" cols="16" rows="3"/>${bean.map.description}</textarea>
					</td>
			</table>

		</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u><insta:ltext key="generalmasters.dialysisaccesssites.addoredit.s"/></u></b><insta:ltext key="generalmasters.dialysisaccesssites.addoredit.ave"/></button></td>
			<c:if test="${param._method=='show'}">
				<td>&nbsp;|&nbsp;</td>
				<td><a href="#" onclick="window.location.href='${cpath}/master/dialysisAccessSite.do?_method=add'"><insta:ltext key="generalmasters.dialysisaccesssites.addoredit.add"/></a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="generalmasters.dialysisaccesssites.addoredit.dialaccesssitelist"/></a></td>
		</tr>
	</table>
	</form>
</body>
</html>
