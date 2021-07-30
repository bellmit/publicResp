<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Rate Variation Reports - Insta HMS</title>
	<script>
		function onSubmit(option) {

			document.inputform.format.value = option;
			var groupBy = document.inputform.dataOfview.value;
			var type = getRadioSelection(document.inputform.reportType);
			var returnstatus=true;
			if(document.inputform.groupBy.selectedIndex==0){
			 alert("Select GroupBy");
			 returnstatus=false;
			 document.inputform.groupBy.focus();
			 return false;
			 }
			 if(returnstatus){
			if (groupBy == 'tpa_name'){
					if (type == 'detail') {
							document.inputform.method.value='tpaDuesBillWiseReport';
					}else{
							document.inputform.method.value='tpaDuesReport';
					}
			}else if (groupBy == 'tpa_name_off'){
					document.inputform.method.value = 'tpaWriteOffReport';
			}else if (groupBy == 'patient_dues' || groupBy == 'patient_write_off'){
					document.inputform.method.value="patientExceptionsReport"
			}else{
					document.inputform.method.value='rateVariationDashboard';
			}
             returnstatus=validateFromToDate(document.inputform.fromDate, document.inputform.toDate);

			}
			if(returnstatus)
			 document.inputform.submit();

		}

		function onInit() {
			document.getElementById('pd').checked = true;
			setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
			document.inputform.dataOfview.selectedIndex = 0;
			populateFilterDetails();
			onChangeReportType();
		}

		function onChangeReportType() {
			var sel = document.inputform.groupBy;
			var isbillNothere=false;
			var type = getRadioSelection(document.inputform.reportType);
			if ( (type == 'detail')) {
			  for (i=0;i<sel.options.length;i++){
						if (sel.options[i].value == 'bill_no'){
							isbillNothere=true;
						}
					}
				 if(!isbillNothere){
				 insertIntoSelectBox(sel, sel.options.length, "Bill No", "bill_no");
				 }
				document.getElementById('viewButton').disabled = true;
			} else {
					for (i=0;i<sel.options.length;i++){
						if (sel.options[i].value == 'bill_no'){
							sel.remove(i);
						}
					}
				document.getElementById('viewButton').disabled = false;
			}

		}

	  function onChangeFilterBy() {

	      var sel = document.inputform.filterValue;
	     var filterBy = document.inputform.filterBy.value;
	     if (filterBy != "") {
		 loadSelectBox(sel, gGroupList[filterBy].list, gGroupList[filterBy].column,
				gGroupList[filterBy].column, "Select", "*");
		 if (gGroupList[filterBy].addNull) {
			insertIntoSelectBox(sel, 1, "(None)", "");
		}
	  } else {
		loadSelectBox(sel, null, null, null, "Select", "*");
	 }
	 sel.selectedIndex = 0;
 }


function populateFilterDetails(){

    var dataofView=document.inputform.dataOfview.value;
    var isdiscount=false;
     if(dataofView=="chargegroup_name")
      isdiscount=true;
      document.inputform.groupBy.length=1;
      document.inputform.filterBy.length=1;
      document.inputform.filterValue.length=1;
      for(var i=0;i<displayFieldNames.length;i++){
       if((!isdiscount)&&((displayFieldInnerValues[i]=="chargehead_name")||(displayFieldInnerValues[i]=="chargegroup_name")))
			 continue;
			 addOption(document.inputform.groupBy, displayFieldNames[i], displayFieldInnerValues[i]);
   }

   for(var i=0;i<displayFieldNames.length;i++){
       if((!isdiscount)&&((displayFieldInnerValues[i]=="chargehead_name")||(displayFieldInnerValues[i]=="chargegroup_name")) || (displayFieldInnerValues[i] =='bill_no') )
       continue;
      addOption(document.inputform.filterBy, displayFieldNames[i], displayFieldInnerValues[i]);
   }

}


 function addOption(selectbox,text,value ) {
	var optn = document.createElement("OPTION");
	optn.text = text;
	optn.value = value;
	selectbox.options.add(optn);
}

	</script>
</head>

<html>
	<body onload="onInit()">
		<div class="pageHeader">Rate Variation Report</div>
		<form name="inputform" method="GET"
			action="${cpath}/billing/RateVariationReport.do" target="_blank" >
			<input type="hidden" name="method" value="rateVariationDashboard">
			<input type="hidden" name="format" value="screen">

			<div class="tipText">
				This report gives you the exceptions (Rate changes, discounts, dues, write offs)
				between two dates.
			</div>
			<table align="center">
				<tr>
					<td colspan="2">Select the type of output:</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="sum" name="reportType" onchange="onChangeReportType()" value="dashboard"
						checked>
						<label for="sum">Tabular Summary: totals against for all
							Rate Plans/Patient Types.
						</label>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="radio" id="bill" name="reportType" onchange="onChangeReportType()" value="detail">
						<label for="bill">Item-wise: actual individual amounts of the exception in each bill (no preview)</label>
					</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
				</tr>

			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
			 	<jsp:param name="addTable" value="N"/>
				<jsp:param name="skipWeek" value="Y"/>
			</jsp:include>

			<tr>
				<td colspan="2" style="padding-top: 1em;">Select the data to be viewed & select the grouping criteria and the filter criteria:</td>
			</tr>
		</table>
    		<table align="center">
				<tr>
					<td>Data to view:</td>
					<td>
						<select name="dataOfview" onchange="populateFilterDetails();">
							<option value="chargegroup_name">Discounts</option>
							<option value="org_name">Master Variations</option>
							<option value="tpa_name">TPA Dues</option>
							<option value="tpa_name_off">TPA Write Off</option>
							<option value="patient_dues">Patient Dues</option>
							<option value="patient_write_off">Patient Write Off</option>
						</select>
					</td>
				</tr>
				<tr>
					<td align="right">Group By:</td>
					<td>
						<select name="groupBy">
							<option selected value="">--Select--</option>

						</select>
					</td>
				</tr>
				<tr>
					<td align="right">Filter By:</td>
					<td>
						<select name="filterBy" onchange="onChangeFilterBy()">
							<option selected value="">(No Filter)</option>

						</select>
					</td>
					<td>=
						<select name="filterValue">
							<option value="*">..(All)..</option>
						</select>
					</td>
				</tr>

			</table>
			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<button type=button accesskey="V" name="viewButton" id="viewButton"
						onclick="return onSubmit('screen')">Pre<b><u>v</u></b>iew</button>
					</td>
					<td>
						<button type="button" accesskey="P" onclick="return onSubmit('pdf')"><b><u>P</u></b>rint</button>
					</td>
				</tr>
			</table>
		</form>
		   <script>
			var gGroupList = {};
			gGroupList.visit_type_name = {list: ${visitTypesJSON}, column: "visit_type_name", addNull: false};
			gGroupList.chargehead_name = {list: ${chargeHeadsJSON}, column: "chargehead_name", addNull: false};
			gGroupList.chargegroup_name = {list: ${chargeGroupsJSON}, column: "chargegroup_name", addNull: false};
			gGroupList.ac_head = {list: ${accountHeadsJSON}, column: "account_head_name", addNull: false};
			gGroupList.dept_name = {list: ${departmentsJSON}, column: "dept_name", addNull: true};
			gGroupList.doctor_name = {list: ${doctorsJSON}, column: "doctor_name", addNull: true};
			gGroupList.referer = {list: ${referersJSON}, column: "referer_name", addNull: true};
			gGroupList.ward_name = {list: ${wardsJSON}, column: "ward_name", addNull: true};
			gGroupList.tpa_name = {list: ${tpasJSON}, column: "tpa_name", addNull: true};
			gGroupList.org_name = {list: ${orgsJSON}, column: "org_name", addNull: true};
			gGroupList.category_name = {list: ${categoriesJSON}, column: "category_name", addNull: true};
			var displayFieldNames=<%= request.getAttribute("fieldValues") %>;
			var displayFieldInnerValues=<%= request.getAttribute("fieldNames") %>;
			gGroupList.bill_type = {column:"type", addNull:false, list:[
			{type:"Bill Now"},
			{type:"Bill Later"},
			{type:" Pharmacy Bill"}
			]};

			</script>
	</body>
</html>


