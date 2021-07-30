<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<%
	String patientId = request.getParameter("patient_id");
	if (patientId == null || patientId.equals("")) {
		request.setAttribute("testAdditionalDocList", Collections.EMPTY_LIST);
	} else {
		String filter = request.getParameter("filter");
		String category = "";
		if (filter != null && !filter.equals(""))
			category = filter.equals("Laboratory") ? "DEP_LAB" : "DEP_RAD";
		
		request.setAttribute("testAdditionalDocList", LaboratoryDAO.getTestDocuments(patientId, category));
	}
%>
<%@page import="java.util.Collections"%>
<%@page import="com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO"%>

<div id="testInfoDialog" style="display:${empty testAdditionalDocList ?'none':'block'}">
<legend class="fieldSetLabel" style="margin-top: 10px">Additional Order Details</legend>
<table class="resultList" style="margin-top: 10px" id="ad_test_info_table">
	<tr>
		<th>Package</th>
		<th>Test Name</th>
		<th>Additional Test Information</th>
		<th>User Notes</th>
		<th style="width: 250px"></th>
		<th style="width: 16px"></th>
	</tr>
	<c:set var="numTestDocList" value="${fn:length(testAdditionalDocList)}"/>
	<c:set var="prescIdOfTest" value=""/>
	<c:set var="prevRecord" value=""/>
	<c:set var="fileIndex" value="0"/>
	<c:forEach begin="1" end="${numTestDocList+1}" var="i" varStatus="loop">
		<c:set var="testDoc" value="${testAdditionalDocList[i-1].map}"/>
		<%-- show the extrarow only when user is having atleast one document attached. --%>
		<c:set var="extraNewRow" value="${i != 1 && testDoc.prescribed_id != prescIdOfTest}"/>

		<c:if test="${extraNewRow}">
			<tr id="extraRow" class="dummyRow">
				<td class="indent">
					<label></label>
					<input type="hidden" name="ad_test_doc_id" value=""/>
					<input type="hidden" name="ad_main_row_id" value="${prevRecord.prescribed_id}"/>
					<input type="hidden" name="ad_test_id" value="${prevRecord.test_id}"/>
					<input type="hidden" name="ad_clinical_notes" value=""/>
					<input type="hidden" name="ad_package_activity_index" value=""/>
					<input type="hidden" name="ad_test_category" value="${prevRecord.category}"/>
					<input type="hidden" name="ad_test_row_edited" value="false"/>
					<input type="hidden" name="ad_notes_entered" value="false"/>
					<input type="hidden" name="ad_test_doc_delete" value="false"/>
					<input type="hidden" name="ad_test_name" value="${prevRecord.test_name}">
					<input type="hidden" name="ad_test_info_reqts" value="${prevRecord.additional_info_reqts}">
					<!-- additional_info_reqts -->
				</td>
				<td class="indent">
					<label></label>
				</td>
				<td>
					<label></label>
				</td>
				<td>
					<label></label>
				</td>
				<!-- <td>
					<label></label>
				</td> -->
				<td>
					<input type="file" id="ad_test_file_upload${fileIndex}" name="ad_test_file_upload[${fileIndex}]" 
						class="testFileUpload" 
						onchange="setTestDocRowEdited(this)" style="width: 200px; display: none" accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/>
					<c:set var="fileIndex" value="${fileIndex+1}"/>
				</td>
				<td>
					<a href="javascript:void(0)" onclick="return cancelTestAdtnlDoc(this);" title="Cancel Test Additional Document"  
						style="display: none">
						<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
					</a>
					<c:choose>
						<c:when test="${not empty param.allowAdd && not param.allowAdd}">
							<c:set var="tad_extra_title" value="Cannot add New Document (patient is discharged)"/>
							<c:set var="tad_extra_img" value="Add1.png"/>
							<c:set var="tad_extra_onclick" value="return false;"/>
						</c:when>
						<c:otherwise>	<%-- normal add --%>
							<c:set var="tad_extra_title" value="Add Test Additional Document"/>
							<c:set var="tad_extra_img" value="Add.png"/>
							<c:set var="tad_extra_onclick" value="cloneTestDocRow(this); return false;"/>
						</c:otherwise>
					</c:choose>
					<button type="button" name="btnAddItem" title="${tad_extra_title}"
						onclick="${tad_extra_onclick}"
						class="imgButton"><img src="${cpath}/icons/${tad_extra_img}"></button>
				</td>
			</tr>
		</c:if>
		<c:choose>
			<c:when test="${empty testDoc}">
				<tr style="display: none" >
					<td >
						<label></label>
						<input type="hidden" name="ad_test_doc_id" value="${testDoc.doc_id}"/>
						<input type="hidden" name="ad_main_row_id" value="${testDoc.prescribed_id}"/>
						<input type="hidden" name="ad_test_id" value="${testDoc.test_id}"/>
						<input type="hidden" name="ad_clinical_notes" value="${testDoc.clinical_notes}"/>
						<input type="hidden" name="ad_package_activity_index" value=""/>
						<input type="hidden" name="ad_test_category" value="${ifn:cleanHtmlAttribute(testDoc.category)}"/>
						<input type="hidden" name="ad_test_row_edited" value="false"/>
						<input type="hidden" name="ad_notes_entered" value="false"/>
						<input type="hidden" name="ad_test_doc_delete" value="false"/>
						<input type="hidden" name="ad_test_name" value="${testDoc.test_name}"/>
						<input type="hidden" name="ad_test_info_reqts" value="${testDoc.additional_info_reqts}">
					</td>
					<td>
						<label></label>
					</td>
					<td >
						<label></label>
					</td>
					<td >
						<label style="float: left"></label>
						<a name="_editAnchor" href="javascript:Edit" onclick="return showTestAdInfoDialog(this);"
							title="Edit Test Additional Details" style="float:right">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
						
					</td>
					<td>
						<input type="file" name="ad_test_file_upload" class="testFileUpload" onchange="setTestDocRowEdited(this)" style="width: 200px" accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/>
					</td>
					<td>
						<a href="javascript:void(0)" onclick="return cancelTestAdtnlDoc(this);" title="Cancel Test Additional Document" >
							<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
						</a>
				
						<button type="button" name="btnAddItem" title="Add Test Additional Document" style="display: ${empty testDoc ? 'block' : 'none'}"
								onclick="cloneTestDocRow(this); return false;"
								class="imgButton"><img src="${cpath}/icons/Add.png"></button>
					</td>
				</tr>
			</c:when>
			<c:otherwise>
				<tr class="${testDoc.prescribed_id != prescIdOfTest ? 'mainRow' : ''}">
					<c:set var="indentClass" value="${testDoc.prescribed_id == prescIdOfTest ? 'indent' : ''}"/>
					<td class="${indentClass}">
						<c:choose>
							<c:when test="${testDoc.prescribed_id != prescIdOfTest}">
								<insta:truncLabel value="${testDoc.package_name}" length="20"/>
							</c:when>
							<c:otherwise>
								<label></label>
							</c:otherwise>
						</c:choose>
						<input type="hidden" name="ad_test_doc_id" value="${testDoc.doc_id}"/>
						<input type="hidden" name="ad_main_row_id" value="${testDoc.prescribed_id}"/>
						<input type="hidden" name="ad_test_id" value="${testDoc.test_id}"/>
						<input type="hidden" name="ad_clinical_notes" value="${testDoc.clinical_notes}"/>
						<input type="hidden" name="ad_package_activity_index" value=""/>
						<input type="hidden" name="ad_test_category" value="${ifn:cleanHtmlAttribute(testDoc.category)}"/>
						<input type="hidden" name="ad_test_row_edited" value="false"/>
						<input type="hidden" name="ad_notes_entered" value="false"/>
						<input type="hidden" name="ad_test_doc_delete" value="false"/>
						<input type="hidden" name="ad_test_name" value="${testDoc.test_name}"/>
						<input type="hidden" name="ad_test_info_reqts" value="${testDoc.additional_info_reqts}">
					</td>
					<td class="${indentClass}">
						<c:choose>
							<c:when test="${testDoc.prescribed_id != prescIdOfTest}">
								<insta:truncLabel value="${testDoc.test_name}" length="20"/>
								<c:if test="${not empty testDoc.common_order_id}">
									[${testDoc.common_order_id}]
								</c:if>
							</c:when>
							<c:otherwise>
								<label></label>
							</c:otherwise>
						</c:choose>
					</td>
					<td>
						<label><insta:truncLabel value="${testDoc.additional_info_reqts}" length="20"/></label>
					</td>
					<td >
						<c:choose>
							<c:when test="${testDoc.prescribed_id != prescIdOfTest}">
								<insta:truncLabel value="${testDoc.clinical_notes}" length="20" style="float: left"/>
								<a name="_editAnchor" href="javascript:Edit" onclick="return showTestAdInfoDialog(this);"
									title="Edit Test Additional Details" style="float: right">
									<img src="${cpath}/icons/Edit.png" class="button" />
								</a>
								
							</c:when>
							<c:otherwise>
								<label ></label>
							</c:otherwise>
						</c:choose>
					</td>
					<td>
						<c:choose>
							<c:when test="${not empty testDoc.doc_id}">
								<c:url var="testDocUrl" value="/${testDoc.category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'}/TestDocumentsPrint.do">
									<c:param name="_method" value="print"/>
									<c:param name="doc_id" value="${testDoc.doc_id}"/>
								</c:url>
								<a href="${testDocUrl}" title="View Test Document" target="_blank">View</a>
							</c:when>
							<c:otherwise>
								<input type="file" id="ad_test_file_upload${fileIndex}" name="ad_test_file_upload[${fileIndex}]" 
									class="testFileUpload" 
									onchange="setTestDocRowEdited(this)" style="width: 200px;" accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/>
							</c:otherwise>
						</c:choose>
						<c:set var="fileIndex" value="${fileIndex+1}"/>
					</td>
					<td>
						<a href="javascript:void(0)" onclick="return cancelTestAdtnlDoc(this);" title="Cancel Test Additional Document" >
							<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
						</a>
						<c:choose>
							<c:when test="${not empty param.allowAdd && not param.allowAdd}">
								<c:set var="tad_title" value="Cannot add New Document (patient is discharged)"/>
								<c:set var="tad_img" value="Add1.png"/>
								<c:set var="tad_onclick" value="return false;"/>
							</c:when>
							<c:otherwise>	<%-- normal add --%>
								<c:set var="tad_title" value="Add Test Additional Document"/>
								<c:set var="tad_img" value="Add.png"/>
								<c:set var="tad_onclick" value="cloneTestDocRow(this); return false;"/>
							</c:otherwise>
						</c:choose>
						<button type="button" name="btnAddItem" title="${tad_title}" style="display: none"
								onclick="${tad_onclick}"
								class="imgButton"><img src="${cpath}/icons/${tad_img}"></button>
					</td>
				</tr>
			</c:otherwise>
		</c:choose>
		<c:set var="prevRecord" value="${testDoc}"/>
		<c:set var="prescIdOfTest" value="${testDoc.prescribed_id}"/>
	</c:forEach>
</table>
</div>

<div id="addTestAddiotionalInfoDialog" style="display: none">
	<div class="bd">
		<input type="hidden" id="adTestAdInfoRowId" value=""/>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Add/Edit User Notes</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Additional Test Info:</td>
					<td><label id="test_info"></label></td>
				</tr>
				<tr>
					<td class="formlabel">User Notes</td>
					<td><textarea name="d_test_additional_info" id="d_test_additional_info" rows="4" cols="60"></textarea></td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="button" id="adTestAdInfoOk" value="Ok">
						<input type="button" id="adTestAdInfoCancel" value="Cancel">
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>
