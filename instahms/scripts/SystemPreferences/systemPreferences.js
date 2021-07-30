/*
 * On Submitting the form updating the userName who clicked on it.
 */
function update() {
	if(!validate()){
		return false;
	}else {
		$('#mod_username').val(userid);
	    document.systemPreferencesInsta.submit();
	}
}

function validate() {
    var protocol = $('#protocol').val();
    if(protocol == '') {
        alert("protocol cannot be empty, default smtp");
        return false;
    }
    
    var portNumber = $('#port_number').val();
    if(portNumber == '') {
        alert("Port Number Cannot be empty, default 25");
        return false;
    }
    
    var calendarDate = $('#calendar_start_day').val();
    if(calendarDate == '') {
        alert("calendar date cannot be empty, default 0");
        return false;
    }
    
    var uploadLimit = $('#upload_limit_in_mb').val();
    if(uploadLimit == '') {
        alert("upload limit cannot be empty, default 10");
        return false;
    }
    
    var pharmacySaleMargin = $('#pharmacy_sale_margin_in_per').val();
    if(pharmacySaleMargin == '') {
        alert("Pharmacy Sale Margin cannot be empty, default 0.00");
        return false;
    } 
    
    var daily_checkpoint1 = $('#daily_checkpoint1').val();
    if(daily_checkpoint1 == '') {
        alert("daily checkpoint1 cannot be empty, default 08:00:00");
        return false;
    } 
    
    var daily_checkpoint2 = $('#daily_checkpoint2').val();
    if(daily_checkpoint2 == '') {
        alert("daily checkpoint2 cannot be empty, default 20:00:00");
        return false;
    } 
    
    var cfd_max_count = $('#cfd_max_count').val();
    if(cfd_max_count == '') {
        alert("Cfd Max Count cannot be empty, default 4");
        return false;
    } 
    
    var pacValidityDays = $('#pac_validity_days').val();
    if(pacValidityDays == '') {
        alert("Pac Validity Days cannot be empty, default 30");
        return false;
    }
    
    return true;
}

