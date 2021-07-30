<%@page import="com.bob.hms.common.Constants" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
 <%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
<title><insta:ltext key="laboratory.testinfoviewer.title.instahms"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="/diagnostics/test_information_viewer.js"/>
<insta:link type="script" file="ajax.js" />

<style type="text/css">
	.ygtvlabel, .ygtvlabel:link, .ygtvlabel:visited, .ygtvlabel:hover{
		margin-left:2px;text-decoration:none;cursor:pointer;
	}
	.myDiv{
		border-right:solid 1px #000000;
	}
	.scroll {
	       overflow:auto;
	       width: 20em;
	       height:30em;
	}

</style>
<script language="javascript" type="text/javascript">
var cpath = '<%= request.getContextPath()%>';
var roleId='${ifn:cleanJavaScript(roleId)}';
var docsFound = ${not empty docList};
</script>

</head>
<body  class="setMargin yui-skin-sam" onload="treeInit()">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<h1 >${prescBean.map.test_name} - <insta:ltext key="laboratory.testinfoviewer.title"/></h1>

<insta:feedback-panel/>

<c:choose>
<c:when test="${prescBean.map.hospital == 'hospital'}">
	<insta:patientdetails  visitid="${prescBean.map.pat_id}" showClinicalInfo="true" />
</c:when>
<c:otherwise>
	<insta:incomingpatientdetails incomingVisitId="${prescBean.map.pat_id}"/>
</c:otherwise>
</c:choose>

<form name="test_info_viewer_form" action="TestInformationViewer.do">
<input type="hidden" name="_method" value="list">

<table width="100%" height="100%" border="0">
	<c:set var="clinical_notes" value=""/>
	<c:if test="${not empty collPrescBean}">
		<c:set var="clinical_notes" value="${collPrescBean.map.clinical_notes}"/>
	</c:if>
	<c:if test="${empty collPrescBean && not empty prescBean}">
		<c:set var="clinical_notes" value="${prescBean.map.clinical_notes}"/>
	</c:if>
	<c:if test="${not empty clinical_notes}">
		<tr>
			<td colspan="3">
				<fieldset class="fieldSetBorder" >
					<legend class="fieldSetLabel"><insta:ltext key="laboratory.testinfoviewer.user.notes"/></legend>
					<div >${clinical_notes}</div>
				</fieldset>
			</td>
		</tr>
	</c:if>
	<c:choose>
		<c:when test ="${not empty docList}" >
			<tr>
				<td valign="top" width="30%">
					<fieldset class="fieldSetBorder" >
						<legend class="fieldSetLabel"><insta:ltext key="laboratory.testinfoviewer.documents"/></legend>
						<div ><div id="treeDiv1"></div></div>
					</fieldset>
				</td>
				<td width="1%"></td>
				<td  valign="top" width="70%">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel"><insta:ltext key="laboratory.testinfoviewer.documentdetails"/></legend>
						<table border="0" width="100%">
							<tr>
								<td>
									<table width="100%" >
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
															<iframe  id="display1" name="display1" src="" height="99%" width="100%" frameborder="1" >
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
														<td><a href='javascript:void(0)' id="prevLink" target='display1' style='background-color:#fff'>&lt; <insta:ltext key="laboratory.testinfoviewer.previous"/></a> |
															<a href='javascript:void(0)' id="nextLink" target='display1' style='background-color:#fff'><insta:ltext key="laboratory.testinfoviewer.next"/> &gt;</a>
														</td>
														<td></td>
														<td></td>
														<td></td>
														<td style="width: 250px;text-align: right">
															<insta:selectdb name="printerId" id="printerId" table="printer_definition"
																valuecol="printer_id"  displaycol="printer_definition_name" dummyvalue="${dummyvalue}"/>

														</td>
														<td style="width: 20px"><a href="#" id="imgLink" target="_blank"><img class="newWindow" id="newWindowImg" src="${cpath}/images/cleardot.gif"/></a></td>

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
		<c:when test="${empty docList && empty clinical_notes}">
			<tr><td  colspan="3" valign="top" align="center" style="font-size:15pt;"><insta:ltext key="laboratory.testinfoviewer.norecordstodisplay"/></td></tr>
		</c:when>
	</c:choose>
</table>
</form>
	<script>
		var VisitId = '${ifn:cleanJavaScript(param.patient_id)}';
		var allDocsList = ${docListJSON};
		var docTypeDetails = ${docTypeValues};
	</script>
</body>
</html>
