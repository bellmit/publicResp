<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Sections Edit - Insta HMS</title>
	<c:set var="pagePath" value="<%=URLRoute.SECTIONS_PATH %>"/>
	<c:set var="modAccumed" value="${preferences.modulesActivatedMap['mod_accumed']}"/>
	<script>
		function assignValue(){
			if(document.getElementById("allow_all_normal").checked)
				document.getElementById("allow_all_normal_hidden").value="Y";
			else
				document.getElementById("allow_all_normal_hidden").value="N";
		}
	
	</script>

</head>
<body>
	<h1>Edit Section</h1>
	<insta:feedback-panel/>
	<form action="update.htm" method="POST" name="section">
		
		<input type="hidden" name="section_id" value="${ifn:cleanHtmlAttribute(param.section_id)}"/>
		<table class="formtable">
			<tr>
				<td class="formlabel">Section Name: </td>
				<td><input type="text" name="section_title" id="section_title" class="required"
						value="${bean.section_title}" title="Section Name is mandatory"></td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Allow All Normal: </td>
				<td><input type="checkbox" name="allow_all_normal_name" id="allow_all_normal"  onchange="assignValue();"
						${bean.allow_all_normal == 'Y' ? 'checked' : ''}/>
						<input type="hidden" name="allow_all_normal" id="allow_all_normal_hidden" value="${bean.allow_all_normal}" /></td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Linked to: </td>
				<td><select disabled class="dropdown validate-not-first" name="linked_to" id="linked_to" title="Linked to is mandatory">
						<option value="">-- Select --</option>
						<option value="patient" ${bean.linked_to == 'patient' ? 'selected' : ''}>Patient</option>
						<option value="visit" ${bean.linked_to == 'visit' ? 'selected' : ''}>Visit</option>
						<option value="order item" ${bean.linked_to == 'order item' ? 'selected' : ''}>Order Item</option>
						<option value="form" ${bean.linked_to == 'form' ? 'selected' : ''}>Form</option>
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
						<option value="A" ${param._method == 'add' || bean.status == 'A' ? 'selected' : ''}>Active</option>
						<option Value="I" ${bean.status == 'I' ? 'selected' : ''}>Inactive</option>
					</select>
				</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<c:set var="isMandatoryEnabled" value="${ empty mandatory_fields}"/>
				<td class="formlabel">Section Mandatory: </td>
				<td><select class="dropdown validate-not-first" name="section_mandatory" id="section_mandatory" title="Please select the Section Mandatory field." ${isMandatoryEnabled ? '': 'disabled'}>
						<option value="">-- Select --</option>
						<option value="true" ${bean.section_mandatory ? 'selected' : ''}>Yes</option>
						<option Value="false" ${param._method == 'add' || !bean.section_mandatory ? 'selected' : ''}>No</option>
					</select>
					<img class="imgHelpText" title="In IP Forms the Section Mandatory is applied only when the form is 'Save & Close'" src="${cpath}/images/help.png" style="float:right">
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
						<option value="true" ${bean.allow_duplicate ? 'selected' : ''}>Yes</option>
						<option Value="false" ${!bean.allow_duplicate ? 'selected' : ''}>No</option>
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
							filtered="false" dummyvalue="-- Select --" dummyvalueid="" orderby="section_type" value="${bean.section_type_id}"/></td>
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
				
						| <a href="add.htm" title="Add New Section">Add New Section</a>
					
				| <a href="list.htm?sortOrder=section_title&sortReverse=false&status=A" title="Show Sections List">Section List</a>
				<insta:screenlink addPipe="true" screenId="mas_section_role_rights"
					label="Section Role Rights"
					extraParam="?_method=edit&section_id=${bean.section_id}" />
				</td>
			</tr>
		</table>
	</form>
</body>
</html>
