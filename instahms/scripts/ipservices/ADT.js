var toolbar = {}
		toolbar.Bed= {
			title: toolbarOptions["beddetails"]["name"],
			imageSrc: "icons/Edit.png",
			href: 'pages/ipservices/BedDetails.do?_method=getIpBedDetailsScreen',
			onclick: 'validateBillStatus',
			description: toolbarOptions["beddetails"]["description"]
		};
		toolbar.AllocateBed={
			title: toolbarOptions["allocatebed"]["name"],
			imageSrc: "icons/allocate.png",
			href: 'pages/ipservices/AllocateBed.do?_method=getBedAllocationScreen',
			onclick: 'validateBillStatus',
			description: toolbarOptions["allocatebed"]["description"]
		};
		toolbar.ShiftBed={
			title: toolbarOptions["shiftbed"]["name"],
			imageSrc: "icons/shift.png",
			href: 'pages/ipservices/ShiftBed.do?_method=getShiftBedScreen'
		};
		toolbar.BystanderBed={
			title: toolbarOptions["bystanderbed"]["name"],
			imageSrc: "images/man-icon.png",
			href: 'pages/ipservices/AllocateBed.do?_method=getBedAllocationScreen&bystander=Y'
		};
		toolbar.Discharge= {
			title: toolbarOptions["discharge"]["name"],
			imageSrc: "icons/Signoff.png",
			href: 'discharge/DischargePatient.do?_method=getDischargeDetails',
			description: null,
			show : (discharge == 'A')
	};


	function validateDischarge (anchor, params, id, toolbar) {
		return funDischarge(params.mrno, params.patientid, params.doctorId,params.billStatusOk,params.paymentOk, id);
	}

	function validateBillStatus(anchor, params, id, toolbar) {
		return checkBillStatus('Bed Details', params.mrno, params.patientid,params.bedName);
	}

	function onKeyPressMrno(e) {
		if (isEventEnterOrTab(e)) {
			return onChangeMrno();
		} else {
			return true;
		}
	}

	function onChangeMrno() {
		var theForm = document.ipdashboardform;
		var mrnoBox = theForm.mrNo;

		// complete
		var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

		if (!valid) {
			showMessage("js.registration.adt.invalidmrno");
			theForm.mrno.value = ""
			theForm.mrno.focus();
			return false;
		}
	}

	function getValue(obj) {
		for(var i=0; i<obj.length; i++){
		alert(obj[i].selected);
		if(obj[i].selected){
			obj[i].selected = false;
		}else{
			obj[i].selected = true;
		}
		}
	}

	function initToolbar() {
		initMrNoAutoComplete(cpath, 'mrno', 'mrnoContainer', 'active');
		createToolbar(toolbar);
	}
