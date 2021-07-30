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
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Consolidated Bill List</title>

<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>	
	
	
	<script type="text/javascript">
	
	var toolbar = {
			Edit: {
				title: "View",
				imageSrc: "icons/Edit.png",
				href: 'billing/consolidatedbill.do?_method=show',
				onclick: null,
				description: "View Consolidated Bill details"
			}
		};
	
	function initSearchList()
	{
		initMrNoAutoComplete(cpath, 'mrno', 'mrnoContainer', 'active');
		createToolbar(toolbar);
	}
	</script>
</head>

<c:set var="headerName" value="Consolidated Bill List"/>
<c:set var="keyField" value="consolidated_bill_no,mr_no"/>
<c:set var="searchFormName" value="consolidatedbillSearchForm"/>
<!--  <field_name>:<display_name> -->
<c:set var="listColumns" value="mr_no:Mr No,main_visit_id: Main Visit Id,patient_name:Patient Name,consolidated_bill_no:Consolidated Bill No,cpbstatus:Status,open_date:Open Date,total_amount:Bill Amt,pat_amt:Pat. Amt"/>
<c:set var="sortableColumns" value="consolidated_bill_no,patient_name,mr_no,open_date"/>

<body onload="initSearchList();">

 <insta:list-dashboard displayName="${headerName}" searchFormName="${searchFormName}" > 
		<insta:search-lessoptions form="${searchFormName}" >
		<div class="searchBasicOpts" > 
			<div class="sboField">
				<div class="sboFieldLabel">MR No./Patient Name:</div>
				<div class="sboFieldInput">
					<div id="mrNoAutocomplete">
						<input type="text" name="mr_no" id="mrno" size="10" value="${ifn:cleanHtmlAttribute(param['mr_no'])}"/>
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
		</div>
		
		<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel">Open Date</div>
				<div class="sfField">
					<div class="sfFieldSub">From</div>
						<insta:datewidget name="open_date" valid="past" id="open_date0" value="${paramValues.open_date[0]}" />
					</div>
					<div class="sfField">
					<div class="sfFieldSub">To</div>
						<insta:datewidget name="open_date" valid="past" id="open_date1" value="${paramValues.open_date[1]}" />
						<input type="hidden" name="open_date@op" value="ge,le">
						<input type="hidden" name="open_date@type" value="date">
						<input type="hidden" name="open_date@cast" value="y">
					</div>
			</td>
			<td>
				<insta:search-basic-field fieldName="consolidated_bill_no" type="t" displayName="Consolidated Bill No" style="height:69px;"/>
			</td> 
			<td>
			<!-- Credit Note search bar -->
				<div class="sfLabel">Consolidated Credit Note</div>
				<div class="sfField">
					<insta:checkgroup name="is_consolidated_credit_note" selValues="${paramValues.is_consolidated_credit_note}"
							opvalues="true,false" optexts="Included,Not Included" />
							<input type="hidden" name="is_consolidated_credit_note@cast" value="y"/>
				</div>
			</td>   
			<td>	
				<insta:search-basic-field fieldName="status" type="c" opvalues="A,F,C" optexts="Open,Finalized,Closed" displayName="Status" style="height:69px;"/> 
			</td> 
			    
		</tr>
		</table>
		</insta:search-lessoptions>
	  	 <insta:search-result keyField="${keyField}" columns="${listColumns}"  sortableColumns="${sortableColumns}" dataList="${pagedList}" showToolbar="Y"/>
 		 
 		 <div  class="legend">
	 		<div class="flag"><img src="${cpath}/images/empty_flag.gif"/></div>
			<div class="flagText">Open</div>
			<div class="flag"><img src="${cpath}/images/blue_flag.gif"/></div>
			<div class="flagText">Finalized</div>
			<div class="flag"><img src="${cpath}/images/red_flag.gif"/></div>
			<div class="flagText">Closed</div>
		</div>
		
	 </insta:list-dashboard> 

</body>
</html>