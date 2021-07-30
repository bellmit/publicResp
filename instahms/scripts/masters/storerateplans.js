function validate() {

		if ( document.getElementById("store_rate_plan_name").value == '' ) {
			alert("Please Enter Stores Rate Plan Name ");
			document.getElementById("store_rate_plan_name").focus();
			return false;
		}
		if ( ratePlanName != document.getElementById("store_rate_plan_name").value && isDuplicte() ) {
			alert("Rate Plan already exists");
			document.getElementById("store_rate_plan_name").focus();
			return false;
		}
		if ( document.storerateplanform._method.value == 'show'  && document.forms[0].status.value == 'I' && !empty(RPListJSON) ) {
			alert("Can not be Inactive since this is used by one of the Billing Rate Plan");
			document.forms[0].status.focus();
			return false;
		}
		document.forms[0].submit();

	}

	function isDuplicte(){
		if ( !empty(existingStoresRatePlansJSON[document.getElementById("store_rate_plan_name").value]) )
			return true;
		return false;

	}

	function onChangeTaxRate(taxRate){
		if ( taxRate.value == '' ) taxRate.value = 0;

	}
