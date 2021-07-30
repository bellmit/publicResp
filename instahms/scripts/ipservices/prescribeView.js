
var enablepersist="on" //Enable saving state of content structure using session cookies? (on/off)
var collapseprevious="no" //Collapse previously open content when opening present? (yes/no)

var contractsymbol='- ' //HTML for contract symbol. For image, use: <img src="whatever.gif">
var expandsymbol='+ ' //HTML for expand symbol.


if (document.getElementById){
document.write('<style type="text/css">')
document.write('.switchcontent{display:none;}')
document.write('</style>')
}

function getElementbyClass(rootobj, classname){
var temparray=new Array()
var inc=0
for (i=0; i<rootobj.length; i++){
if (rootobj[i].className==classname)
temparray[inc++]=rootobj[i]
}
return temparray
}


function contractcontent(omit){
var inc=0
while (ccollect[inc]){
if (ccollect[inc].id!=omit)
ccollect[inc].style.display="none"
inc++
}
}

function expandcontent(curobj, cid){
var spantags=curobj.getElementsByTagName("SPAN")
var showstateobj=getElementbyClass(spantags, "showstate")
if (ccollect.length>0){
if (collapseprevious=="yes")
contractcontent(cid)
document.getElementById(cid).style.display=(document.getElementById(cid).style.display!="block")? "block" : "none"
if (showstateobj.length>0){ //if "showstate" span exists in header
if (collapseprevious=="no")
showstateobj[0].innerHTML=(document.getElementById(cid).style.display=="block")? contractsymbol : expandsymbol
else
revivestatus()
}
}
}

function revivecontent(){
contractcontent("omitnothing")
selectedItem=getselectedItem()
selectedComponents=selectedItem.split("|")
for (i=0; i<selectedComponents.length-1; i++)
document.getElementById(selectedComponents[i]).style.display="block"
}

function revivestatus(){
var inc=0
while (statecollect[inc]){
if (ccollect[inc].style.display=="block")
statecollect[inc].innerHTML=contractsymbol
else
statecollect[inc].innerHTML=expandsymbol
inc++
}
}

function get_cookie(Name) {
var search = Name + "="
var returnvalue = "";
if (document.cookie.length > 0) {
offset = document.cookie.indexOf(search)
if (offset != -1) {
offset += search.length
end = document.cookie.indexOf(";", offset);
if (end == -1) end = document.cookie.length;
returnvalue=unescape(document.cookie.substring(offset, end))
}
}
return returnvalue;
}

function getselectedItem(){
if (get_cookie(window.location.pathname) != ""){
selectedItem=get_cookie(window.location.pathname)
return selectedItem
}
else
return ""
}

function saveswitchstate(){
var inc=0, selectedItem=""
while (ccollect[inc]){
if (ccollect[inc].style.display=="block")
selectedItem+=ccollect[inc].id+"|"
inc++
}

document.cookie=window.location.pathname+"="+selectedItem
}

function do_onload(){
uniqueidn=window.location.pathname+"firsttimeload"
var alltags=document.all? document.all : document.getElementsByTagName("*")
ccollect=getElementbyClass(alltags, "switchcontent")
statecollect=getElementbyClass(alltags, "showstate")
if (enablepersist=="on" && ccollect.length>0){
document.cookie=(get_cookie(uniqueidn)=="")? uniqueidn+"=1" : uniqueidn+"=0"
firsttimeload=(get_cookie(uniqueidn)==1)? 1 : 0 //check if this is 1st page load
if (!firsttimeload)
revivecontent()
}
if (ccollect.length>0 && statecollect.length>0)
revivestatus()
}

if (window.addEventListener)
window.addEventListener("load", do_onload, false)
else if (window.attachEvent)
window.attachEvent("onload", do_onload)
else if (document.getElementById)
window.onload=do_onload

if (enablepersist=="on" && document.getElementById)
window.onunload=saveswitchstate



function getData(){
	var a = new Array("1","2","3","4","5");
	for(var i=0;i<5;i++){
		//alert(a[i]);
	}

	var tab = document.getElementById("newTable");
	var tabLen = tab.rows.length;
	if(tabLen>0){
		for(var j=0;j<tabLen;j++){
			tab.deleteRow();
		}
	}
	tabRow=tab.insertRow(0);
	tabCell= tabRow.insertCell(0);
	tabCell.className="withData";
	tabCell.innerHTML = 'gfhgfhgfh';
	tabCell =tabRow.insertCell(1);
	tabCell.className="withData";
	tabCell.innerHTML='hgfhgfh';
	for(var k=0;k<5;k++){
		tabRow=tab.insertRow(k+1);
		tabCell=tabRow.insertCell(0);
		tabCell.className="withData";
		tabCell.innerHTML='sdasdsad';
		tabCell=tabRow.insertCell(1);
		tabCell.className="withData";
		tabCell.innerHTML='sadasdas';
	}

}

function initDisplay(){
var alltags=document.all? document.all : document.getElementsByTagName("*");
ccollect=getElementbyClass(alltags, "switchcontent");
for ( i =0; i< ccollect.length; i++){
		ccollect[i].style.display="block";
	}
}
function setIds(){
	var cancelFlag = false;
	if(document.getElementById("prescriptiontable") != null){
		var prestablerows = document.getElementById("prescriptiontable").rows.length;
		var checkboxes = document.getElementsByName('cancel');
		if(!(checkboxes.length > 0)){
			document.forms[0].cancel.value = document.forms[0].cancel.id +'-'+ document.forms[0].cancel.value;
		}
		else{
			for(var i = 0; i<checkboxes.length;i++){
				if(checkboxes[i].checked){
					checkboxes[i].value = checkboxes[i].id+'-'+checkboxes[i].value;
					if(!checkboxes[i].disabled){
						cancelFlag = true;
					}
				}
			}
		}
		if(cancelFlag){
			document.forms[0].action = "ipserviceView.do?method=cancelPrescriptions";
			document.forms[0].submit();
		}else{
			document.forms[0].action = "ipserviceView.do?method=editQuantity";
			document.forms[0].submit();
		}
	}

}

var msg = '<%= request.getParameter("msg") %>';
if(msg == 'null')msg=' ';
function editable(id){
	document.getElementById(id).readOnly = false;
	return true;
}
var count = 0;
function addvalues(value,id,head){
count++;
	var innerOtherTabObj = document.getElementById("quantitytable");
	var trObj = "", tdObj = "";
	trObj = innerOtherTabObj.insertRow(count-1);
	tdObj = trObj.insertCell(0);

	tdObj.innerHTML = '<input type="hidden" name="quantityvalue" id="quantityvalue" value="'+ value +'">';
	tdObj = trObj.insertCell(1);

	tdObj.innerHTML = '<input type="hidden" name="quantityid" id="quantityid" value="'+ id +'">';
	tdObj = trObj.insertCell(2);

	tdObj.innerHTML = '<input type="hidden" name="chargehead" id="chargehead" value="'+ head +'">';
}
function submitValues(){

	document.forms[0].action = "ipserviceView.do?method=editQuantity";
	document.forms[0].submit();
}


function printOrders(){
	var url = cpath+"/pages/ipservices/ipservicePrint.do?method=printOrder&mrno="+document.forms[0].mrno.value+"&patientId="+document.forms[0].patientid.value;
	window.open(url);
}