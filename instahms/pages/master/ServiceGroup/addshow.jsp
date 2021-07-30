<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Service Group - Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="ServiceGroup/ServiceGroup.js"/>
<script>
	var serviceGroupsList = ${serviceGroupsList};
</script>
</head>

<body onload="serviceGroupAutoComplete();">

	<form >
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="service_group_id" id="service_group_id" value="${bean.map.service_group_id}">
	<c:set var="canInactivate" value="${bean.map.service_group_id != -1}"/>
	<table width="100%">
		<tr>
			<td >
				<c:if test="${param._method eq 'add'}"><h1>Add Service Group</h1></c:if>
				<c:if test="${param._method ne 'add'}"><h1>Edit Service Group</h1></c:if>
			</td>

			<c:if test="${param._method ne 'add'}">
			<td align="right">
				<table>
					<tr>
						<td >Service Group Name:</td>
						<td valign="top" style="width: 10em">
							<div id="serviceGroupAutocomplete">
								<input type="text" name="serviceGroup_name" id="serviceGroup_name"
									class="field" />
								<div id="serviceGroupContainer"></div>
							</div>
							<input type="hidden" name="serviceGroup_id" id="serviceGroup_id" >
						</td>
						<td >
							<input name="getDetails" type="submit" class="button" value="Find" onclick="return searchGroup();">
						</td>
					</tr>
				</table>
			</td>
			</c:if>
		</tr>
	</table>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Service Group Name:</td>
				<td valign="top">
						<input type="text" name="service_group_name" id="service_group_name"
						 	value="${bean.map.service_group_name}" class="field" ${canInactivate?'':'disabled'}>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>

			<tr>
				<td class="formlabel">Display Order:</td>
				<td>
					<input type="text" name="display_order" id="display_order" value="${bean.map.display_order}" class="field"
						onkeypress="return enterNumOnly(event)">
				</td>
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<td>

					<select name="status" id="status" ${canInactivate?'':'disabled'} class="dropdown">
						<option value="A"<c:if test="${bean.map.status eq 'A'}">selected</c:if>>Active</option>
						<option value="I" <c:if test="${bean.map.status eq 'I'}">selected</c:if>>Inactive</option>
					</select>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Alias/Code:</td>
				<td>
					<input type="text" name="service_group_code" id="service_group_code" value="${bean.map.service_group_code }"/>
				</td>
			</tr>

			<c:if test="${param._method ne 'add'}">
				<tr>
					<td class="formlabel">Last Modified User:</td>
					<td><b>${bean.map.username }</b></td>
				</tr>

				<tr>
					<td class="formlabel">Last Modified Date:</td>
					<td><b><fmt:formatDate value="${bean.map.mod_time}" pattern="dd-MM-yyyy HH:mm"/></b></td>
				</tr>
			</c:if>
		</table>
	</fieldset>

		<c:url var="url" value="ServiceGroup.do">
				<c:param name="_method" value="list"/>
			</c:url>
		<table class="screenActions">
			<tr>
				<td >
					<button type="button" accesskey="S" onclick="validate();"><b><u>S</u></b>ave</button> |
					<a href="<c:out value='${url}' />">Service Group Lists</a>
				</td>
			</tr>
		</table>
	</form>
</body>

</html>
