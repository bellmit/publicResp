var toolbar = {
	EditPreCycle: {
		title: "Edit Pre Cycle",
		imageSrc: "icons/Edit.png",
		href: '/IVF/IVFPreCycle.do?_method=show',
		onclick: null,
		description: "Edit Pre Cycle"
	},

	EditDailyTreatment: {
		title: "Edit Daily Treatment",
		imageSrc: "icons/Edit.png",
		href:'/IVF/IVFDailyTreatment.do?_method=list',
		onclick: null,
		description: "Edit Daily Treatment"
	},

	EditCycleCompletion: {
		title: "Edit Cycle Completion",
		imageSrc: "icons/Edit.png",
		href:'/IVF/IVFCycleCompletion.do?_method=show',
		onclick: null,
		description: "Edit Cycle Completion"
	},
};

function initivfSessionList(){
	Insta.initMRNoAcSearch(contextPath, "mrno", "mrnoContainer", "all",null,null);
	createToolbar(toolbar);
}

function CalculateBMI(obj,objvalue) {
	makeingDec(objvalue,obj);
	var weight = document.IVFpreCycle.weight.value;
	var height = document.IVFpreCycle.height.value;
	if(height!='' && !(height>=100 && height<=300)){
		alert("Please enter valid range of 100 to 300 for height.");
		document.IVFpreCycle.height.value = '';
		setTimeout('document.IVFpreCycle.height.focus()',100);
		document.getElementById("bmiLbl").textContent = '';
		document.getElementById("bmi").value = null;
		return false;
	}
	if(weight!='' && !(weight>=10 && weight<=500)){
		alert("Please enter valid range of 10 to 500 for weight.");
		document.IVFpreCycle.weight.value = '';
		setTimeout('document.IVFpreCycle.weight.focus()',100);
		document.getElementById("bmiLbl").textContent = '';
		document.getElementById("bmi").value = null;
		return false;
	}
	if(height!='' && weight!='' && height!=0 && weight!=0) {
		height = height*0.01;
		var den = height*height;
		document.getElementById("bmiLbl").textContent = formatAmountValue(weight/den);
		document.getElementById("bmi").value = formatAmountValue(weight/den);
	}else{
		document.getElementById("bmiLbl").textContent = '';
		document.getElementById("bmi").value = null;
	}
}

function onChangeharmoneResults(obj,objvalue,harmone){

	makeingDec(objvalue,obj);

	if(harmone=='fsh'){
		if(!(objvalue>=0 && objvalue<=30)){
			document.getElementById("fsh").value = '';
			alert("Please enter valid value for FSH between 0 and 30 .");
			setTimeout('document.getElementById("fsh").focus()', 100);
			return false;
		}
	}else if(harmone=='lh'){
		if(!(objvalue>=15 && objvalue<=99)){
			alert("Please enter valid value for LH between 15 and 99 .");
			document.getElementById("lh").value = '';
			setTimeout('document.getElementById("lh").focus()',100);
			return false;
		}
	}else if(harmone=='tsh'){
		if(!(objvalue>=0 && objvalue<=9.99)){
			alert("Please enter valid value for TSH between 0 and 9.99");
			document.getElementById("tsh").value='';
			setTimeout('document.getElementById("tsh").focus()',100);
			return false;
		}
	}else if(harmone=='prl'){
		if(!(objvalue>=0 && objvalue<=500)){
			alert("Please enter valid value for PRL between 0 and 500");
			document.getElementById("prl").value='';
			setTimeout('document.getElementById("prl").focus()',100);
			return false;
		}
	}else if(harmone=='amh'){
		if(!(objvalue>=0 && objvalue<=20)){
			alert("Please enter valid value for AMH between 0 and 20");
			document.getElementById("amh").value='';
			setTimeout('document.getElementById("amh").focus()',100);
			return false;
		}
	}
}

function makeingDec(objValue, obj) {
    if (objValue == '' || isNaN(objValue)) objValue = 0;
    obj.value = parseFloat(objValue).toFixed(decDigits);
}