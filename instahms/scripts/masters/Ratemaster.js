


   	 var obj = new Insta.Grid("thegrid");
    YAHOO.util.Event.addListener(window, "load", function() {obj.init()});

   	obj.setSize(540,100);


    var myColumns = ["Medicine Name","Supplier","Medicine MRP","Rate(Rs)","Discount(%)","Discount(Rs)","AbatedAmount(%)","AbatedAmount(RS)"];

	obj.setHeaderHeight(20);
	obj.setHeaderText(myColumns);
	obj.setColumnCount(8);
	obj.setRowCount(0);
	obj.setCellEditable(false);
	var myarray1=new Array(1);
	var myarray2=new Array(1);
	var sum=0;
	var serial=0;
    var countofrows=0;

    function Add(){
       if(document.forms[0].medicineName.value=="") {
	   		alert("Enter medicine name");
	   		document.forms[0].medicineName.focus();
	   		return false;
	  }

      if(document.forms[0].vendor_id.options.selectedIndex==0){
	   		alert("Select the supplier");
	   		document.forms[0].vendor_id.focus();
	   		return false;
	  }
	  if(document.forms[0].medicineMRP.value==""){
	  	alert("Enter the Medicine MRP");
	  	document.forms[0].medicineMRP.focus();
	  	return false;
	  }
	//  if(document.forms[0].medicineMRP.value!=""){
	//	  if(document.forms[0].medicineMRP.value==0){
	//	  	alert("Medicine MRP should be greater than zero");
	//	  	document.forms[0].medicineMedicine.focus();
	//	  	return false;
	//	  }
	//  }
	  if(document.forms[0].medicineMRP.value.length>10){
	  	alert("Enter less than 10 char in medicine MRP");
	  	document.forms[0].medicineMRP.focus();
	  	return false;
	  }
	  if(document.forms[0].medicineMRP.value!=""){
	  	document.forms[0].medicineMRP.value=parseFloat(document.forms[0].medicineMRP.value,10).toFixed(2);
	  }

	  if(document.forms[0].standardrate.value==""){
	   	alert("Enter the standard rate");
	   	document.forms[0].standardrate.focus();
	  	 return false;
	  }

	 // if(document.forms[0].standardrate.value!=""){
	//	  if(document.forms[0].standardrate.value==0){
	//	  	alert("Standard rate should be greater than zero");
	//	  	document.forms[0].standardrate.focus();
	//	  	return false;
	//	  }
	//  }

	  if(document.forms[0].standardrate.value.length >10) {
	  	 alert("Enter less than 10 char in standard rate");
	   	 document.forms[0].standardrate.focus();
	   	 return false;
	  }

	  if(document.forms[0].standardrate.value!="")  {
	  	 document.forms[0].standardrate.value=parseFloat(document.forms[0].standardrate.value,10).toFixed(2);
	  }

	  	var disValue=document.getElementById("discount").value;
		var disType=document.getElementById("discountType").options[document.getElementById("discountType").selectedIndex].value;
		var standardrate=document.getElementById("rate").value;
		if(disType==""&&disValue==""){
			document.getElementById("discount").value=0;
		}
		if(disType!=""&&disValue==""){
		alert("Enter the discount value");
		document.getElementById("discount").value=0;
		document.forms[0].discountType.selectedIndex=0;
		document.getElementById("discount").focus();
		return false;
		}
		if(document.forms[0].discount.value!="" && document.forms[0].discount.value!=0 ){
			if(document.forms[0].discountType.selectedIndex==0){
			alert("Select the Discount Type either RS or %");
			//document.forms[0].discount.value=0;
			document.forms[0].discountType.selectedIndex=0;
			document.forms[0].discountType.focus();
			return false;
			}
		}
		if (disType=='%'){
	 		if(disValue>=100){
		 		alert("Discount value cannot be greater than or equal to 100")
		 		document.getElementById("discount").value=0;
		 		return false;
	 		}

		}else if(disType=='RS'){
		 		if(parseFloat(disValue)>=parseFloat(standardrate)){
			 		alert("Discount value cannot be greater than the standard rate")
			 		document.forms[0].discount.value=0;
			 		return false;
		 		}
		}

	   if(document.forms[0].standardtax.value==""){
		  document.forms[0].standardtax.value=0;
	  }

		if(document.forms[0].standardtax.value!=""){
		   document.forms[0].standardtax.value=parseFloat(document.forms[0].standardtax.value,10).toFixed(2);
	   }

	   	if(document.forms[0].standardtax.value>99){
		   alert("Standard tax should be less than 100");
		   return false;
	   }
	   if(document.forms[0].abatedmrppercentage.value>99){
		   alert("Enter the abated MRP percentage value less than 100");
		   return false;
	   }
	   if(countofrows>0){
	   for(i=0;i<countofrows;i++){


			    if(myarray2[i][1]==document.getElementById("vendorid").options[document.getElementById("vendorid").selectedIndex].value && myarray2[i][0]==document.getElementById("medicineName").value ){
				    alert("Duplicate entry");
				    return false;
			    }
		    }
	   }
    if(!confirm("Do you want  to add this record to grid ?"))
    {
    return false;
    }

      var ritemname=document.getElementById("medicineName").value;
	  var rvendor=document.getElementById("vendorid").options[document.getElementById("vendorid").selectedIndex].value;
	  var rmedicinemrp=document.getElementById("medicineMRP").value;
	  var rsdrate=document.getElementById("rate").value;
	  var rdiscount=document.getElementById("discount").value;
	  var rtax=document.getElementById("tax").value;
	  var abatedAmountPercentage=document.getElementById("abatedmrppercentage").value;
	  var abatedAmountRupes=document.getElementById("abatedmrprupes").value;
	  countofrows++;
	  obj.addRow(serial++)
	  return true;
	  obj.refresh();

	}



   	obj.onRowAdded = function(row){


	window.status = "Row added: " + row;
	var ritemnametext=document.getElementById("medicineName").value;
	var ritemname=document.getElementById("medicineName").value;
    var rvendor=document.getElementById("vendorid").options[document.getElementById("vendorid").selectedIndex].value;
    var rvendortext=document.getElementById("vendorid").options[document.getElementById("vendorid").selectedIndex].text;
    var rmedicinemrp=document.getElementById("medicineMRP").value;
	var rsdrate=document.getElementById("rate").value;
	var rdiscount=document.getElementById("discount").value;
	var disType=document.getElementById("discountType").value;
	var rtax=document.getElementById("tax").value;
	var abatedAmountPercentage=document.getElementById("abatedmrppercentage").value;
	var abatedAmountRupes=document.getElementById("abatedmrprupes").value;

	myarray2[(row)]=new Array(2)
	//myarray2[(row)]=new Array(2)
	myarray2[(row)][0]=ritemname;
	myarray2[(row)][1]=rvendor;
	myarray2[(row)][2]=rmedicinemrp;
	myarray2[(row)][3]=rsdrate;
	var rdiscount1=rdiscount;
	if(disType=='%'){
		if(rdiscount>0){
			rdiscount1=parseFloat(document.forms[0].discount.value,10).toFixed(2);
		}
	}else if(disType=='RS'){
		//rdiscount1=(parseFloat(rdiscount)*parseFloat(100/rsdrate)).toFixed(2);
		rdiscount1=parseFloat(document.forms[0].discount.value,10).toFixed(2);
	}
	myarray2[(row)][4]=rdiscount1
	myarray2[(row)][5]=rtax;
	myarray2[(row)][6]=abatedAmountPercentage;
	myarray2[(row)][7]=abatedAmountRupes;
	myarray2[(row)][8]=disType;
	this.setCellText(ritemnametext,0,row);
	this.setCellText(rvendortext,1,row);
	this.setCellText(rmedicinemrp,2,row);
	this.setCellText(rsdrate,3,row);
	if (disType=='%'){

	   // obj.setHeaderText(["Medicine Name","Brand","Supplier","Medicine MRP","Rate(Rs)","Discount(%)","Discount(Rs)","AbatedAmount(%)"," AbatedAmount(RS)"]);

	this.setCellText(rdiscount,4,row);
	this.setCellText(0,5,row);
    //obj.setColumnWidth(0,6);
	}else if(disType=='RS'){
		//this.setCellText(0,5,row);
		this.setCellText(rdiscount,5,row);
		this.setCellText(0,4,row);
	  //  obj.setColumnWidth(0,5);
	}
	else{
		this.setCellText(rdiscount,4,row);
		this.setCellText(rdiscount,5,row);
	}
	this.setCellText(abatedAmountPercentage,6,row);
	this.setCellText(abatedAmountRupes,7,row);
	document.getElementById("medicineName").value="";
	document.getElementById("vendorid").options[0].selected=true;
	document.getElementById("medicineMRP").value="";
	document.getElementById("rate").value="";
	document.getElementById("discount").value="";
	document.getElementById("tax").value="";
	document.getElementById("abatedmrppercentage").value="";
	document.getElementById("abatedmrprupes").value="";
	document.getElementById("discountType").selectedIndex=0;
	sum=row+1;
    obj.refresh();

  }


  function Delete(){

	if(serial==0)
	{
	alert("No records exist");
	}

	else
	{

		var i = obj.getCurrentRow();
		obj.deleteRow(i);
		obj.refresh();
		}
	}


		obj.onRowDeleting = function(row){
		if(row==-1)
		{
		alert("No records exist");
		}

		else
		{
		return !confirm("Do you want to delete the selected row?"+row);
		}
		}

		obj.onRowDeleted = function(row){

		myarray2[(row)]=new Array(12)
		myarray2[(row)][0]=""
		myarray2[(row)][1]=""
		myarray2[(row)][2]=""
		myarray2[(row)][3]=""
		myarray2[(row)][4]=""
		myarray2[(row)][5]=""
		myarray2[(row)][6]=""
		myarray2[(row)][7]=""
		myarray2[(row)][8]=""
		window.status = "Row deleted: " + row;
		}





    var Ritemname;
    var Rbrandname;
	var Rvendor;
	var Rmedicinemrp;
	var Rstandardrate;
	var Rstandarddiscount;
	var Rtax;
	var abatedAmountPercentage;
	var abatedAmountRupes;
	var dType;
	function addValues(){


		for(i=0;i<myarray2.length;i++)
		{
		 Ritemname=myarray2[i][0];
		 Rvendor=myarray2[i][1];
		 Rmedicinemrp=myarray2[i][2];
		 Rstandardrate=myarray2[i][3];
		 Rstandarddiscount=myarray2[i][4];
		 Rtax=myarray2[i][5];
		 abatedAmountPercentage=myarray2[i][6];
		 abatedAmountRupes=myarray2[i][7];
		 dType=myarray2[i][8];

		 // alert(Ritemname+""+Rvendor+""+Rstandardrate+""+Rstandarddiscount+""+Rtax);
			if((Ritemname!="")&&(Rvendor!="")&&(Rmedicinemrp!="")&&(Rstandardrate!="")&&(Rstandarddiscount!="")&&(Rtax!="")&&(abatedAmountPercentage!="")&&(abatedAmountRupes!=""))
			{

			innerhtml();
			}
	     }
	   return true;
	  }
		var slNo = 1;
		function innerhtml()
	  {
	 // alert("html");
	 // alert("Ritemname"+Rvendor+""+Rstandardrate+""+Rstandarddiscount+""+Rtax);
		var oTable = document.getElementById('tabdisplay');
		oTR = oTable.insertRow(0);


		oTD = oTR.insertCell(0);
		oTD.innerHTML = '<input type="hidden" size="0"  name="SerialNo" value="' + slNo++ +'" readonly>';


		oTD = oTR.insertCell(0);
		oTD.innerHTML = '<input type="hidden" size="0"  name="medicineIds" value="' +Ritemname+ '" readonly>';

	    oTD = oTR.insertCell(0);
		oTD.innerHTML = '<input type="hidden" size="17"  name="supplierCodes" value="' + Rvendor + '" readonly>';

		oTD = oTR.insertCell(0);
		oTD.innerHTML = '<input type="hidden" size="17" name="medicineMRPs" value="'+Rmedicinemrp+'" readonly>';

		 oTD = oTR.insertCell(0);
		oTD.innerHTML = '<input type="hidden" size="17"  name="standardRates" value="'+Rstandardrate+'" readonly>';

		 oTD = oTR.insertCell(0);
		oTD.innerHTML = '<input type="hidden" size="17"  name="standardDiscount" value="'+Rstandarddiscount+'" readonly>';



	   oTD = oTR.insertCell(0);
		oTD.innerHTML = '<input type="hidden" size="17"  name="tax" value="'+Rtax+'" readonly>';

		oTD = oTR.insertCell(0);
		oTD.innerHTML = '<input type="hidden" size="17"  name="hiddenabatedmrppercentage" value="'+abatedAmountPercentage+'" readonly>';

		oTD = oTR.insertCell(0);
		oTD.innerHTML = '<input type="hidden" size="17"  name="hiddenabatedmrprupes" value="'+abatedAmountRupes+'" readonly>';

		oTD = oTR.insertCell(0);
		oTD.innerHTML = '<input type="hidden" size="17"  name="discountType1" value="'+dType+'" readonly>';


		}



		function validate(){
			 var x=obj.getRowCount();
	 		 if(x==0) {
	  			alert("Enter atleast one row ");
	  			return false;
	  		 }

       		 if(!confirm("Do you want to save ?"))     {
	        	return false;
	         }
	         return true;
	  }

	  function disable(){
		    document.forms[0].save.disabled=true;
	  }

	  function enable(){
			document.forms[0].save.disabled=false;
	  }

	  function clrByMedicineName(){
	  	document.getElementById("vendorid").selectedIndex=0;
	   	clrBySupplier();
	  }



		function floatValidate(amount){
			if((amount.search(/^([0-9]+\.?[0-9]*|\.[0-9]+)$/)==-1)){
	    		alert("Please enter valid digits")
	    		return false;
	  		}
			else{
				return true;
			}
		}


	function resetCount(){
		dotCount=0;
	}
	function AbatedAmountCalculation(){
		var mrp=document.forms[0].medicineMRP.value;
		var abatedmrppercentage=document.forms[0].abatedmrppercentage.value;

	if(abatedmrppercentage=="" ){
		abatedmrppercentage=0;
		document.forms[0].abatedmrppercentage.value=abatedmrppercentage;
	}

		var abatedrs=(mrp)*(abatedmrppercentage/100);
		var abatedAmount=parseFloat(mrp-abatedrs);
		document.forms[0].abatedmrprupes.value=abatedAmount.toFixed(2);
	}

	function AbatedAmount(){
		var mrp=document.forms[0].medicineMRP.value;
		var abatedmrppercentage=document.forms[0].abatedmrppercentage.value;
		if(mrp==""){
			alert("Enter the mrp");
			document.forms[0].medicineMRP.focus();
			return false;
		}
	if(abatedmrppercentage=="" ){
		abatedmrppercentage=0;
		document.forms[0].abatedmrppercentage.value=abatedmrppercentage;
	}

		var abatedrs=(mrp)*(abatedmrppercentage/100);
		var abatedAmount=parseFloat(mrp-abatedrs);
		document.forms[0].abatedmrprupes.value=abatedAmount.toFixed(2);

	}
	function clrBySupplier(){
		document.forms[0].medicineMRP.value="";
		document.forms[0].standardrate.value="";
        document.forms[0].discount.value="";
        document.forms[0].standardtax.value="";
         document.forms[0].abatedmrppercentage.value="";
        document.forms[0].abatedmrprupes.value="";
	}


	function getDetails()
	{
		clrBySupplier();
        document.forms[0].update.value="empty";
		var item=document.getElementById("medicineName").value;

		var vendor=document.getElementById("vendorid").options[document.getElementById("vendorid").selectedIndex].value

 	   	if(window.XMLHttpRequest){
				req = new XMLHttpRequest();
		 }


		 else if(window.ActiveXObject){
			req = new ActiveXObject("MSXML2.XMLHTTP");
		 }

		 req.onreadystatechange = onResponse;
		 var url="PharmacyRateMasterAction.do?method=ajaxGetRate&medicineId="+item+"&supplierCode="+vendor;

		 req.open("POST",url.toString(), true);

		 req.setRequestHeader("Content-Type", "text/xml");
		 req.send(null);
  }

  function checkReadyState(obj){
		 if(obj.readyState == 4){
			if(obj.status == 200){
					return true;
			}
		 }
  }

  var tstr=null;
  var x=null;
  function onResponse(){
  	if(checkReadyState(req)){

  	 	tstr=req.responseXML;



	        if(tstr!=null){
			 	getXML();

			}
		}
  }

  var doc;
  function getXML(){
	       doc = tstr;

	    x=doc.documentElement;

	   gotDetials();

  }




  	function getMedicineDetails()
	{

		clrBySupplier();

		var medicineName=document.getElementById("medicineName").value;

 	   var ajaxreq = newXMLHttpRequest();
    	var url="PharmacyRateMasterAction.do?method=getMedicineDetails&medName="+medicineName;
    	getResponseHandlerText(ajaxreq, displayMedicines, url);



  }

  function displayMedicines(responseData){

   var medicineDetails=eval(responseData);
 			YAHOO.example.medicineNamesArray = [];
			YAHOO.example.medicineNamesArray.length = medicineDetails.length;

			for (var i=0;i<medicineDetails.length;i++) {
				YAHOO.example.medicineNamesArray[i] = medicineDetails[i];
			}
			YAHOO.example.ACJSAddArray = new function() {
			// Instantiate first JS Array DataSource
				this.oACDS = new YAHOO.widget.DS_JSArray(YAHOO.example.medicineNamesArray);
				// Instantiate first AutoComplete
				this.oServiceAutoComp = new YAHOO.widget.AutoComplete('medicineName','medicine_dropdown',this.oACDS);
				this.oServiceAutoComp.prehighlightClassName = "yui-ac-prehighlight";
				this.oServiceAutoComp.useShadow = true;
				this.oServiceAutoComp.minQueryLength = 0;
				this.oServiceAutoComp.allowBrowserAutocomplete = false;
				this.oServiceAutoComp.maxResultsDisplayed = 30;
				this.oServiceAutoComp.textboxFocusEvent.subscribe(function(){
					var sInputValue = YAHOO.util.Dom.get('medicineName').value;
					if(sInputValue.length == 0) {
						var oSelf = this;
						setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
					}
				});

  }

}


function New()
  {
   document.forms[0].action="PharmacyRateMasterAction.do?method=getMedicineList";
   document.forms[0].submit();
  }
function gotDetials()
{
	x=doc.documentElement;
  	var len=x.childNodes[0].childNodes.length;

  	 for( i=0;i<len;i++){

  	 	document.forms[0].standardrate.value=x.childNodes[0].childNodes[i].attributes.getNamedItem('class2').nodeValue;
       document.forms[0].standardtax.value=x.childNodes[0].childNodes[i].attributes.getNamedItem('class3').nodeValue;
      document.forms[0].medicineMRP.value=x.childNodes[0].childNodes[i].attributes.getNamedItem('class1').nodeValue;
       document.forms[0].abatedmrppercentage.value=x.childNodes[0].childNodes[i].attributes.getNamedItem('class5').nodeValue;
       document.forms[0].abatedmrprupes.value=x.childNodes[0].childNodes[i].attributes.getNamedItem('class6').nodeValue;
       var countType=x.childNodes[0].childNodes[i].attributes.getNamedItem('class7').nodeValue;
       if(countType=='%'){
       		document.forms[0].discount.value=x.childNodes[0].childNodes[i].attributes.getNamedItem('class4').nodeValue;
    	    document.getElementById("discountType").selectedIndex=1;
       }else if(countType=='RS'){
			//document.forms[0].discount.value=Math.round(parseFloat(x.childNodes[0].childNodes[i].attributes.getNamedItem('class4').nodeValue)*parseFloat(document.forms[0].standardrate.value/100));
			document.forms[0].discount.value=x.childNodes[0].childNodes[i].attributes.getNamedItem('class4').nodeValue;
			document.getElementById("discountType").selectedIndex=2;
       }else{
       document.forms[0].discount.value=x.childNodes[0].childNodes[i].attributes.getNamedItem('class4').nodeValue;
       }
       var abatedmrppercentage=document.forms[0].abatedmrppercentage.value;
       var abatedmrprupes=document.forms[0].abatedmrprupes.value;
	   if(document.forms[0].medicineMRP.value!="" && document.forms[0].standardrate.value!="" &&document.forms[0].discount.value!="" &&document.forms[0].standardtax.value!="" && abatedmrppercentage!="" && abatedmrprupes!="")
   		{
         document.forms[0].update.value="update";
  		}
 		}

}

 	  var arrayList=new Array();


function funcClose(){
		if(confirm("Do you want to close ?")){
			document.forms[0].action = "../../../../ClosePage.do";
			document.forms[0].submit();
			return true;
		}else return false;

	}

	function makeingDec(objValue,obj){
    if (objValue!= '') {
		document.getElementById(obj.id).value = parseFloat(objValue).toFixed(2);
	}
}
