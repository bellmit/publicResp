

		var currentTime = new Date();

		var day = getFullDay(currentTime.getDate());
		var month = getFullMonth(currentTime.getMonth());
		var year = currentTime.getFullYear();


			var noOfCharges = 0;
			function generateVoucher(){
				document.forms[0].method.value="insertValues";
				if(noOfCharges>0){
					document.forms[0].noOfCharges.value = noOfCharges;
				}
				if (noOfCharges == 0){
					alert("Plese select items to transfer amount ");
					return false;
				}else {
				}

				for(i=0;i<=document.forms[0].statusCheck.length;i++){
					if(document.forms[0].statusCheck[i].checked ){
						 if(document.forms[0].chargeAmount[i].value.length == 0){
						 	alert("Please enter the amount to be transfered");
						 		document.forms[0].chargeAmount[i].focus();
						 	return false;
						 }
					}
				}

			}
			function getSelectedValuesCount(checkBox,rowId){
				if (checkBox.checked){
					noOfCharges++;
				}
				else{
					noOfCharges--;
				}
			}

			function getCheckValues(){
				var form = document.forms[0];
				form.method.value="getDetails";
				form.pageNum.value='';
				if (!validateForm()){
					return false;
				}
			}

		function validateForm(){
			if (document.getElementById("fdate").value == ""){
				alert("Enter fromdate" );
				return false;
			}
			if (document.getElementById("tdate").value == ""){
				alert("Enter todate ");
				return false;
			}

			if (!doValidateDateField(document.getElementById("fdate"))){
				return false;
			}
			if (!doValidateDateField(document.getElementById("tdate"))){
				return false;
			}

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
				document.getElementById("tdate").value = cfulldate;
				return false;
			}

			if(getDateDiff(document.getElementById("fdate").value,document.getElementById("tdate").value)<0){
				alert("From date should not greater than Todate");
				return false;
			}

			return true;
		}

		function validateAmount(billedAmt,tAmt,transferedAmt){
			if((tAmt.value)>(billedAmt-transferedAmt)){
				alert("Transfer amount should not be greater than billed amount.U can transfer only Rs"+(billedAmt-transferedAmt).toFixed(2));
			}

		}

		function resetAll(){
			document.getElementById("p").checked=true;
			document.forms[0].fdate.value = day+"-"+month+"-"+year;
			document.forms[0].tdate.value = day+"-"+month+"-"+year;

		}
