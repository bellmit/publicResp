function getLastDayForMonth(month, year) {
	var dt = new Date(parseInt(year),parseInt(month),0);
	return [(dt.getDate() < 10 ? ('0'+ dt.getDate()) : dt.getDate()),(dt.getMonth() < 9 ? ('0'+ (dt.getMonth()+1)) : (dt.getMonth()+1)),dt.getFullYear()].join('-');
}
function getLastDayForMonthSQL(month, year) {
	var dt = new Date(parseInt(year),parseInt(month),0);
	return [dt.getFullYear(),(dt.getMonth() < 9 ? ('0'+ (dt.getMonth()+1)) : (dt.getMonth()+1)),(dt.getDate() < 10 ? ('0'+ dt.getDate()) : dt.getDate())].join('-');
}

function enterNumOnlyANDdot(e)
{
   var key=0;
	if(window.event || !e.which)
	{
		key = e.keyCode;
   	}
	else
	{
		key = e.which;
	}

     if((key>=48)&&(key<=57)||key==8||key==9||key==46)
     {
        key=key;
        return true;
     }
     else
     {
       key=0;
       return false;
     }

}

//----------------------------------  StockEntry BEGIN ----------------------------------------------------

		function loadGRNDetails(){
		var grn	=	document.getElementById("grnNos").options[document.getElementById("grnNos").selectedIndex].value;
		var grnDetailsList = document.getElementById("GRN").getElementsByTagName("GRNs");
		var grnDetailsListRoot = grnDetailsList.item(0);
		grnDetailsOffList = grnDetailsListRoot.getElementsByTagName("grn");
		var grnDetailsLength = grnDetailsOffList.length;
		index2 = 0
		for(var grnDetailsItr=0;grnDetailsItr<grnDetailsLength;grnDetailsItr++){
			grnDetailsOff=grnDetailsOffList.item(grnDetailsItr);
			if(grn==grnDetailsOff.attributes.getNamedItem('class2').nodeValue){
				document.getElementById("poNo").value				=	grnDetailsOff.attributes.getNamedItem('class1').nodeValue;
				document.getElementById("grnNo").value				=	grnDetailsOff.attributes.getNamedItem('class2').nodeValue;
				document.getElementById("poDate").value				=	grnDetailsOff.attributes.getNamedItem('class3').nodeValue;
				document.getElementById("grnDate").value			=	grnDetailsOff.attributes.getNamedItem('class4').nodeValue;
				document.getElementById("invoiceNo").value			=	grnDetailsOff.attributes.getNamedItem('class5').nodeValue;
				document.getElementById("invoiceDate").value		=	grnDetailsOff.attributes.getNamedItem('class6').nodeValue;
				document.getElementById("docNo").value				=	grnDetailsOff.attributes.getNamedItem('class7').nodeValue;
				document.getElementById("docDate").value			=	grnDetailsOff.attributes.getNamedItem('class8').nodeValue;
				document.getElementById("deliveryDate").value		=	grnDetailsOff.attributes.getNamedItem('class9').nodeValue;
				document.getElementById("supplierName").value		=	grnDetailsOff.attributes.getNamedItem('class10').nodeValue;
				//document.getElementById("stockEntryDate").value		=	getCurrentDate();
				getGrnItemDetails();
			}
		}
	}

    function checkReadyState(obj){
		 if(obj.readyState == 4){
			if(obj.status == 200){
					return true;
			}
		 }
  }

	function onResponse(){
		if(checkReadyState(reqObject))
		{
		 	resText=reqObject.responseXML;
		 	//alert("----------"+resText);
		 	if(resText!=null)
	        {
	            x=reqObject.responseXML.documentElement;
			 	getXMLData();
			}
		}
  }

	function getGrnItemDetails(){

	    document.getElementById("chk3").style.display='block';
		document.getElementById("chk4").style.display='block';
		if(window.XMLHttpRequest){
			reqObject=new  XMLHttpRequest();
		}else if(window.ActiveXObject){
			reqObject=new ActiveXObject("MSXML2.XMLHTTP");
		}
            //alert(reqObject);
			reqObject.onreadystatechange=onResponse;


			var url="../../pages/pharmacy/StockEntry.do?method=getGRNMedicineDetails&grnNo="+document.getElementById("grnNos").options[document.getElementById("grnNos").selectedIndex].value;
			reqObject.open("POST",url.toString(), true);
			reqObject.setRequestHeader("Content-Type", "text/xml");
			reqObject.send(null);
	}

    var index2 = 0;
	var xmlDataLen =0;
	function getXMLData(){
		xmlDataLen = x.childNodes[0].childNodes.length;
	  	var nodes = x.childNodes[0].childNodes;
	  	if(xmlDataLen != 0){
	   		//var grnDetailsDivObj = document.getElementById("grnDetailsDiv");
	  		//grnDetailsDivObj.style.display = "none";
	  		//var buttonDivObj = document.getElementById("buttonDiv");
	  		//buttonDivObj.style.display = "none";
	  		//}else{
	  		var grnDetailsTableObj = document.getElementById("grnDetailsItemTable");
	  		var grnDetailsDivObj = document.getElementById("grnDetailsDiv");
	  		grnDetailsDivObj.style.display = "block";
	  		var buttonDivObj = document.getElementById("buttonDiv");
	  		buttonDivObj.style.display = "block";

	  		var medicineId="",medicineName="",batchCode="",bonusQuantity=0,qtySupplied=0,totalQty=0,uomCode="",uomName="";
	  		var qtyPerUnit=0,expiryDate="",binRack="",costPrice=0,mrp=0,totalAmount=0;

	 		// To delete previous content of the page inorder to render every time refreshed data
			if(grnDetailsTableObj.rows.length>0){
				for(d=0;grnDetailsTableObj.rows.length>0;d++){
					grnDetailsTableObj.deleteRow((grnDetailsTableObj.rows.length)-1);
				}
			}

	  		grnDetailsTableObj.width= '900px';
	  		var trObj="",tdObj="";

			// Header BEGIN
  			trObj = grnDetailsTableObj.insertRow(index2);
			trObj.className="poclass";
			tdObj = trObj.insertCell(0);
			tdObj.innerHTML = 'Select';
			tdObj = trObj.insertCell(1);
			tdObj.innerHTML = 'Medicine Name';
			tdObj = trObj.insertCell(2);
			tdObj.innerHTML = 'Batch No';
			tdObj = trObj.insertCell(3);
			tdObj.innerHTML = 'Bonus Quantity';
			tdObj = trObj.insertCell(4);
			tdObj.innerHTML = 'Qty Supplied';
			tdObj = trObj.insertCell(5);
			tdObj.innerHTML = 'Total Quantity';
			tdObj = trObj.insertCell(6);
			//tdObj.innerHTML = 'UOM';
			//tdObj = trObj.insertCell(7);
			tdObj.innerHTML = 'Quantity/Unit';
			tdObj = trObj.insertCell(7);
			tdObj.innerHTML = 'Expiry Date';
			tdObj = trObj.insertCell(8);
			tdObj.innerHTML = 'Cost Price';
			tdObj = trObj.insertCell(9);
			tdObj.innerHTML = 'Total Amount';
			tdObj = trObj.insertCell(10);
			tdObj.innerHTML = 'Bin/Rack';
			tdObj = trObj.insertCell(11);
			tdObj.innerHTML = 'MRP';
			//tdObj = trObj.insertCell(0);
			//tdObj.innerHTML = 'Se';

            ++index2;
			// Header END

	  		for(var xmlDataItr=0;xmlDataItr<xmlDataLen;xmlDataItr++){
				medicineId 		= 	nodes[xmlDataItr].attributes.getNamedItem('class1').nodeValue;
				medicineName 	= 	nodes[xmlDataItr].attributes.getNamedItem('class2').nodeValue;
				batchCode 		= 	nodes[xmlDataItr].attributes.getNamedItem('class3').nodeValue;
				bonusQuantity		= 	nodes[xmlDataItr].attributes.getNamedItem('class4').nodeValue;
				qtySupplied 	= 	nodes[xmlDataItr].attributes.getNamedItem('class5').nodeValue;
				totalQty 		= 	nodes[xmlDataItr].attributes.getNamedItem('class6').nodeValue;
				//uomCode 		= 	nodes[xmlDataItr].attributes.getNamedItem('class7').nodeValue;
				//uomName 		= 	nodes[xmlDataItr].attributes.getNamedItem('class8').nodeValue;
				qtyPerUnit 		= 	nodes[xmlDataItr].attributes.getNamedItem('class7').nodeValue;
				expiryDate 		= 	nodes[xmlDataItr].attributes.getNamedItem('class8').nodeValue;
				if(expiryDate=="") expiryDate="-";
				costPrice 		= 	nodes[xmlDataItr].attributes.getNamedItem('class9').nodeValue;
				if(costPrice=="") costPrice='0.0';
				totalAmount 	= 	nodes[xmlDataItr].attributes.getNamedItem('class10').nodeValue;
				if(totalAmount=="") totalAmount='0.0';
				mrp 			= 	nodes[xmlDataItr].attributes.getNamedItem('class11').nodeValue;

 				trObj = grnDetailsTableObj.insertRow(index2);
				trObj.className="innerHtmlText";
				tdObj = trObj.insertCell(0);
				tdObj.innerHTML = '<input type="checkbox" onClick="enableFields('+xmlDataItr+');" name="check" id="check'+xmlDataItr+'"  value="'+xmlDataItr+'">';
				tdObj = trObj.insertCell(1);
				tdObj.innerHTML = medicineName+'<input type="hidden" name="medicineId" value="'+medicineId+'">';
				tdObj = trObj.insertCell(2);
				tdObj.innerHTML = batchCode+'<input type="hidden" name="batchCode" value="'+batchCode+'">';
				tdObj = trObj.insertCell(3);
				tdObj.innerHTML = bonusQuantity;
				tdObj = trObj.insertCell(4);
				tdObj.innerHTML = qtySupplied;
				tdObj = trObj.insertCell(5);
				tdObj.innerHTML = totalQty;
				//tdObj = trObj.insertCell(6);
				//tdObj.innerHTML = uomName;
				tdObj = trObj.insertCell(6);
				tdObj.innerHTML = qtyPerUnit;
				tdObj = trObj.insertCell(7);
				tdObj.innerHTML = expiryDate;
				tdObj = trObj.insertCell(8);
				tdObj.innerHTML = costPrice;
				tdObj = trObj.insertCell(9);
				tdObj.innerHTML = totalAmount;
				tdObj = trObj.insertCell(10);
				tdObj.innerHTML = '<input type="text" size="6" readOnly="readOnly"  name="binRack" id="binRack'+xmlDataItr+'" value="" maxlength="9">';
				tdObj = trObj.insertCell(11);
				tdObj.innerHTML = '<input type="text" size="6" readOnly="readOnly" name="mrp" id="mrp'+xmlDataItr+'" value="'+mrp+'" maxlength="7" onkeypress="return enterNumOnlyANDdot(event);">';
				//tdObj = trObj.insertCell(0);
				//tdObj.innerHTML = '<input type="checkbox" onClick="enableFields('+xmlDataItr+');" name="check" id="check'+xmlDataItr+'"  value="'+xmlDataItr+'">';
                ++index2;

			}
	  	}
	}

	function enableFields(index){

		if(document.getElementById("check"+index).checked){
		    document.getElementById("mrp"+index).readOnly=false;
			document.getElementById("binRack"+index).readOnly=false;
		}else{
			document.getElementById("mrp"+index).readOnly=true;
			document.getElementById("binRack"+index).readOnly=true;
		}
	}

	function confirmStockEntry(){
		var checkStatus = false;
		if(xmlDataLen>0){
			for(var i=0;i<xmlDataLen;i++){
				if(document.getElementById("check"+i).checked){
					checkStatus = true;
					break;
				}
			}
			if(!checkStatus){
				alert("Check any medicine to enter the stock");
				return false;
			}else{
				for(var i=0;i<xmlDataLen;i++){
					if(document.getElementById("check"+i).checked){
						var binrack=document.getElementById("binRack"+i).value;
						var mrp=document.getElementById("mrp"+i).value;
						if(binrack==""){
							alert("Enter the Bin/Rack Number");
							document.getElementById("binRack"+i).focus();
							return false;

						}
						if(mrp==""){
							alert("Please Enter The MRP");
							document.getElementById("mrp"+i).focus();
							return false;
						}

					}
				}
			}

			if(confirm("Do you want to submit?")){
				document.forms[0].submit();
			}else return false;
		}
	}

//----------------------------------  GRN BEGIN ----------------------------------------------------
//GRN START HERE

			function loadPODetails(){
			var poNo	=	document.getElementById("poNos").options[document.getElementById("poNos").selectedIndex].value;
			if(poNo!=""){
					//alert("poNo="+poNo);
					//getPoItemDetails();
			}else{
				alert("Select any PO No");
				document.getElementById("poNos").focus();
				return false;
			}
			document.getElementById("aggregatePoDiscount").value=0;
			document.getElementById("invoiceAmount").value=0;
			document.getElementById("chk").style.display='block';
			document.getElementById("chk1").style.display='block';
			index1 =0;
			getPoMedicineDetails();
			document.getElementById("invoiceNo").focus();
		}


		function getPoMedicineDetails(){

		 if(window.XMLHttpRequest){
			reqObject=new  XMLHttpRequest();
		}else if(window.ActiveXObject){
			reqObject=new ActiveXObject("MSXML2.XMLHTTP");
		}
            //alert(reqObject);
			reqObject.onreadystatechange=onResponse1;


			var url="../../pages/pharmacy/grnGeneration.do?method=getPOMedicineDetails&poNo="+document.getElementById("poNos").options[document.getElementById("poNos").selectedIndex].value;
			reqObject.open("POST",url.toString(), true);
			reqObject.setRequestHeader("Content-Type", "text/xml");
			reqObject.send(null);


		//var ajaxReqObject = newXMLHttpRequest();
		//var url="../../pages/pharmacy/grnGeneration.do?method=getPOMedicineDetails&poNo="+document.getElementById("poNos").options[document.getElementById("poNos").selectedIndex].value;
		//getResponseHandler(ajaxReqObject,getPoXMLData,url);
	}
	var x1='';
	function onResponse1(){
		if(checkReadyState(reqObject))
		{
		 	resText=reqObject.responseXML;
		 	//alert("----------"+resText);
		 	if(resText!=null)
	        {
	            x1=reqObject.responseXML.documentElement;
			 	getPoXMLData();
			}
		}
  }
    var index1 =0;
    var itemDiscountType="",poDiscountType="",poDiscount=""
	var poxmlDataLen =0;
	var qtySum = 0;
	var qtyTot = 0;
	var vatTot = 0;
	var rateTot = 0;
	
	function getPoXMLData(){
		poxmlDataLen = x1.childNodes[0].childNodes.length;
	  	var nodes = x1.childNodes[0].childNodes;
	  	qtySum = x1.childNodes[0].childNodes.length;
	  	if(poxmlDataLen != 0){

	  		var grnDetailsTableObj = document.getElementById("grnDetailsItemTable");
	  		var grnDetailsDivObj = document.getElementById("grnDetailsDiv");
	  		grnDetailsDivObj.style.display = "block";
	  		var buttonDivObj = document.getElementById("buttonDiv");
	  		buttonDivObj.style.display = "block";



	  		document.getElementById("poDate").value=nodes[0].attributes.getNamedItem('class14').nodeValue;
	  		document.getElementById("vendorName").value=nodes[0].attributes.getNamedItem('class15').nodeValue;
	  		document.getElementById("deliveryDate").value=nodes[0].attributes.getNamedItem('class16').nodeValue;
            document.getElementById("aggregatePoDiscount").value = nodes[0].attributes.getNamedItem('class8').nodeValue;
            vatTot = parseFloat(nodes[0].attributes.getNamedItem('class22').nodeValue) + parseFloat(nodes[0].attributes.getNamedItem('class23').nodeValue);
            //alert("vatTot--->"+vatTot);
            for(var z=0;z<qtySum;z++){
               qtyTot = qtyTot + parseInt(nodes[0].attributes.getNamedItem('class7').nodeValue,10);
            }
            //alert("qtyTot===>"+qtyTot);
            rateTot = vatTot/qtyTot ;
            //alert("rateTot------"+rateTot);
	  		var medicineId="",medicineName="",orderQty=0,uom="",uom_id="",qtyPerUnit="",focQty="";unitCost="",itemDicCount="",vat=""
	  		var poDate="",supplierName="",deliveryDate="",receivedQty="",grnFocReceived="", receivedBatches=""
	  		var expiryDates="";
	  		var rate=parseInt(0,10);
	 		// To delete previous content of the page inorder to render every time refreshed data
			if(grnDetailsTableObj.rows.length>0){
				for(d=0;grnDetailsTableObj.rows.length>0;d++){
					grnDetailsTableObj.deleteRow((grnDetailsTableObj.rows.length)-1);
				}
			}

	  		grnDetailsTableObj.width= '1000px';
	  		var trObj="",tdObj="";

			// Header BEGIN

			trObj = grnDetailsTableObj.insertRow(index1);
			trObj.className="poclass";
			tdObj = trObj.insertCell(0);
			tdObj.innerHTML = 'Select';
			tdObj = trObj.insertCell(1);
			tdObj.innerHTML = 'Medicine Name';
			tdObj = trObj.insertCell(2);
			tdObj.innerHTML = 'Ordered Qty';
			tdObj = trObj.insertCell(3);
			tdObj.innerHTML = 'Received Qty';
			tdObj = trObj.insertCell(4);
			tdObj.innerHTML = 'Qty/Unit';
			tdObj = trObj.insertCell(5);
			//tdObj.innerHTML = 'UOM';
			//tdObj = trObj.insertCell(6);
			tdObj.innerHTML = 'Expiry Date (MM-YY)';
			tdObj = trObj.insertCell(6);
			tdObj.width= '1000';
			tdObj.innerHTML = 'Supplied Qty';
			tdObj = trObj.insertCell(7);
			tdObj.innerHTML = 'Request Bonus Qty';
			tdObj=trObj.insertCell(8);
			tdObj.innerHTML='Received Bonus Qty'
			tdObj = trObj.insertCell(9);
			tdObj.innerHTML = 'Bonus Qty';
			tdObj = trObj.insertCell(10);
			tdObj.width= '1000';
			tdObj.innerHTML = 'Batch No';
			tdObj = trObj.insertCell(11);
			tdObj.innerHTML = 'UnitPrice';
			tdObj = trObj.insertCell(12);
			tdObj.innerHTML = 'Total Qty';

			tdObj = trObj.insertCell(13);
			tdObj.innerHTML = 'Total Cost';
			tdObj = trObj.insertCell(14);
			tdObj.innerHTML = 'Discount';
			tdObj = trObj.insertCell(15);
			tdObj.innerHTML = '<div id="s1" style="display:none">PoDiscount</div>';
			tdObj = trObj.insertCell(16);
			tdObj.innerHTML = 'Item Taxable Cost';
			tdObj = trObj.insertCell(17);
			tdObj.innerHTML = 'VAT(%)';
			tdObj = trObj.insertCell(18);
			tdObj.innerHTML = 'Total Price(Including Tax)';
			tdObj = trObj.insertCell(19);
			tdObj.innerHTML = 'Landed Price(LP)';
			//tdObj = trObj.insertCell(0);
			//tdObj.innerHTML = 'Select';
            var all = "ALL";

			// Header END
            ++index1;
	  		for(var xmlDataItr=0;xmlDataItr<poxmlDataLen;xmlDataItr++){
				medicineId 		= 	nodes[xmlDataItr].attributes.getNamedItem('class1').nodeValue;
				medicineName 		= 	nodes[xmlDataItr].attributes.getNamedItem('class2').nodeValue;
				orderQty 		= 	parseInt(nodes[xmlDataItr].attributes.getNamedItem('class7').nodeValue);
				issuePerBaseUnit= 	nodes[xmlDataItr].attributes.getNamedItem('class6').nodeValue;
				//uom 	= 	nodes[xmlDataItr].attributes.getNamedItem('class17').nodeValue;
				//uomId=nodes[xmlDataItr].attributes.getNamedItem('class17').nodeValue;
				focQty 		= 	nodes[xmlDataItr].attributes.getNamedItem('class11').nodeValue;
				unitPrice 		= 	nodes[xmlDataItr].attributes.getNamedItem('class10').nodeValue;
				discount 		= 	nodes[xmlDataItr].attributes.getNamedItem('class5').nodeValue;
				poDiscount 		= 	nodes[xmlDataItr].attributes.getNamedItem('class8').nodeValue;
				vat 		= 	nodes[xmlDataItr].attributes.getNamedItem('class9').nodeValue;
				rate = parseFloat(parseFloat(nodes[xmlDataItr].attributes.getNamedItem('class3').nodeValue) + parseFloat(rateTot)).toFixed(2);
				itemDiscountType=nodes[xmlDataItr].attributes.getNamedItem('class13').nodeValue;
				poDiscountType=nodes[xmlDataItr].attributes.getNamedItem('class12').nodeValue;
				receivedQty=parseInt(nodes[xmlDataItr].attributes.getNamedItem('class17').nodeValue);
				grnFocReceived=nodes[xmlDataItr].attributes.getNamedItem('class18').nodeValue;
				receivedBatches=nodes[xmlDataItr].attributes.getNamedItem('class19').nodeValue;
				expiryDates=nodes[xmlDataItr].attributes.getNamedItem('class20').nodeValue;
				// if client is received full quantity for a particular medicine
				if(parseInt(orderQty)==parseInt(receivedQty)){
					var  dicountRupes1= 0;
					var  poDiscountInrupes1=0;
					var totalWithBonus=parseInt(receivedQty)+parseInt(grnFocReceived);
					var totalcost1=parseInt(orderQty,10)*parseFloat(unitPrice,10);
					if(itemDiscountType=='%'){
						 dicountRupes1=parseFloat(totalcost1)*parseFloat(discount/100);
				 	}else{
				 		dicountRupes1=parseInt(orderQty,10)*parseFloat(discount);
				 	}
					var afterRemovingDiscount1=(totalcost1-dicountRupes1).toFixed(2);
					if(poDiscountType=='RS'){
						poDiscountInrupes1=poDiscount;
					}else{
						poDiscountInrupes1=(parseFloat(afterRemovingDiscount1)*parseFloat(poDiscount/100)).toFixed(2);
					}
					var grntotalwithtax1=((totalcost1)-dicountRupes1-poDiscountInrupes1);
					totalCost1=totalcost1.toFixed(2);
					var afterRemovingAllDiscounts=parseFloat(grntotalwithtax1).toFixed(2);
					var itemtotalAmount=((rate)*(orderQty));
					var status="Goods Received"

					trObj = grnDetailsTableObj.insertRow(index1);
					trObj.className="innerHtmlText";

					tdObj = trObj.insertCell(0);
					tdObj.innerHTML = '<input type="checkbox" onClick="grnEnableFields('+xmlDataItr+');" name="check" id="check'+xmlDataItr+'"  value="'+xmlDataItr+'" tabindex="7">';

					tdObj = trObj.insertCell(1);
					tdObj.innerHTML = medicineName+'<input type="hidden" name="medicineId" value="'+medicineId+'">';
					tdObj = trObj.insertCell(2);
					tdObj.innerHTML = orderQty+'<input type="hidden" name="orderQty" id="orderQty'+xmlDataItr+'" value="'+orderQty+'">';
					tdObj = trObj.insertCell(3);
					tdObj.innerHTML = receivedQty+'<input type="hidden" name="receivedQty" id="receivedQty'+xmlDataItr+'" value="'+receivedQty+'">';
					tdObj = trObj.insertCell(4);
					tdObj.innerHTML = issuePerBaseUnit+'<input type="hidden" name="quantityPerUnit" value="'+issuePerBaseUnit+'">';
					//tdObj = trObj.insertCell(5);
					//tdObj.innerHTML = uom+'<input type="hidden" name="uomId" value="'+uomId+'">';;
					tdObj = trObj.insertCell(5);
					tdObj.innerHTML =expiryDates+'<input type="hidden" size="10" readOnly="readOnly"  name="expiryDate" id="expiryDate'+xmlDataItr+'" value="'+expiryDates+'" tabindex="8">';
					tdObj = trObj.insertCell(6);
					tdObj.width= '1000';
					tdObj.innerHTML =status+'<input type="hidden" size="6" readOnly="readOnly"  name="suppliedQty" id="suppliedQty'+xmlDataItr+'"  onchange="return calculateCost(\''+xmlDataItr+'\')"     value="'+status+'">';
					tdObj = trObj.insertCell(7);
					tdObj.innerHTML =focQty+'<input type="hidden" size="6"   name="focQty" id="focQty'+xmlDataItr+'" value="'+focQty+'">';
					tdObj = trObj.insertCell(8);
					tdObj.innerHTML =grnFocReceived+'<input type="hidden" size="6"   name="grnFocReceived" id="grnFocReceived'+xmlDataItr+'" value="'+grnFocReceived+'">';
					tdObj = trObj.insertCell(9);
					tdObj.innerHTML = '-'+'<input type="hidden" size="6" readOnly="readOnly"   name="receivedFocQty" id="receivedFocQty'+xmlDataItr+'" onchange="return calculateTotalQty(\''+xmlDataItr+'\')"  value="">';
					tdObj = trObj.insertCell(10);
					tdObj.width= '1000';
					tdObj.innerHTML =receivedBatches+'<input type="hidden" size="6" readOnly="readOnly"  name="batchNo" id="batchNo'+xmlDataItr+'" value="'+receivedBatches+'" maxlength="20">';
					tdObj = trObj.insertCell(11);
					tdObj.innerHTML =unitPrice+'<input type="hidden" size="6"   name="unitPrice" id="unitPrice'+xmlDataItr+'" value="'+unitPrice+'">';
					tdObj = trObj.insertCell(12);
					tdObj.innerHTML =totalWithBonus+'<input type="hidden" size="6" readOnly="readOnly" class="innerHTMLTextBoxHide"  name="totalQty" id="totalQty'+xmlDataItr+'" value="'+totalWithBonus+'">';

					tdObj = trObj.insertCell(13);
					tdObj.innerHTML =totalCost1+'<input type="hidden" size="6" readOnly="readOnly"  class="innerHTMLTextBoxHide" name="totalCost" id="totalCost'+xmlDataItr+'" value="'+totalcost1+'">';
					tdObj = trObj.insertCell(14);
					tdObj.innerHTML =discount+" ("+itemDiscountType+")"+'<input type="hidden" size="6" readOnly="readOnly" class="innerHTMLTextBoxHide" name="itemDiscountType" id="itemDiscountType'+xmlDataItr+'" value="'+itemDiscountType+'">'+'<input type="hidden" size="6"   name="itemDiscount" id="itemDiscount'+xmlDataItr+'" value="'+discount+'">';
					tdObj = trObj.insertCell(15);
					tdObj.innerHTML ='<div id="s3" style="display:none">'+poDiscount+" ("+poDiscountType+")"+'<input type="hidden" size="6" readOnly="readOnly" name="poDiscountType" id="poDiscountType'+xmlDataItr+'" value="'+poDiscountType+'">'+'<input type="hidden" size="6"   name="poDiscount" id="poDiscount'+xmlDataItr+'" value="'+poDiscount+'"></div>';
					tdObj = trObj.insertCell(16);
					tdObj.innerHTML =afterRemovingAllDiscounts+'<input type="hidden" size="6" readOnly="readOnly" class="innerHTMLTextBoxHide" name="taxableCost" id="taxableCost'+xmlDataItr+'" value="'+afterRemovingAllDiscounts+'">';
					tdObj = trObj.insertCell(17);
					tdObj.innerHTML =vat+'<input type="hidden" size="6"   name="vat" id="vat'+xmlDataItr+'" value="'+vat+'" class="innerHTMLTextBoxHide">';
					tdObj = trObj.insertCell(18);
					tdObj.innerHTML =itemtotalAmount+'<input type="hidden" size="6"  readOnly="readOnly" class="innerHTMLTextBoxHide" name="totalPriceWithTax" id="totalPriceWithTax'+xmlDataItr+'" value="'+itemtotalAmount+'">';
					tdObj = trObj.insertCell(19);
					tdObj.innerHTML =rate+'<input type="hidden" size="6" readOnly="readOnly" class="innerHTMLTextBoxHide" name="landAtPrice" id="landAtPrice'+xmlDataItr+'" value="'+rate+'">';

					document.getElementById("check"+xmlDataItr).disabled=true;
				}else{

					trObj = grnDetailsTableObj.insertRow(index1);
					trObj.className="innerHtmlText";




					tdObj = trObj.insertCell(0);
					tdObj.innerHTML = '<input type="checkbox" onClick="grnEnableFields('+xmlDataItr+');" name="check" id="check'+xmlDataItr+'"  value="'+xmlDataItr+'">';
					tdObj = trObj.insertCell(1);



					tdObj.innerHTML = medicineName+'<input type="hidden" name="medicineId" id="medicineId'+xmlDataItr+'" value="'+medicineId+'">';
					tdObj = trObj.insertCell(2);
					tdObj.innerHTML = orderQty+'<input type="hidden" name="orderQty" id="orderQty'+xmlDataItr+'" value="'+orderQty+'">';
					tdObj = trObj.insertCell(3);
					tdObj.innerHTML = receivedQty+'<input type="hidden" name="receivedQty" id="receivedQty'+xmlDataItr+'" value="'+receivedQty+'">';
					tdObj = trObj.insertCell(4);
					tdObj.innerHTML = issuePerBaseUnit+'<input type="hidden" name="quantityPerUnit" value="'+issuePerBaseUnit+'">';
					//tdObj = trObj.insertCell(5);
					//tdObj.innerHTML = uom+'<input type="hidden" name="uomId" value="'+uomId+'">';
					tdObj = trObj.insertCell(5);
					tdObj.innerHTML ='<input type="text" name="expmonth" id="expmonth'+xmlDataItr+'" readOnly="readOnly" value="" size="2" maxlength="2" onkeypress="return enterNumOnlyzeroToNine(event);" onblur="chkMon(\''+xmlDataItr+'\')">'+'-'+'<input type="text" name="expyear" id="expyear'+xmlDataItr+'" readOnly="readOnly" value="" size="2" maxlength="2" onkeypress="return enterNumOnlyzeroToNine(event);"  onblur="chkYear1(\''+xmlDataItr+'\')">';
 					tdObj = trObj.insertCell(6);
					tdObj.width= '1000';
					tdObj.innerHTML ='<input type="hidden" size="6" readOnly="readOnly"  name="hiddenTaxWithPrice" id="hiddenTaxWithPrice'+xmlDataItr+'"  value="">'+'<input type="text" size="6" readOnly="readOnly"  name="suppliedQty" id="suppliedQty'+xmlDataItr+'" onkeypress="return enterNumOnly(event)"  onchange="return calculateCost(\''+xmlDataItr+'\')"     value="">';

					tdObj = trObj.insertCell(7);
					tdObj.innerHTML =focQty+'<input type="hidden" size="6"   name="focQty" id="focQty'+xmlDataItr+'" value="'+focQty+'">';

					tdObj = trObj.insertCell(8);
					tdObj.innerHTML =grnFocReceived+'<input type="hidden" size="6"   name="grnFocReceived" id="grnFocReceived'+xmlDataItr+'"  value="'+grnFocReceived+'">';


					tdObj = trObj.insertCell(9);
					tdObj.innerHTML ='<input type="hidden" size="6" readOnly="readOnly"  name="aggregatehiddenPoDiscount" id="aggregatehiddenPoDiscount'+xmlDataItr+'"  value="">'+'<input type="text" size="6" readOnly="readOnly" onkeypress="return enterNumOnly(event)"   name="receivedFocQty" id="receivedFocQty'+xmlDataItr+'" onchange="return calculateTotalQty(\''+xmlDataItr+'\')"  value="">';

					tdObj = trObj.insertCell(10);
					tdObj.width= '1000';
					tdObj.innerHTML = '<input type="text" size="6" readOnly="readOnly"  name="batchNo" id="batchNo'+xmlDataItr+'" value="" maxlength="20">';

					tdObj = trObj.insertCell(11);
					tdObj.innerHTML =unitPrice+'<input type="hidden" size="6"   name="unitPrice" id="unitPrice'+xmlDataItr+'" value="'+unitPrice+'">';


					tdObj = trObj.insertCell(12);
					tdObj.innerHTML ='<input type="text" size="6" readOnly="readOnly" class="innerHTMLTextBoxHide" style="text-align: center;"  name="totalQty" id="totalQty'+xmlDataItr+'" value="">';



					tdObj = trObj.insertCell(13);
					tdObj.innerHTML ='<input type="text" size="6" readOnly="readOnly"  name="totalCost" class="innerHTMLTextBoxHide" style="text-align: center;" id="totalCost'+xmlDataItr+'" value="">';

					tdObj = trObj.insertCell(14);

					tdObj.innerHTML =discount+" ("+itemDiscountType+")"+'<input type="hidden" size="6" readOnly="readOnly"  name="itemDiscountType" id="itemDiscountType'+xmlDataItr+'" value="'+itemDiscountType+'">'+'<input type="hidden" size="6"   name="itemDiscount" id="itemDiscount'+xmlDataItr+'" value="'+discount+'">';

					tdObj = trObj.insertCell(15);
					tdObj.innerHTML ='<div id="s2" style="display:none">'+poDiscount+" ("+poDiscountType+")"+'<input type="hidden" size="6" readOnly="readOnly" name="poDiscountType" id="poDiscountType'+xmlDataItr+'" value="'+poDiscountType+'">'+'<input type="hidden" size="6"   name="poDiscount" id="poDiscount'+xmlDataItr+'" value="'+poDiscount+'"></div>';

					tdObj = trObj.insertCell(16);
					tdObj.innerHTML ='<input type="text" size="6" readOnly="readOnly"  name="taxableCost" class="innerHTMLTextBoxHide" style="text-align: center;" id="taxableCost'+xmlDataItr+'" value="">';

					tdObj = trObj.insertCell(17);
					tdObj.innerHTML =vat+'<input type="hidden" size="6"   name="vat" id="vat'+xmlDataItr+'" value="'+vat+'">';

					tdObj = trObj.insertCell(18);
					tdObj.innerHTML ='<input type="text" size="6"  readOnly="readOnly"  class="innerHTMLTextBoxHide" style="text-align: center;" name="totalPriceWithTax" id="totalPriceWithTax'+xmlDataItr+'" value="">';

					tdObj = trObj.insertCell(19);
					var exDate = getLastDayForMonthSQL(document.getElementById('expmonth'+xmlDataItr).value,document.getElementById('expyear'+xmlDataItr).value);
					tdObj.innerHTML = '<input type="text" size="6" readOnly="readOnly" class="innerHTMLTextBoxHide" style="text-align: center;" name="landAtPrice" id="landAtPrice'+xmlDataItr+'" value="'+rate+'">'+'<input type="hidden" size="10" readOnly="readOnly"  name="expiryDate" id="expiryDate'+xmlDataItr+'" value="' + exDate + '">';


					//tdObj = trObj.insertCell(0);
					//tdObj.innerHTML = '<input type="checkbox" onClick="grnEnableFields('+xmlDataItr+');" name="check" id="check'+xmlDataItr+'"  value="'+xmlDataItr+'">';


				}
             ++index1;
			}
	  	}

	}
	function grnEnableFields(index){

		if(document.getElementById("check"+index).checked){
			document.getElementById("suppliedQty"+index).readOnly=false;
			document.getElementById("receivedFocQty"+index).readOnly=false;
			document.getElementById("batchNo"+index).readOnly=false;
			document.getElementById("expmonth"+index).readOnly = false;
			document.getElementById("expyear"+index).readOnly = false;

		}else{
		    var totAmt=document.getElementById("invoiceAmount").value;
		    //alert(document.getElementById("totalPriceWithTax"+index).value);
		    totAmt=totAmt-document.getElementById("totalPriceWithTax"+index).value;
            document.getElementById("invoiceAmount").value=totAmt.toFixed(2);
		    //document.getElementById("expiryDate"+index).value="";
		    document.getElementById("suppliedQty"+index).value="";
		    document.getElementById("batchNo"+index).value="";
		    document.getElementById("taxableCost"+index).value="";
		    document.getElementById("totalQty"+index).value="";
		    document.getElementById("totalCost"+index).value="";
		    document.getElementById("totalPriceWithTax"+index).value="";
		    document.getElementById("hiddenTaxWithPrice"+index).value="";
			document.getElementById("suppliedQty"+index).readOnly=true;
			document.getElementById("receivedFocQty"+index).readOnly=true;
			document.getElementById("batchNo"+index).readOnly=true;
			document.getElementById("expmonth"+index).readOnly = true;
			document.getElementById("expyear"+index).readOnly = true;

		}
	}

	function confirmGrngeneration(){



			var invoiceNumber=document.getElementById("invoiceNo").value;
			var invoiceDate=document.getElementById("invoiceDate").value;
			var grnEntry = document.getElementById("grnEntryDate").value;

			if(grnEntry==""){
				alert("Enter the GRN date");
				openCalendar('grnEntryDate',"past");
				return false;
			}
			if(invoiceNumber==""){
				alert("Enter the invoice number");
				document.getElementById("invoiceNo").focus();
				return false;
			}
			if(invoiceDate==""){
				alert("Enter the invoice date");
				document.getElementById("invoiceDate").focus();
				//show_calendar('forms[0].invoiceDate');
				openCalendar('invoiceDate',"past");
				return false;
			}
			 if(document.getElementById("invoiceDate").value!=""){
			 if(isFutureDate('invoiceDate') < 0){
				alert("Invoice date canot be future date")
				//show_calendar('forms[0].invoiceDate');
				openCalendar('invoiceDate',"past");
				return false;
	 	}
		}
		if(document.getElementById("docDate").value!=""){
	 	if(isFutureDate('docDate') < 0){
			alert("Document date canot be future date")
			//show_calendar('forms[0].docDate');
			openCalendar('docDate',"past");
			return false;
	 }
	}

	if(document.getElementById("grnEntryDate").value!=""){
	 if(isFutureDate('grnEntryDate')< 0){
		alert("GRN date canot be future date")
		//show_calendar('forms[0].grnEntryDate');
		openCalendar('grnEntryDate',"past");
		//document.getElementById("grnEntryDate").focus();
		return false;
	 }
	}

		var grncheckStatus = false;
		if(poxmlDataLen>0){
			for(var i=0;i<poxmlDataLen;i++){
				if(document.getElementById("check"+i).checked){
					grncheckStatus = true;
					break;
				}
			}
			if(!grncheckStatus){
				alert("Check any medicine to enter the grn");
				return false;
			}else{
				for(var i=0;i<poxmlDataLen;i++){
					if(document.getElementById("check"+i).checked){
						//var expDate=document.getElementById("expiryDate"+i).value;
						//document.getElementById("check"+i).value="checked";
						if (trimAll(document.getElementById("expmonth"+i).value) == '') {
							alert("Enter month");
							document.getElementById("expmonth"+i).value = '';
							document.getElementById("expmonth"+i).focus();
							return false;
						}
						if (trimAll(document.getElementById("expyear"+i).value) == '') {
							alert("Enter year");
							document.getElementById("expyear"+i).value = '';
							document.getElementById("expyear"+i).focus();
							return false;
						}
						var suppliedQty=document.getElementById("suppliedQty"+i).value;
						var mon = document.getElementById("expmonth"+i).value;
						var year = document.getElementById("expyear"+i).value;
						var now = new Date();
					    var century = now.getFullYear();
					    var s = century.toString();
					    var yearPrefix = s.substring(0,2);

						document.getElementById("expiryDate"+i).value = getLastDayForMonthSQL(mon,yearPrefix+year);

						if(suppliedQty=="" || parseInt(suppliedQty)==0){
							alert("Enter the supplied quantity");
							document.getElementById("suppliedQty"+i).value=0;
							document.getElementById("suppliedQty"+i).focus();
							return false;
						}
						var receivedFoc=document.getElementById("receivedFocQty"+i).value;
						if(receivedFoc==""){
							document.getElementById("receivedFocQty"+i).value=0;
						}
						var batchno=document.getElementById("batchNo"+i).value;
						if(batchno==""){
							alert("Enter the batch number");
							document.getElementById("batchNo"+i).focus();
							return false;
						}
					}
				}
			}

			if(confirm("Do you want to submit?")){
				document.forms[0].submit();
			}else return false;
		}
	}
   var invoiceAmount=0;
   var aggregatePodiscount=0;
	function calculateCost(index1){

		//alert("in grn qunatity");
		var grnQuantity=document.getElementById("suppliedQty"+index1).value;
		if(grnQuantity=="" || parseInt(grnQuantity)==0){
			alert("Enter the proper quantity ");
			document.getElementById("suppliedQty"+index1).focus();
			return false;
		}
		//alert("grnQuantity==="+grnQuantity);
	    var grnunitPrice=document.getElementById("unitPrice"+index1).value;
	    //alert("grnunitPrice====="+grnunitPrice);
		var landAtmrp=document.getElementById("landAtPrice"+index1).value;
		var receivedQuantity=document.getElementById("receivedQty"+index1).value;
		var orderQty=document.getElementById("orderQty"+index1).value;
		if(parseInt(receivedQuantity,10)!=0){
		var diffOrderQty=parseInt(orderQty)-parseInt(receivedQuantity);
		if(parseInt(grnQuantity,10) > parseInt(diffOrderQty,10)){
				alert("Already GRN Is Received For This Medicine :Enter For Remaining Qty");
				document.getElementById("suppliedQty"+index1).value=0;
				return false;
			}
			}
			if(parseInt(grnQuantity,10) > parseInt(orderQty,10)){
				alert("Supplied Quantity Should Be Less Than The Order Quantity");
				document.getElementById("suppliedQty"+index1).value=0;
				return false;
			}

			var totalcost=parseInt(grnQuantity,10)*parseFloat(grnunitPrice,10);
			var totalcost1=parseInt(grnQuantity,10)*parseFloat(landAtmrp,10);
			totalcost=parseFloat(totalcost,10)
			var grndiscount=document.getElementById("itemDiscount"+index1).value;
			var grnpoDiscount=document.getElementById("poDiscount"+index1).value;
			var vat=document.getElementById("vat"+index1).value;
			var itemDiscountType=document.getElementById("itemDiscountType"+index1).value;
			var poDiscountType=document.getElementById("poDiscountType"+index1).value;
			var dicountRupes;
		if(itemDiscountType=='%'){
				 dicountRupes=parseFloat(totalcost)*parseFloat(grndiscount/100);
		 }
		 else{
		 		dicountRupes=parseInt(grnQuantity,10)*parseFloat(grndiscount);
		 }
			var afterRemovingDiscount=(totalcost-dicountRupes).toFixed(2);
		//document.forms[0].hiddenPoDiscountType.value=poDiscountType;
		if(poDiscountType=='RS'){
			poDiscountInrupes=grnpoDiscount;
		}else{
			poDiscountInrupes=(parseFloat(afterRemovingDiscount)*parseFloat(grnpoDiscount/100)).toFixed(2);
		}
			grntotalwithtax=((totalcost)-dicountRupes);
			var vatInrupes=(grntotalwithtax)*(vat/100);
			document.getElementById("totalCost"+index1).value=totalcost.toFixed(2);
			document.getElementById("taxableCost"+index1).value=parseFloat(grntotalwithtax).toFixed(2);
			var totalAmount=((landAtmrp)*(grnQuantity));
			document.getElementById("totalPriceWithTax"+index1).value=parseFloat(totalAmount).toFixed(2);

			invoiceAmount=document.getElementById("invoiceAmount").value;
			if(invoiceAmount==""){
				document.getElementById("invoiceAmount").value=0;
				invoiceAmount=0;
			}
			//alert("invoiceAmount"+invoiceAmount);
			//alert("totalAmount"+totalAmount);
			//alert("hidden tax with price===========>"+document.getElementById("hiddenTaxWithPrice"+index1).value);
			//alert("totalPriceWithTax===========>"+document.getElementById("totalPriceWithTax"+index1).value);

			var hiddenTaxWithPrice=document.getElementById("hiddenTaxWithPrice"+index1).value;
			if(hiddenTaxWithPrice==""){
				hiddenTaxWithPrice=0;
				document.getElementById("hiddenTaxWithPrice"+index1).value=0;
			}

			if(parseFloat(totalAmount)!=parseFloat(hiddenTaxWithPrice)){
				document.getElementById("invoiceAmount").value=((parseFloat(invoiceAmount)-parseFloat(hiddenTaxWithPrice))+parseFloat(totalAmount)).toFixed(2);
			}
			else{
				document.getElementById("invoiceAmount").value=(parseFloat(invoiceAmount)+parseFloat(totalAmount)).toFixed(2);
			}

			document.getElementById("hiddenTaxWithPrice"+index1).value=totalAmount;
			//alert("invoiceAmount=="+invoiceAmount);
			aggregatePodiscount=document.getElementById("aggregatePoDiscount").value;
			if(aggregatePodiscount==""){
				document.getElementById("aggregatePoDiscount").value=0;
				aggregatePodiscount=0;
			}
			document.getElementById("totalQty"+index1).value=grnQuantity;

			var hiddenAggregatePoDiscount=document.getElementById("aggregatehiddenPoDiscount"+index1).value;
			if(hiddenAggregatePoDiscount==""){
				hiddenAggregatePoDiscount=0;
			}

			/*if(parseFloat(hiddenAggregatePoDiscount)!=0){

				if(hiddenAggregatePoDiscount==aggregatePodiscount){

					document.getElementById("aggregatePoDiscount").value=hiddenAggregatePoDiscount;
				}
				else{
					document.getElementById("aggregatePoDiscount").value=(parseFloat(aggregatePodiscount)+parseFloat(grnpoDiscount)).toFixed(2);
				}

			}
			else{
				document.getElementById("aggregatePoDiscount").value=(parseFloat(aggregatePodiscount)+parseFloat(grnpoDiscount)).toFixed(2);

			}*/

			document.getElementById("aggregatehiddenPoDiscount"+index1).value=document.getElementById("aggregatePoDiscount").value;
		//document.getElementById("grn_landedprice").value=(parseFloat(totalcost1,10)/parseFloat(Quantity,10)).toFixed(2);


	return true;
	}
	function calculateTotalQty(qtyindex){
		var receivedFoc=document.getElementById("receivedFocQty"+qtyindex).value;
		var totalqty=document.getElementById("totalQty"+qtyindex).value;
		var foc=document.getElementById("focQty"+qtyindex).value;
		var grnReceivedFoc=document.getElementById("grnFocReceived"+qtyindex).value;
		var supQty = document.getElementById("suppliedQty"+qtyindex).value;
		if(receivedFoc==""){
			document.getElementById("receivedFocQty"+qtyindex).value=0;
		}
		if(totalqty==""){
			document.getElementById("totalQty"+qtyindex).value=0;
		}
		if(parseInt(receivedFoc) > parseInt(foc)){
			alert("Received Bonus Quantity Should Not Be Greater Than Requested FOC Qty");
			document.getElementById("receivedFocQty"+qtyindex).value=0;
			document.getElementById("receivedFocQty"+qtyindex).focus();
			return false;
		}
		if(parseInt(grnReceivedFoc)!=0){
			var diffFoc=parseInt(foc)-parseInt(grnReceivedFoc);
			if(parseInt(diffFoc)==0){
				alert("Already Requested Foc Quantity Is Received");
				document.getElementById("receivedFocQty"+qtyindex).value=0
				document.getElementById("receivedFocQty"+qtyindex).focus();
				return false;

			}
			if(parseInt(receivedFoc) > parseInt(diffFoc)){
				alert("Already GRN Is Received For This Medicine :Enter For Remaining Qty");
				document.getElementById("receivedFocQty"+qtyindex).value=0
				document.getElementById("receivedFocQty"+qtyindex).focus();
				return false;
			}
		}
		if(receivedFoc ==''){
		receivedFoc = 0;
		}
		document.getElementById("totalQty"+qtyindex).value=parseInt(supQty,10)+parseInt(receivedFoc,10);
	}

	function chkMon(index){
	if(document.getElementById("expmonth"+index).value == 0 || document.getElementById("expmonth"+index).value > 12 ){
		alert("month should be 1-12 only");
		document.getElementById("expmonth"+index).value='';
		document.getElementById("expmonth"+index).focus();
		return false;
	}
	if(document.getElementById("expmonth"+index).value.length == 1){
		document.getElementById("expmonth"+index).value = '0'+document.getElementById("expmonth"+index).value;
	}
}

	function chkYear1(index){
	if(document.getElementById("expyear"+index).value.length == 1){
		document.getElementById("expyear"+index).value = '0'+document.getElementById("expyear"+index).value;
	}
}


//GRN END HERE
