function init(){
   var codetype = document.getElementById('code_type').value;
   if(codetype != null && codetype != ''){
       document.getElementById("codeSearch").style.display = '';
   }
   autoCodeSearch();
}
var rAutoComp;
function autoCodeSearch() {
     var datasource = new YAHOO.util.LocalDataSource({result: codeSearch});
     datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
     datasource.responseSchema = {
		resultsList : "result",
		fields : [
			{key : "code"},
			{key : "code_desc"},
			{key : "code_type"},
			{key : "status"}],
		numMatchFields: 2
	};
	var rAutoComp = new YAHOO.widget.AutoComplete('searchCode','codescontainer', datasource);
	rAutoComp.minQueryLength = 0;
	rAutoComp.maxResultsDisplayed = 20;
	rAutoComp.forceSelection = false ;
	rAutoComp.animVert = false;
	rAutoComp.resultTypeList = false;
	rAutoComp.typeAhead = false;
	rAutoComp.allowBroserAutocomplete = false;
	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	rAutoComp.autoHighlight = true;
	rAutoComp.useShadow = false;
	rAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	rAutoComp.formatResult = function(codeSearch) {
	     var diagList = codeSearch;
	     //var details = highlight(diagList.code);
	     return diagList.code + " / (<span class='additional-info'>" +
			diagList.code_desc + "</span>)" ;
	}
	if (rAutoComp._elTextbox.value != '') {
		rAutoComp._bItemSelected = true;
		rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
	}
}
function getCodeDetails(){
  var code = document.getElementById('searchCode').value
  var codetype = document.getElementById('code_type').value;
  var doctor = document.diagnosiscodeform.doctor_id.value;
  var fav = document.diagnosiscodeform.favourite.value;
  document.diagnosiscodeform.action = 'DiagnosisCodeFavourites.do?_method=list&doctor_id='+doctor+'&code='+codetype+'&fav='+fav+'&searchCode='+code;
  document.diagnosiscodeform.submit();
}
function onChange(obj){
  var codetype = document.getElementById('code_type').value;
  var doctor = document.diagnosiscodeform.doctor_id.value;
  var fav = '';
  document.diagnosiscodeform.action = 'DiagnosisCodeFavourites.do?_method=list&doctor_id='+doctor+'&code='+codetype+'&fav='+fav;
  document.diagnosiscodeform.submit();
}
function showFavourites(obj) {
  var codetype = document.getElementById('code_type').value;
  var doctor = document.diagnosiscodeform.doctor_id.value;
   if(obj.checked){
    var fav = obj.value;
      document.diagnosiscodeform.action = 'DiagnosisCodeFavourites.do?_method=list&doctor_id='+doctor+'&code='+codetype+'&fav='+fav+'&pageNum='+1;
      document.diagnosiscodeform.submit();
   }
}

function doUpload() {
	var form = document.uploadICDFavform;
	if (form.xlsICDFile.value == "") {
		showMessage("js.patient.doctor.diagnosisfavourites.select.file");
		return false;
	}
	form.action='DiagnosisCodeFavourites.do?_method=importICDFavDetailsFromXls&doctor_id='+doctor;
	form.submit();
}
