<%@ tag dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="insta" tagdir="/WEB-INF/tags" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="columns" required="false" %>
<%@ attribute name="rowTemplate" required="true" fragment="true" %>
<%--
	Generates a inline-editable data table .
	The row of the table is determined by the templateRow attribute. 
	The data provided will be pre-filled into the table.
	 

	Example Usage:
		<insta:selectoptions name="reg_time" value="N" opvalues="Y,N" optexts="Yes,No"/>

    This will produce the following html output
		<select name="reg_time">
			<option value="Y">Yes</option>
			<option value="N" selected="true">No</option>
		</select>
--%>
<c:set var="attrs">
	<c:forEach items="${dynattrs}" var="attr" >
		${attr.key}=${attr.value}
	</c:forEach>
</c:set>

<table class="dashboard" id="${id}" cellpadding="0" cellspacing="0" ${attrs}>
	<tr class="header">
		<c:forEach items="${fn:split(columns,',')}" var="columnText" varStatus="status">
	  	<td>${columnText}</td>
		</c:forEach>
		<%-- last column for the delete and add button --%>
		<td class="last"></td>
	</tr>
	<jsp:invoke fragment="rowTemplate"/>
</table>


