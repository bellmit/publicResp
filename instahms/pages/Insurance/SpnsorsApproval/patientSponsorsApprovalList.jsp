<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page isELIgnored="false" %>

 <c:set var="cpath" value="${pageContext.request.contextPath}"/> 

<html>
<head>
<meta name="i18nSupport" content="true"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title><insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.title"/></title>

	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	
	<c:set var="addEditUrl" value="${(screenId eq 'patient_sponsors_approval') ? 'PatientSponsorsApproval' : 'ApproveSponsorsApproval' }"/>
	
	<script type="text/javascript">
		
		var toolbar = {
				Edit: {
					title: "View/Edit",
					imageSrc: "icons/Edit.png",
					href: 'Insurance/${addEditUrl}.do?_method=show',
					onclick: null,
					description: "View and/or Edit Patient Approvals details"
				}
			};

		function initSearchList()
		{
			initMrNoAutoComplete(cpath, 'mrno', 'mrnoContainer', 'active');
			createToolbar(toolbar);
		}

	</script>
</head>

<c:set var="headerdis">
 <insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.header"/>
</c:set>
<c:set var = "approvalid">
<insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.approvalid"/>
</c:set>
<c:set var ="status">
<insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.status"/>
</c:set>
<c:set var ="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var ="patientname">
<insta:ltext key="ui.label.patient.name"/>
</c:set>
<c:set var ="sponsor">
<insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.sponsor"/>
</c:set>
<c:set var ="validfrom">
<insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.validfrom"/>
</c:set>
<c:set var ="validupto">
<insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.validupto"/>
</c:set>

<c:set var="addLink" value="New Approval"/>
<c:set var="headerName" value="${headerdis}"/>
<c:set var="keyField" value="sponsor_approval_id,mr_no"/>
<c:set var="searchFormName" value="PatientSponsorSearchForm"/>
<!--  <field_name>:<display_name> -->
<c:set var="listColumns" value="mr_no:${mrno},patient_name:${patientname},approval_no:${approvalid},tpa_name:${sponsor},validity_start:${validfrom},validity_end:${validupto},approved_by:Approved By"/>
<c:set var="sortableColumns" value="approval_no,patient_name,mr_no,tpa_name,validity_start,validity_end"/>

<body onload="initSearchList();">

 <insta:list-dashboard displayName="${headerName}" searchFormName="${searchFormName}" > 
		<insta:search-lessoptions form="${searchFormName}" >
		<div class="searchBasicOpts" > 
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.mrnopatientname"/>:</div>
				<div class="sboFieldInput">
					<div id="mrNoAutocomplete">
						<input type="text" name="mr_no" id="mrno" size="10" value="${ifn:cleanHtmlAttribute(param['mr_no'])}"/>
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
		</div>
		<div>
			<insta:search-basic-field fieldName="approval_no" type="t" displayName="${approvalid}" style="height:69px;"/>
		</div>	
		<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel"><insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.sponsorname"/>:</div>
				 <div class="sfField">
				<insta:selectdb name="tpa_id" valuecol="tpa_id" table="tpa_master" displaycol="tpa_name" 
					values="${paramValues.tpa_id}" multiple="multiple" orderby="tpa_name" filtercol="status" filtervalue="A"
					style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;"/>
				</div>
			</td>
			<td>
					<div class="sfLabel"><insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.validitydate"/>:</div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.validityfrom"/>:</div>
						<insta:datewidget name="validity_start"  id="validity_start" value="${param.validity_start}" />
						<input type="hidden" name="validity_start@type" value="date">
						<input type="hidden" name="validity_start@cast" value="y">
						  <input type="hidden" name="validity_start@op" value="ge"/>  
					</div>
					<div class="sfField">
					<div class="sfFieldSub"><insta:ltext key="insurance.sponsorsapproval.patientsponsorsapprovallist.validityto"/>:</div>
						<insta:datewidget name="validity_end"  id="validity_end" value="${param.validity_end}" />
						<input type="hidden" name="validity_end@type" value="date">
						<input type="hidden" name="validity_end@cast" value="y">
						  <input type="hidden" name="validity_end@op" value="le"/>  
					</div>  
			</td>	 
			<td>
				<insta:search-basic-field fieldName="status" type="c" opvalues="A,I,C" optexts="Active,Inactive,Closed" displayName="${status}" style="height:69px;"/> 
			</td> 
			<td>
				<insta:search-basic-field fieldName="approval_status" type="c" opvalues="Y,N" optexts="Approved,Pending Approval" displayName="Approval Status" style="height:69px;"/> 
			</td>   
		</tr>
		</table>
		</insta:search-lessoptions>
	     <insta:search-result keyField="${keyField}" columns="${listColumns}"  sortableColumns="${sortableColumns}" dataList="${pagedList}" showToolbar="Y" statusCol="combine_flag_status"/>  
 		 <insta:list-footer addEditUrl="${addEditUrl}" displayName="${addLink}"  legends = "empty_flag.gif:Approved,yellow_flag.gif:Pending Approval,grey_flag.gif:Inactive,red_flag.gif:Closed" /> 
	 </insta:list-dashboard> 
</body>
</html>
