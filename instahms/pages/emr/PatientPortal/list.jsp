<%@page import="com.bob.hms.common.Constants"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
<title>Patient List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<c:set var="emrurldate"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("emr_url_date") %>'
	scope="request" />

<insta:link type="script" file="date_go.js" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="instadate.js" />
<insta:link type="css" path="scripts/yui2.5.2/fonts/fonts-min.css" />
<insta:link type="css"
	path="scripts/yui2.5.2/treeview/assets/skins/sam/treeview.css" />
<insta:link type="script"
	file="yui2.5.2/yahoo-dom-event/yahoo-dom-event.js" />
<insta:link type="script" file="yui2.5.2/treeview/treeview.js" />
<insta:link type="script" file="/emr/MainTreeView.js" />
<style type="text/css">
.myDiv{
border-right:solid 1px #000000;
}
</style>
<script language="javascript" type="text/javascript">

var cpath = '<%= request.getContextPath()%>';

var docsFound = ${not empty allDocs};

var emrurldate = '${emrurldate}';


</script>
</head>
<body class="setMargin" onload="treeInit()">
<br/>
<insta:patientgeneraldetails mrno="${mr_no}" addExtraFields="true" showClinicalInfo="true" />
<form name="emrTreeForm" action="${cpath}/patient/MedicalRecord.do">
<input type="hidden"name="_method" value="list">
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}">
<input type="hidden" name="VisitId" />

<table width="100%" height="100%" border="0">
	<c:choose>
		<c:when test="${not empty allDocs}">
			<tr>
				<td valign="top" width="30%">
				<table>
					<tr>
						<td><select name="filterType"
							onchange="getEMRSearchResults();">
							<option value="visits">Visit</option>
							<option value="docType">Document Type</option>
						</select></td>
					</tr>
					<tr id="documentTypeSearch" style="display:none;">
						<td>
						<table class="formtable">
							<tr>
								<td class="formlabel">Include:</td>
								<td><insta:selectdb name="indocType" table="doc_type"
									valuecol="doc_type_id" displaycol="doc_type_name"
									value="${param.indocType}" dummyvalue="All"
									dummyvalueId="*" onchange="docTypeChange(true);" filtered="true"
									filtercol="status" title="Document Type is mandatory." /></td>
							<tr>
							<tr>
								<td class="formlabel">Exclude:</td>
								<td><insta:selectdb name="exdocType" table="doc_type"
									valuecol="doc_type_id" displaycol="doc_type_name"
									value="${param.exdocType}" dummyvalue="None"
									dummyvalueId="*" onchange="docTypeChange(false);" filtered="true"
									filtercol="status" title="Document Type is mandatory." /></td>
							</tr>
						</table>
						</td>
					</tr>
					<tr id="datesearch" style="display:none;">
						<td style="width: 100%">
						<table style="width: 100%">
							<tr>
								<td class="formlabel">From:</td>
								<td><insta:datewidget name="fromDate" btnPos="left" /></td>
							<tr>
							<tr>
								<td class="formlabel">To:</td>
								<td><insta:datewidget name="toDate" btnPos="left" /></td>
							</tr>
						</table>
						</td>
					</tr>
					<tr>
						<td><input type="button" value="Search"
							onclick="submitValues();"></td>
					</tr>
					<tr>
						<td>
						<fieldset class="fieldSetBorder" style="margin-top: 10px">
						<legend class="fieldSetLabel">Documents</legend>
						<div>
						<div id="treeDiv1"></div>
						</div>
						</fieldset>
						</td>
					</tr>
				</table>
				</td>
				<td valign="top" width="80%">
				<fieldset class="fieldSetBorder"><legend
					class="fieldSetLabel">Document details</legend>
				<table border="0" width="100%">
					<tr>
						<td>
						<table width="100%">
							<tr>
								<td valign="top">
								<table id="documentSummary" class="formtable">
								</table>
								</td>
							</tr>
							<tr>
								<td valign="top">
									<table border="0" width="100%" height="600px">
										<tr>
											<td valign="top">
												<iframe  name="display1" id="display1" src="" height="99%" width="100%" frameborder="1" >
												</iframe>
											</td>
										</tr>
									</table>
								</td>
							</tr>
							<tr>
								<td>
								<table id="navigate" class="formtable" style="margin-top: 0px">
									<tr>
										<td><a href='javascript:void(0)' id="prevLink"
											target='display1' style='background-color:#fff'>&lt; Prev</a>
										| <a href='javascript:void(0)' id="nextLink" target='display1'
											style='background-color:#fff'>Next &gt;</a></td>
										<td></td>
										<td></td>
										<td></td>
										<td></td>
										<td></td>
									</tr>
								</table>
								</td>
							</tr>
						</table>
						</td>
					</tr>
				</table>
				</fieldset>
				</td>
			</tr>
		</c:when>
		<c:otherwise>
			<tr>
				<td valign="top" align="center" style="font-size:15pt;">No
				Records to display</td>
			</tr>
		</c:otherwise>
	</c:choose>
</table>
</form>
<script>
		var mrnoFromAction = '${not empty param.mr_no ? param.mr_no : param.userId}';
		var VisitId = '${ifn:cleanJavaScript(param.VisitId)}';
		var allDocsList = <%=request.getAttribute("filteredDocs")!=null?request.getAttribute("filteredDocs"):"''"%>;
		var filterTypeFromAction = '${ifn:cleanJavaScript(filterTypeFromAction)}';
		var docTypeDetails = ${docTypeValues};
		var orderUrl = '';
	</script>
</body>
</html>
