var toolbar = {
    Edit: {
        title: "Edit Document",
        imageSrc: "icons/Edit.png",
        href: '/SpecialityTreatment/SplTreatment.do?method=show',
        onclick: null,
    },

    status: {
        title: "Edit Status",
        imageSrc: "icons/Change.png",
        href: '/SpecialityTreatment/TreatmentListScreen.do?method=show'
    },

    followup: {
        title: "Add Followup",
        imageSrc: "icons/Edit.png",
        href: '/SpecialityTreatment/SplTreatment.do?method=add&fFlag=true'
    }
};

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

function autoFillDoctors() {

    var dataSource1 = new YAHOO.widget.DS_JSArray(doctorList, {
        queryMatchContains: true
    });
    var oAutoComp1 = new YAHOO.widget.AutoComplete('fdoctor', 'doctor_dropdown', dataSource1);

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
}
var docNameInput = function (sType, aArgs) {
    var oData = aArgs[2];
    document.forms[0].fdoctor.value = oData;
}

function onChangeMrno(formVal) {
    var mrNoFlag = false;
    var mrnoBox = ""
    if (formVal == "mrNo") {
        mrnoBox = document.forms[0].mrNo;
        mrNoFlag = true;
    } else {
        mrnoBox = document.forms[0].mrno;
    }

    // complete
    var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

    if (!valid) {
        alert("Invalid MR No. Format");
        clearPatientDetails();
        document.forms[0].mrno.value = ""
        document.forms[0].mrno.focus();
        return false;
    }
    if ((valid) && (!mrNoFlag)) {
        onSelectMrno();
    }
}

function clearSearch() {
    document.forms[0].treatment.value = "";
    document.forms[0].mrNo.value = "";
    document.forms[0].fdoctor.value = "";
    document.forms[0].fdate.value = "";
    document.forms[0].tdate.value = "";
}
/* This function to populate patient Details
   in the corresponding text fields or labels*/

function populatePatientDetails() {
    if (document.forms[0].treatmentFollUp.value != "true") {
        document.getElementById('followUpTreatment').style.display = "none";
    }
    if (document.forms[0].hmrno.value != "") {
        document.getElementById("mrno").value = document.forms[0].hmrno.value;
        document.getElementById("mrno").disabled = true;
        onSelectMrno();
        if (treatmentDetailJSON.length > 0) {
            var options = document.forms[0].dept_id;
            var options1 = document.forms[0].treatName;
            var options2 = document.forms[0].doctor;
            options.length = 0;
            addOption(options, treatmentDetailJSON[0].dept_name, treatmentDetailJSON[0].dept_id);
            options1.length = 0;
            addOption(options1, treatmentDetailJSON[0].treatment_name, treatmentDetailJSON[0].treatment_name);
            getTreatmentFormNames();
            options2.length = 0;
            addOption(options2, treatmentDetailJSON[0].doctor_name, treatmentDetailJSON[0].doctor_id);
            document.getElementById('dept_name').innerHTML = treatmentDetailJSON[0].dept_name;
            document.getElementById('treatMent_Name').innerHTML = treatmentDetailJSON[0].treatment_name;
            document.getElementById('doc_Name').innerHTML = treatmentDetailJSON[0].doctor_name;
            document.getElementById('dMrno').style.display = "none";
            document.getElementById('assignTreatment').style.display = "none";
        }
    }
}

function getPatientDetails(patientDetils) {
    setPatientDetails(patientDetils);
    var options = document.forms[0].status;
    if (treatmentJSON.length > 0) {
        document.getElementById("documentName").innerHTML = treatmentJSON[0].form_title;
        document.getElementById("treatmentName").innerHTML = treatmentJSON[0].treatment_name;
        document.getElementById("doctorName").innerHTML = treatmentJSON[0].doctor_name;
        setSelectedIndex(options, treatmentJSON[0].status);
        if (treatmentJSON[0].comments != null) document.getElementById("comments").value = treatmentJSON[0].comments;
        document.getElementById("due_date").value = formatDueDate(treatmentJSON[0].due_date);
        document.forms[0].previousDate.value = formatDueDate(treatmentJSON[0].due_date);
    }
}

function formatDueDate(dateMSecs) {
    var dateObj = new Date(dateMSecs);
    var dateStr = formatDate(dateObj, 'ddmmyyyy', '-');
    return dateStr;
}

function getTreatmentNames() {
    document.forms[0].addedForms.length = 0;
    document.forms[0].tForms.length = 0;
    myarray.length = 0;
    counterfrows = 0;
    var treatmentName = document.getElementById("treatName");
    var formName = document.forms[0].tForms;
    var deptId = document.getElementById("dept_id").value;
    list = document.getElementById("TREATMENT").getElementsByTagName("TREATMENTs");
    root = list.item(0);
    offlist = root.getElementsByTagName("treatment");
    len = offlist.length;
    var k = 0;
    treatmentName.length = 1;
    for (var i = 0; i < len; i++) {
        off = offlist.item(i);
        if (deptId == off.attributes.getNamedItem('class2').nodeValue) {
            k++;
            treatmentName.length = k + 1;
            treatmentName.options[k].text = off.attributes.getNamedItem('class1').nodeValue;
            treatmentName.options[k].value = off.attributes.getNamedItem('class1').nodeValue;
        }
    }
    getDoctorNames(deptId)
}

function getTreatmentFormNames() {
    var treatmentForm = document.getElementById("tForms");
    var treatmentName = document.getElementById("treatName").value;
    var deptId = document.getElementById("dept_id").value;
    list = document.getElementById("TREATMENTFORM").getElementsByTagName("TREATMENTFORMs");
    root = list.item(0);
    offlist = root.getElementsByTagName("treatmentform");
    len = offlist.length;
    var k = 0;

    for (var i = 0; i < len; i++) {
        off = offlist.item(i);
        if (treatmentName == off.attributes.getNamedItem('class3').nodeValue && deptId == off.attributes.getNamedItem('class4').nodeValue) {
            treatmentForm.length = k + 1;
            treatmentForm.options[k].text = off.attributes.getNamedItem('class2').nodeValue;
            treatmentForm.options[k].value = off.attributes.getNamedItem('class1').nodeValue;
            k++;

        }
    }
}

function getDoctorNames(deptId) {
    var doctor = document.getElementById("doctor");
    list = document.getElementById("DOCTOR").getElementsByTagName("DOCTORs");
    root = list.item(0);
    offlist = root.getElementsByTagName("doctor");
    len = offlist.length;
    var k = 0;
    doctor.length = 1;
    for (var i = 0; i < len; i++) {
        off = offlist.item(i);
        if (deptId == off.attributes.getNamedItem('class3').nodeValue) {
            k++;
            doctor.length = k + 1;
            doctor.options[k].text = off.attributes.getNamedItem('class2').nodeValue;
            doctor.options[k].value = off.attributes.getNamedItem('class1').nodeValue;
        }

    }
}

/*This function is for  adding treatment names and deleteing
   treatment names*/

var myarray = new Array(0);
var myarray1 = new Array(0);
var counterfrows = 0;

function addForms(value) {
    var serail = 0;
    var formlen = document.forms[0].tForms.length;
    var addedformlen = document.forms[0].addedForms;
    var formName = "";
    var formValue = "";
    var options = document.forms[0].tForms;

    if (myarray.length > 0) {
        if (value == "Add >") {
            for (var g = 0; g < formlen; g++) {
                for (var i = 0; i < myarray.length; i++) {
                    if (document.forms[0].tForms[g].selected) {
                        formName = document.forms[0].tForms[g].text;
                        if ((myarray[i][0] == formName)) {
                            alert("This record already exist's");
                            return false;
                        }
                    }
                }
            }
        } else {
            for (var g = 0; g < formlen; g++) {
                for (var i = 0; i < myarray.length; i++) {
                    formName = document.forms[0].tForms[g].text;
                    if ((myarray[i][0] == formName)) {
                        alert("This record already exist's");
                        return false;
                    }
                }
            }
        }
    }

    for (var l = 0; l < formlen; l++) {
        if (value == "Add >") {
            if (document.forms[0].tForms[l].selected) {
                myarray[counterfrows] = new Array(2)
                formName = document.forms[0].tForms[l].text;
                formValue = document.forms[0].tForms[l].value;
                myarray[counterfrows][0] = formName;
                myarray[counterfrows][1] = formValue;
                counterfrows++;
            }
        } else {
            myarray[counterfrows] = new Array(2)
            formName = document.forms[0].tForms[l].text;
            formValue = document.forms[0].tForms[l].value;
            myarray[counterfrows][0] = formName;
            myarray[counterfrows][1] = formValue;
            options[0].selected = true;
            counterfrows++;
        }
    }
    addRows();
}

function addRows() {
    var addedForm = document.getElementById("addedForms");
    var k = 0;
    addedForm.length = 0;
    for (var i = 0; i < myarray.length; i++) {
        if (myarray[i][0] == "") {
            continue;
        }
        addedForm.length = k + 1;
        addedForm.options[k].text = myarray[i][0];
        addedForm.options[k].value = myarray[i][1];
        k++;
    }
}

function removeForms(value) {
    var addedForm = document.getElementById("addedForms");
    var addedformlen = document.forms[0].addedForms;
    var addedFormname = "";
    var rFlag = false;
    for (var i = 0; i < addedformlen.length; i++) {
        if (value == "< Remove") {
            if (document.forms[0].addedForms[i].selected) {
                addedFormname = document.forms[0].addedForms[i].text;
                for (var k = 0; k < myarray.length; k++) {
                    if (addedFormname == myarray[k][0]) {
                        document.forms[0].addedForms[i].text = "";
                        document.forms[0].addedForms[i].value = "";
                        myarray[k][0] = "";
                        myarray[k][1] = "";
                        rFlag = true;
                        document.forms[0].addedForms[i].selected = false;
                        counterfrows--;

                    }
                }
            }
        } else {
            counterfrows = 0;
            myarray.length = 0;
            addedForm.length = 0;
            break;
        }
    }
    if (rFlag) {
        populateForms();
    }
}

function populateForms() {

    var addtreatmentForm = document.forms[0].addedForms;
    addtreatmentForm.length = 0;
    var t = 0;

    for (var k = 0; k < myarray.length; k++) {
        if (myarray[k][0] == "") {
            continue;
        }
        addtreatmentForm.length = t + 1;
        addtreatmentForm.options[t].text = myarray[k][0];
        addtreatmentForm.options[t].value = myarray[k][1];
        t++;

    }
    counterfrows++;
}

function onSelectMrno() {

    var mrno = document.getElementById("mrno").value;
    var ajaxReqObject = newXMLHttpRequest();
    var url = "SplTreatment.do?method=getPatientDetailsJSON&mrno=" + mrno;
    getResponseHandlerText(ajaxReqObject, handlePatientDetailResponse, url);

}

var patientDetils = null;

function handlePatientDetailResponse(responseText) {
    if (responseText == null) return;
    if (responseText == "") return;
    eval("patientDetils = " + responseText);
    if (patientDetils.length > 0) {
        setPatientDetails(patientDetils);
    } else {
        alert("Not a valid MRNO");
        clearPatientDetails();
    }
}

function setPatientDetails(patientDetils) {
    document.getElementById('patientMrno').innerHTML = patientDetils[0].mr_no;
    document.getElementById('patientName').innerHTML = patientDetils[0].patient_name + ' ' + patientDetils[0].last_name;
    document.getElementById("patientAge").innerHTML = patientDetils[0].age + patientDetils[0].agein;
    document.getElementById("patientSex").innerHTML = patientDetils[0].patient_gender;
    document.getElementById("patientContactNo").innerHTML = patientDetils[0].patient_phone;
    if (document.getElementById("patientOldMrno") != null) document.getElementById("patientOldMrno").innerHTML = patientDetils[0].oldmrno;
}

function clearPatientDetails() {
    document.getElementById('patientMrno').innerHTML = '';
    document.getElementById('patientName').innerHTML = '';
    document.getElementById("patientAge").innerHTML = "";
    document.getElementById("patientSex").innerHTML = "";
    document.getElementById("patientContactNo").innerHTML = "";
    document.getElementById("mrno").value = "";
    document.forms[0].dept_id.selectedIndex = 0;
}

function validate() {
    var valid = true;
    var mrno = document.getElementById("mrno").value;
    if (mrno == "") {
        alert("Enter Mrno");
        document.getElementById("mrno").focus();
        return false;
    }
    if (document.getElementById("dept_id").value == "") {
        alert("Select Department");
        document.getElementById("dept_id").focus();
        return false;
    }
    if (document.getElementById("treatName").value == "") {
        alert("Select Treatment Name");
        document.getElementById("treatName").focus();
        return false;
    }
    if (document.getElementById("doctor").value == "") {
        alert("Select Doctor");
        document.getElementById("doctor").focus();
        return false;
    }
    if (myarray.length == 0) {
        alert("Add Treatment Forms to Added Forms");
        document.getElementById("tForms").focus();
        return false;
    }
    if (counterfrows == 0) {
        alert("No forms are added !");
        return false;
    }
    if (valid) {
        var addedformlen = document.forms[0].addedForms.length;
        var options = document.forms[0].addedForms;
        for (var i = 0; i < addedformlen; i++) {

            options[i].selected = true;
        }
    }
    if (valid) {
        if (document.forms[0].from.value != "") {
            document.forms[0].action = "Assigntreatment.do?method=create";
        } else {
            document.forms[0].action = "SplTreatment.do?method=create";
        }
        document.forms[0].submit();
    }
}


function sendAlertMessage() {
    var alertCheckBox = document.forms[0].sAlert;
    var checkBox = document.forms[0].sAlert;
    var mycounterfrows = 0;
    var flag = false;
    var formNames = "";
    if (checkBox != undefined) {
        if (checkBox.length == undefined) {
            checkBox.length = 1;
        }

        for (var k = 0; k < checkBox.length; k++) {
            if (document.getElementById("sAlert" + k).checked == true) {
                var mrno = document.getElementById("sAlert" + k).value;
                var doctorId = document.getElementById("doctorId" + k).value;
                var formName = document.getElementById("formName" + k).value;
                var documentStatus = document.getElementById("documentStatus" + k).value;


                for (var v = 0; v < myarray1.length; v++) {
                    if (mrno == myarray1[v][0] && documentStatus == myarray1[v][3] && doctorId == myarray1[v][1]) {
                        formNames = myarray1[v][2] + "," + formName;
                        myarray1[v][2] = formNames;
                        flag = true;
                    }
                }

                if (!flag) {
                    myarray1[mycounterfrows] = new Array(4);
                    myarray1[mycounterfrows][0] = mrno;
                    myarray1[mycounterfrows][1] = doctorId;
                    myarray1[mycounterfrows][2] = formName;
                    myarray1[mycounterfrows][3] = documentStatus;
                    mycounterfrows++;
                }
                flag = false;
            }

        }
        var alertType = "";
        var alertPat = false;
        var alertDoc = false;
        for (var l = 0; l < myarray1.length; l++) {
            alertType = document.getElementById("sendAlert").value;

            if (alertType == "AP") {
                if (myarray1[l][3] == "P") {
                    alertPat = true;
                } else {
                    alertPat = false;
                    mycounterfrows = 0;
                    myarray1.length = 0;
                }
            }
            if (alertType == "AD") {
                if (myarray1[l][3] == "R") {
                    alertDoc = true;
                } else {
                    alertDoc = false;
                    mycounterfrows = 0;
                    myarray1.length = 0;
                }

            }
        }

        if (alertType == "AP") {
            if (!alertPat) {
                alert("One or more of the selected documents are not available for patient input.");
                document.forms[0].sendAlert.options[0].selected = true;
                return false;
            }
        }
        if (alertType == "AD") {
            if (!alertDoc) {
                alert("One or more of the selected documents are not available for doctor review.");
                document.forms[0].sendAlert.options[0].selected = true;
                return false;
            }
        }

        for (var i = 0; i < myarray1.length; i++) {

            var treatmentListTable = document.getElementById("treatmentdummyList");
            var alertCheckBox = document.forms[0].sAlert;
            var numRows = treatmentListTable.rows.length;
            var id = numRows - 1; // leave 1 for header
            var row = treatmentListTable.insertRow(id)
            var cell;
            cell = row.insertCell(-1);
            cell.innerHTML = '<input type="hidden" name="hmrnoList" id="hmrnoList' + i + '" value="' + myarray1[i][0] + '">';
            cell = row.insertCell(-1);
            cell.innerHTML = '<input type="hidden" name="hdocIdList" id="hdocIdList' + i + '" value="' + myarray1[i][1] + '">';
            cell = row.insertCell(-1);
            cell.innerHTML = '<input type="hidden" name="hdocNameList" id="hdocNameList' + i + '" value="' + myarray1[i][2] + '">';
            cell = row.insertCell(-1);
            cell.innerHTML = '<input type="hidden" name="hdocumentStatusList" id="hdocumentStatusList' + i + '" value="' + myarray1[i][3] + '">';
        }

        if (myarray1.length > 0) {
            document.forms[0].method.value = "sendMail";
            document.forms[0].action = "SplTreatment.do?method=sendMail";
            document.forms[0].submit();
        } else {
            alert("Please select above documents for sending alert.");
            document.forms[0].sendAlert.options[0].selected = true;
        }
    } else {
        alert("No documents are selected to send alert.");
        document.forms[0].sendAlert.options[0].selected = true;
    }
}

function enableButton() {
    var itemListTable = document.getElementById("treatmentList");
    var numRows = itemListTable.rows.length
    var flag = false;
    for (var i = 1; i <= numRows - 1; i++) {
        if (document.getElementById("close" + i).checked == true) {
            document.getElementById('cButton').style.display = "block";
            flag = true;
            break;
        }
    }
    if (!flag) document.getElementById('cButton').style.display = "none";
}


function assignTreatment() {
    document.forms[0].method.value = "add";
    document.forms[0].action = "SplTreatment.do?method=add";
    document.forms[0].submit();
}

function showPDFDoc() {
    document.forms[0].action = "SplTreatment.do?method=showTreatmentForm";
    document.forms[0].submit();
}

function addOption(selectbox, text, value) {
    var optn = document.createElement("OPTION");
    optn.text = text;
    optn.value = value;
    selectbox.options.add(optn);
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
}

function enablePatTreatmentType() {
    var disabled = document.forms[0].patTreAll.checked;
    document.forms[0].patTreNew.disabled = disabled;
    document.forms[0].patTreInput.disabled = disabled;
    document.forms[0].patTreReview.disabled = disabled;
    document.forms[0].patTreClose.disabled = disabled;
}

function dateValidate() {
    myDate = new Date();
    var currDate = formatDueDate(myDate);
    var portal = document.forms[0].portal.value;
    if (portal == "doctorPortal") {
        if (document.getElementById("due_date").value != document.forms[0].previousDate.value) {
            if (getDateDiff(currDate, document.getElementById("due_date").value) < 0) {
                alert("Due date should not be less than Currentdate");
                return false;
            }
        }
    } else if (document.getElementById("due_date").value != document.forms[0].previousDate.value) {
        if (getDateDiff(currDate, document.getElementById("due_date").value) < 0) {
            alert("Due date should not be less than Currentdate");
            return false;
        }
    }
}

function disabledButton() {
    var tStatus = document.forms[0].tStatus.value;
    if (tStatus == "C") {
        document.forms[0].save.disabled = true;
    }
}

function documentPermission() {
    var docStatus = document.getElementById("status").value;
    var docPermission = document.forms[0].docPermission.value;
    var portal = document.forms[0].portal.value;

    if (portal == "doctorPortal") {
        if ((docStatus == "P") && (docPermission != "RW")) {
            alert("This document does't have Read and Write Permission");
            document.forms[0].status.options[2].selected = true;
            return false;
        }
    } else if ((docStatus == "P") && (docPermission != "RW")) {
        alert("This document does't have Read and Write Permission");
        document.forms[0].status.options[0].selected = true;
    }
}

function checkPortalAccess(id) {
    var docStatus = document.getElementById("documentStatus" + id).value;
    var pAccess = document.getElementById("pAccess" + id).value;
    var mrNO = document.getElementById("sAlert" + id).value;

    if ((pAccess == "f") && (docStatus != "R")) {

        alert("This " + mrNO + " does't have patient portal authorization");
        document.getElementById("sAlert" + id).checked = false;
        return false;

    }
    return true;
}