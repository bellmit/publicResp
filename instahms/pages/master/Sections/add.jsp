<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Sections Add - Insta HMS</title>
	<c:set var="pagePath" value="<%=URLRoute.SECTIONS_PATH %>"/>
	<c:set var="modAccumed" value="${preferences.modulesActivatedMap['mod_accumed']}"/>
	<script>
	</script>

</head>
<body>
	<h1>Add Section</h1>
	<insta:feedback-panel/>
	<form action="create.htm" method="POST" name="section">
		
		<input type="hidden" name="section_id" value="${ifn:cleanHtmlAttribute(param.section_id)}"/>
		<table class="formtable">
			<tr>
				<td class="formlabel">Section Name: </td>
				<td><input type="text" name="section_title" id="section_title" class="required"
						value="" title="Section Name is mandatory"></td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Allow All Normal: </td>
				<td><input type="checkbox" name="allow_all_normal" id="allow_all_normal" value="Y"/></td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Linked to: </td>
				<td><select  class="dropdown validate-not-first" name="linked_to" id="linked_to" title="Linked to is mandatory">
						<option value="">-- Select --</option>
						<option value="patient" >Patient</option>
						<option value="visit" >Visit</option>
						<option value="order item" >Order Item</option>
						<option value="form" >Form</option>
					</select></td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Status: </td>
				<td><select class="dropdown validate-not-first" name="status" id="status" title="Status is mandatory.">
						<option value="">-- Select --</option>
						<option value="A" selected>Active</option>
						<option Value="I" >Inactive</option>
					</select>
					<img class="imgHelpText" title="In IP Forms the Section Mandatory is applied only when the form is 'Save & Close'" src="${cpath}/images/help.png">
				</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Section Mandatory: </td>
				<td><select class="dropdown validate-not-first" name="section_mandatory" id="section_mandatory" title="Please select the Section Mandatory field.">
						<option value="">-- Select --</option>
						<option value="true" >Yes</option>
						<option Value="false" selected>No</option>
					</select>
				</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Allow Repeat Section: </td>
				<td><select class="dropdown validate-not-first" name="allow_duplicate" id="allow_duplicate" title="Please select the Allow Repeat Section field.">
						<option value="">-- Select --</option>
						<option value="true">Yes</option>
						<option Value="false" selected>No</option>
					</select>
				</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<c:if test="${modAccumed == 'Y'}">
			<tr>
				<td class="formlabel">Section Type: </td>
				<td><insta:selectdb name="section_type_id" id="section_type" table="section_type_master" valuecol="section_type_id" displaycol="section_type"
							filtered="false" dummyvalue="-- Select --" dummyvalueid="" orderby="section_type"/></td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			</c:if>
		</table>
		<table class="screenActions">
			<tr>
				<td><input type="submit" name="Save" value="Save"/>
					
				| <a href="list.htm?&sortOrder=section_title&sortReverse=false&status=A" title="Show Sections List">Section List</a>
				</td>
			</tr>
		</table>
	</form>
</body>
</html>
