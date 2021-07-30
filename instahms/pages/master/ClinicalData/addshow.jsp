<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Clinical Data Lab Results - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var resultNamesAndIds = <%= request.getAttribute("resultNamesAndIds") %>;
	var resultlabelIds = <%= request.getAttribute("resultlabelIds") %>;
	var testResultsAutoComplete = <%= request.getAttribute("testResultsAutoComplete") %>;
	var backupName = '';

	function keepBackUp(){
		if(document.ClinicalDataLabResultsMasterForm._method.value == 'update'){
				backupName = document.ClinicalDataLabResultsMasterForm.resultlabel.value;
		}
		resultNameAutoComplete();
	}

	function doClose() {
		window.location.href = "${cpath}/master/ClinicalDataLabResultsMaster.do?_method=list&sortOrder=resultlabel&sortReverse=false&status=A";
	}

	function validateForm() {
		var resultlabel = document.ClinicalDataLabResultsMasterForm.resultlabel.value;
		if (empty(resultlabel)) {
			alert("Result Name is required");
			document.getElementById('resultlabel').focus();
			return false;
		}
		if(!checkDuplicate())
			return false;

		return true;
	}

	var hiddenResultlabelId = '${bean.map.resultlabel_id}';

	function checkDuplicate(){
		var newResultName = trimAll(document.ClinicalDataLabResultsMasterForm.resultlabel.value);
		var resultLabelId = document.ClinicalDataLabResultsMasterForm.resultlabel_id.value;
		if(document.ClinicalDataLabResultsMasterForm._method.value != 'update'){
			for(var i=0;i<resultlabelIds.length;i++){
				item = resultlabelIds[i];
				if(resultLabelId == item.resultlabel_id){
					alert(document.ClinicalDataLabResultsMasterForm.resultlabel.value+" already exists pls enter other name...");
			    	document.ClinicalDataLabResultsMasterForm.resultlabel.value='';
			    	document.ClinicalDataLabResultsMasterForm.resultlabel.focus();
			    	return false;
				}

			}
		}
	 	if(document.forms[0]._method.value == 'update'){
	  		if (backupName != newResultName){
				for(var i=0;i<resultlabelIds.length;i++){
					item = resultlabelIds[i];
					if(resultLabelId == item.resultlabel_id){
						alert(document.forms[0].resultlabel.value+" already exists pls enter other name");
				    	document.ClinicalDataLabResultsMasterForm.resultlabel.focus();
				    	return false;
	  				}
	  			}
	 		}
	 	}
	 	return true;
	}//end of function

	var autocomp = null;

	function resultNameAutoComplete() {
			var datasource = new YAHOO.util.LocalDataSource({result: testResultsAutoComplete});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "result_test_name"},{key : "resultlabel"},{key : "resultlabel_id"},{key : "units"} ]
			};
			autoComp = new YAHOO.widget.AutoComplete('resultlabel','resultContainer', datasource);
			autoComp.minQueryLength = 0;
			autoComp.maxResultsDisplayed = 20;
			autoComp.forceSelection = true ;
			autoComp.animVert = false;
			autoComp.resultTypeList = false;
			autoComp.autoHighlight = false;

			autoComp.itemSelectEvent.subscribe(function() {
				var resultTestNames = document.getElementById("resultlabel").value;
				if(resultTestNames != '') {
					for ( var i=0 ; i< testResultsAutoComplete.length; i++){
						if(resultTestNames == testResultsAutoComplete[i]["result_test_name"]){
							document.getElementById("resultlabel").value = testResultsAutoComplete[i]["result_test_name"];
							document.ClinicalDataLabResultsMasterForm.resultlabel_id.value = testResultsAutoComplete[i]["resultlabel_id"];
							document.getElementById('units').textContent = testResultsAutoComplete[i].units;
							break;
						}
					}
				}else{
					document.getElementById("resultlabel").value = "";
				}
			});

		}

</script>

</head>
<body onload="keepBackUp();">

<form action="ClinicalDataLabResultsMaster.do" method="POST" name="ClinicalDataLabResultsMasterForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="resultlabel_id" value="${bean.map.resultlabel_id}"/>
	<input type="hidden" name="resultlabel_id_update" value="${bean.map.resultlabel_id}"/>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Results </h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Result Name:</td>
				<td>
					<div id="resultlabelId" style="padding-bottom: 1.5em;width=160px;">
						<input type="text" name="resultlabel" id="resultlabel"
							style="width: 155px" value="${bean.map.result_test_name}"/>
						<div id="resultContainer" style="width: 80em;"></div>
					</div>
				</td>
				<td><img class="imgHelpText" src="${cpath}/images/help.png" title="Type a result shortname or result detailedanme or testname to search among existing results.Autocomplete format is Shortname/Detailedname(testname)."/></td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Units:</td>
				<td>
					<label id="units">${bean.map.units}</label>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>

			<tr>
				<td class="formlabel">Display Order:</td>
				<td>
					<input type="text" class="number" name="display_order" id="display_order" value="${bean.map.display_order}"/>
				</td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateForm()"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a href="ClinicalDataLabResultsMaster.do?_method=add" >Add</a></c:if>
		| <a href="javascript:void(0)" onclick="doClose();">Result List</a>
	</div>
</form>

</body>
</html>
