
		function enablePatientType() {
			var disabled = document.forms[0].patientAll.checked;

			document.forms[0].patientIp.disabled = disabled;
			document.forms[0].patientOp.disabled = disabled;

			if(disabled){
				document.forms[0].patientIp.checked = !disabled;
				document.forms[0].patientOp.checked = !disabled;
			}

		}

		function clearSearch() {
			var theForm = document.forms[0]
			theForm.patientAll.checked = true;
			theForm.statusAll.checked = true;
			theForm.mrNo.value = "";
			theForm.fdate.value = "";
			theForm.tdate.value = "";
			enablePatientType();
			enableStatus();
		}

		function init() {
			var checkAll = false;
			checkAll = checkAll || document.forms[0].patientIp.checked;
			checkAll = checkAll || document.forms[0].patientOp.checked;
			if(!checkAll)
				document.forms[0].patientAll.checked = true;

			getPatients();
		}


		function enableStatus(){

			var disabled = document.forms[0].statusAll.checked;

			document.forms[0].statusInActive.disabled = disabled;
			document.forms[0].statusActive.disabled = disabled;

			if(disabled){
				document.forms[0].statusInActive.checked = !disabled;
				document.forms[0].statusActive.checked = !disabled;
			}
		}

		function getPatients(){

			var status = false;
			status = status || document.forms[0].statusInActive.checked;
			status = status || document.forms[0].statusActive.checked;
			if (!status)
				document.forms[0].statusAll.checked = true;

		}

		function onKeyPressMrno(e) {
			if (isEventEnterOrTab(e)) {
				return onChangeMrno();
			} else {
				return true;
			}
		}

		function onChangeMrno() {
			var mrnoBox = document.forms[0].mrNo;

			// complete
			var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

			if (!valid) {
				alert("Invalid MR No. Format");
				document.forms[0].mrNo.value = ""
				document.forms[0].mrNo.focus();
				return false;
			}
		}

function getNextPage(startPage,endPage){
	var form  = document.forms[0];
	form.startPage.value = parseInt(endPage) + 1 ;
	form.endPage.value = parseInt(endPage) + 10;
	form.pageNum.value = parseInt(endPage) + 1 ;
	document.forms[0].submit();
}

function getPrevPage(startPage,endPage){
	var form  = document.forms[0];
	form.startPage.value = parseInt(startPage) - 10;
	form.endPage.value = parseInt(form.startPage.value) + 9;
	form.pageNum.value = parseInt(startPage) - 10;
	document.forms[0].submit();
}
function checkPageNum( pageno ){
		document.forms[0].pageNum.value = pageno;
		document.forms[0].submit();
}

function getReport(){
	var pall=document.forms[0].patientAll.checked;
	var pip = document.forms[0].patientIp.checked;
	var pop = document.forms[0].patientOp.checked;


	var sall = document.forms[0].statusAll.checked;
	var sinactive = document.forms[0].statusInActive.checked;
	var sactive = document.forms[0].statusActive.checked;
	var fromdate = document.forms[0].fdate.value;
	var todate = document.forms[0].tdate.value;

	var mrNo = document.forms[0].mrNo.value;
	var fdate = document.forms[0].fdate.value;
	var tdate = document.forms[0].tdate.value;
	dateValidation();
	window.open("patientdues.do?method=getReport&pall="+pall+"&pip="+pip+"&pop="+pop
		+"&sall="+sall+"&sinactive="+sinactive+"&sactive="+sactive+"&mrNo="+mrNo+"&fdate="+fdate+"&tdate="+tdate);
}

function resetPageValues(){
	document.forms[0].startPage.value="";
	document.forms[0].endPage.value="";
	document.forms[0].pageNum.value="";
   if (!dateValidation()) {
	   	return false;
   }
}


function dateValidation(){

	var fdate = document.getElementById("fdate").value;
	var tdate = document.getElementById("tdate").value;
	if (fdate != "" || tdate != ""){
		var msg = validateDateStr(document.getElementById("fdate").value,"past");
		if (msg == null){
		}else{
			alert("From "+msg);
			return false;
		}

		var msg = validateDateStr(document.getElementById("tdate").value,"past");
		if (msg == null){
		}else{
			alert("To "+msg);
			return false;
		}


		if (getDateDiff(document.getElementById("fdate").value,document.getElementById("tdate").value)<0){
			alert("From date should not greater than Todate");
			return false;
		}
	}

return true;
}
