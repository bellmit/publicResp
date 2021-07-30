<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Collection Approval - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<script type="text/javascript">
		function init() {
//			createToolbar(toolBar);
			initPkgValueCapDialog();
		}
		function doApprove(){
			var checkBox = document.getElementsByName("_updateReceiptCollection");
			var count = 0;
			for(var i=0; i<checkBox.length; i++) {
				if(checkBox[i].checked) count++;
			}
			if(count==0) {
				alert('Please select atleast one Collection for approval.');
				return false;
			}else{
				document.CollectionApprovalForm.submit();
			}
		}
		function searchValidation(){
			if (!doValidateDateField(document.getElementById("receipt_date0"),'past')) {
				document.getElementById("receipt_date0").value="";
				return false;
			}
			if (!doValidateDateField(document.getElementById("receipt_date1"),'past')) {
				document.getElementById("receipt_date1").value="";
				return false;
			}
		}
		function getColletionDetails(obj){
			var collId = obj;
			collId = encodeURIComponent(collId);
			makeAjaxCallforStaticPackage(collId);
			showValueCollectionDialog(obj);
			return null;
		}
		function makeAjaxCallforStaticPackage(collId, curPage) {
			var url = cpath +'/Insurance/CollectionApproval.do?_method=getCollectionApprovalDetails&collection_id='+collId;

			YAHOO.util.Connect.asyncRequest('GET', url,
				{ 	success: populateCollectionDetails,
					failure: failedToCollectionDetails,
					argument: []
				});
		}
		function populateCollectionDetails(response) {
			var mr_no='';
			var patient_name='';
			var visit_dat='';
			var amt_recd='';
			var recd_dat='';

		 	if (response.responseText != undefined) {
				var pacakgeDetails = eval('(' + response.responseText + ')');
				var table = document.getElementById('staticCollectionDetailsTab');
					for (var i=1; i<table.rows.length; ) {
						table.deleteRow(i);
					}
					var dtoList = pacakgeDetails.dtoList;
				for(var i=0;i<dtoList.length;i++){
					mr_no = dtoList[i].mr_no;
					patient_name= dtoList[i].patient_name;
					visit_dat= dtoList[i].visit_date;
					recd_dat= dtoList[i].receipt_date;
					amt_recd = dtoList[i].amount;
					table.innerHTML += '<tr>'+'<td class="formlabel" style="text-align: center;">'+mr_no+'</td>'
					+'<td class="formlabel" style="text-align: center;">'+patient_name+'</td>'+
					'<td class="formlabel" style="text-align: center;">'+visit_dat+'</td>'+
					'<td class="formlabel" style="text-align: center;">'+recd_dat+'</td>'+
					'<td class="formlabel" style="text-align: center;">'+amt_recd+'</td></tr>';
				}
			}
		}

		function failedToCollectionDetails() {
		}
		function showValueCollectionDialog(obj) {
			valueCollectionDialog.show();
			valueCollectionDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
		}
		function initPkgValueCapDialog() {
			var dialogDiv = document.getElementById("valueCollectionDialog");
			if (empty(dialogDiv)) return ;
			dialogDiv.style.display = 'block';
			valueCollectionDialog = new YAHOO.widget.Dialog("valueCollectionDialog",{
					width:"500",
					fixedcenter: true,
					text: "Package Value Cap",
					context :["btnValueCap", "tl", "tl"],
					visible:false,
					modal:true,
					constraintoviewport:true
				});
			var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
			                                              { fn:onCancel,valueCollectionDialog,
			                                                correctScope:true } );
			valueCollectionDialog.cancelEvent.subscribe(onCancel);
			valueCollectionDialog.cfg.queueProperty("keylisteners", escKeyListener);
			valueCollectionDialog.render();

		}
		function onCancel() {
			valueCollectionDialog.hide();
		}
	</script>
</head>
<body onload="init()">
	<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
	<h1>Collection Approval</h1>
	<insta:feedback-panel/>
	<form name="CollectionApprovalSearchForm" method="GET">
		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
	 	<insta:search-lessoptions form="CollectionApprovalSearchForm" validateFunction="searchValidation()">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField" >
						<div class="sfLabel"><insta:ltext key="search.patient.visit.status"/></div>
						<c:set var="statusOptions">Pending,Approved
						</c:set>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="P,A" optexts="${statusOptions}" selValues="${paramValues['status']}"/>
						</div>
					</td>
					
					<td class="sboField">
						<div class="sfLabel" >Date</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="search.patient.visit.discharge.date.from"/>:</div>
							<insta:datewidget name="receipt_date" id="receipt_date0"
								value="${paramValues.receipt_date[0]}" onchange="doValidateDateField(this,'past');"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="search.patient.visit.discharge.date.to"/>:</div>
							<insta:datewidget name="receipt_date" id="receipt_date1"
								value="${paramValues.receipt_date[1]}" onchange="doValidateDateField(this,'past');"/>
							<input type="hidden" name="receipt_date@op" value="ge,le"/>
						</div>
					</td>
				</tr>
			</table>
		</insta:search-lessoptions> 
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	</form>
	<form action="CollectionApproval.do" method="POST" name="CollectionApprovalForm">
		<input type="hidden" name="_method" value="update"/>
		<input type="hidden" name="_searchMethod" value="update"/>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th style="padding-top: 0px;padding-bottom: 0px">
						<input type="checkbox" name="_checkAllForClose" onclick="return checkOrUncheckAll('_updateReceiptCollection', this)"/>
					</th>
					<th>Collection Id</th>
					<th>Sponsor</th>
					<th>No. of Receipts</th>
					<th>Amt. Received</th>
					<th>User Name</th>
					<insta:sortablecolumn name="receipt_date" title="Date"/>
					<th>Status</th>
					<th></th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						 id="toolbarRow${st.index}">
						<td>
							<c:set var="disabled" value=""/>
							<c:set var="checked" value=""/>
							<input type="checkbox" name="_updateReceiptCollection" value="${record.collection_id}" ${(record.status=='A') ? 'disabled':''}/>
						</td>
						<td>${record.collection_id}</td>
						<td>${record.tpa_name}</td>
						<td>${record.no_of_receipts}</td>
						<td>${record.amount}</td>
						<td>${record.username}</td>
						<td>${record.receipt_date}</td>
						<td>${record.status}</td>
						<td>
							<a onclick="return getColletionDetails(${record.collection_id});" title='Collection Summary'>
							<img src="${cpath}/icons/View.png" class="button" />
							</a>							
						</td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults  hasResults="${hasResults}"/>
		</div>
	
	 	<c:url var="url" value="CollectionApproval.do">
			<c:param name="_method" value="update"></c:param>
		</c:url>
	
		<table class="screenActions">
			<tr>
				<td><button type="button" value="Approve" accesskey="A" id="updateDetails" name="updateDetails" onclick="return doApprove();">
				<label><u><b><insta:ltext key="common.button.char.a"/></b></u>pprove</label></button></td>
			</tr>
		</table> 
	</form>
	<div id="valueCollectionDialog" style="display:none;">
		<div class="bd" style="overflow:auto;overflow-x:hidden;height:250px;">
			<fieldset class="fieldSetBorder" >
				<legend class="fieldSetLabel">Collection Summary</legend>
				<table class="formtable" id="staticCollectionDetailsTab">
					<tr>
						<td class="forminfo" style="text-align: center;width: 140px !important;">Mr. No.</td>
						<td class="forminfo" style="text-align: center;width: 175px !important;">Patient Name</td>
						<td class="forminfo" style="text-align: center;">Visit Date</td> 
						<td class="forminfo" style="text-align: center;">Recd Date</td> 
						<td class="forminfo" style="text-align: center;">Amt. Recd</td>
					</tr>
				    <tr>
					</tr>
			</table>
			</fieldset>
		</div>
	</div>
</body>
</html>

