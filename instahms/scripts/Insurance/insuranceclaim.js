function validateInsuClaimAmt(index) {
			if (document.getElementById("insuClaimAmt"+index).value == "") {
				document.getElementById("insuClaimAmt"+index).value = 0;
			}

			var insCAmt = parseFloat(document.getElementById('insuClaimAmt'+index).value);
			var bamt = parseFloat(document.getElementById('amt'+index).value);
			if( (eval(insCAmt > bamt ) && (document.getElementById('chargeGrpId'+index).value != 'DIS') )) {
				alert("Insurance claim amount should be less than Bill Amt");
				document.getElementById('insuClaimAmt'+index).focus();
				resetClaimTotals()
				return false;
			}
			document.getElementById('patPayAmt'+index).value =  eval ( parseFloat(document.getElementById('amt'+index).value )-
				             (parseFloat(document.getElementById('insuClaimAmt'+index).value))
				     );

			resetClaimTotals();
			return true;
		}

		function disableDates() {
			var chargeTable = document.getElementById("chargeTable");
			var tablen = chargeTable.rows.length - 2;
			for (var i=1; i< tablen ; i++) {
				document.getElementById("postedDate"+i).readOnly = true;
			}
		}

		function validateSave() {

			var chargeTable = document.getElementById("chargeTable");
			var tablen = chargeTable.rows.length - 2 ;
			var status;
			for (var i=1; i< tablen ; i++) {
				status = validateInsuClaimAmt(i);
				if(!status) break;
			}
			if(!status) return false;

			validateTotals();
			return true;
		}
		function validateTotals()  {
			var totClaimAmt = parseFloat(document.getElementById('totInsClaimAmt').value,10);
			var approLimit = parseFloat(document.getElementById('approvedLimit').value,10);
			if ( totClaimAmt > approLimit ) {
				alert("Note: Claim amount total is greater than Approval limit, but you can  still continue generating your insurance claim.");
			}
			return true;
		}
		var totInsAmt = 0;
		var totPatPayAmount = 0;
		function resetClaimTotals() {
			totInsAmt = 0;
			totPatPayAmount = 0;
			var chargeTable = document.getElementById("chargeTable");
			var tablen = chargeTable.rows.length - 2 ;
			for (var i=1; i< tablen ; i++) {
				totInsAmt = eval(totInsAmt + parseFloat(document.getElementById('insuClaimAmt'+i).value, 10) );
				totPatPayAmount = eval(totPatPayAmount + parseFloat(document.getElementById('patPayAmt'+i).value, 10) );
			}
			document.getElementById('totInsClaimAmt').value = totInsAmt;
			document.getElementById('totPatPayAmt').value = totPatPayAmount;
		}

		function printInsuClaim(printObj) {
			var mrNo = document.getElementById("mrNo").value;
			var insuranceID = document.getElementById("insuranceID").value;
			var visitId = document.getElementById("visitId").value;
			var billNo = document.getElementById("billNo").value;
			var admitDoctor = document.getElementById("admitDoctor").value;
			var refDoctor = document.getElementById("refDoctor").value;
			var url = "InsuranceClaimAction.do?method=printInsuClaim&mrno="+mrNo+"&insuranceID="+insuranceID+"&visitId="+visitId+"&billNo="+billNo;
			url = url + "&admitDoctor=" + admitDoctor + "&refDoctor=" + refDoctor;

			if(printObj.id == "insuClaimPrint") url = url + "&patPayDetailed=N";
			else if(printObj.id == "patWillPayPrint")  url = url + "&patPayDetailed=Y";

			window.open(url);
		}

		function txtToolTip(txtTool) {
			txtTool.title=txtTool.value;
		}

		function applyChangesToTable() {
			var chargeTable = document.getElementById("chargeTable");
			var tablen = chargeTable.rows.length - 2;

			var rowAmtField = 0;
			var rowAmtPaise = 0;
			var rowPaidPaise = 0;
			var totalClaimPaise = 0;
			var totalPatientPayAmt = 0;
			document.mainform.totInsClaimAmt.value = 0;

			for (var i=1; i< tablen ; i++) {
				rowAmtField = document.getElementById("amt"+i);
				rowAmtPaise = getPaise(rowAmtField.value);

				document.getElementById("insuClaimAmt"+i).value = formatAmountPaise((rowAmtPaise-rowPaidPaise));
				document.getElementById('patPayAmt'+i).value =  eval ( parseFloat(document.getElementById('amt'+i).value )-
				             (parseFloat(document.getElementById('insuClaimAmt'+i).value))
				     );
				totalPatientPayAmt = parseFloat(totalPatientPayAmt) + parseFloat(document.getElementById('patPayAmt'+i).value);
				totalClaimPaise += (rowAmtPaise-rowPaidPaise);

			}
			document.mainform.totInsClaimAmt.value = formatAmountPaise( totalClaimPaise );
			document.getElementById('totPatPayAmt').value = formatAmountPaise( totalPatientPayAmt );
		}
