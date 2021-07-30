var _font_names = [ "Arial",
					"Arial Unicode MS",
					"Arial Black",
					"Times New Roman",
					"Courier New",
					"Verdana",
					"FreeSans",
					"FreeSerif",
					"FreeMono",
					"Serif",
					"Sans-Serif",
					"Comic Sans MS",
					"Georgia",
					"Impact",
					"Trebuchet MS"
					];

function initAutoComplete(inputElStr, containerElStr, txtBoxvalue){

	YAHOO.example.ACJSAddArray = new function() {
		var datasource = new YAHOO.widget.DS_JSArray(_font_names);
		var fontAutoComplete = new YAHOO.widget.AutoComplete(inputElStr, containerElStr, datasource);
		fontAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		fontAutoComplete.typeAhead = true;
		// Enable a drop-shadow under the container element
		fontAutoComplete.useShadow = true;
		// Disable the browser's built-in autocomplete caching mechanism
		fontAutoComplete.allowBrowserAutocomplete = false;
		// Require user to type at least 0 characters before triggering a query
		fontAutoComplete.minQueryLength = 0;
		//commas and/or spaces may delimited queries
		//autoComp.delimChar = [];
		// Display up to 20 results in the container
		fontAutoComplete.maxResultsDisplayed = 20;
		// Do not automatically highlight the first result item in the container
		fontAutoComplete.autoHighlight = false;
		// disable force selection,user can type his/her own complaint(which is not there in master)
		if (txtBoxvalue) {
			document.getElementById(inputElStr).value = txtBoxvalue;
		}
	}
}

function doClose() {
	window.location.href = "${cpath}/master/PrinterDefinition.do?method=list";
}
function focus(){
	document.forms[0].printer_definition_name.focus();
}

function enableDisableFields(){
	var objects = document.forms[0].print_mode;
	var feedObject = document.forms[0].continuous_feed;
	for(var i=0;i<objects.length; i++){
		var fieldValue = objects[i].value;
		if(objects[i].checked==true){
			if(fieldValue == 'P'){
				document.forms[0].text_mode_column.disabled=true;
				document.forms[0].page_width.disabled=false;
			}else if (fieldValue == 'T'){
				document.forms[0].text_mode_column.disabled = false;
			}
		}
	}

	for(var j=0;j<feedObject.length;j++){
		var feedValues = feedObject[j].value;
		if(feedObject[j].checked==true){
			if(feedValues == 'Y'){
				document.forms[0].page_height.disabled = true;
			} else  {
				document.forms[0].page_height.disabled = false;
			}
		}
	}
	showPageNumberOptions();
}


function validateFields(){
	var printerName=document.forms[0].printer_definition_name.value;
	var pageHeight = document.forms[0].page_height.value;
	var topMargin = document.forms[0].top_margin.value;
	var bottomMargin = document.forms[0].bottom_margin.value;
	var leftMargin = document.forms[0].left_margin.value;
	var rightMargin = document.forms[0].right_margin.value;
	var pageWidth = document.forms[0].page_width.value;
	var height = parseInt(topMargin) + parseInt(bottomMargin) + 1;
	var width = parseInt(leftMargin) + parseInt(rightMargin) + 1;

	if(printerName == ''){
		alert("Enter Printer Name");
		return false;
	}
	var feedObject = document.forms[0].continuous_feed;
	for(var j=0;j<feedObject.length;j++){
		var feedValues = feedObject[j].value;
		if(feedObject[j].checked==true){
		  if(feedValues == 'N'){
				if(pageHeight <height){
					alert("Page Height should be more than top margin + bottom margin");
					return false;
				}
		  }
		}
	}

	var objects = document.forms[0].print_mode;
	for(var i=0;i<objects.length; i++){
		var fieldValue = objects[i].value;
		if(objects[i].checked==true){
			if(fieldValue == 'P'){
				if(pageWidth < width) {
					alert("Page Width should be more than left margin + right margin");
					return false;
				}
			}
		}
	}

	return true;
}

function showPageNumberOptions(){

	var page_number_op = document.forms[0].page_number;
	var page_number = '';

	for (var i=0; i<page_number_op.length; i++) {
		if (page_number_op[i].checked) {
			page_number = page_number_op[i].value;
		}
	}

	if (page_number == 'Y') {
		document.getElementById('pg_position').style.display = 'table-row';
		document.getElementById('pg_no_style').style.display = 'table-row';
		document.getElementById('pg_vertical_position').style.display = 'table-row';
	} else {
		document.getElementById('pg_position').style.display = 'none';
		document.getElementById('pg_no_style').style.display = 'none';
		document.getElementById('pg_vertical_position').style.display = 'none';
	}
}


