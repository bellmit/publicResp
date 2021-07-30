function showMore(divMoreId) {

	var saveSearch = document.getElementById('_save_search');
	var divMore = document.getElementById(divMoreId);

	if (divMore.style.display == 'none') {
		// show it
		show(divMore, true);
		document.getElementById('aMore').innerHTML = getString("js.common.search.less.options");
		if (saveSearch) {
			show(saveSearch, true);
		}
	} else {
		// hide it
		show(divMore, false);
		document.getElementById('aMore').innerHTML = getString("js.common.search.more.options");
		if (saveSearch) {
			show(saveSearch, false);
			showSaveInputs(false);
		}
	}

}

function showSaveInputs(doShow) {
	show(document.getElementById('_save_inputs'), doShow);
}

function toggleShow(obj) {
	obj.style.display = obj.style.display == 'block' ? 'none' : 'block'
}

function show(obj, doShow) {
	if ((doShow == null) || doShow)
		obj.style.display = 'block';
	else
		obj.style.display = 'none';
}

function saveSearch(form) {
	var mysearches = form._mysearch;
	var search_name = form._search_name.value;
	if (empty(search_name))  {
		showMessage("js.common.message.search.name.prompt");
		document.getElementById('_search_name').focus();
		return false;
	}
	for (var i=0; i<mysearches.options.length; i++) {
		var savedsearch = mysearches.options[i].text;
		if (trim(savedsearch.toUpperCase()) == trim(search_name.toUpperCase())) {
			showMessage("js.common.message.search.name.duplicate");
			return false;
		}
	}
	form.action = cpath + "/SearchAction.do";
	form._method.value = 'saveSearch';
	form.submit();
}

function onSearchChange(value, form) {
	if (value == 'nosearch') {
		return false;
	}
	form.action = cpath + "/SearchAction.do";
	form._method.value = 'getMySearch';
	form.submit();
	return true;
}

function isFilterActive(form) {
	var Els = form.elements;
	for (var i in Els) {
		var el = Els[i];
		var type = el.type ? (el.type.toString()).toLowerCase() : '';
		if (type == 'text' || type == 'textarea') {
			if (el.value != '')
				return true;
		}
		if ((type == 'checkbox') && (el.getAttribute("isallcheckbox") == null) && el.checked) {
			debug("checkbox " + el.isallcheckbox + " " + el.checked);
			return true;
		}
		if ((type == 'radio') && el.checked)
			return true;
		if (type == 'select-one' && el.options.selectedIndex > 0)
			return true;
		if (type =='select-multiple' && el.options.selectedIndex >= 0)
			return true;
	}
	debug("Filter is not active");
	return false;
}

function showFilterActive(form) {
	if (isFilterActive(form)) {
		show(document.getElementById('_filters_active'), true);
	} else {
		show(document.getElementById('_filters_active'), false);
	}
}

function initMrNoAutoComplete(cpath) {
	Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
}

function initLocalAutoComplete(field, dataMap, chooseFields) {
	var container = field + "_container";
	var sAutoComp = null;
	if (null != dataMap) {
		var dataList = dataMap[field];
		var datasource = new YAHOO.util.LocalDataSource({result: dataList});
		datasource.responseType = YAHOO.util.DataSourceBase.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : "result",
			fields : chooseFields
		};
		sAutoComp = new YAHOO.widget.AutoComplete(field, container, datasource);
		sAutoComp.minQueryLength = 0;
	 	sAutoComp.maxResultsDisplayed = 20;
	 	sAutoComp.forceSelection = false ;
	 	sAutoComp.animVert = false;
	 	sAutoComp.resultTypeList = false;
	 	sAutoComp.typeAhead = false;
	 	sAutoComp.allowBroserAutocomplete = false;
	 	sAutoComp.prehighlightClassname = "yui-ac-prehighlight";
		sAutoComp.autoHighlight = true;
		sAutoComp.useShadow = false;
	 	if (sAutoComp._elTextbox.value != '') {
				sAutoComp._bItemSelected = true;
				sAutoComp._sInitInputValue = sAutoComp._elTextbox.value;
		}
	}
 	return sAutoComp;
}

function showReportStatusFTUE() {
	var isPlayTour = localStorage.getItem(userid+"_reportStatusTour");
	if(!isPlayTour) {
	$('.reportStatus').addClass('step-1');
    $('#report-tour').mytour({
      start: 0,
      buttons: {
        next: 'NEXT',
        prev: 'PREV',
        firstNextText: 'MORE',
        lastNextText: 'OK',
        finish: '<img src="'+cpath+'/images/close-button.png" class="close-button"/>'
      },
      autoPlay: false,
      timer: 5000,
      steps: '#report-tour-steps',
      stepHolder: 'li',
      onStart: function() {
        $('#report-tour').click();
      },        
      tooltipPositionOffset : {
        nubLeft : [17],
        tooltipLeft : [140]
      },
      onShow: null,
      beforePlay: null,
      afterPlay: null,
      onFinish: function() {
        $('.reportStatus').removeClass('step-1');
        localStorage.setItem(userid+"_reportStatusTour", "1");
      },  
      debug: false
    });
	}
}

function showPatientTestReportStatusFTUE() {
	var isPlayTour = localStorage.getItem(userid+"_patientTestReportStatusTour");
	if(!isPlayTour) {
	$('.reportStatus').addClass('step-1');
    $('#report-tour').mytour({
      start: 0,
      buttons: {
        next: 'NEXT',
        prev: 'PREV',
        firstNextText: 'MORE',
        lastNextText: 'OK',
        finish: '<img src="'+cpath+'/images/close-button.png" class="close-button"/>'
      },
      autoPlay: false,
      timer: 5000,
      steps: '#report-tour-steps',
      stepHolder: 'li',
      onStart: function() {
        $('#report-tour').click();
      },        
      tooltipPositionOffset : {
        nubLeft : [17],
        tooltipLeft : [140]
      },
      onShow: null,
      beforePlay: null,
      afterPlay: null,
      onFinish: function() {
        $('.reportStatus').removeClass('step-1');
        localStorage.setItem(userid+"_patientTestReportStatusTour", "1");
      },  
      debug: false
    });
	}
}
var referalArray = [];
var prescribedDocArray = [];
function loadReferals() {
	if (referalsJSON != null) {
		referalArray.length = referalsJSON.length;
		for ( i=0 ; i< referalsJSON.length; i++){
			var item = referalsJSON[i];
				referalArray[i] = item["REF_NAME"];
		}
	}
}

function loadPrescribedDoctors(){
if(prescribedDocJSON != null){
    	    prescribedDocArray.length = prescribedDocJSON.length;
    	    for(j=0; j< prescribedDocJSON.length; j++){
    	        var item1 = prescribedDocJSON[j];
    	            prescribedDocArray[j] = item1["DOCTOR_NAME"];
    	    }
    }
}

function referalAutoComplete(refId, refName, refContainer){

	YAHOO.example.ACJSAddArray = new function() {
		datasource = new YAHOO.widget.DS_JSArray(referalArray);
		var autoComp = new YAHOO.widget.AutoComplete(refName, refContainer ,datasource);

		autoComp.formatResult = Insta.autoHighlight;
		autoComp.prehighlightClassName = "yui-ac-prehighlight";
		autoComp.typeAhead = false;
		autoComp.useShadow = false;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.queryMatchContains = true;
		autoComp.minQueryLength = 0;
		autoComp.maxResultsDisplayed = 20;
		autoComp.forceSelection = true;
		autoComp._bItemSelected = true;

		autoComp.textboxBlurEvent.subscribe(function() {
			var referralName = YAHOO.util.Dom.get(refName).value;
			if(referralName == '') {
				YAHOO.util.Dom.get(refId).value = '';
			}
		});

		autoComp.itemSelectEvent.subscribe(function() {
			var referralName = YAHOO.util.Dom.get(refName).value;
			if(referralName != '') {
				for ( var i=0 ; i< referalsJSON.length; i++){
						if(referralName == referalsJSON[i]["REF_NAME"]){
							YAHOO.util.Dom.get(refId).value = referalsJSON[i]["REF_ID"];
							YAHOO.util.Dom.get(refName).value = referalsJSON[i]["REF_NAME"];
					}
				}
			}else{
				YAHOO.util.Dom.get(refId).value = '';
			}
		});
	 }
}

function prescribedDoAutoComplete(refId, refName, refContainer){

	YAHOO.example.ACJSAddArray = new function() {
		datasource = new YAHOO.widget.DS_JSArray(prescribedDocArray);
		var autoComp = new YAHOO.widget.AutoComplete(refName, refContainer ,datasource);

		autoComp.formatResult = Insta.autoHighlight;
		autoComp.prehighlightClassName = "yui-ac-prehighlight";
		autoComp.typeAhead = false;
		autoComp.useShadow = false;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.queryMatchContains = true;
		autoComp.minQueryLength = 0;
		autoComp.maxResultsDisplayed = 20;
		autoComp.forceSelection = true;
		autoComp._bItemSelected = true;

		autoComp.textboxBlurEvent.subscribe(function() {
			var referralName = YAHOO.util.Dom.get(refName).value;
			if(referralName == '') {
				YAHOO.util.Dom.get(refId).value = '';
			}
		});

		autoComp.itemSelectEvent.subscribe(function() {
			var referralName = YAHOO.util.Dom.get(refName).value;
			if(referralName != '') {
				for ( var i=0 ; i< prescribedDocJSON.length; i++){
						if(referralName == prescribedDocJSON[i]["DOCTOR_NAME"]){
							YAHOO.util.Dom.get(refId).value = prescribedDocJSON[i]["PRES_DOCTOR"];
							YAHOO.util.Dom.get(refName).value = prescribedDocJSON[i]["DOCTOR_NAME"];
					}
				}
			}else{
				YAHOO.util.Dom.get(refId).value = '';
			}
		});
	 }
}
