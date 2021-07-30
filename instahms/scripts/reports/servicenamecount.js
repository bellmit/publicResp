		var fromDate, toDate,category ;

		function onInit() {

			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
			setDateRangeYesterday(fromDate, toDate);
			document.forms[0].all.checked=true;
			selectDepartments();
		}
		function validateCategory(){
			var form = document.forms[0];

			if(!validateDepartmentNames()){
				return false;
			}

			if (validateFromToDate(document.inputform.fromDate,document.inputform.toDate)){
				return true;
			}else{
				return false;
			}
		}

		function selectDepartments(){
			var disable = document.forms[0].all.checked;
			var deptLen = document.forms[0].deptIdArray.length;
			for (i=deptLen-1;i>=0;i--){
				document.forms[0].deptIdArray[i].selected = disable;
			}
		}


		function validateDepartmentNames(){
			var temp="";
			var deptSelected = false;
			var len = document.forms[0].deptIdArray.length;
			var options = document.forms[0].deptIdArray;

				for (var i=0;i<len;i++){
					if (options[i].selected == true)
						deptSelected=true;
				}

				if(!deptSelected){
					alert("Please, select at least one department name for the report");
					return false;
				}
				return true;
			}

			function deselectAll(){
				document.forms[0].all.checked = false;
			}


