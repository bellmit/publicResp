function toggle_visibility(id) {
    var e = document.getElementById("SponsorReceiptsForm");
 	var e_getDetails = document.getElementById("getDetails");
    if ( id ){
        e.style.display = 'block';
        e_getDetails.disabled = true;
    }
    else{
        e.style.display = 'none';
    }
}

function validate(){
	if(document.sponsorReceiptStatusForm.sponsor_id.value == ''){
		alert("Please select sponsor.");
		document.sponsorReceiptStatusForm.sponsor_id.focus();
		return false;
	}
	fromdate = document.sponsorReceiptStatusForm.open_date0.value;
    todate = document.sponsorReceiptStatusForm.open_date1.value;
	if(fromdate != '' && todate != ''){
        if(getDateDiff(fromdate,todate) == -1) {
          alert("To date Should be greater than From date");
          document.sponsorReceiptStatusForm.open_date1.value = '';
          document.sponsorReceiptStatusForm.open_date1.focus();
          return false;
        }
	}
	if (!doValidateDateField(document.getElementById("open_date0"),'past')) {
		document.getElementById("open_date0").value="";
		return false;
	}
	if (!doValidateDateField(document.getElementById("open_date1"),'past')) {
		document.getElementById("open_date1").value="";
		return false;
	}
	document.sponsorReceiptStatusForm.submit();
}
function enterNumOnlyzeroToNine(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharNumber(c) );
}
function isCharNumber(c) {
	return ( (c >=48) && (c <= 57) );
}

function isCharControl(c) {
	return (c<32);
}
function getEventChar(e) {
	e = (e) ? e : event;
	var charCode;
	if (e.charCode != null) {
		if (e.altKey || e.ctrlKey)
			return 0;
		return e.charCode;
	} else if (e.which != null) {
		if (e.altKey || e.ctrlKey)
			return 0;
		return e.which;
	} else {
		return e.keyCode;
	}
}