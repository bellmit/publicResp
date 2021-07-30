<%@ tag dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="insta" tagdir="/WEB-INF/tags" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="false" %>
<%@ attribute name="columnDefs" required="false" %>
<%@ attribute name="dataList" required="false" type="java.util.List"%>
<%@ attribute name="displayColumns" required="false" %>
<%@ attribute name="rowTemplate" required="false" fragment="true" %>
<%@ attribute name="editorId" required="false" %>
<%@ attribute name="allowDelete" required="false" %>
<%@ attribute name="allowInsert" required="false" %>
<%@ attribute name="allowEdit" required="false" %>
<%@ attribute name="columnHeaders" required="false" %>

<%--
<%@ attribute name="rowTemplate" required="true" fragment="true" %>
--%>
<%--
	Generates a inline-editable data table .
	The row of the table is determined by the templateRow attribute. 
	The data provided will be pre-filled into the table.
--%>
<c:set var="attrs">
	<c:forEach items="${dynattrs}" var="attr" >
		${attr.key}=${attr.value}
	</c:forEach>
</c:set>
<c:set var="gridStyle" value="${empty displayColumns ? '' : 'dialog_displayColumns'}"/>

<c:if test="${not empty title}">
<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">${title}</legend>
</c:if>
<table class="detailList ${gridStyle}" cellspacing="0" cellpadding="0" id="${id}" ${attrs}>
	<jsp:invoke fragment="rowTemplate"></jsp:invoke>
</table>

<c:if test="${not empty title}">
</fieldset>
</c:if>
<script type="text/javascript">
var _dt_config = {
		actions : {
			insert: ${not empty allowInsert ? allowInsert : 'true'},
			edit: ${not empty allowEdit ? allowEdit : 'true'},
			delete: ${not empty allowDelete ? allowDelete : 'true'}},
		headers : "${not empty columnHeaders ? columnHeaders : ''}"
};

//var dataGrid = null;
function initDataGrid(id, dataList) {
	var _dt_column_defs = [
	                   	<c:forTokens items="${columnDefs}" delims=";" var="columnDef" varStatus="cStatus">
	                   		<c:set var="col" value="${fn:split(columnDef,':')}"/>
	                   		<c:if test="${fn:length(col) > 0}">
	                   		{field: "${col[0]}", name:"${fn:length(col) > 1 ? col[1] : ifn:prettyPrint(col[0])}"}<c:out value="${cStatus.last ? '' : ','}"/>
	                   		</c:if>
	                   	</c:forTokens>
	                   ];
	var dg = new Insta.DataGrid(id, _dt_config, _dt_column_defs);
	dg.render();
	dg.init(dataList);
	dg.registerEditor('${editorId}');
	return dg;
}
</script>
