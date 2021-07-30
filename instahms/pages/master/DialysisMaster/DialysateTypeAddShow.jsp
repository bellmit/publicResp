<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="generalmasters.dialysatetype.addoredit.dialysatetypeinstahms"/></title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function doClose() {
			window.location.href = "${cpath}/master/DialysateType.do?_method=list&status=A&sortOrder=dialysate_type_name&sortReverse=false";
		}

	</script>
	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
</head>
		<c:set var="status">
 			<insta:ltext key="generalmasters.dialysatetype.addoredit.active"/>,
 			<insta:ltext key="generalmasters.dialysatetype.addoredit.inactive"/>
		</c:set>
<body>
	<form action="DialysateType.do" method="POST">
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>

		<c:set var="addText"><insta:ltext key="generalmasters.dialysiscommon.addoredit.add"/></c:set>
		<c:set var="editText"><insta:ltext key="generalmasters.dialysiscommon.addoredit.edit"/></c:set>
		<h1>${param._method == 'add' ? addText : editText} <insta:ltext key="generalmasters.dialysatetype.addoredit.dialysatetype"/></h1>
		<insta:feedback-panel/>
		<c:if test="${param._method == 'show'}">
			<input type="hidden" name="dialysate_type_id" value="${bean.map.dialysate_type_id}"/>

		</c:if>

		<fieldset class="fieldsetborder">

			<table class="formTable">
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysatetype.addoredit.dialysatetype"/>:</td>
					<td>
						 <input type="text" name="dialysate_type_name" value="${bean.map.dialysate_type_name}"
						 	class="required" title='<insta:ltext key="generalmasters.dialysatetype.addoredit.dialysatetypeisrequired"/>' maxlength="50"/>
					</td>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysatetype.addoredit.status"/></td>
					<td>
						 <insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive"/>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysatetype.addoredit.potassium"/></td>
					<td>
						<input type="text" name="potasium" value="${bean.map.potasium}"
							class="number" onkeypress="return enterNumOnlyANDdot(event)" />
					</td>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysatetype.addoredit.calcium"/></td>
					<td>
						<input type="text" name="calcium" value="${bean.map.calcium}"
							class="number" onkeypress="return enterNumOnlyANDdot(event)" />
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysatetype.addoredit.magnesium"/></td>
					<td>
						<input type="text" name="magnesium" value="${bean.map.magnesium}"
							class="number" onkeypress="return enterNumOnlyANDdot(event)" />
					</td>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysatetype.addoredit.sodium"/></td>
					<td>
						<input type="text" name="sodium" value="${bean.map.sodium}"
							class="number" onkeypress="return enterNumOnlyANDdot(event)" />
					</td>
				</tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysatetype.addoredit.glucose"/></td>
					<td>
						<input type="text" name="glucose" value="${bean.map.glucose}"
							class="number" onkeypress="return enterNumOnlyANDdot(event)" />
					</td>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysatetype.addoredit.remarks"/></td>
					<td>
						 <textarea name="remarks" cols="16" rows="3"/>${bean.map.remarks}</textarea>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
			</table>

		</fieldset>

		<table class="screenActions">
			<tr>
				<td><button type="submit" accesskey="S"><b><u><insta:ltext key="generalmasters.dialysatetype.addoredit.s"/></u></b><insta:ltext key="generalmasters.dialysatetype.addoredit.ave"/></button></td>
				<c:if test="${param._method=='show'}">
					<td>&nbsp;|&nbsp;</td>
					<td><a href="#" onclick="window.location.href='${cpath}/master/DialysateType.do?_method=add'"><insta:ltext key="generalmasters.dialysatetype.addoredit.add"/></a></td>
				</c:if>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="generalmasters.dialysatetype.addoredit.dialysatetypelist"/></a></td>
			</tr>
		</table>
	</form>
</body>
</html>
