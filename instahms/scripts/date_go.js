//Created New File after 8311,which is deleted in revision 8266

var weekend = [0,6];
var weekendColor = "#e0e0e0";
var fontface = "Arial";
var fontsize = 1;
var gNow= new Date();
var gServerInitEpochTime = null;
var gLocalInitEpochTime = null;

gMonthShortNames = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];

function setServerInitEpochTime(t) {
	gServerInitEpochTime = t;
	var localInitTime = new Date();
	gLocalInitEpochTime = localInitTime.getTime();
}

function getServerTime() {

	/*
	 * if we were told what the server time was at the beginning, we do easy
	 * calculation based on what the local time was at the beginning and "now".
	 */
	 if (gServerInitEpochTime) {
		var now = new Date();
		now.setTime(now.getTime() + (gServerInitEpochTime - gLocalInitEpochTime));
		return now;
	} else {
		/*
		 * We were not told what the initial time was. We get the server time "now"
		 * using XHR
		 */
		if(window.XMLHttpRequest) {
			req = new XMLHttpRequest();
		}
		else if(window.ActiveXObject) {
			req = new ActiveXObject("MSXML2.XMLHTTP");
		}
		//req.onreadystatechange = globalHmsResponse;
		// false below indicates a synchronous call. this is because we need to return
		// the value immediately.
		req.open("POST", "../../pages/ServerTime.do?method=getTime", false);
		req.setRequestHeader("Content-Type", "text/plain");
		req.send(null);

        /*
		 * since we called req.open with "false", it is a synchronous call, and we'll
 		 * have the response right away. In firefox, the readystatechange is never called
		 * (unlike IE) if it's a synchronous call, so we cannot do it in a separate function.
		 */
		var dt = new Date();
		if (req.status == 200) {
			var tstr = req.responseText;    // in the form 17:41:01 06-JUL-2008

			dt = new Date();
			dt.setHours(parseFloat(tstr.substring(0,2)));
			dt.setMinutes(parseFloat(tstr.substring(3,5)));
			dt.setSeconds(parseFloat(tstr.substring(6,10)));
			dt.setDate(parseFloat(tstr.substring(9,11)));

			var mm = tstr.substring(12,15);
			for(var i=0;i<12;i++){
				if(gMonthShortNames[i] == mm){
					dt.setMonth(i);
				}
			}

			dt.setYear(parseInt(tstr.substring(16,20)));
            return dt;
		} else {
			// we received a non-200 status. Try and return at least local current date
			return new Date();
		}
	}
}


// To get Current Date in format like 01-MAR-2008
function getCurrentDate() {
	var months=new Array('JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC');
	var sysdate=getServerTime();
        if (!sysdate) {
            // TODO: this can be null if the ajax call has not yet returned, which is usually
            // true in Mozilla. Need to fix this in a different way
            sysdate = new Date();
        }
	var date=sysdate.getDate();
	var month=months[sysdate.getMonth()];
	var year=sysdate.getFullYear();
	if(date<10){
		date="0"+date;
	}
	var hours=sysdate.getHours();
	var minutes=sysdate.getMinutes();
	var seconds=sysdate.getSeconds();
	var amorpm = 'AM'
	if(hours>12){
	  	hours=hours-12;
	  	amorpm='PM';
	}
	if(hours==12){
		amorpm='PM';
	}
	if(hours<=9){
		hours ='0' + hours;
	}
	if(minutes<10){
		minutes="0"+minutes;
	}
	if(seconds<10){
		seconds="0"+seconds;
	}
	var d= date+'-'+month+'-'+year;
	var t=hours+":"+minutes+":"+seconds + " " + amorpm;
	return d;
}

function getCurrentDateAndTime() {
	var months=new Array('JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC');
	var sysdate=getServerTime();
        if (!sysdate) {
            // TODO: this can be null if the ajax call has not yet returned, which is usually
            // true in Mozilla. Need to fix this in a different way
            sysdate = new Date();
        }
	var date=sysdate.getDate();
	var month=months[sysdate.getMonth()];
	var year=sysdate.getFullYear();
	if(date<10){
		date="0"+date;
	}
	var hours=sysdate.getHours();
	var minutes=sysdate.getMinutes();
	var seconds=sysdate.getSeconds();
	var amorpm = 'AM'
	if(hours>12){
	  	hours=hours-12;
	  	amorpm='PM';
	}
	if(hours==12){
		amorpm='PM';
	}
	if(hours<=9){
		hours ='0' + hours;
	}
	if(minutes<10){
		minutes="0"+minutes;
	}
	if(seconds<10){
		seconds="0"+seconds;
	}
	var d= date+'-'+month+'-'+year;
	var t=hours+":"+minutes+":"+seconds + " " + amorpm;
	return d + t;
}

function getCurrentTime() {
	var months=new Array('JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC');
	var sysdate=getServerTime();
        if (!sysdate) {
            // TODO: this can be null if the ajax call has not yet returned, which is usually
            // true in Mozilla. Need to fix this in a different way
            sysdate = new Date();
        }
	var date=sysdate.getDate();
	var month=months[sysdate.getMonth()];
	var year=sysdate.getFullYear();
	if(date<10){
		date="0"+date;
	}
	var hours=sysdate.getHours();
	var minutes=sysdate.getMinutes();
	var seconds=sysdate.getSeconds();
	var amorpm = 'AM'
	if(hours>12){
	  	hours=hours-12;
	  	amorpm='PM';
	}
	if(hours==12){
		amorpm='PM';
	}
	if(hours<=9){
		hours ='0' + hours;
	}
	if(minutes<10){
		minutes="0"+minutes;
	}
	if(seconds<10){
		seconds="0"+seconds;
	}
	var d= date+'-'+month+'-'+year;
	var t=hours+":"+minutes+":"+seconds + " " + amorpm;
	return t;
}


