<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Section Fields Add/Edit - Insta HMS</title>
	<script>
		var avlbl_options = 'avlbl_markers';
		var selected_options = 'selected_markers';
	</script>
	<insta:link type="script" file="master/Sections/sectionfields.js" />
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="script" file="shiftelements.js"/>

	<script>
	</script>

</head>
<body onload="init();">
	<h1>Add/Edit Section Field</h1>
	<insta:feedback-panel/>
	<form action="SectionFieldsMaster.do?_method=${param._method == 'add' ? 'create' : 'update'}" method="POST" name="sectionfield" autocomplete="off" enctype="multipart/form-data">
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>
		<input type="hidden" name="field_id" value="${ifn:cleanHtmlAttribute(param.field_id)}"/>
		<input type="hidden" name="section_id" value="${ifn:cleanHtmlAttribute(param.section_id)}"/>
		<input type="hidden" name="hidden_field_type" value="${field_details.map.field_type}"/>
		<c:set var="regExpPattern" value="${regExpPattern}"/>
		<table class="formtable">
			<tr>
				<td class="formlabel">Section Name: </td>
				<td class="forminfo">${param._method == 'add' ? section_details.map.section_title : field_details.map.section_title}</td>
				<td class="formlabel">Field Name: </td>
				<td><input type="text" name="field_name" id="field_name" value="${field_details.map.field_name}"></td>
				<td class="formlabel">Field Type: </td>
				<td class="forminfo">
					<select name="field_type" id="field_type" class="dropdown validate-not-first" title="Field Type is mandatory." onchange="onFieldTypeChange();">
						<option value="">-- Select --</option>
						<option value="text" ${field_details.map.field_type == 'text' ? 'selected' : ''}>Text Area</option>
						<option value="wide text" ${field_details.map.field_type == 'wide text' ? 'selected' : ''}>Wide Text Area</option>
						<option value="dropdown" ${field_details.map.field_type == 'dropdown' ? 'selected' : ''}>Dropdown</option>
						<option value="checkbox" ${field_details.map.field_type == 'checkbox' ? 'selected' : ''}>Checkbox</option>
						<option value="image" ${field_details.map.field_type == 'image' ? 'selected' : ''}>Image</option>
						<option value="date" ${field_details.map.field_type == 'date' ? 'selected' : ''}>Date</option>
						<option value="datetime" ${field_details.map.field_type == 'datetime' ? 'selected' : ''}>Date Time</option>
					</select>
				</td>
			<tr>
			<tr>
				<td class="formlabel">Has Others: </td>
				<td><input type="checkbox" name="allow_others" id="allow_others" value="Y" ${field_details.map.allow_others == 'Y' ? 'checked' : ''}/></td>
				<td class="formlabel">Has Normal: </td>
				<td><input type="checkbox" name="allow_normal" id="allow_normal" value="Y" ${field_details.map.allow_normal == 'Y' ? 'checked' : ''}></td>
				<td class="formlabel">Default Text: </td>
				<td><input type="text" name="normal_text" id="normal_text" value="${field_details.map.normal_text}"/></td>
			<tr>
			<tr>
				<td class="formlabel">Display Order</td>
				<td><input type="text" name="field_display_order" id="field_display_order" value="${field_details.map.display_order}" class="required number validate-number"
					title="Display Order is mandatory. And it should be a number."/></td>
				<td class="formlabel">Status</td>
				<td>
					<select class="dropdown validate-not-first" name="field_status" id="field_status" title="Field Status is mandatory.">
						<option value="">-- Select --</option>
						<option value="A" ${param._method == 'add' || field_details.map.status == 'A' ? 'selected' : ''}>Active</option>
						<option Value="I" ${field_details.map.status == 'I' ? 'selected' : ''}>Inactive</option>
					</select>
				</td>
				<td class="formlabel">No. of Lines: </td>
				<td><input type="text" name="no_of_lines" id="no_of_lines" value="${field_details.map.no_of_lines}"/></td>
			<tr>
			<tr>
				<td class="formlabel" >Image:</td>
				<td><input type="file" name="file_content" id="file_content"  accept="<insta:ltext key="upload.accept.image"/>"/><b>(Upload limit: 10MB, width: 800px, height: 400px)</b>
					<c:if test="${param._method == 'show' && field_details.map.field_type == 'image'}">
						<c:url var="imageUrl" value="/master/SectionFields/ViewImage.do">
							<c:param name="_method" value="viewImage"/>
							<c:param name="field_id" value="${param.field_id}"/>
							<c:param name="section_id" value="${param.section_id}"/>
						</c:url>
						<a href="<c:out value='${imageUrl}' />" title="View Image" target="_blank">View</a>
					</c:if>
				</td>
				<td class="formlabel">Observation Type: </td>
				<td><insta:selectdb name="observation_type" table="mrd_supported_code_types"
							valuecol="code_type" displaycol="code_type" value="${field_details.map.observation_type}"
							dummyvalue="--Select--"/></td>
				<td class="formlabel">Observation Code: </td>
                <td><input type="text" name="observation_code" id="observation_code" value="${field_details.map.observation_code}"></td>
			</tr>
			<tr>
				<td class="formlabel">Mandatory in Txn: </td>
				<td><input type="radio" name="is_mandatory" value="true" ${field_details.map.is_mandatory ? 'checked' : ''}>Yes
					<input type="radio" name="is_mandatory" value="false" ${(param._method == 'add' || !field_details.map.is_mandatory) ? 'checked' : ''}>No
				</td>
				<td class="formlabel">Use in Presenting Complaint: </td>
				<td>
					<input type="radio" name="use_in_presenting_complaint" value="Y" ${field_details.map.use_in_presenting_complaint == 'Y' ? 'checked' : ''}>Yes
					<input type="radio" name="use_in_presenting_complaint" value="N" ${(param._method == 'add' || field_details.map.use_in_presenting_complaint == 'N') ? 'checked' : ''}>No
				</td>
				<td class="formlabel">Field Phrase Category: </td>
				<td>
					<c:choose>
					<c:when test="${param._method == 'add'}">
						<insta:selectdb name="field_phrase_category_id" id="field_phrase_category_id"
							table="phrase_suggestions_category_master" displaycol="phrase_suggestions_category"
							valuecol="phrase_suggestions_category_id" filtered="true" dummyvalue="-- Select --"/>
					</c:when>
					<c:otherwise>
						<insta:selectdb name="field_phrase_category_id" id="field_phrase_category_id"
							table="phrase_suggestions_category_master" displaycol="phrase_suggestions_category"
							valuecol="phrase_suggestions_category_id" filtered="true" dummyvalue="-- Select --"
							value="${field_details.map.field_phrase_category_id}" orderby="phrase_suggestions_category" selected="true"/>
					</c:otherwise>
					</c:choose>
				</td>
			</tr>
			<tr>
			    <td class="formlabel" valign="top">Regular Expression :</td>
			    <td class="forminfo">
			       <select name="regexp_field_id" id="regexp_field_id" class="dropdown">
						<option value="">-- Select --</option>
						<c:forEach items="${regExpFieldList}" var="regexp">
						     <option value="${regexp.map.pattern_id}" ${field_details.map.pattern_id eq regexp.map.pattern_id ? 'selected' : '' }>${regexp.map.pattern_name }</option>
						</c:forEach>
				   </select>
			    </td>
			    <td class="formlabel">Default To Current Date/Time :</td>
			    <td class="forminfo">
			    	<input type="checkbox" name="default_to_current_datetime" value="Y" ${field_details.map.default_to_current_datetime == 'Y' ? 'checked' : ''}> </input>
			    </td>
			</tr>
		</table>
		<table width="342" style="padding-right:5; padding-left:10px;border-width:0px; margin:10px 0px 10px 0px;">
			<tr>
				<td align="center" style="padding-right: 4pt; border-width:0px; margin:0px; width:134px;">
					Available Markers
					<br />
					<select name="avlbl_markers" id="avlbl_markers" style="width:15em;padding-left:5; color:#666666;"
						multiple="true" size="15" onDblClick="moveSelectedOptions(this,this.form.selected_markers);">
						<c:forEach items="${available_markers}" var="a_marker">
							<c:set var="markerNotSelected" value="true"/>
							<c:forEach items="${selected_markers}" var="s_marker">
								<c:if test="${a_marker.map.image_id == s_marker.map.image_id}">
									<c:set var="markerNotSelected" value="false"/>
								</c:if>
							</c:forEach>
							<c:if test="${markerNotSelected && a_marker.map.status == 'A'}">
								<option value="${a_marker.map.image_id}" title="${a_marker.map.label}">${a_marker.map.label}</option>
							</c:if>
						</c:forEach>
					</select>
				</td>
				<td valign="top" align="left" style="padding-right:0;">
					<br />
					<br />
					<input type="button" name="addLstFldsButton" value=">" onclick="addListFields();"/>

				</td>
				<td valign="top" align="center" style="width:134px;padding-left:4pt;">
					Selected Markers
					<br />
					<select  size="15" style="width:15em;padding-left:5; color:#666666;" multiple id="selected_markers" name="selected_markers" onDblClick="moveSelectedOptions(this,this.form.avlbl_markers);">
						<c:forEach items="${selected_markers}" var="s_marker">
							<option value="${s_marker.map.image_id}" title="${s_marker.map.label}">${s_marker.map.label}</option>
						</c:forEach>
					</select>
				</td>
				<td>
					<div align="center">
						<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionUp(selected_markers);"> <img src="${cpath}/icons/std_up.png" width=10 height=8/>  </button>
						<br />
						<br />
						<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionDown(selected_markers);"><img src="${cpath}/icons/std_down.png" width=10 height=8/> </button>
						<br />
						<br />
						<br /><br />
						<br /><br />
						<br /><br />
						<br /><br />
						<br/><br/>
					</div>
				</td>
			</tr>
		</table>

		<table class="detailList dialog_displayColumns" style="margin-top: 10px" cellspacing="0" cellpadding="0" id="itemsTable" border="0" width="100%">
			<tr>
				<th>Option Value</th>
				<th>Value Code</th>
				<th>Status</th>
				<th>Display Order</th>
				<th>Option Phrase Category</th>
				<th>Regular Expression</th>
				<th style="width: 16px;"></th>
				<th style="width: 16px;"></th>
			</tr>
			<c:set var="numOptions" value="${fn:length(options_list)}"/>
			<c:forEach begin="1" end="${numOptions+1}" var="i" varStatus="loop">
				<c:set var="option" value="${options_list[i-1].map}"/>
				<c:if test="${empty option.option_id}">
					<c:set var="style" value='style="display:none"'/>
				</c:if>
				<tr ${style}>
					<td>
						<input type="hidden" name="option_id" value="${option.option_id}"/>
						<input type="hidden" name="option_value" value="${option.option_value}"/>
						<input type="hidden" name="value_code" value="${option.value_code}">
						<input type="hidden" name="option_status" value="${option.option_status}"/>
						<input type="hidden" name="option_display_order" value="${option.option_display_order}"/>
						<input type="hidden" name="option_phrase_category" value="${option.option_phrase_category_id}"/>
						<input type="hidden" name="option_regexp_field" value="${option.pattern_id }" />
						<input type="hidden" name="edited" value='false'/>
						<label>${option.option_value}</label>
					</td>
					<td><label>${option.value_code}</label></td>
					<td><label>${option.option_status}</label></td>
					<td><label>${option.option_display_order}</label></td>
					<td><insta:truncLabel value="${option.phrase_category_name}" length="20"/></td>
					<td><label>${regExpPattern[option.pattern_id]}</label></td>
					<td style="text-align: center">
						<c:choose>
							<c:when test="${not empty option.option_id}">
								<img src="${cpath}/icons/delete_disabled.gif"" class="imgDelete button" />
							</c:when>
							<c:otherwise>
								<a href="javascript:Cancel Item" onclick="return cancelOption(this);" title="Cancel Item" >
									<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
								</a>
							</c:otherwise>
						</c:choose>
					</td>
					<td style="text-align: center">
						<a name="_editAnchor" href="javascript:Edit" onclick="return showEditOptionDialog(this);"
							title="Edit Option Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
				</tr>
			</c:forEach>
		</table>
		<table class="addButton" style="height: 25px;">
			<tr>
				<td style="width: 16px; text-align: right">
					<button type="button" name="btnAddItem" id="btnAddItem" title="Add Option (Alt_Shift_+)"
						onclick="showAddOptionDialog(this); return false;"
						accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</tr>
		</table>
		<div ><b>* Options are mandatory for Field Type 'Dropdown' and 'Checkbox'.</b></div>
		<table class="screenActions">
			<tr>
				<td>
					<c:url value="SectionFieldsMaster.do" var="listUrl">
						<c:param name="_method" value="list"/>
						<c:param name="section_id" value="${param.section_id}"/>
					</c:url>
					<c:url value="SectionFieldsMaster.do" var="addUrl">
						<c:param name="_method" value="add"/>
						<c:param name="section_id" value="${param.section_id}"/>
					</c:url>
					<input type="button" name="Save" value="Save" onclick="return saveFieldValues();">
					| <a href="<c:out value='${listUrl}' />">Fields List</a>
					<c:if test="${param._method == 'show'}">
					| <a href="<c:out value='${addUrl}' />">Add New Field</a>
					</c:if>
					| <a href="<c:out value='${cpath}/master/sections/list.htm?&sortOrder=section_title&sortReverse=false&status=A' />" title="Show Sections List">Section List</a>
				</td>
			</tr>
		</table>
		<div id="addOptionDialog" style="display: none">
			<div class="bd">
				<div id="addOptionDialogFields">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Add Option</legend>
						<table class="formtable">
							<tr>
								<td class="formlabel">Option Value: </td>
								<td><input type="text" name="d_option_value" id="d_option_value"/></td>
							</tr>
							<tr>
								<td class="formlabel">Value Code: </td>
								<td><input type="text" name="d_value_code" id="d_value_code"/></td>
							</tr>
							<tr>
								<td class="formlabel">Status: </td>
								<td><select class="dropdown" name="d_option_status" id="d_option_status">
										<option value="">-- Select --</option>
										<option value="A" selected>Active</option>
										<option value="I">Inactive</option>
									</select>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Display Order: </td>
								<td><input type="text" name="d_display_order" id="d_display_order"/></td>
							</tr>
							<tr>
								<td class="formlabel">Phrase Category</td>
								<td>
									<insta:selectdb name="d_phrase_category_id" id="d_phrase_category_id"
										table="phrase_suggestions_category_master" displaycol="phrase_suggestions_category"
										valuecol="phrase_suggestions_category_id" orderby="phrase_suggestions_category" filtered="true" dummyvalue="-- Select --"/>
								</td>
							</tr>
							<tr>
							  <td class="formlabel" valign="top">Regular Expression:</td>
							  <td class="forminfo">
			                        <select name="d_regexp_field_id" id="d_regexp_field_id" class="dropdown">
						                 <option value="">-- Select --</option>
						                 <c:forEach items="${regExpFieldList}" var="regexp">
						                      <option value="${regexp.map.pattern_id}" ${field_details.map.pattern_id eq regexp.map.pattern_id ? 'selected' : '' }>${regexp.map.pattern_name }</option>
					                     </c:forEach>
				                    </select>
				              </td>
				          </tr>
						</table>
					</fieldset>
				</div>
				<table style="margin-top: 10">
					<tr>
						<td>
							<button type="button" name="Add" id="Add" accesskey="A" >
								<b><u>A</u></b>dd
							</button>
							<input type="button" name="Close" value="Close" id="Close"/>
						</td>
					</tr>
				</table>
			</div>
		</div>
		<div id="editOptionDialog" style="display: none">
			<input type="hidden" name="editRowId" id="editRowId" value=""/>
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Edit Prescription</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Option Value: </td>
							<td><input type="text" name="ed_option_value" id="ed_option_value" onchange="setEdited();"/></td>
						</tr>
						<tr>
							<td class="formlabel">Value Code: </td>
							<td><input type="text" name="ed_value_code" id="ed_value_code" onchange="setEdited();"/></td>
						</tr>
						<tr>
							<td class="formlabel">Status: </td>
							<td><select class="dropdown" name="ed_option_status" id="ed_option_status" onchange="setEdited();">
									<option value="">-- Select --</option>
									<option value="A">Active</option>
									<option value="I">Inactive</option>
								</select>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Display Order: </td>
							<td><input type="text" name="ed_display_order" id="ed_display_order" onchange="setEdited();" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
						</tr>
						<tr>
							<td class="formlabel">Phrase Category</td>
							<td>
								<insta:selectdb name="ed_phrase_category_id" id="ed_phrase_category_id"
									table="phrase_suggestions_category_master" displaycol="phrase_suggestions_category"
									valuecol="phrase_suggestions_category_id" filtered="true" orderby="phrase_suggestions_category" dummyvalue="-- Select --"/>
							</td>
						</tr>
						<tr>
							  <td class="formlabel" valign="top">Regular Expression:</td>
							  <td class="forminfo">
			                        <select name="ed_regexp_field_id" id="ed_regexp_field_id" class="dropdown">
						                 <option value="">-- Select --</option>
						                 <c:forEach items="${regExpFieldList}" var="regexp">
						                      <option value="${regexp.map.pattern_id}" >${regexp.map.pattern_name }</option>
					                     </c:forEach>
				                    </select>
				              </td>
				        </tr>
					</table>
					<table style="margin-top: 10">
						<tr>
							<td>
								<input type="button" id="editOk" name="editok" value="Ok">
								<input type="button" id="editCancel" name="cancel" value="Cancel" />
								<input type="button" id="editPrevious" name="previous" value="<<Previous" />
								<input type="button" id="editNext" name="next" value="Next>>"/>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
	</form>

</body>
</html>
