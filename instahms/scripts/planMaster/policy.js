	  function onChangeFilterChargeGroup() {
			document.getElementById("selAllChkbx").checked = false;
			selectAllForDiscounts();
			loadChargeHeads();
			filterCharges();
			var chargeGroupId = document.policyMasterForm.filterChargeGroup.value;
			if (chargeGroupId != "")
				YAHOO.util.Dom.addClass(document.policyMasterForm.filterChargeGroup, 'filterActive');
			else
				YAHOO.util.Dom.removeClass(document.policyMasterForm.filterChargeGroup, 'filterActive');
		}

		function onChangeFilterChargeHead() {
			document.getElementById("selAllChkbx").checked = false;
			selectAllForDiscounts();
			filterCharges();
			var chargeHeadId = document.policyMasterForm.filterChargeHead.value;
			if (chargeHeadId != "")
				YAHOO.util.Dom.addClass(document.policyMasterForm.filterChargeHead, 'filterActive');
			else
				YAHOO.util.Dom.removeClass(document.policyMasterForm.filterChargeHead, 'filterActive');
		}

		function loadChargeHeads() {
			var chargeGroupId = document.policyMasterForm.filterChargeGroup.value;
			var chargeHeads = jChargeHeads.sort(function(val1, val2) {
					if ((val1["CHARGEHEAD_NAME"] + "") < (val2["CHARGEHEAD_NAME"] + "")) {
						return - 1;
					}
					if ((val1["CHARGEHEAD_NAME"] + "") > (val2["CHARGEHEAD_NAME"] + "")) {
						return 1;
					}
					return 0;
				});
			if (chargeGroupId != "")
				chargeHeads = filterList(jChargeHeads, 'CHARGEGROUP_ID', chargeGroupId);

			loadSelectBox(document.policyMasterForm.filterChargeHead, chargeHeads, 'CHARGEHEAD_NAME', 'CHARGEHEAD_ID', "(All)", "");
			document.policyMasterForm.filterChargeHead.selectedIndex = 0;
		}

		function filterCharges() {
			var num = getNumCharges();
		   	var table = document.getElementById("chargesTable");
			var filterGroup = document.policyMasterForm.filterChargeGroup.value;
			var filterHead = document.policyMasterForm.filterChargeHead.value;
			for (var i=1; i<=num; i++) {
				var row = table.rows[i];
				var chargeGroup = getElementByName(row, 'chargeGroupId').value;
				var chargeHead = getElementByName(row, 'chargeHeadId').value;
				var show = true;
				if ((filterGroup != "") && (filterGroup != chargeGroup))
					show = false;
				if ((filterHead != "") && (filterHead != chargeHead))
					show = false;
				if (show) {
					row.style.display = "";
				} else {
					row.style.display = "none";
				}
			}
		}

		function getNumCharges() {
			return document.getElementById("chargesTable").rows.length-1;
		}

		function init(){
			initDialog1();
			initDialog();
			initDialog2();
			//hidCorporateInsu();
			populateInsuranceCategory();
			if(mapPlanCat!=null && mapPlanCat!='') {
				setSelectedIndex(document.getElementById('category_id'),mapPlanCat);
			}
			sortDropDown(document.getElementById('category_id'));
			populateInsuranceTpaname();
			
			if(document.getElementById("op_episode_visit").checked){
				document.getElementById('op_visit_limit').readOnly = true;
				document.getElementById('op_episode_limit').readOnly = false;
			}else {
				document.getElementById('op_visit_limit').readOnly = false;
				document.getElementById('op_episode_limit').readOnly = true;
			}
			
			var opVisitLimit = document.getElementById("op_visit_limit").value;
			var opEpisodeLimit = document.getElementById("op_episode_limit").value;
			
			var opPlanlimit = document.getElementById("op_planlimit").value;
			var opVistdeductible = document.getElementById("op_visit_deductible").value;
			var opVisitCopay = document.getElementById("op_visit_copay").value;
			
			if(!opVisitLimit)
				document.getElementById("op_visit_limit").value= '0.00';
			if(!opEpisodeLimit)
				document.getElementById("op_episode_limit").value= '0.00';
			
			if(!opPlanlimit)
				document.getElementById("op_planlimit").value= '0.00';
			if(!opVistdeductible)
				document.getElementById("op_visit_deductible").value= '0.00';
			if(!opVisitCopay)
				document.getElementById("op_visit_copay").value= '0.00';
			if(!document.getElementById("op_visit_copay_limit").value)
				document.getElementById("op_visit_copay_limit").value= '0.00';
			
			
			if(!document.getElementById("ip_planlimit").value)
				document.getElementById("ip_planlimit").value= '0.00';
			if(!document.getElementById("ip_visit_deductible").value)
				document.getElementById("ip_visit_deductible").value= '0.00';
			if(!document.getElementById("ip_visit_copay").value)
				document.getElementById("ip_visit_copay").value= '0.00';
			if(!document.getElementById("episode_visit_ip").value)
				document.getElementById("episode_visit_ip").value= '0.00';
			if(!document.getElementById("ip_per_day_limit").value)
				document.getElementById("ip_per_day_limit").value= '0.00';
			if(!document.getElementById("ip_visit_copay_limit").value)
				document.getElementById("ip_visit_copay_limit").value= '0.00';
					
		
		}

		function hidCorporateInsu(){
			if ('Y' == corporateInsu) {
		  		 	document.getElementById("corporateInsu1").style.display='none';
		  	    	document.getElementById("corporateInsu2").style.display='none';
		  	    	document.getElementById("corporateInsu3").style.display='none';
		  		 	} else {
		  	    	document.getElementById("corporateInsu1").style.display='';
		   		    document.getElementById("corporateInsu2").style.display='';
		   		    document.getElementById("corporateInsu3").style.display='';
		    	}
	    }

		function setSelectedIndex(opt, set_value) {
		  var index=0;
		  for(var i=0; i<opt.options.length; i++) {
		    var opt_value = opt.options[i].value;
		    if (opt_value == set_value) {
		      opt.selectedIndex = i;
		      return;
		    }
		  }
		}

		var myDialog;
		function initDialog() {
		    myDialog = new YAHOO.widget.Dialog('myDialog', {
		        width:"500px",
		        visible: false,
		        modal: true,
		        constraintoviewport: true,

		    });

		    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                                scope:myDialog,
	                                                correctScope:true } );
	        var entKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
	                                              { fn:handleOk,
	                                                scope:myDialog,
	                                                correctScope:true } );
			myDialog.cfg.queueProperty("keylisteners", [escKeyListener,entKeyListener]);
		    myDialog.render();
		}

		var myDialog1;
		function initDialog1(){
			myDialog1 = new YAHOO.widget.Dialog('myDialog1', {
		        width:"500px",
		        visible: false,
		        modal: true,
		        constraintoviewport: true,

		    });

			var escKeyListener1 = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel1,
	                                                scope:myDialog1,
	                                                correctScope:true } );
	        var entKeyListener1 = new YAHOO.util.KeyListener(document, { keys:13 },
	                                              { fn:handleOk1,
	                                                scope:myDialog1,
	                                                correctScope:true } );
			myDialog1.cfg.queueProperty("keylisteners", [escKeyListener1,entKeyListener1]);
			myDialog1.cancelEvent.subscribe(handleCancel1);
		    myDialog1.render();
		}

		function handleCancel() {
		    myDialog.cancel();
		}

		var updtdChargeHeadListArr = new Array();
		var index= 0;
		var patientType = 'i';


		function handleOk() {
			if(validateAmount(document.getElementById("_dlg_cat_amt_fld"))== false){
		    	return false;
		    }
		    if(validateAmount(document.getElementById("_dlg_amt_fld"))== false){
		    	return false;
		    }
		    if(validateAmount(document.getElementById("_dlg_perc_fld"))== false){
		    	return false;
		    }
		    if(validateAmount(document.getElementById("_dlg_trmnt_fld"))== false){
		    	return false;
		    }
		    if(validateAmount(document.getElementById("_dlg_cap_fld"))== false){
		    	return false;
		    }
			var categoryHidden = document.getElementById("dlg_itemCatId");
			var categoryId = categoryHidden.value;
			if(document.getElementById("_dlg_perc_fld").value >100){
				alert("Percentage cannot be more than 100...");
				return;
			}
			var patTyp = document.getElementById('_dlg_patient_type').value ;
			document.getElementById(categoryId+"_catAmtLbl_"+patTyp).textContent = document.getElementById("_dlg_cat_amt_fld").value==null || document.getElementById("_dlg_cat_amt_fld").value==''?formatAmountValue(0.00):formatAmountValue(document.getElementById("_dlg_cat_amt_fld").value);
			document.getElementById(categoryId+"_amtLbl_"+patTyp).textContent = document.getElementById("_dlg_amt_fld").value==null || document.getElementById("_dlg_amt_fld").value==''?formatAmountValue(0.00):formatAmountValue(document.getElementById("_dlg_amt_fld").value);
			document.getElementById(categoryId+"_perLbl_"+patTyp).textContent = document.getElementById("_dlg_perc_fld").value == null || document.getElementById("_dlg_perc_fld").value==''?formatAmountValue(0.00):formatAmountValue(document.getElementById("_dlg_perc_fld").value);
			document.getElementById(categoryId+"_capLbl_"+patTyp).textContent = document.getElementById("_dlg_cap_fld").value==null || document.getElementById("_dlg_cap_fld").value==''?'':formatAmountValue(document.getElementById("_dlg_cap_fld").value);
			document.getElementById(categoryId+"_treatLbl_"+patTyp).textContent = document.getElementById("_dlg_trmnt_fld").value == null || document.getElementById("_dlg_trmnt_fld").value==''?'':formatAmountValue(document.getElementById("_dlg_trmnt_fld").value);
			document.getElementById(categoryId+"_amtHidden_"+patTyp).value = document.getElementById("_dlg_amt_fld").value==null || document.getElementById("_dlg_amt_fld").value==''?formatAmountValue(0.00):formatAmountValue(document.getElementById("_dlg_amt_fld").value);
			document.getElementById(categoryId+"_catAmtHidden_"+patTyp).value = document.getElementById("_dlg_cat_amt_fld").value==null || document.getElementById("_dlg_cat_amt_fld").value==''?formatAmountValue(0.00):formatAmountValue(document.getElementById("_dlg_cat_amt_fld").value);
			document.getElementById(categoryId+"_perHidden_"+patTyp).value = document.getElementById("_dlg_perc_fld").value == null || document.getElementById("_dlg_perc_fld").value==''?formatAmountValue(0.00):formatAmountValue(document.getElementById("_dlg_perc_fld").value);
			document.getElementById(categoryId+"_capHidden_"+patTyp).value = document.getElementById("_dlg_cap_fld").value;
			document.getElementById(categoryId+"_treatHidden_"+patTyp).value = document.getElementById("_dlg_trmnt_fld").value;
			var e = document.getElementById("_category_payable_fld");
        	var _cpVal = e.options[e.selectedIndex].value;
        	document.getElementById(categoryId+"_category_payable_"+patTyp).value=_cpVal;
        	var e1 = document.getElementById("_category_prior_auth_required_fld");
        	var _paVal = e1.options[e1.selectedIndex].value;
        	document.getElementById(categoryId+"_category_prior_auth_required_"+patTyp).value=_paVal;
			var insert = true;
			for(i=0;i<index;i++){
				if(updtdChargeHeadListArr[i] == categoryId+"@"+patTyp || isNaN(parseInt(categoryId))){
					insert = false;
					if(isNaN(parseInt(categoryId))) {
					 	alert(document.getElementById('dlg_itemCatName'));
					}
				}

			}
			if(insert){
				updtdChargeHeadListArr[index++] = categoryId+"@"+patTyp;
			}
			document.getElementById("updtdChargeHeadList").value = updtdChargeHeadListArr;
			document.getElementById('myDialog').style.display='none';
	   		 myDialog.cancel();
		}

		function handleOk1(){
			if(document.getElementById("_dlg_amt_fld1").value==""
			  && document.getElementById("_dlg_cat_amt_fld1").value==""
			  && document.getElementById("_dlg_perc_fld1").value==""
			  && document.getElementById("_dlg_cap_fld1").value==""
			  && document.getElementById("_dlg_trmnt_fld1").value==""){
			  	document.getElementById("_dlg_amt_fld1").value= 0;
			  	document.getElementById("_dlg_cat_amt_fld1").value= 0;
			  	document.getElementById("_dlg_perc_fld1").value = 0 ;
			  	document.getElementById("_dlg_cap_fld1").value = '';
			  	document.getElementById("_dlg_trmnt_fld1").value = '';
            }

			if(validateAmount(document.getElementById("_dlg_cat_amt_fld1"))== false){
		    	return false;
		    }
			if(validateAmount(document.getElementById("_dlg_amt_fld1"))== false){
		    	return false;
		    }
		    if(validateAmount(document.getElementById("_dlg_perc_fld1"))== false){
		    	return false;
		    }
		    if(validateAmount(document.getElementById("_dlg_trmnt_fld1"))== false){
		    	return false;
		    }
		    if(validateAmount(document.getElementById("_dlg_cap_fld1"))== false){
		    	return false;
		    }
		    if(document.getElementById("_dlg_perc_fld1").value >100){
				alert("Percentage cannot be more than 100...");
				return;
			}
			var disCheckElmts;
			if(patientType == 'i')
				disCheckElmts = document.policyMasterForm.chkbx_i;
			else
				disCheckElmts = document.policyMasterForm.chkbx_o;
			for(var i=0;i<disCheckElmts.length; i++) {
				if(disCheckElmts[i].checked) {
					var chrgHdId = disCheckElmts[i].value;
					if(document.getElementById("_dlg_cat_amt_fld1").value!='') {
						document.getElementById(chrgHdId+"_catAmtLbl_"+patientType).textContent= formatAmountValue(document.getElementById("_dlg_cat_amt_fld1").value);
						document.getElementById(chrgHdId+"_catAmtHidden_"+patientType).value = document.getElementById("_dlg_cat_amt_fld1").value;
					}
					if(document.getElementById("_dlg_amt_fld1").value!='') {
						document.getElementById(chrgHdId+"_amtLbl_"+patientType).textContent= formatAmountValue(document.getElementById("_dlg_amt_fld1").value);
						document.getElementById(chrgHdId+"_amtHidden_"+patientType).value = document.getElementById("_dlg_amt_fld1").value;
					}
					if(document.getElementById("_dlg_perc_fld1").value!='') {
						document.getElementById(chrgHdId+"_perLbl_"+patientType).textContent = formatAmountValue(document.getElementById("_dlg_perc_fld1").value);
						document.getElementById(chrgHdId+"_perHidden_"+patientType).value = document.getElementById("_dlg_perc_fld1").value;
					}

					document.getElementById(chrgHdId+"_treatLbl_"+patientType).textContent = document.getElementById("_dlg_trmnt_fld1").value==''? '':formatAmountValue(document.getElementById("_dlg_trmnt_fld1").value);
					document.getElementById(chrgHdId+"_treatHidden_"+patientType).value = document.getElementById("_dlg_trmnt_fld1").value;

					document.getElementById(chrgHdId+"_capLbl_"+patientType).textContent = document.getElementById("_dlg_cap_fld1").value==''? '': formatAmountValue(document.getElementById("_dlg_cap_fld1").value);
					document.getElementById(chrgHdId+"_capHidden_"+patientType).value = document.getElementById("_dlg_cap_fld1").value;

					var e = document.getElementById("_category_payable_fld1");
		        	var _cpVal = e.options[e.selectedIndex].value;
		        	document.getElementById(chrgHdId+"_category_payable_"+patientType).value=_cpVal;
		        	var e1 = document.getElementById("_category_prior_auth_required_fld1");
		        	var _paVal = e1.options[e1.selectedIndex].value;
		        	document.getElementById(chrgHdId+"_category_prior_auth_required_"+patientType).value=_paVal;
					var insert = true;
					for(var j=0;j<=index;j++){
						if(updtdChargeHeadListArr[j] == chrgHdId+"@"+patientType)
						insert = false;
					}
					if(insert){
						updtdChargeHeadListArr[index++] = chrgHdId+"@"+patientType;
					}
				}

			}
			document.getElementById("updtdChargeHeadList").value = updtdChargeHeadListArr;
			document.getElementById('myDialog1').style.display='none';
		    myDialog1.cancel();
		}

		function handleCancel1(){
			 document.getElementById('myDialog1').style.display='none';
			 document.getElementById('myDialog1').style.visibility='hidden';
			 myDialog1.hide();
		}

		function showDialog(id,patTyp) {
			enableNextPrev();
			var row = getThisRow(document.getElementById(id+"_amtLbl_"+patTyp).parentNode);
			var idz = getRowChargeIndex(row);
			document.getElementById('_dlg_patient_type').value = patTyp;
			YAHOO.util.Dom.addClass(row, 'editing');
			document.policyMasterForm.editRowId.value = idz;

			document.getElementById('myDialog').style.display='block';
			var button1 = document.getElementById(id+"_img_"+patTyp);
		    myDialog.cfg.setProperty("context", [button1, "tr", "br"], false);

		    var categoryId = id;

		    var categoryName,isInsurancePayable;
		    
		    for(var i=0; i<jChargeHeads.length;i++){
		    	var ele = jChargeHeads[i];
		    	if(ele["insurance_category_id"] == id){
		    		categoryName =ele["insurance_category_name"];
		    		isInsurancePayable = ele["insurance_payable"];
		    	}
		    }
		    //isInsurancePayable =  getElementByName(row,"_insurancePayable_"+patTyp).value;
		    isInsurancePayable =  getElementByName(row,'category_payable').value;
		    var itemCatLabel = document.getElementById("dlg_itemCatName");
		    itemCatLabel.textContent = categoryName;
		    var itemCatHidden = document.getElementById("dlg_itemCatId");
		    itemCatHidden.value = id;
		    var catAmtFld = document.getElementById("_dlg_cat_amt_fld");
		    var amtFld = document.getElementById("_dlg_amt_fld");
		   	var percFld = document.getElementById("_dlg_perc_fld");
		   	var trmntFld = document.getElementById("_dlg_trmnt_fld");
		   	var capFld = document.getElementById("_dlg_cap_fld");

		   	catAmtFld.value =  parseFloat(document.getElementById(categoryId+"_catAmtLbl_"+patTyp).textContent);
		   	amtFld.value = parseFloat(document.getElementById(categoryId+"_amtLbl_"+patTyp).textContent);

		   	percFld.value =  parseFloat(document.getElementById(categoryId+"_perLbl_"+patTyp).textContent);

		   	capFld.value = document.getElementById(categoryId+"_capLbl_"+patTyp).textContent == null
		   					|| document.getElementById(categoryId+"_capLbl_"+patTyp).textContent == ''? ''
		   					:  parseFloat(document.getElementById(categoryId+"_capLbl_"+patTyp).textContent);

		   	trmntFld.value = document.getElementById(categoryId+"_treatLbl_"+patTyp).textContent == null
		   					|| document.getElementById(categoryId+"_treatLbl_"+patTyp).textContent == ''? ''
		   					:  parseFloat(document.getElementById(categoryId+"_treatLbl_"+patTyp).textContent);
		   	var _paVal = document.getElementById(categoryId+"_category_prior_auth_required_"+patTyp).value;
			document.getElementById("_category_prior_auth_required_fld").value=_paVal.trim();
			var _cpVal = document.getElementById(categoryId+"_category_payable_"+patTyp).value;
			document.getElementById("_category_payable_fld").value=_cpVal;
	   		if(isInsurancePayable=='null' || isInsurancePayable=='false' || _cpVal=='N') {
	   			document.getElementById("_dlg_cat_amt_fld").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_amt_fld").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_perc_fld").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_trmnt_fld").value ="";
	   		 	document.getElementById("_dlg_cap_fld").value = "";
	   		 	document.getElementById("_dlg_trmnt_fld").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_dlg_cap_fld").setAttribute("readOnly", "readOnly");
	   		 	document.getElementById("_category_prior_auth_required_fld").disabled=true;
	   		 	if(isInsurancePayable=='null' || isInsurancePayable=='false'){
	   		 		document.getElementById("_category_payable_fld").disabled=true;
	   		 	}else{
	   		 		document.getElementById("_category_payable_fld").disabled=false;
	   		 	}
	   		    
	   		}else{
	   			document.getElementById("_dlg_cat_amt_fld").removeAttribute("readOnly");
	   			document.getElementById("_dlg_amt_fld").removeAttribute("readonly");
	   		 	document.getElementById("_dlg_perc_fld").removeAttribute("readonly");
	   		 	document.getElementById("_dlg_trmnt_fld").removeAttribute("readonly");
	   		 	document.getElementById("_dlg_cap_fld").removeAttribute("readonly");
	   		 	document.getElementById("_category_payable_fld").disabled=false;
	   		 	document.getElementById("_category_prior_auth_required_fld").disabled=false;
	   		}
		    myDialog.show();
		}

		function showDialogExt(patTyp){
			var isOpChkAtleastOne = false;
			var isIpChkAtleastOne = false;

			if (patTyp == 'o') {
				disCheckElmts = document.policyMasterForm.chkbx_o;
				if(disCheckElmts === undefined) {
					alert("No Insurance Item Category is mapped for OP");
					return false;
				}
				for (var i=0; i<disCheckElmts.length; i++) {
					if (disCheckElmts[i].checked) {
						isOpChkAtleastOne = true;
					}
				}
				if (!isOpChkAtleastOne) {
					alert('Please Select one or more categories');
					return false;
				}
			} else {
				disCheckElmts  = document.policyMasterForm.chkbx_i;
				if(disCheckElmts === undefined) {
					alert("No Insurance Item Category is mapped for IP");
					return false;
				}
				for (var j=0; j<disCheckElmts.length; j++) {
					if (disCheckElmts[j].checked) {
						isIpChkAtleastOne = true;
					}
				}
				if (!isIpChkAtleastOne) {
					alert('Please Select one or more categories');
					return false;
				}
			}

			patientType = patTyp;
			document.getElementById('myDialog1').style.display='block';
			document.getElementById('myDialog1').style.visibility='visible';
			var button1 = document.getElementById("sel_img_"+patTyp);
		    myDialog1.cfg.setProperty("context", [button1, "tr", "br"], false);
		    document.getElementById("_dlg_amt_fld1").value = 0;
		    document.getElementById("_dlg_cat_amt_fld1").value = 0;
			document.getElementById("_dlg_perc_fld1").value = 0 ;
		 	document.getElementById("_dlg_cap_fld1").value = '';
		  	document.getElementById("_dlg_trmnt_fld1").value = '';
			myDialog1.show();
		}


		function getRowChargeIndex(row) {
			return row.rowIndex - 1;
		}

		function getThisRow(node) {
			return findAncestor(node, "TR");
		}

		function getChargeRow(i, patTyp) {
			i = parseInt(i);
			var table = document.getElementById("chargesTable_"+patTyp);
			return table.rows[i +1];
		}

	    function getIndexedValue(name, index) {
			var obj = getIndexedFormElement(document.policyMasterForm, name, index);
			if (obj)
				return obj.value;
			else
				return null;
		}
	    
	    function enableNextPrev() {
	    	document.policyMasterForm.prevBtn.disabled = false;
	    	document.policyMasterForm.nextBtn.disabled = false;
	    }

		
		function showNextOrPrevCharge(navigate) {
			handleOk();
			var id = document.policyMasterForm.editRowId.value;
			var patTyp = document.getElementById('_dlg_patient_type').value;
			var row = getChargeRow(id,patTyp);
			id = id-(numberOfSystemCategory/2);
			if (navigate == 'next')
				id++;
			else if(navigate == 'prev')
				id--;
			var len = document.getElementsByName("edit_insu_cat_id_"+patTyp).length;
			if (id >= 0 && len>1 && getIndexedValue("edit_insu_cat_id_"+patTyp, id) != null &&
						getIndexedValue("edit_insu_cat_id_"+patTyp, id) != ''){
				YAHOO.util.Dom.removeClass(row, 'editing');
				var checkid = id;
				if (navigate == 'next') {
					checkid--;
				}else if(navigate == 'prev') {
					checkid++;
				}
				if (checkid >= 0 && getIndexedValue("edit_insu_cat_id_"+patTyp, checkid) != null &&
						getIndexedValue("edit_insu_cat_id_"+patTyp, checkid) != '') {
					if (navigate == 'next')
						document.policyMasterForm.prevBtn.disabled = false;
					else if(navigate == 'prev')
						document.policyMasterForm.nextBtn.disabled = false;
					showDialog(getIndexedValue("edit_insu_cat_id_"+patTyp, id),patTyp);
				}
			}else {
				if (navigate == 'next') {
					showDialog(getIndexedValue("edit_insu_cat_id_"+patTyp, id-1),patTyp);
					document.policyMasterForm.nextBtn.disabled = true;
					}else if(navigate == 'prev') {
						showDialog(getIndexedValue("edit_insu_cat_id_"+patTyp, id+1),patTyp);
						document.policyMasterForm.prevBtn.disabled = true;
					}
			}
		}

		function doCancel() {
			window.location.href="${cpath}/master/PolicyMaster.do?_method=list";
		}

		function validateForm(){
			if(document.policyMasterForm.plan_name.value == null || document.policyMasterForm.plan_name.value==''){
			 	alert("Please enter the Plan name");
			 	return false;
			} else if(document.policyMasterForm.insurance_co_id.value == null || document.policyMasterForm.insurance_co_id.value==''){
				alert("Please select the Insurance company Name...");
				return false;
			}else if(document.policyMasterForm.category_id){
					if(document.policyMasterForm.category_id.value == null || document.policyMasterForm.category_id.value==''){
					alert("Please select the Plan type...");
					return false;
					}
			} else if(!document.getElementById('ipApplicablez').checked &&
									!document.getElementById('opApplicablez').checked) {
				alert('Please select either IP-Applicable or OP-Applicable ');
				document.getElementById('ipApplicablez').focus();
				return false;
			}  
			
			if(document.policyMasterForm.limit_type.value == 'R' && document.policyMasterForm.case_rate_count.value == '') {
				alert("Please select  No. of Case Rates Allowed");
				document.getElementById('case_rate_count').focus();
				return false;
			}

			if(document.policyMasterForm.base_rate){
				document.policyMasterForm.base_rate.value =
						trim(document.policyMasterForm.base_rate.value) == '' ? 0 : trim(document.policyMasterForm.base_rate.value);
				if (!validateAmount(document.policyMasterForm.base_rate, "Base Rate must be a valid amount"))
					return false;
			}

			if(document.policyMasterForm.gap_amount){
				document.policyMasterForm.gap_amount.value =
						trim(document.policyMasterForm.gap_amount.value) == '' ? 0 : trim(document.policyMasterForm.gap_amount.value);
				if (!validateAmount(document.policyMasterForm.gap_amount, "Gap amount must be a valid amount"))
					return false;
			}

			if(document.policyMasterForm.marginal_percent){
				document.policyMasterForm.marginal_percent.value =
						trim(document.policyMasterForm.marginal_percent.value) == '' ? 0 : trim(document.policyMasterForm.marginal_percent.value);
				if (!validateDecimal(document.policyMasterForm.marginal_percent, "Marginal percent must be a valid number", 2))
					return false;

				var margPer = getAmount(document.policyMasterForm.marginal_percent.value);
				if (margPer > 100) {
					alert("Marginal percent cannot be greater than 100%");
					document.policyMasterForm.marginal_percent.focus();
					return false;
				}
			}
			
			if(document.policyMasterForm.add_on_payment_factor){
				document.policyMasterForm.add_on_payment_factor.value =
						trim(document.policyMasterForm.add_on_payment_factor.value) == '' ? 75 : trim(document.policyMasterForm.add_on_payment_factor.value);
				if (!validateDecimal(document.policyMasterForm.add_on_payment_factor, "Add on payment factor must be a valid number", 2))
					return false;

				var margPer = getAmount(document.policyMasterForm.add_on_payment_factor.value);
				if (margPer > 100) {
					alert("Add on payment factor cannot be greater than 100%");
					document.policyMasterForm.add_on_payment_factor.focus();
					return false;
				}
			}



			if(document.policyMasterForm.perdiem_copay_per){
				document.policyMasterForm.perdiem_copay_per.value =
						trim(document.policyMasterForm.perdiem_copay_per.value) == '' ? 0 : trim(document.policyMasterForm.perdiem_copay_per.value);
				if (!validateDecimal(document.policyMasterForm.perdiem_copay_per, "Perdiem Co-pay(%) must be a valid number", 2))
					return false;

				var perdiemPer = getAmount(document.policyMasterForm.perdiem_copay_per.value);
				if (perdiemPer > 100) {
					alert("Perdiem Co-pay(%) cannot be greater than 100%");
					document.policyMasterForm.perdiem_copay_per.focus();
					return false;
				}
			}

			if(document.policyMasterForm.perdiem_copay_amount){
				document.policyMasterForm.perdiem_copay_amount.value =
						trim(document.policyMasterForm.perdiem_copay_amount.value) == '' ? 0 : trim(document.policyMasterForm.perdiem_copay_amount.value);
				if (!validateAmount(document.policyMasterForm.perdiem_copay_amount, "Perdiem Co-pay Amount must be a valid amount"))
					return false;

				if (getPaise(document.policyMasterForm.perdiem_copay_amount.value) !=0
						&& perdiemPer != 0) {
					alert("Please enter either Perdiem Co-pay(%) or Perdiem Co-pay Amount.");
					if(document.policyMasterForm.perdiem_copay_per)
						document.policyMasterForm.perdiem_copay_per.focus();

					return false;
				}
			}

			if(document.policyMasterForm.op_visit_copay_limit){
				document.policyMasterForm.op_visit_copay_limit.value =
						trim(document.policyMasterForm.op_visit_copay_limit.value) == '' ? 0 : trim(document.policyMasterForm.op_visit_copay_limit.value);
				if (!validateAmount(document.policyMasterForm.op_visit_copay_limit, "OP Co-pay Limit must be a valid amount"))
					return false;
			}
			if(document.policyMasterForm.ip_visit_copay_limit){
				document.policyMasterForm.ip_visit_copay_limit.value =
						trim(document.policyMasterForm.ip_visit_copay_limit.value) == '' ? 0 : trim(document.policyMasterForm.ip_visit_copay_limit.value);
				if (!validateAmount(document.policyMasterForm.ip_visit_copay_limit, "IP Co-pay Limit must be a valid amount"))
					return false;
			}

			if(document.getElementById("plan_code")) {
				var planCode = document.getElementById('plan_code').value;
				if(planCode !=null && planCode.trim() !=""){
					var planCodeLength = planCode.trim().length;
						if(planCodeLength<4){
							alert("Plan code must be atleast 4 characters in length.");
							return false;
						}
				}
			}
			if(document.getElementById("insurance_validity_start_date") != null)
				var fromDt = getDateFromField(document.getElementById("insurance_validity_start_date"));

			if(document.getElementById("insurance_validity_end_date") != null)
				var toDt = getDateFromField(document.getElementById("insurance_validity_end_date"));

			var dateCompareMsg = "Plan Valid Till Date cannot be less than Plan Valid From Date";
			var tillDate = "Please Enter Plan Valid Till Date";
			var fromDate = "Please Enter Plan Valid From Date";
			/*
			if(corporateInsu == 'Y' && (document.policyMasterForm.insurance_validity_start_date.value == null || document.policyMasterForm.insurance_validity_start_date.value=='')){
			 	alert(fromDate);
			 	document.getElementById("insurance_validity_start_date").focus();
			 	return false;
			}
			*/
			if(!validatePolicyDatesIsEmpty(fromDt,toDt,tillDate,fromDate)){
				return false;
			}

			if(!validatePolicyDates(fromDt,toDt)){
				alert(dateCompareMsg);
				document.getElementById("insurance_validity_end_date").focus();
				return false;
			}
            
			
			var op_planlimit = document.getElementById('op_planlimit').value.trim();
        	if (isNaN(op_planlimit)) {
        		  alert("please enter only numbers in plan Limit");
        		  document.getElementById('op_planlimit').focus();
        		  return false;
        		}
        	var op_episode_visit = document.getElementById('op_episode_limit').value.trim();
	       	if (isNaN(op_episode_visit)) {
	               alert("please enter only numbers in Sponsor Episode Visit OP Limit");
	               document.getElementById('op_episode_visit').focus();
	               return false;
	        }
	       	
        	var op_visit_limit = document.getElementById('op_visit_limit').value.trim();
        	if (isNaN(op_visit_limit)) {
               alert("please enter only numbers in Visit episode Sponsor Limit");
               document.getElementById('op_visit_limit').focus();
               return false;
             }
           
            var op_visit_deductible = document.getElementById('op_visit_deductible').value.trim();
            if (isNaN(op_visit_deductible)) {
                 alert("please enter only numbers in Visit Deductible");
                 document.getElementById('op_visit_deductible').focus();
                 return false;
                }
            var op_visit_copay = document.getElementById('op_visit_copay').value.trim();
            if (isNaN(op_visit_copay)) {
                 alert("please enter only numbers in Visit Copay ");
                 document.getElementById('op_visit_copay').focus();
                 return false;
                }
            var op_visit_copay_limit = document.getElementById('op_visit_copay_limit').value.trim();
            if (isNaN(op_visit_copay_limit)) {
                 alert("please enter only numbers in Visit Max Copay ");
                 document.getElementById('op_visit_copay_limit').focus();
                 return false;
                }
            
           	var ip_planlimit = document.getElementById('ip_planlimit').value.trim();
        	if (isNaN(ip_planlimit)) {
        		  alert("please enter only numbers in plan Limit");
        		  document.getElementById('ip_planlimit').focus();
        		  return false;
        		}
        	
           
            var episode_visit_ip = document.getElementById('episode_visit_ip').value.trim();
            if (isNaN(episode_visit_ip)) {
                 alert("please enter only numbers in Visit Sponsor Limit");
                 document.getElementById('episode_visit_ip').focus();
                 return false;
                }
           
          
           	var ip_per_day_limit = document.getElementById('ip_per_day_limit').value.trim();
           	if (isNaN(ip_per_day_limit)) {
           		  alert("please enter only numbers in Per Day Limit");
           		document.getElementById('ip_per_day_limit').focus();
           		  return false;
           		}
           	
           	var ip_visit_deductible = document.getElementById('ip_visit_deductible').value.trim();
            if (isNaN(ip_visit_deductible)) {
                 alert("please enter only numbers in Visit Deductible");
                 document.getElementById('ip_visit_deductible').focus();
                 return false;
                }
            var ip_visit_copay = document.getElementById('ip_visit_copay').value.trim();
            if (isNaN(ip_visit_copay)) {
                 alert("please enter only numbers in Visit Copay ");
                 document.getElementById('ip_visit_copay').focus();
                 return false;
                }
            var ip_visit_copay_limit = document.getElementById('ip_visit_copay_limit').value.trim();
            if (isNaN(ip_visit_copay_limit)) {
                 alert("please enter only numbers in Visit Max Copay ");
                 document.getElementById('ip_visit_copay_limit').focus();
                 return false;
                }
            
            var from_date = document.getElementById('insurance_validity_start_date').value.trim();
            var to_date = document.getElementById('insurance_validity_end_date').value.trim();
            var from_date_array = from_date.split('-'); 
            var to_date_array = to_date.split('-');
            var Date1 = new Date();
            Date1.setFullYear(from_date_array[2],from_date_array[1]-1,from_date_array[0]);
            var Date2 = new Date();
            Date2.setFullYear(to_date_array[2],to_date_array[1]-1,to_date_array[0]);
            
            if (Date1 > Date2)
            {
          	  alert("To date cannot be less than from date ");
      		  document.getElementById('to_date').focus();
              return false;
            }
			
            var opCopayValue = parseFloat(document.getElementById('op_visit_copay').value.trim());
            var ipCopayValue = parseFloat(document.getElementById('ip_visit_copay').value.trim());
            if(opCopayValue > 100){
            	 alert("Copay value cannot be greater than  100 ");
            	 document.getElementById('op_visit_copay').focus();
                 return false;
            }
            if(ipCopayValue > 100){
           	 alert("Copay value cannot be greater than  100 ");
           	 document.getElementById('ip_visit_copay').focus();
                return false;
           }
            if (document.policyMasterForm.plan_code.value) {
            	document.policyMasterForm.plan_code.value = document.policyMasterForm.plan_code.value.trim();
            }
            var opVisitLimit = document.getElementById("op_visit_limit").value.trim();
            var opEpisodeLimit = document.getElementById("op_episode_limit").value.trim();
            
			var opPlanlimit = document.getElementById("op_planlimit").value.trim();
			var opVistdeductible = document.getElementById("op_visit_deductible").value.trim();
			var opVisitCopay = document.getElementById("op_visit_copay").value.trim();
			
			if(!opVisitLimit)
				document.getElementById("op_visit_limit").value = '0.00';
			
			if(!opEpisodeLimit)
				document.getElementById("op_episode_limit").value = '0.00';
			
			if(!opPlanlimit)
				document.getElementById("op_planlimit").value= '0.00';
			if(!opVistdeductible)
				document.getElementById("op_visit_deductible").value= '0.00';
			if(!opVisitCopay)
				document.getElementById("op_visit_copay").value= '0.00';
			if(!document.getElementById("op_visit_copay_limit").value.trim())
				document.getElementById("op_visit_copay_limit").value= '0.00';
			
			
			if(!document.getElementById("ip_planlimit").value.trim())
				document.getElementById("ip_planlimit").value= '0.00';
			if(!document.getElementById("ip_visit_deductible").value.trim())
				document.getElementById("ip_visit_deductible").value= '0.00';
			if(!document.getElementById("ip_visit_copay").value.trim())
				document.getElementById("ip_visit_copay").value= '0.00';
			if(!document.getElementById("episode_visit_ip").value.trim())
				document.getElementById("episode_visit_ip").value= '0.00';
			if(!document.getElementById("ip_per_day_limit").value.trim())
				document.getElementById("ip_per_day_limit").value= '0.00';
			if(!document.getElementById("ip_visit_copay_limit").value.trim())
				document.getElementById("ip_visit_copay_limit").value= '0.00';
            
			document.policyMasterForm.submit();
			return true;
		}

		function validatePolicyDates(fromDt,toDt) {
			if ((toDt != null) && (fromDt != null)) {
			if (fromDt > toDt) {
				return false;
					}
				}
			return true;
		}

		function validatePolicyDatesIsEmpty(fromDt,toDt,tillDate,fromDate) {
			if (((toDt == '') && (fromDt != ''))){
				alert(tillDate);
				document.getElementById("insurance_validity_end_date").focus();
				return false;
			}
			if (((toDt != '') && (fromDt == ''))){
				alert(fromDate);
				document.getElementById("insurance_validity_start_date").focus();
				return false;
			}
			return true;
		}



	function selectAllForDiscounts(patTyp) {
		var disCheckElmts;
		if(patTyp == 'o')
			disCheckElmts = document.policyMasterForm.chkbx_o;
		else
			disCheckElmts = document.policyMasterForm.chkbx_i;
		if (disCheckElmts != undefined) {
			if (document.getElementById("selAllChkbx" + patTyp).checked) {
				for (var i = 0; i < disCheckElmts.length; i++) {
					disCheckElmts[i].checked = true;
				}
			} else {
				for (var i = 0; i < disCheckElmts.length; i++)
					disCheckElmts[i].checked = false;
			}
		}
	}

	function checkifselAllChkbxChkd(patTyp){
		if(document.getElementById("selAllChkbx"+patTyp).checked)
			document.getElementById("selAllChkbx"+patTyp).checked= false;
	}


 function enterAlphanNumericnSpecial(e) {
    var key=0;
	if(window.event){
		key = e.keyCode;
	} else {
		key = e.which;
	}
    if((key>=65)&&(key<=90)||(key>=48)&&(key<=57) ||(key>=97)&&(key<=122)||key==8||key==9||key==32 || key==38||key==40||key==41||key==44 || key==0) {
       key=key;
       return true;
    } else {
      return false;
    }
}
 
 function updatevalues(Object){
	 if(Object.checked) {
		 if(Object.id == 'op_episode_visit'){
			 document.getElementById('op_visit_limit').readOnly = true;
			 document.getElementById('op_episode_limit').readOnly = false;
		 }
	 }else {
		 document.getElementById('op_visit_limit').readOnly = false;
		 document.getElementById('op_episode_limit').readOnly = true;
	 }
 }

 function populateInsuranceTpaname() {
		var i=0;
		var tpaSel = document.createElement('select');
		var prevtpaSel = document.getElementById('sponsor_id');
		prevtpaSel.parentNode.replaceChild(tpaSel,prevtpaSel);
		tpaSel.id = "sponsor_id";
		tpaSel.name = "sponsor_id";
		tpaSel.setAttribute ('class','dropdown');
		var existtpa = document.getElementById('tpaid').value;
		//tpaSel.disabled = true;
		//var method = document.getElementsByName("_method")[0].value;
		var tpaOpt1 = document.createElement('option');
	  	  tpaOpt1.text = '-- Select --';
		  tpaOpt1.value = '';
		  tpaSel.appendChild(tpaOpt1);
		
		 for(var j=0; j<insuNameTpaList.length; j++) {
			var tpaEle = insuNameTpaList[j];
			if(tpaEle.insurance_co_id == document.policyMasterForm.insurance_co_id.value) {
				//catSel.removeAttribute("disabled");
				//tpaSel.length = i;
				if(tpaEle.tpa_id == existtpa){
					var tpaOpt = document.createElement('option');
					tpaOpt.text = tpaEle.tpa_name;
					tpaOpt.value = tpaEle.tpa_id;
					tpaOpt.selected = 'selected';
					tpaSel.appendChild(tpaOpt);
					
				}else{
				var tpaOpt = document.createElement('option');
				tpaOpt.text = tpaEle.tpa_name;
				tpaOpt.value = tpaEle.tpa_id;
				tpaSel.appendChild(tpaOpt);
				}
				i++
			}
	     }
		 
		  if(i==0){
			  
			   for(var j=0; j<tpaLists.length; j++) {
					var tpaEle = tpaLists[j];
					//tpaSel.length = i;
					if(tpaEle.tpa_id == existtpa){
						var tpaOpt = document.createElement('option');
						tpaOpt.text = tpaEle.tpa_name;
						tpaOpt.value = tpaEle.tpa_id;
						tpaOpt.selected = 'selected';
						tpaSel.appendChild(tpaOpt);
					}else{
					var tpaOpt = document.createElement('option');
				    tpaOpt.text = tpaEle.tpa_name;
					tpaOpt.value = tpaEle.tpa_id;
					tpaSel.appendChild(tpaOpt);
					}
					i++	
			 	}
		 }	  
 } 		
 
 function onVisitSponsorLimit(obj,type){
	 	var visitsponsorlimit=obj.value;
		if(type=='op'){
			document.getElementById('op_visit_limit').value =formatAmountPaise(getPaise(visitsponsorlimit));
			document.getElementById("op_episode_limit").value= '0.00';
		}
		if(type=='ip'){
			document.getElementById('episode_visit_ip').value =formatAmountPaise(getPaise(visitsponsorlimit));
		}

	}
 function onVisitEpisodeSponsorLimit(obj,type){
	 var episodeSponsorLimit = obj.value;
	 document.getElementById('op_episode_limit').value =formatAmountPaise(getPaise(episodeSponsorLimit));
	 document.getElementById("op_visit_limit").value= '0.00';
 }
 
 function onPlanLimit(obj,type) {
	 var planlimit = obj.value;
	 var availablelimit;
		if(type == 'op') {
			document.getElementById('op_planlimit').value =formatAmountPaise(getPaise(planlimit));
		}
		if(type == 'ip') {
			document.getElementById('ip_planlimit').value =formatAmountPaise(getPaise(planlimit));
		}
		
 }
 

	function onVisitdeductible(obj,type){
		var visitdeductible=obj.value;
		if(type=='op'){
			document.getElementById('op_visit_deductible').value =formatAmountPaise(getPaise(visitdeductible));
		}
		if(type=='ip'){
			document.getElementById('ip_visit_deductible').value =formatAmountPaise(getPaise(visitdeductible));
		}
	}
	
	function onVisitCopay(obj,type){
		var visitcopay=obj.value;
		if(type=='op'){
			document.getElementById('op_visit_copay').value =formatAmountPaise(getPaise(visitcopay));
		}
		if(type=='ip'){
			document.getElementById('ip_visit_copay').value =formatAmountPaise(getPaise(visitcopay));
		}
	}

	function onMaxCopay(obj,type){
		var maxcopay=obj.value;
		if(type=='op'){
			document.getElementById('op_visit_copay_limit').value =formatAmountPaise(getPaise(maxcopay));
		}
		if(type=='ip'){
			document.getElementById('ip_visit_copay_limit').value =formatAmountPaise(getPaise(maxcopay));
		}
	}
	
	function onPerDayLimit(obj) {
		var perdaylimit=obj.value;
			document.getElementById('ip_per_day_limit').value =formatAmountPaise(getPaise(perdaylimit));
		
	
	}
	
	var addCategoryDialog;
	function initDialog2() {
		addCategoryDialog = new YAHOO.widget.Dialog('addCategoryDialog', {
	        width:"500px",
	        visible: false,
	        modal: true,
	        constraintoviewport: true

	    });
	    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                              { fn:handleDialogCancel,
                                                scope:addCategoryDialog,
                                                correctScope:true } );
        var entKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
                                              { fn:addToTable,
                                                scope:addCategoryDialog,
                                                correctScope:true } );
        addCategoryDialog.cfg.queueProperty("keylisteners", [escKeyListener,entKeyListener]);
        addCategoryDialog.cancelEvent.subscribe(handleDialogCancel);
        addCategoryDialog.render();
	}
	

	function handleDialogCancel() {
		document.getElementById('addCategoryDialog').style.display = 'none';
		document.getElementById('addCategoryDialog').style.visibility = 'hidden';
		document.getElementById("insurance_item_category").options.selectedIndex = 0;
		addCategoryDialog.hide();
	}
	
	function addToTable() {
		var insuranceCatId = document.getElementById('insurance_item_category').options[document.getElementById('insurance_item_category').selectedIndex].value;
		var insuranceCatName = document.getElementById('insurance_item_category').options[document.getElementById('insurance_item_category').selectedIndex].text;
		if (trim(insuranceCatId) == '') {
			alert('Please Select Insurance Item Category, Or Close it');
			document.getElementById("insurance_item_category").focus();
			return false;
		} 
		var patType = document.getElementById('_dlg_patient_type').value ;
		var insuCatId = document.getElementsByName("insurance_category_id_"+patType);
		for (var i=0;i<insuCatId.length;i++) {
			if(insuCatId[i].value == insuranceCatId) {
				alert("Duplicate Insurance Item Category is not allowed");
				document.getElementById("insurance_item_category").options.selectedIndex = 0;
				document.getElementById("insurance_item_category").focus();
				return false;

			}
		}
		
		var isInsurancePayable;
	    for(var j=0; j<jChargeHeads.length;j++){
	    	var ele = jChargeHeads[j];
	    	if(ele["insurance_category_id"] == insuranceCatId){
	    		isInsurancePayable = ele["insurance_payable"];
	    	}
	    }
	    var limitTypeElement = document.getElementById("limit_type");
	    var limitType = limitTypeElement.options[limitTypeElement.selectedIndex].value;
	    insertNewRow(insuranceCatId, insuranceCatName, isInsurancePayable, patType,'N',limitType);
	    document.getElementById("insurance_item_category").options.selectedIndex = 0;
	}
	function insertNewRow(insuranceCatId, insuranceCatName, isInsurancePayable, patType, systemCategory, limitType) {		
		var tableObj = document.getElementById('chargesTable_'+patType);
		var len = tableObj.rows.length;
		var templateRow = tableObj.rows[len-1];
	   	var row = '';
	   	row = templateRow.cloneNode(true);
	   	if(systemCategory =='N') {
	   		row.style.display = '';
	   		YAHOO.util.Dom.insertBefore(row, templateRow);
	   	} else {
	   		row.style.display = 'none';
	   		numberOfSystemCategory++;
	   		var systemCategoryRow = tableObj.rows[0];
	   		YAHOO.util.Dom.insertAfter(row, systemCategoryRow);
	   	}
	   	
	   	var checkBox = document.createElement("INPUT");
	    if(isInsurancePayable == 'Y' && limitType != 'R' && systemCategory == 'N'){
	    	checkBox.setAttribute("type", "checkbox");
	    	checkBox.setAttribute("name", "chkbx_"+patType);
	    	checkBox.setAttribute("value", insuranceCatId);
	    	checkBox.setAttribute("onclick", "checkifselAllChkbxChkd('"+patType+"')");
	    }else {
	    	checkBox.setAttribute("type", "checkbox");
	    	checkBox.setAttribute("name", "dummyChkbox");
	    	checkBox.setAttribute("value", "");
	    }
		
		var cell0 = document.createElement("TD");
		cell0.appendChild(checkBox);
		
		var x0 = document.createElement("INPUT");
		x0.setAttribute("type", "hidden");
		x0.setAttribute("name", "insurance_category_id");
		x0.setAttribute("value", insuranceCatId);
		
		var x1 = document.createElement("INPUT");
		x1.setAttribute("type", "hidden");
		x1.setAttribute("name", "insurance_category_id_"+patType);
		x1.setAttribute("value", insuranceCatId);
		
		if(systemCategory =='N') {
			var tempRow = document.createElement("INPUT");
			tempRow.setAttribute("type", "hidden");
			tempRow.setAttribute("name", "edit_insu_cat_id_"+patType);
			tempRow.setAttribute("value", insuranceCatId);
		}
		var x2 = document.createElement("INPUT");
		x2.setAttribute("type", "hidden");
		x2.setAttribute("name", "patient_type");
		x2.setAttribute("value", patType);
		
		var x3 = document.createElement("INPUT");
		x3.setAttribute("type", "hidden");
		x3.setAttribute("name", "insurance_category_name");
		x3.setAttribute("value", insuranceCatName);
		
		var x4 = document.createElement("INPUT");
		x4.setAttribute("type", "hidden");
		x4.setAttribute("name", "deleted");
		x4.setAttribute("id", insuranceCatId+"_deleted_"+patType);
		x4.setAttribute("value", false);
		
		var x5 = document.createElement("INPUT");
		x5.setAttribute("type", "hidden");
		x5.setAttribute("name", "category_payable");
		x5.setAttribute("id", insuranceCatId+"_category_payable_"+patType);
		x5.setAttribute("value", isInsurancePayable);
		
		var x6 = document.createElement("INPUT");
		x6.setAttribute("type", "hidden");
		x6.setAttribute("name", "category_prior_auth_required");
		x6.setAttribute("id", insuranceCatId+"_category_prior_auth_required_"+patType);
		x6.setAttribute("value", "");
		
		var x12 = document.createElement("INPUT");
		x12.setAttribute("type", "hidden");
		x12.setAttribute("name", "insurance_plan_details_id");
		x12.setAttribute("value", "");

		var cell1 = document.createElement("TD");
		cell1.innerHTML = insuranceCatName;
		cell1.appendChild(x0);
		cell1.appendChild(x1);
		if(systemCategory =='N') {
			cell1.appendChild(tempRow);
		}
		cell1.appendChild(x2);
		cell1.appendChild(x3);
		cell1.appendChild(x4);
		cell1.appendChild(x5);
		cell1.appendChild(x6);
		cell1.appendChild(x12);

		var treatLbl = document.createElement("LABEL");
	    var textTreatLbl = document.createTextNode("");
		treatLbl.setAttribute("id", insuranceCatId+"_treatLbl_"+patType);
		treatLbl.appendChild(textTreatLbl);
		
		var x7 = document.createElement("INPUT");
		x7.setAttribute("type", "hidden");
		x7.setAttribute("name", "per_treatment_limit");
		x7.setAttribute("id", insuranceCatId+"_treatHidden_"+patType);
		x7.setAttribute("value", "");
		
		var cell2 = document.createElement("TD");
		cell2.setAttribute("style", "text-align:right;");
		cell2.appendChild(treatLbl);
		cell2.appendChild(x7);
		
		var catAmtLbl = document.createElement("LABEL");
		var textCatAmtLbl = document.createTextNode('0.00');
		catAmtLbl.setAttribute("id", insuranceCatId+"_catAmtLbl_"+patType);
		catAmtLbl.appendChild(textCatAmtLbl);
		
		var x8 = document.createElement("INPUT");
		x8.setAttribute("type", "hidden");
		x8.setAttribute("name", "patient_amount_per_category");
		x8.setAttribute("id", insuranceCatId+"_catAmtHidden_"+patType);
		x8.setAttribute("value", 0);
		
		var cell3 = document.createElement("TD");
		cell3.setAttribute("style", "text-align:right;");
		cell3.appendChild(catAmtLbl);
		cell3.appendChild(x8);
		
		var amtLabel = document.createElement("LABEL");
		var textAmtLabel = document.createTextNode('0.00');
		amtLabel.setAttribute("id", insuranceCatId+"_amtLbl_"+patType);
		amtLabel.appendChild(textAmtLabel);
		
		var x9 = document.createElement("INPUT");
		x9.setAttribute("type", "hidden");
		x9.setAttribute("name", "patient_amount");
		x9.setAttribute("id", insuranceCatId+"_amtHidden_"+patType);
		x9.setAttribute("value", 0);
		
		var cell4 = document.createElement("TD");
		cell4.setAttribute("style", "text-align:right;");
		cell4.appendChild(amtLabel);
		cell4.appendChild(x9);
		
		var perLbl = document.createElement("LABEL");
		var textPerLbl = document.createTextNode('0.00');
		perLbl.setAttribute("id", insuranceCatId+"_perLbl_"+patType);
		perLbl.appendChild(textPerLbl);
		
		var percentLabel = document.createTextNode("%");
		
		var x10 = document.createElement("INPUT");
		x10.setAttribute("type", "hidden");
		x10.setAttribute("name", "patient_percent");
		x10.setAttribute("id", insuranceCatId+"_perHidden_"+patType);
		x10.setAttribute("value", 0.00);
		
		var cell5 = document.createElement("TD");
		cell5.setAttribute("style", "text-align:right;");
		cell5.appendChild(perLbl);
		cell5.appendChild(percentLabel);
		cell5.appendChild(x10);
		
		var capLbl = document.createElement("LABEL");
	    var textCapLbl = document.createTextNode("");
	    capLbl.setAttribute("id", insuranceCatId+"_capLbl_"+patType);
	    capLbl.appendChild(textCapLbl);
		
		var x11 = document.createElement("INPUT");
		x11.setAttribute("type", "hidden");
		x11.setAttribute("name", "patient_amount_cap");
		x11.setAttribute("id", insuranceCatId+"_capHidden_"+patType);
		x11.setAttribute("value", "");
		
		var cell6 = document.createElement("TD");
		cell6.setAttribute("style", "text-align:right;");
		cell6.appendChild(capLbl);
		cell6.appendChild(x11);
		
		var cell7 = document.createElement("TD");
		cell7.setAttribute("style", "text-align:right;");
		
	    var img = document.createElement("img");
	    img.setAttribute("src", cpath + "/icons/delete.gif");
	    var anchor = document.createElement("A");
	    anchor.setAttribute("name", "trashIcon");
	    anchor.setAttribute("href", "javascript:Cancel Item");
	    anchor.setAttribute("title", "Cancel Item");
	    anchor.setAttribute("onclick", " return changeElsColor('"+insuranceCatId+"_deleted_"+patType+"', this);");
	    anchor.appendChild(img);
	    cell7.appendChild(anchor);
		
		
		var cell8 = document.createElement("TD");
		cell8.setAttribute("style", "text-align:right;");
		var imgEdit = document.createElement("img");
		if(isInsurancePayable == 'Y' && limitType != 'R') {
			imgEdit.setAttribute("id", insuranceCatId+"_img_"+patType);
			imgEdit.setAttribute("src", cpath + "/icons/Edit.png");
			imgEdit.setAttribute("onclick", "showDialog('"+insuranceCatId+"','"+patType+"');");
			cell8.appendChild(imgEdit);
		}else {
			imgEdit.setAttribute("src", cpath + "/icons/Edit1.png");
			cell8.appendChild(imgEdit);
		}
		
		row.appendChild(cell0);
		row.appendChild(cell1);
		row.appendChild(cell2);
		row.appendChild(cell3);
		row.appendChild(cell4);
		row.appendChild(cell5);
		row.appendChild(cell6);
		row.appendChild(cell7);
		row.appendChild(cell8);
	}
	
	function addInruranceItemCategories(obj, patType) {
		document.getElementById('addCategoryDialog').style.display='block';
		document.getElementById('addCategoryDialog').style.visibility='visible';
		document.getElementById('_dlg_patient_type').value = patType;
		var button1 = document.getElementById("addCategories_"+patType);
		addCategoryDialog.cfg.setProperty("context", [button1, "tr", "br"], false);
		addCategoryDialog.show();
	}
	
	function changeElsColor(elementId, obj) {

	var trObj = getThisRow(obj);
	var trashimgObj = trObj.cells[7].getElementsByTagName("img")[0];
	var markRowForDelete = document.getElementById(elementId).value == 'false' ? 'true'
			: 'false';
	document.getElementById(elementId).value = document
			.getElementById(elementId).value == 'false' ? 'true'
			: 'false';
	
	if (markRowForDelete == 'true') {
		trashimgObj.src = cpath + '/icons/undo_delete.gif';
	} else {
		trashimgObj.src = cpath + '/icons/delete.gif';
	}
	return false;
	}
	
	function ajaxCallForCategoryMapped() {
		var insuranceCompanyId = document.policyMasterForm.insurance_co_id.value;
		
		var xhttp = newXMLHttpRequest();
		var url = cpath + "/master/insuranceplans/getcategories.json?&insurance_co_id="+insuranceCompanyId;
		xhttp.open("GET", url.toString(), false);
		xhttp.send(null);
		if (xhttp.readyState == 4) {
			if ((xhttp.status == 200) && (xhttp.responseText != null)) {
				return xhttp.responseText;
			}
		}
		return null;
	}
	function categoriesMapped() {
		//deleting the existing data row
		var opTable = document.getElementById('chargesTable_o');
		var opRows = opTable.rows.length;
		for(i =1; i<opRows-1 ;i++){
			opTable.deleteRow(1);
		}
		var ipTable = document.getElementById('chargesTable_i');
		var ipRows = ipTable.rows.length;
		for(i =1; i<ipRows-1 ;i++){
			ipTable.deleteRow(1);
		}
		
		//insert selected insurance company mapped category
		var data = JSON.parse(ajaxCallForCategoryMapped());
		numberOfSystemCategory = 0;
		var limitTypeElement = document.getElementById("limit_type");
		var limitType = limitTypeElement.options[limitTypeElement.selectedIndex].value;
		for (i = 0; i < data.chargeBean.length; i++) {
			var insuranceCatId = data.chargeBean[i].insurance_category_id;
			var insuranceCatName = data.chargeBean[i].insurance_category_name;
			var isInsurancePayable = data.chargeBean[i].category_payable;
			var patType = data.chargeBean[i].patient_type;
			var systemCategory = data.chargeBean[i].system_category;
			insertNewRow(insuranceCatId, insuranceCatName, isInsurancePayable, patType, systemCategory, limitType);       	
		}
	}

	function onEnablePreAuthAmtLimit(obj) {
		if (obj.checked) {
			document.getElementById('enable_pre_authorized_limitz').value = 'Y';
			document.getElementById('enable_pre_authorized_limit').value = 'Y';
			document.getElementById('excluded_charge_groups_list').disabled = false;
			document.getElementById('op_pre_authorized_amount').disabled = false;
		} else {
			document.getElementById('enable_pre_authorized_limitz').value = 'N';
			document.getElementById('enable_pre_authorized_limit').value = 'N';
			document.getElementById('excluded_charge_groups_list').disabled = true;
			document.getElementById('op_pre_authorized_amount').disabled = true;
		}
	}

	function onExcludeChargeGroupChange(obj) {
		var excludedGroups = $("#excluded_charge_groups_list").val();
		if (excludedGroups) {
			document.getElementById("excluded_charge_groups").value = excludedGroups.join(',');
		}
	}