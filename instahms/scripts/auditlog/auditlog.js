var fieldLookup = {};
var searchForm = null;
var searchParamsUrl = null;
var selectedField = null;
//var oldValue = null;
//var newValue = null;

/*
Event Handler for the table selection
*/
function onChangeTable() {
    var table_name = searchForm.al_table.value;
    selectedField = "";
    loadFields(table_name);
}

function loadFields(table_name) {
    if (table_name != "") {
		var searchUrl = searchParamsUrl + "&al_table=" + table_name;
        var ajaxReqObject = newXMLHttpRequest();
        getResponseHandlerText(ajaxReqObject, fieldDataHandler, searchUrl);
    }
}
/*
Ajax response handler for receiving the field and value details for the selected table
*/

function fieldDataHandler(responseText) {

    /* The responseText is a json string and looks like this for a given table:
    {
      fieldList: 	[
      					{ name: "claim_status", display_name: "Claim Status"},
      			  		{ name: "approval_amount", display_name: "Approval Amount" },...
      			  	]

      fieldValueMap:{
	      				claim_status: [	{id:"O", value:"Open"},
      									{id: "S", value:"Submitted",
      									{""},...],
                		status: [	{id:"O", value="Open"},
                					{id:"X", value:"Cancelled"} ...],
              			}
    }
    */

	eval("var fieldData = " + responseText);
	fieldLookup = fieldData.fieldValueMap;
	populateFieldList(fieldData.fieldList);
	setSelectedField();
}

function populateFieldList(fieldList) {
	if (!empty(fieldList)) {
		var fields = searchForm.field_name;
		fields.length = fieldList.length+1;

		for(var i=0; i<fieldList.length; i++) {
			var option = new Option(fieldList[i].display_name,fieldList[i].name);
			fields[i+1] = option;
		}
	}
}

/*
Autocomplete function that returns an array of field values from the json data
*/

function getFieldValueArray () {

	var currentField = searchForm.field_name.value;
	var fieldValueList = [];

	if (!empty(currentField)) {
		if (!empty (fieldLookup)) {
			fieldValueList = fieldLookup[currentField];
		}
	}
	return {result : fieldValueList};
}

/*
Function to create an autocomplete widget
*/
function createAutoCompleteInput(valueInput, valueDropDown, valueHidden) {

	var datasource = new YAHOO.util.FunctionDataSource(getFieldValueArray);
	datasource.responseType = YAHOO.util.FunctionDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : 'result',
		fields : [ 	{key : 'VALUE'},
					{key : 'ID'}
				 ]
	};

	this.oAutoComp = new YAHOO.widget.AutoComplete(valueInput, valueDropDown, datasource);
	this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	this.oAutoComp.useShadow = true;
	this.oAutoComp.minQueryLength = 0;
	this.oAutoComp.autoHighlight = false;
	this.oAutoComp.allowBrowserAutocomplete = false;
	this.oAutoComp.forceSelection = true;
	this.oAutoComp.resultTypeList = false;
	this.oAutoComp.maxResultsDisplayed = 200;
	this.oAutoComp.applyLocalFilter = true;
	this.oAutoComp.queryMatchContains = true;

	this.oAutoComp.itemSelectEvent.subscribe(function(e, args, data) {
		// args is an array containing the following :
		// oSelf, elItem, oData

		searchForm[valueInput].value = args[2].VALUE;
		searchForm[valueHidden].value = args[2].ID;
	});

	this.oAutoComp.selectionEnforceEvent.subscribe(function(e, args, data) {
		searchForm[valueHidden].value = "";
	});

}

/*
Function to reset values on de-selection / reload
*/
function clearValueFields() {
	searchForm._old_value_input.value = '';
	searchForm.old_value.value = '';
	searchForm._new_value_input.value = '';
	searchForm.new_value.value = '';
	setValueFieldState();
}

function setValueFieldState() {
	var valueList  = getFieldValueArray();
	// valueList is of the form {result : []}

	if (!empty(valueList) && !empty(valueList.result))  {
		searchForm._old_value_input.disabled = false;
		searchForm._new_value_input.disabled = false;
	} else {
		searchForm._old_value_input.disabled = true;
		searchForm._new_value_input.disabled = true;
	}
}

/*
Event handler for selection of a field
*/

function onChangeField() {
	clearValueFields();
}

/*
Page initialization function - pre-creates the autocomplete and the toolbar
*/
function init(form, urlPrefix) {

	searchForm = form;
	searchParamsUrl = cpath + '/' + urlPrefix + '/auditlog/AuditLogSearch.do?_method=getSearchParams';
	selectedField = searchForm._selected_field.value;

	var selected_table = searchForm.al_table.value;
	if (selected_table != "") {
		loadFields(selected_table);
	}

	createAutoCompleteInput('_old_value_input', '_old_value_dropdown', 'old_value');
	createAutoCompleteInput('_new_value_input', '_new_value_dropdown', 'new_value');
	createToolbar(toolbar);
}

function setSelectedField() {
	if (!empty(selectedField)) {
		setDropdownIndex(searchForm.field_name, selectedField);
	}
	setSelectedValues();
	return;
}

function setSelectedValues() {

	if (empty(selectedField)) {
		searchForm.old_value.value = "";
		searchForm.new_value.value = "";
		searchForm._old_value_input.value = "";
		searchForm._new_value_input.value = "";
	} else {

		var fieldValues = fieldLookup[selectedField];

		if (null != fieldValues) {
			for (var i = 0; i < fieldValues.length; i++) {
				if (null != fieldValues[i]) {
					if (fieldValues[i].ID == searchForm.old_value.value) {
						searchForm._old_value_input.value = fieldValues[i].VALUE;
					}
					if (fieldValues[i].ID == searchForm.new_value.value) {
						searchForm._new_value_input.value = fieldValues[i].VALUE;
					}
				}
			}
		}
	}
	setValueFieldState();
	return;
}

/*
 * Set the selected index in a dropdown based on the value supplied,
 * this will calculated the index by going through available values in the dropdown.
 */
function setDropdownIndex(opt, set_value) {
	var index=0;
	for(var i=0; i<opt.options.length; i++) {
		var opt_value = opt.options[i].value;
		if (opt_value == set_value) {
			opt.options[i].selected = true;
			return;
		}
	}
}//end of setSelectedIndex
