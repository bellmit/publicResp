<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insta HMS</title>

<!--
-->


<script>
	function FromToCheck() {
		var fromObj = document.mainform.fromDate;
		var toObj = document.mainform.toDate;

		if ( (fromObj.value != "") && (toObj.value != "") ) {
			fromDt = getDateFromField(fromObj);
			toDt = getDateFromField(toObj);

			if ( (toDt != null) && (fromDt != null) ) {
				if (fromDt > toDt) {
					alert("To date cannot be less than from date");
					alert(fromDt - toDt);
					alert(new Date(gServerNow));
					return false;
				}
			}
		}
		return true;
	}

	function validateForm() {
		/* Make sure all date fields are valid */
		if (!doValidateDateField(document.mainform.fromDate)) 
			return false;
		if (!doValidateDateField(document.mainform.toDate)) 
			return false;
		if (!doValidateDateField(document.mainform.aFutureDate, 'future'))
			return false;

		/* do mandatory validation */
		if (document.mainform.aPastDate.value == "")  {
			alert("Past Date is required");
			return false;
		}

		if (!doValidateDateField(document.mainform.aPastDate, 'past'))
			return false;

		// special check: ensure from-to are proper
		if (!FromToCheck())
			return false;

		return true;
	}
</script>
</head>

<body>
<form name="mainform">

<hr>
Current values from the form submission: from date: ${ifn:cleanHtml(param.fromDate)}, to date: ${ifn:cleanHtml(param.toDate)}

	<hr>
	Test extravalidation:<p>

From Date ::: <insta:datewidget name="fromDate" value="10-10-2008"/>
To Date ::: <insta:datewidget name="toDate" extravalidation="FromToCheck()" />


<hr>
Test past, future and required fields. Double click is disabled, and buttons are enabled<p>

Any Date (future) <insta:datewidget name="aFutureDate" valid="future" value="today" calDblclick="false" calButton="true"/> <br/>
Any Date (past) <insta:datewidget name="aPastDate" valid="past" calDblclick="false" calButton="true"/> (required)
<p>
<input value="Submit" type="submit" onclick="return validateForm()"/>
</form>
</body>
</html>
