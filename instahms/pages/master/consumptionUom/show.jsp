<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.CONSUMPTION_UOM_PATH %>"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Consumption UOM - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function keepBackUp(){
				backupName = document.consumptionuomform.consumption_uom.value;
		}

		function doClose() {
			window.location.href = "${cpath}/${pagePath}.htm?&sortOrder=consumption_uom" +
						"&sortReverse=false";
		}
		function focus() {
			document.consumptionuomform.consumption_uom.focus();
		}

          Insta.masterData=${ifn:convertListToJson(consumptionuomlist)};
	</script>
</head>
<body onload= "keepBackUp();" >
        <h1 style="float:left">Edit Consumption UOM</h1>
        <c:url var="searchUrl" value="/master/consumptionuom/show.htm"/>
        <insta:findbykey keys="consumption_uom,cons_uom_id" fieldName="cons_uom_id" method="show" url="${searchUrl}"/>
	<form action="update.htm"  name="consumptionuomform" method="POST">
	<input type="hidden" name="cons_uom_id" value="${bean.cons_uom_id}"/>

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Consumption UOM:</td>
			<td>
				<input type="text" name="consumption_uom" value="${bean.consumption_uom}" onblur="capWords(consumption_uom)"
						class="required validate-length" length="50"
						title="Consumption UOM is required and max length of name can be 50" />
			</td>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive"/>
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|<a href="${cpath}/${pagePath}/add.htm?">Add</a>
		|<a href="javascript:void(0)" onclick="doClose();">Consumption UOM List</a>
	</div>

</form>
</body>
</html>
