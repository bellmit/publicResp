<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Bed Names</title>
<insta:link type="js" file="masters/wardandbedmaster.js" />
<insta:link type="js" file="date_go.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="ajax.js" />

<script type="text/javascript">
var bedTypeStatus = '${ifn:cleanJavaScript(requestScope.bedTypeStatus)}';
var cpath = '${cpath}';

	function doClose() {
		window.location.href = "${cpath}/pages/masters/insta/admin/WardAndBedMasterAction.do?method=getWardandBedMaster";
	}
</script>
</head>
<body>
<html:form
	action="/pages/masters/insta/admin/WardAndBedMasterAction.do?">

	<input type="hidden" name="method" value="updateBedNames" />
	<input type="hidden" name="wardId" value="${ifn:cleanHtmlAttribute(wardId)}">
	<input type="hidden" name="bedTypeToUpdate" value="${ifn:cleanHtmlAttribute(bedType)}" />
<h1>Bed Names</h1>
<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >

		<tr>
			<td>&nbsp;</td>
		</tr>

		<tr>
			<td>Ward Name:<b> ${wardName}</b> &nbsp; bedType:<b> ${ifn:cleanHtml(bedType)}
			</b></td>
		</tr>

		<c:set var="bedNamesList" value="${pagedList.dtoList}" />
		<tr>
			<td>
			<table class="dataTable" cellpadding="0" cellspacing="0">
				<tr>
					<th>Bed Name</th>
					<th>Status</th>
					<th>Occupancy</th>
					<th>Bed Name</th>
					<th>Status</th>
					<th>Occupancy</th>
				</tr>

				<c:set var="iterationValue" value="1" />
				<tr>
					<c:forEach var="item" items="${bedNamesList}">
						<td>
							<input type="text" value="${item.map.bed_name}"
								name="bedName" size="12"  />
							<input type="hidden" name="bedId" value="${item.map.bed_id}">
						</td>
						<td><select name=bedStatus id="bedStatus${item.map.bed_id}" style="width: 8em" class="dropdown" onchange="return canBeInactivate('', '${item.map.bed_id}', this);">
							<option value="A"
								<c:if test="${item.map.status eq 'A'}" > Selected </c:if>>Active</option>
							<option value="I"
								<c:if test="${item.map.status eq 'I'}" > Selected </c:if>>Inactive</option>
						</select></td>
						<td class="forminfo">${item.map.occupancy eq 'N' ? 'Vacant' : 'Occupied' }</td>

						<c:if test="${iterationValue eq 2}">
							</tr>
						</c:if>

						<c:choose>
							<c:when test="${iterationValue eq 2}">
								<c:set var="iterationValue" value="1" />
							</c:when>
							<c:when test="${iterationValue eq 1}">
								<c:set var="iterationValue" value="2" />
							</c:when>
						</c:choose>
				</c:forEach>
			</table>
			</td>
		</tr>
	</table>
	</fieldset>

		<div class="screenActions">
			<button type="submit" name="save" accesskey="S"><b><u>S</u></b>ave</button> |
			<a href="javaScript:void(0)" onclick="doClose();">Ward And Bed List</a>
		</div>

</html:form>
</body>
</html>
