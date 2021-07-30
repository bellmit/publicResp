//----------------------------------  StockEntry BEGIN ----------------------------------------------------

	function loadGRNDetails(){
		var grn	=	document.getElementById("grnNos").options[document.getElementById("grnNos").selectedIndex].value;
		var grnDetailsList = document.getElementById("GRN").getElementsByTagName("GRNs");
		var grnDetailsListRoot = grnDetailsList.item(0);
		grnDetailsOffList = grnDetailsListRoot.getElementsByTagName("grn");
		var grnDetailsLength = grnDetailsOffList.length;
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
				document.getElementById("vendorName").value			=	grnDetailsOff.attributes.getNamedItem('class11').nodeValue;
				document.getElementById("stockEntryDate").value		=	getCurrentDate();

				document.getElementById("chk").style.display='block';
		        document.getElementById("chk1").style.display='block';
				getGrnItemDetails();
				//alert("in grn");
			}
		}
	}
    var index4 =0;
	var grnDetailsTableObj = '';
	function getGrnItemDetails(){
		//var ajaxReqObject = newXMLHttpRequest();
		//var url="../../pages/Inventory/InventoryStockEntryAction.do?method=getGRNItemDetails&grnNo="+document.getElementById("grnNos").options[document.getElementById("grnNos").selectedIndex].value;
		//getResponseHandler(ajaxReqObject,getXMLData,url);

			if(window.XMLHttpRequest)
			{
				reqObject=new  XMLHttpRequest();
			}
			else if(window.ActiveXObject)
			{
				reqObject=new ActiveXObject("MSXML2.XMLHTTP");
			}

			reqObject.onreadystatechange=onResponse1;
			var url="../../pages/Inventory/InventoryStockEntryAction.do?method=getGRNItemDetails&grnNo="+document.getElementById("grnNos").options[document.getElementById("grnNos").selectedIndex].value;
			reqObject.open("POST",url.toString(), true);
			reqObject.setRequestHeader("Content-Type", "text/xml");
			reqObject.send(null);

			//var url="../../pages/pharmacy/PharmacyDirectPOAction.do?method=getMedicineDetails&supplierId="+supplierId;
			//getResponseHandler(ajaxReqObject,getXMLMedicinesData,url);
	 	}

		function checkReadyState(obj)
		{
			 if(obj.readyState == 4)
		 	{
				if(obj.status == 200)
				{
					return true;
				}
		 	}
  		}

	    var x='';
		function onResponse1()
		{
			if(checkReadyState(reqObject))
			{
		 		resText=reqObject.responseXML;
			 	if(resText!=null)
		        {
	            x=reqObject.responseXML.documentElement;
			 	getXMLData();
				}
			}
  		}

	var index4=0;
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
	  		grnDetailsTableObj = document.getElementById("grnDetailsItemTable");
	  		var grnDetailsDivObj = document.getElementById("grnDetailsDiv");
	  		grnDetailsDivObj.style.display = "block";//ananta
	  		var buttonDivObj = document.getElementById("buttonDiv");
	  		buttonDivObj.style.display = "block";//ananta

	  		var itemCode="",itemName="",batchCode="",freeSupply=0,qtySupplied=0,totalQty=0,uomCode="",uomName="";
	  		var qtyPerUnit=0,expiryDate="",binRack="",costPrice=0,mrp=0,totalAmount=0;

	 		// To delete previous content of the page inorder to render every time refreshed data
			if(grnDetailsTableObj.rows.length>0){
				for(d=0;grnDetailsTableObj.rows.length>0;d++){
					grnDetailsTableObj.deleteRow((grnDetailsTableObj.rows.length)-1);
				}
				index4 = 0;
			}

	  		grnDetailsTableObj.width= '1200px';
	  		var trObj="",tdObj="";

			// Header BEGIN

			trObj = grnDetailsTableObj.insertRow(index4);

			trObj.className="innerHtmlHeader";

			tdObj = trObj.insertCell(0);
			tdObj.innerHTML = 'Select';

			tdObj = trObj.insertCell(1);
			tdObj.innerHTML = 'Item Name';
			tdObj = trObj.insertCell(2);
			tdObj.innerHTML = 'Batch No';
			tdObj = trObj.insertCell(3);
			tdObj.innerHTML = 'Free Supply';
			tdObj = trObj.insertCell(4);
			tdObj.innerHTML = 'Qty Supplied';
			tdObj = trObj.insertCell(5);
			tdObj.innerHTML = 'Total Quantity';
			tdObj = trObj.insertCell(6);
			tdObj.innerHTML = 'UOM';
			tdObj = trObj.insertCell(7);
			tdObj.innerHTML = 'Quantity/Unit';
			tdObj = trObj.insertCell(8);
			tdObj.innerHTML = 'Expiry Date';
			tdObj = trObj.insertCell(9);
			tdObj.innerHTML = 'Cost Price';
			tdObj = trObj.insertCell(10);
			tdObj.innerHTML = 'Total Amount';
			tdObj = trObj.insertCell(11);
			tdObj.innerHTML = 'Bin/Rack';
			tdObj = trObj.insertCell(12);
			tdObj.innerHTML = 'MRP';
			// Header END

			++index4;
	  		for(var xmlDataItr=0;xmlDataItr<xmlDataLen;xmlDataItr++){
				itemCode 		= 	nodes[xmlDataItr].attributes.getNamedItem('class1').nodeValue;
				itemName 		= 	nodes[xmlDataItr].attributes.getNamedItem('class2').nodeValue;
				batchCode 		= 	nodes[xmlDataItr].attributes.getNamedItem('class3').nodeValue;
				freeSupply		= 	nodes[xmlDataItr].attributes.getNamedItem('class4').nodeValue;
				qtySupplied 	= 	nodes[xmlDataItr].attributes.getNamedItem('class5').nodeValue;
				totalQty 		= 	nodes[xmlDataItr].attributes.getNamedItem('class6').nodeValue;
				uomCode 		= 	nodes[xmlDataItr].attributes.getNamedItem('class7').nodeValue;
				uomName 		= 	nodes[xmlDataItr].attributes.getNamedItem('class8').nodeValue;
				qtyPerUnit 		= 	nodes[xmlDataItr].attributes.getNamedItem('class9').nodeValue;
				expiryDate 		= 	nodes[xmlDataItr].attributes.getNamedItem('class10').nodeValue;
				if(expiryDate=="") expiryDate="-";
				costPrice 		= 	nodes[xmlDataItr].attributes.getNamedItem('class11').nodeValue;
				if(costPrice=="") costPrice='0.0';
				totalAmount 	= 	nodes[xmlDataItr].attributes.getNamedItem('class12').nodeValue;
				if(totalAmount=="") totalAmount='0.0';
				if(nodes[xmlDataItr].attributes.getNamedItem('class13').nodeValue!="" || nodes[xmlDataItr].attributes.getNamedItem('class13').nodeValue!=" ")
				//binRack 		= 	nodes[xmlDataItr].attributes.getNamedItem('class1').nodeValue+"/"+nodes[xmlDataItr].attributes.getNamedItem('class14').nodeValue;
				mrp 			= 	nodes[xmlDataItr].attributes.getNamedItem('class15').nodeValue;


				trObj = grnDetailsTableObj.insertRow(index4);

				trObj.className="innerHtmlText";

				tdObj = trObj.insertCell(0);
				tdObj.innerHTML = '<input type="checkbox" onClick="enableFields('+xmlDataItr+');" name="check" id="check'+xmlDataItr+'"  value="'+xmlDataItr+'">';
				tdObj = trObj.insertCell(1);
				tdObj.innerHTML = itemName+'<input type="hidden" name="itemCode" value="'+itemCode+'">';
				tdObj = trObj.insertCell(2);
				tdObj.innerHTML = batchCode+'<input type="hidden" name="batchCode" value="'+batchCode+'">';
				tdObj = trObj.insertCell(3);
				tdObj.innerHTML = freeSupply;
				tdObj = trObj.insertCell(4);
				tdObj.innerHTML = qtySupplied;
				tdObj = trObj.insertCell(5);
				tdObj.innerHTML = totalQty;
				tdObj = trObj.insertCell(6);
				tdObj.innerHTML = uomName;
				tdObj = trObj.insertCell(7);
				tdObj.innerHTML = qtyPerUnit;
				tdObj = trObj.insertCell(8);
				tdObj.innerHTML = expiryDate;
				tdObj = trObj.insertCell(9);
				tdObj.innerHTML = costPrice;
				tdObj = trObj.insertCell(10);

				tdObj.innerHTML = totalAmount;
				tdObj = trObj.insertCell(11);
				tdObj.innerHTML = '<input type="text" size="10" readOnly="readOnly" name="binRack" id="binRack'+xmlDataItr+'" value="'+binRack+'">';
				tdObj = trObj.insertCell(12);
				tdObj.innerHTML = '<input type="text" size="10" readOnly="readOnly" name="mrp" id="mrp'+xmlDataItr+'" value="'+mrp+'">';
				//tdObj = trObj.insertCell(0);
				//tdObj.innerHTML = '<input type="checkbox" onClick="enableFields('+xmlDataItr+');" name="check" id="check'+xmlDataItr+'"  value="'+xmlDataItr+'">';
                index4++;
			}
	  	}
	}



	//This function is for checking all check Box
	//IMPLEMENTED BY ANANTA
	function checkall()
	{
		var len = xmlDataLen;
		//alert(len);
		if(len>0)
		{
			for(d=0;d<len;d++)
			{
			document.getElementById("check"+d).checked=true;
			javascript:enableFields(d);
			}
		}

	}

	//This function is for Clearing all check Box
	//IMPLEMENTED BY ANANTA
	function clearall()
	{
		var len = xmlDataLen;
		//alert(len);
		if(len>0)
		{
			for(d=0;d<len;d++)
			{
			document.getElementById("check"+d).checked=false;
			document.getElementById("binRack"+d).readOnly=true;
			document.getElementById("mrp"+d).readOnly=true;
			}
		}
	}





	function enableFields(index){
		if(document.getElementById("check"+index).checked){
			document.getElementById("binRack"+index).readOnly=false;
			document.getElementById("mrp"+index).readOnly=false;
		}else{
			document.getElementById("binRack"+index).readOnly=true;
			document.getElementById("mrp"+index).readOnly=true;
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
				alert("Check any item to enter the stock");
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





//----------------------------------  GRN Generation BEGIN ----------------------------------------------------


//GRN START HERE



//----------------------------------  GRN Generation END ----------------------------------------------------

//----------------------------------  StockEntry BEGIN ----------------------------------------------------

