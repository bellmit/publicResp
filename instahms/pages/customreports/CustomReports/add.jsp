<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" isELIgnored="false"%>

<html>
<head>
<title>Add Custom Report - Insta HMS</title>
<script>
function addTableRow() {
  var countNode = document.getElementById("count");
  var count = parseInt(countNode.value) + 1;

  var row = new YAHOO.util.Element(document.createElement('tr'));
  row.set('id', 'row'+count);

  var td1 = document.createElement('td');
  var td4 = document.createElement('td');
  var td2 = document.createElement('td');
  var td3 = document.createElement('td');
  row.appendChild(td1);
  row.appendChild(td4);
  row.appendChild(td2);
  row.appendChild(td3);

  var var_input = document.createElement('input');
  var varelem = new YAHOO.util.Element(var_input);
  varelem.set('name','report_var'+count);
  varelem.set('type','text');
  varelem.addClass('validate-alphanum');
  td1.appendChild(var_input);

  var var_label_input = document.createElement('input');
  var varlabelelem = new YAHOO.util.Element(var_label_input);
  varlabelelem.set('name','report_var_label'+count);
  varlabelelem.set('type','text');
  td4.appendChild(var_label_input);

  var sel = document.createElement('select');
	sel.name='report_var_type'+count;
  var opt1 = document.createElement('option');
  opt1.appendChild(document.createTextNode('Text'));
  var opt1elem = new YAHOO.util.Element(opt1);
  opt1elem.set('value','T');
  var opt2 = document.createElement('option');
  opt2.appendChild(document.createTextNode('Date'));
  var opt2elem = new YAHOO.util.Element(opt2);
  opt2elem.set('value','D');
  sel.appendChild(opt1);
  sel.appendChild(opt2);
	td2.appendChild(sel);

  var addbtn = document.getElementById('addbtn' + (count -1));
	var newaddbtn = addbtn.cloneNode(true);
  var addelem = new YAHOO.util.Element(newaddbtn);
  addelem.set('id', 'addbtn'+count);
  var delbtn = document.getElementById('delbtn' + (count -1));
  var newdelbtn = delbtn.cloneNode(true);
  var delelem = new YAHOO.util.Element(newdelbtn);
  delelem.set('id', 'delbtn'+count);

  td3.appendChild(newaddbtn);
  td3.appendChild(newdelbtn);

  var table = new YAHOO.util.Element('variables');
  table.appendChild(row);

  var add = new YAHOO.util.Element(addbtn);
  add.setStyle('display','none');
  var del = new YAHOO.util.Element(delbtn);
  del.setStyle('display','none');
  countNode.value = count;
}
function delTableRow() {
	var countNode = document.getElementById("count");
  var count = parseInt(countNode.value);

  if (count == 1) {
		return;
	}

	var row = document.getElementById("row"+count);
	row.parentNode.removeChild(row);
  count = (count -1);

  var addbtn = document.getElementById('addbtn' + count);
  var add = new YAHOO.util.Element(addbtn);
  add.setStyle('display','');
  var delbtn = document.getElementById('delbtn' + count);
  var del = new YAHOO.util.Element(delbtn);
  del.setStyle('display','');

  countNode.value = count;
}

function typeChange(e) {
	var selected = getSelText(e);
	var subreports = document.getElementById('subreports');
	var repParams = new YAHOO.util.Element(document.getElementById('repParams'));
	var subReports = new YAHOO.util.Element(subreports);
	var csvSupported = document.getElementById('ftl_csv_supported');
	var metadata = new YAHOO.util.Element(document.getElementById('metadata_row'));
	var viewName = new YAHOO.util.Element(document.getElementById('csv_name'));

	// view name required for csv, metadata for the others
	if (selected == 'csv') {
		viewName.setStyle('display','');
		metadata.setStyle('display','none');
	} else {
		viewName.setStyle('display','none');
		metadata.setStyle('display','');
	}

	// params required for csv, jrxml and ftl
	if (selected == 'csv' || selected == 'jrxml' || selected == 'ftl')
		repParams.setStyle('display','');
	else
		repParams.setStyle('display','none');

	// subReports required for jrxml only
	if (selected == 'jrxml')
		subReports.setStyle('display','');
	else
		subReports.setStyle('display','none');

	// CSV Supported Yes/No required for FTL only
	if (selected == 'ftl')
		csvSupported.disabled = false;
	else
		csvSupported.disabled = true;

}

var allreportNames = ${allReportNamesJSON};

function onSubmitCheck() {
	for(var i=0; i<allreportNames.length; i++){
		var report = allreportNames[i];
		if(report.report_name.trim().toUpperCase() == (document.getElementById('report_name').value).trim().toUpperCase() ){
			alert("A custom report of the same name already exists... Please try another name");
			return false;
		}
	}

	if(document.forms[0].report_metadata.value==null || document.forms[0].report_metadata.value== ""){
		if(document.getElementById('report_type').value != 'csv') {
			alert("Please input the Report Metadata File...");
			return false;
		}
	}
	return true;
}
</script>
</head>
<body>

<form method="POST" action="AddCustomReport.do" enctype="multipart/form-data" onSubmit="return onSubmitCheck()">
<input type="hidden" name="method" value="create">
<div class="pageHeader">Add Custom Report</div>
<div style="text-align:center"><insta:feedback-panel/></div>
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Report Details</legend>
	<table class="formtable" width="100%">
		<tr>
			<td>Report Name:</td>
			<td><input type="text" name="report_name" id ="report_name" value="" class="required validate-length" length="255" ></td>
		</tr>
		<tr>
			<td>Report Description:</td>
			<td><textarea name="report_desc" cols="33" rows="2"  class="validate-length" length="1024"></textarea></td>
		</tr>
		<tr>
			<td>Report Type:</td>
			<td><insta:selectoptions name="report_type" id="report_type" value="jrxml" opvalues="jrxml,csv,srjs,ftl" optexts="jrxml,csv,srjs,ftl" onchange="typeChange(this)" /></td>
		</tr>
		<tr>
			<td>CSV Supported:</td>
			<td><insta:selectoptions name="ftl_csv_supported" id="ftl_csv_supported" value="N" opvalues="Y,N" optexts="Yes,No" disabled="true"/></td>
		</tr>
		<tr id="metadata_row">
			<td>Report Metadata File:</td>
			<td><input type="file" name="report_metadata" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
		</tr>
		<tr id="csv_name" style="display:none">
			<td>CSV View Name:</td>
			<td><input type="text" name="csv_view_name"  value=""/></td>
		</tr>
	</table>
</fieldset>

<fieldset class="fieldSetBorder" id="repParams" >
	<legend class="fieldSetLabel">Report Parameters</legend>
  <input type="hidden" id="count" name="var_count" value="1"/>
	<table class="dashboard" >
    <tbody id="variables">
		<tr>
			<th>Variable</th>
			<th>Display Label</th>
			<th>Type</th>
			<th>&nbsp;</th>
		</tr>
		<tr id="row1">
			<td><input name="report_var1" type="text" class="validate-alphanum"/></td>
			<td><input name="report_var_label1" type="text" /></td>
			<td>
				<select name="report_var_type1">
					<option value="T">Text</option>
					<option value="D">Date</option>
				</select>
			</td>
			<td>
				<input id="addbtn1" type="button" onclick="return addTableRow();" value="+" />
				<input id="delbtn1" type="button" onclick="return delTableRow();" value="-" />
			</td>
		</tr>
 		</tbody>
  </table>
</fieldset>
<fieldset id="subreports" class="fieldSetBorder">
        <legend class="fieldSetLabel">Sub Reports</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Sub Report 1:</td>
			<td><input type="file" name="subreport1" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
			<td class="formlabel">Sub Report 2:</td>
			<td><input type="file" name="subreport2" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
		</tr>
		<tr>
			<td class="formlabel">Sub Report 3:</td>
			<td><input type="file" name="subreport3" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
			<td class="formlabel">Sub Report 4:</td>
			<td><input type="file" name="subreport4" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
		</tr>
		<tr>
			<td class="formlabel">Sub Report 5:</td>
			<td><input type="file" name="subreport5" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
			<td class="formlabel">Sub Report 6:</td>
			<td><input type="file" name="subreport6" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
		</tr>
		<tr>
			<td class="formlabel">Sub Report 7:</td>
			<td><input type="file" name="subreport7" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
			<td class="formlabel">Sub Report 8:</td>
			<td><input type="file" name="subreport8" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
		</tr>
		<tr>
			<td class="formlabel">Sub Report 9:</td>
			<td><input type="file" name="subreport9" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
			<td class="formlabel">Sub Report 10:</td>
			<td><input type="file" name="subreport10" accept="<insta:ltext key="upload.accept.report_template"/>,<insta:ltext key="upload.accept.print_template"/>"/></td>
		</tr>
	</table>
</fieldset>
<div>
	<input type="submit" value="Save" onclick="return onSubmitCheck()"/>
	<insta:screenlink screenId="custom_rpt_list" extraParam="?method=list" label="Custom Report List"
		addPipe="true"/>

</div>
</form>
</body>
</html>

