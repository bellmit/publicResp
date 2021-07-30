<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<c:set var="copy" value='<%= request.getParameter("copy") %>' />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="URl" value="${(screenId eq 'patient_sponsors_approval') ? 'PatientSponsorsApproval' : 'ApproveSponsorsApproval' }"/>
<c:set var="dummyValue">
<insta:ltext key="insurance.patientapprovallist.patientapprovals.dummyvalue"/>
</c:set>

<html>
	<head>
		<title><insta:ltext key="insurance.patientapprovallist.patientapprovals.title"/></title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<insta:link type="script" file="Insurance/sponsorapproval.js" />
		<insta:link type="script" file="hmsvalidation.js" />
		<insta:link type="script" file="datagrid.js"/>
	</head>
	<body onload="init()">
		<c:choose>
			<c:when test="${param._method !='add'}">
				<table width="100%">
					<tr>
						<td width="100%"><h1>Edit Patient Approvals</h1></td>
					</tr>
				</table>
		    </c:when>
		    <c:otherwise>
		    	<h1 style="float: left">Add Patient Approvals</h1>
				<insta:patientsearch searchType="mrNo" searchUrl="${URl}.do"  buttonLabel="Find"
					 searchMethod="find" fieldName="mr_no"/>
		    </c:otherwise>
	    </c:choose>
	  	<insta:feedback-panel/>
	    <insta:patientgeneraldetails mrno="${not empty param.mr_no ? param.mr_no : bean.map.mr_no}" />
		<form name="PatientApprovalForm" action="${URl}.do" method="POST"  enctype="multipart/form-data">
			<input type="hidden" name="_method" id="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>
			<input type="hidden" name="mr_no" id= "mr_no" value="${not empty param.mr_no ? param.mr_no : bean.map.mr_no}"/>
		  	<input type="hidden" name="sponsor_approval_id" id= "sponsor_approval_id" value="${ifn:cleanHtmlAttribute(param.sponsor_approval_id)}"/>
			<input type="hidden" name="approval_status" id= "approval_status" value="${bean.map.approval_status eq 'Y' ? 'Y' : 'N'}"/>
			<input type="hidden" name="approved_by" id="approved_by" value="${bean.map.approved_by}"/>

			<legend class="fieldSetLabel" style="margin-top: 8px"></legend>
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="GLtable" border="0" style="margin-top: 8px">
				<tr>
					<th>
						<c:if test="${param._method !='add'}">#</c:if>
						<insta:ltext key="insurance.patientapprovallist.patientapprovals.approvalno"/>
					</th>
					<th><insta:ltext key="insurance.patientapprovallist.patientapprovals.sponsor"/></th>
					<th><insta:ltext key="insurance.patientapprovallist.patientapprovals.validityfrom"/></th>
					<th><insta:ltext key="insurance.patientapprovallist.patientapprovals.validityto"/></th>
					<th><insta:ltext key="insurance.patientapprovallist.patientapprovals.priority"/></th>
					<th>Status</th>
				</tr>
				<c:set var="numpatientgls" value="${fn:length(patientgls)}"/>
				<c:forEach begin="1" end="${numpatientgls+1}" var="i" varStatus="loop">
					<c:set var="patientgl" value="${patientgls[i-1].map}"/>
					<c:if test="${empty patientgl}">
						<c:set var="style" value='style="display:none"'/>
					</c:if>
					<tr ${style}>
						<td>
							<c:if test="${param._method !='add'}">
								<input type="radio" value="${ifn:cleanHtmlAttribute(patientgl.sponsor_approval_id)}"
									${patientgl.sponsor_approval_id == param.sponsor_approval_id ? 'checked' : ''}
									name="selected_sponsor_approval_id" onclick = "setApprovals(this,${i})"/>
							</c:if>
							${patientgl.approval_no}
						</td>
						<td>${patientgl.tpa_name}</td>
						<td>
							<fmt:parseDate value="${patientgl.validity_start}" pattern="yyyy-MM-dd" var="dt"/>
							<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="validityStart"/>
							${validityStart}
						</td>
						<td>
							<fmt:parseDate value="${patientgl.validity_end}" pattern="yyyy-MM-dd" var="dt"/>
							<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="validityEnd"/>
							${validityEnd}
						</td>
						<td>${patientgl.priority}</td>
						<td>${patientgl.status == 'A' ? 'Active' : 'Inactive'}</td>
					</tr>
				</c:forEach>
			</table>
			<legend class="fieldSetLabel" style="margin-top: 12px"></legend>
			<fieldset class="fieldSetBorder">
				<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<td class="formlabel" >Primary Center:</td>
						<td class="forminfo">
							<select name="primary_center_id" id="primary_center_id" class="dropdown">
								<option value="">${dummyValue}</option>
								<c:forEach items="${centers}" var="center">
									<option value="${center.map.center_id}" ${bean.map.primary_center_id == center.map.center_id ? 'selected' : ''}>
										${center.map.center_name}
								</option>
								</c:forEach>
							</select>
							<span class="star">*</span>
						</td>
						<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.sponsor"/>:</td>
						<td class="forminfo">
							<insta:selectdb name="sponsor_id" id="sponsor_id" table="tpa_master" valuecol="tpa_id" displaycol="tpa_name"
								value="${bean.map.sponsor_id}" orderby="tpa_name" dummyvalue="${dummyValue}"/>
							<span class="star">*</span>
						</td>
						<td class="formlabel" ><insta:ltext key="insurance.patientapprovallist.patientapprovals.approvalno"/>:</td>
						<td class="forminfo">
							<input type="text" name="approval_no" id="approval_no" value="${bean.map.approval_no}"/>
							<span class="star">*</span>
						</td>
					</tr>
					<tr>
						<td class="formlabel" ><insta:ltext key="insurance.patientapprovallist.patientapprovals.rateplan"/>:</td>
						<td class="forminfo">
							<input type="hidden" name="org_name" id="org_name" value=""/>
							<select name="org_id" id="org_id" class="dropdown" onchange="onchangeRatePlan(this)">
								<option value="">${dummyValue}</option>
								<c:forEach items="${orgNames}" var="orgNames">
									<c:choose>
										<c:when test="${bean.map.org_id == orgNames.org_id}">
											<option value="${orgNames.org_id}" selected>${orgNames.org_name}</option>
										</c:when>
										<c:otherwise>
											<option value="${orgNames.org_id}">${orgNames.org_name}</option>
										</c:otherwise>
									</c:choose>
								</c:forEach>
						 	</select>
							<span class="star">*</span>
						</td>
						<td class="formlabel" ><insta:ltext key="insurance.patientapprovallist.patientapprovals.priority"/>:</td>
						<td class="forminfo">
							<input type="text" name="priority" id="priority" value="${bean.map.priority}" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="checkDuplicatePriority(this)"/>
							<span class="star">*</span>
						</td>
						<td class="formlabel">Approval Document:</td>
						<td>
		            		<input type="file" name="sponsor_doc" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
		            		<c:if test="${not empty fileBean.map.file_name}">
		            			<button type="button" name="viewAttachment" accesskey="V" value=""
		            				onclick="showAttachment();"><b><u>V</u></b>iew </button>
		            			<insta:truncLabel value="${fileBean.map.file_name}" length="15"/>
							</c:if>
					  	</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.validityfrom"/>:</td>
						<fmt:parseDate value="${bean.map.validity_start}" pattern="yyyy-MM-dd" var="dt"/>
						<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="frm"/>
						<td class="forminfo">
							<insta:datewidget name="validity_start" id="validity_start" value="${frm}"/>
							<span class="star">*</span>
						</td>
						<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.validityto"/>:</td>
						<fmt:parseDate value="${bean.map.validity_end}" pattern="yyyy-MM-dd" var="dt"/>
						<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="to"/>
						<td class="forminfo">
							<insta:datewidget name="validity_end" id="validity_end"  value="${to}"/>
							<span class="star">*</span>
						</td>
						<td class="formlabel">Status :</td>
						<td>
							<insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I"
								optexts="Active,Inactive" />
						</td>
 					</tr>
 					<tr>
 						<td class="formlabel">Approval Status :</td>
 						<td class="forminfo">${bean.map.approval_status == 'Y' && copy != true ? 'Yes' : bean.map.approval_status == 'N' && copy != true ? 'No' : ''}</td>
 						<td class="formlabel">Approved By :</td>
 						<td class="forminfo">${copy != true ? bean.map.approved_by : ''}</td>
 					</tr>
				</table>
			</fieldset>

			<insta:datagrid id="patientApprovalTable"
			columnDefs="applicable_to_name:Item;limit_value:Limit;copay_value:Copay;applicable_to;applicable_to_id;limit_type;copay_type;sponsor_approval_detail_id;item_status"
			editorId="patientApprovalDialog" allowDelete="true" allowEdit="true" allowInsert="true" columnHeaders="Items,Limit,Copay">
  				<jsp:attribute name="rowTemplate">
  					<tr>
  						<td><label id="lbl_applicable_to_name"></label></td>
  						<td class="number"><label id="lbl_limit_value"></label></td>
  						<td class="number"><label id="lbl_copay_value"></label></td>
  					</tr>
  				</jsp:attribute>
			</insta:datagrid>
			<div class="screenActions" style="float: left">
				<button type="button" accesskey="S" onclick="return patientApprovalFormValidate('S');"><b><u>S</u></b>ave</button>
				<%
					String url = null;
					
					java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
					java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext().getAttribute("actionUrlMap");
					if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("approve_sponsors_approval").equals("A")) { // we have the rights.
						url = (String)actionUrlMap.get("approve_sponsors_approval"); // lookup the url for the action.
					}
					
					if(url != null) {%>					
						<button type="button" accesskey="R" onclick="return patientApprovalFormValidate('A');"><b><u>S</u></b>ave & Approve</button>
					<%}	
				%>
				<c:if test="${(numpatientgls gt 0) && (param._method !='add')}">
					<button type="button" accesskey="P" onclick="return patientApprovalFormValidate('P');"><b><u>P</u></b>rocess Previous Months</button>
				</c:if>
				<c:if test="${param._method != 'add'}">
					<a href="${cpath}/Insurance/${URl}.do?_method=add">| Add</a>
				</c:if>
				<insta:screenlink screenId="patient_sponsors_approval" extraParam="?_method=list&status=A&status=P&sortReverse=true&sortOrder=sponsor_approval_id"
					label="Patient Approvals List" addPipe="true"/>
				<insta:screenlink screenId="dialysis_order" addPipe="true" label="Dialysis Order"
					extraParam="?_method=showDialysisOrder&mrNo=${not empty param.mr_no ? param.mr_no : bean.map.mr_no}"/>
				<c:if test="${param._method != 'add'}">
					<a href="${cpath}/Insurance/${URl}.do?_method=add&mr_no=${not empty param.mr_no ? param.mr_no : bean.map.mr_no}&copy=true&sponsor_approval_id=${bean.map.sponsor_approval_id}">| Copy GL</a>
				</c:if>
			</div>
		</form>

		<insta:dialog id="patientApprovalDialog" title="Add/Edit approval details">
			<tr>
 				<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.applicablecategory"/>:</td>
				<td class="forminfo">
					<select class="dropdown" name="applicable_to" id="applicable_to" onchange="onChangeApplicableto(this);">
						<option value="I" selected><insta:ltext key="insurance.patientapprovallist.patientapprovals.item"/></option>
					</select>
					<input type="hidden" name="item_status" id="item_status" value="A" />
				</td>

 			</tr>
 			<tr>
 				<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.limit"/>:</td>
				<td class="forminfo">
					<select class="dropdown" name="limit_type" id="limit_type" onchange="onchangeLimitType(this)">
						<option value="" selected ><insta:ltext key="insurance.patientapprovallist.patientapprovals.select"/></option>
						<option value="Q"><insta:ltext key="insurance.patientapprovallist.patientapprovals.quantity"/></option>
						<option value="A"><insta:ltext key="insurance.patientapprovallist.patientapprovals.amount"/></option>
					</select>
					<span class="star">*</span>
				</td>
				<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.limitvalue"/>:</td>
				<td class="forminfo" >
					<input type="text" name="limit_value" id="limit_value" value="" onkeypress="return onKeyPressAddQty(event,'limit');"/>
					<span class="star">*</span>
				</td>
 			</tr>
 			<tr>
 				<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.copay"/>:</td>
				<td class="forminfo">
					<select class="dropdown" name="copay_type" id="copay_type" onchange="onchangeCopayType(this)">
						<option value="" selected><insta:ltext key="insurance.patientapprovallist.patientapprovals.select"/></option>
						<option value="P"><insta:ltext key="insurance.patientapprovallist.patientapprovals.percent"/></option>
						<option value="A"><insta:ltext key="insurance.patientapprovallist.patientapprovals.amount"/></option>
					</select>
					<span class="star">*</span>
				</td>
				<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.copayvalue"/>:</td>
				<td class="forminfo">
					<input type="text" name="copay_value" id="copay_value" value="" onkeypress="return onKeyPressAddQty(event,'copay');"/>
					<span class="star">*</span>
				</td>
 			</tr>
 			<tr>
 				<td class="formlabel">Item:</td>
				<td class="forminfo">
					<input type="hidden" name="applicable_to_id" id="applicable_to_id" value="" />
					<input type="hidden" name="activity_type" id="activity_type" value=""/>
					<input type="hidden" name="applicable_to_name" id="applicable_to_name" value=""/>

					<div>
						<div id="itemAutoComp"   style="display:none; padding-bottom:20px">
							<input type="text" id="applicable_to_name_item" name="applicable_to_name_item" style="width: 140px;"/>
							<div id="itemcontainer" style="width:35em;"></div>
						</div>
						<span class="star" style="float:right; margin-top:-18px">*</span>
					</div>
				</td>
				<td>

					<img name = "add_item" id="add_item" title="Add Item" onclick="addItem();"
							src='${cpath}/images/check-mark-icon.png' style="float:left; margin-top: -3px"/>

				</td>
 			</tr>
 			<tr>
 				<td colspan="4" class="last">
 					<div id="parent_div"></div>
 				</td>
 			</tr>
		</insta:dialog>
		<script type="text/javascript">
			function showAttachment(){
				window.open("${cpath}//Insurance/${URl}.do?_method=showSponsorDocument&sponsor_approval_id=${fileBean.map.sponsor_approval_id}&cache=false");
			}
			var sesHospitalId = '${ifn:cleanJavaScript(sesHospitalId)}';
			var masterTimeStamp = '${masterTimeStamp}';
			var details = ${not empty detailsListJSON ? detailsListJSON : '[]'};
			var patientApprovalJson = <%= request.getAttribute("patientGlsJson") %>;
			var copy = <%= request.getParameter("copy") %>;
			var existedPriority = '${bean.map.priority}';
			var approvedBy = '${ifn:cleanJavaScript(userid)}';
			var primaryCenterId = <%= request.getAttribute("primaryCenter") %>;
			if(primaryCenterId == null){
				primaryCenterId="";
			}
			var method ='${ifn:cleanJavaScript(param._method)}';
		</script>
	</body>
</html>
