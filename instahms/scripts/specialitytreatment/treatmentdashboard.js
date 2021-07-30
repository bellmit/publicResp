var toolbar = {
    Edit: {
        title: "Treatment Docs",
        imageSrc: "icons/Edit.png",
        href: '/SpecialityTreatment/Treatment.do?method=show'
    }
}

function init() {
    createToolbar(toolbar);
}

/*
 * Complete the MRNO
 */

function onKeyPressMrno(e) {
    if (isEventEnterOrTab(e)) {
        return onChangeMrno();
    } else {
        return true;
    }
}

function onChangeMrno() {
    var mrnoBox = ""
    mrnoBox = document.forms[0].mrNo;

    // complete
    var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

    if (!valid) {
        alert("Invalid MR No. Format");
        document.forms[0].mrNo.value = ""
        document.forms[0].mrNo.focus();
        return false;
    }
}

function autoFillTreatmentNames() {
    dataSource = new YAHOO.widget.DS_JSArray(treatmentsList);
    var oAutoComp = new YAHOO.widget.AutoComplete('treatment', 'treatment_dropdown', dataSource);
    oAutoComp.maxResultsDisplayed = 18;
    oAutoComp.allowBrowserAutocomplete = false;
    oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
    oAutoComp.typeAhead = false;
    oAutoComp.useShadow = false;
    oAutoComp.minQueryLength = 0;
    oAutoComp.forceSelection = true;
    oAutoComp.animVert = false;
    autoFillDoctors();
}

function autoFillDoctors() {
    var dataSource1 = new YAHOO.widget.DS_JSArray(doctorList, {
        queryMatchContains: true
    });
    var oAutoComp1 = new YAHOO.widget.AutoComplete('doctor', 'doctor_dropdown', dataSource1);

    oAutoComp1.formatResult = Insta.autoHighlight;
    oAutoComp1.maxResultsDisplayed = 18;
    oAutoComp1.allowBrowserAutocomplete = false;
    oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
    oAutoComp1.typeAhead = false;
    oAutoComp1.useShadow = false;
    oAutoComp1.minQueryLength = 0;
    oAutoComp1.forceSelection = true;
    oAutoComp1.animVert = false;
    oAutoComp1.itemSelectEvent.subscribe(docNameInput);
    autoFillDepartments();
}

var docNameInput = function (sType, aArgs) {
    var oData = aArgs[2];
    document.forms[0].doctor.value = oData;
}

function autoFillDepartments() {
    dataSource2 = new YAHOO.widget.DS_JSArray(departmentList);
    var oAutoComp2 = new YAHOO.widget.AutoComplete('department', 'department_dropdown', dataSource2);
    oAutoComp2.maxResultsDisplayed = 18;
    oAutoComp2.allowBrowserAutocomplete = false;
    oAutoComp2.prehighlightClassName = "yui-ac-prehighlight";
    oAutoComp2.typeAhead = false;
    oAutoComp2.useShadow = false;
    oAutoComp2.minQueryLength = 0;
    oAutoComp2.forceSelection = true;
    oAutoComp2.animVert = false;
}

function setPatientDetails() {
    document.getElementById('patientMrno').innerHTML = patientDetils[0].mr_no;
    document.getElementById('patientName').innerHTML = patientDetils[0].patient_name + ' ' + patientDetils[0].last_name;
    document.getElementById("patientAge").innerHTML = patientDetils[0].age + patientDetils[0].agein;
    document.getElementById("patientSex").innerHTML = patientDetils[0].patient_gender;
    document.getElementById("patientContactNo").innerHTML = patientDetils[0].patient_phone;
    if (document.getElementById("patientOldMrno") != null) document.getElementById("patientOldMrno").innerHTML = patientDetils[0].oldmrno;
    document.forms[0].mrNo.value = patientDetils[0].mr_no;
}


function closeTreatment() {
    var itemListTable = document.getElementById("treatmentdummyList");

    var closeCheckBox = document.forms[0].close;
    var closeCheckedFlag = false;
    if (closeCheckBox != undefined) {
        if (closeCheckBox.length == undefined) {
            closeCheckBox.length = 1;
        }

        var numRows = itemListTable.rows.length
        var cell;
        var id = numRows - 1;
        var row = itemListTable.insertRow(id);
        var treatmentId = "";
        for (var i = 0; i < closeCheckBox.length; i++) {
            if (document.getElementById("close" + i).checked == true) {
                treatmentId = document.getElementById("close" + i).value;
                closeCheckedFlag = true;
                cell = row.insertCell(-1);
                cell.innerHTML = '<input type="hidden" name="treatmentClose" id="treatmentClose' + i + '" value="' + treatmentId + '">';
            }
        }
        if (closeCheckedFlag) {
            document.forms[0].action = "Treatment.do"
            document.forms[0].method.value = "closeTreatment";
            document.forms[0].submit();
        } else {
            alert("Check one or more treatments to close");
        }
    } else {
        alert(" There are no checked treatments to close");
    }
}

function clearSearch() {
    document.forms[0].treatment.value = "";
    document.forms[0].department.value = "";
    document.forms[0].doctor.value = "";
    document.forms[0].mrNo.value = "";
}