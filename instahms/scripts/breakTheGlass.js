var breakTheGlassDialog;
var breakTheGlassPatientAutocomplete;
window.addEventListener('load', function(e) {
  initBreakTheGlassDialog();
});

function initBreakTheGlassDialog() {
  var breakTheGlassDiv = document.getElementById('break-the-glass-modal');
  if (breakTheGlassDiv) {
      breakTheGlassDiv.style.display = 'block';
  }
  breakTheGlassDialog = new YAHOO.widget.Dialog('break-the-glass-modal',
  {
      width: '600px',
      height: '270px',
      close: true,
      context : ['break-the-glass-modal', 'tr', 'br'],
      visible: false,
      modal: true,
      fixedcenter: true,
      constraintoviewport: true
  });
  var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
    {
      fn: closeBreakTheGlassModal,
      scope: breakTheGlassDialog,
      correctScope: true
    }
  );
  breakTheGlassDialog.cfg.queueProperty("hideaftersubmit", false);
  breakTheGlassDialog.cfg.queueProperty("keylisteners", escKeyListener);
  breakTheGlassDialog.render();
  breakTheGlassPatientAutocomplete = initBreakTheGlassAutocomplete('break_the_glass_form_patient_name', 'break_the_glass_form_patient_container');

}

function openBreakTheGlassModal() {
  breakTheGlassDialog.show();
  document.forms.break_the_glass_form.break_the_glass_form_mr_no.value = '';
  document.forms.break_the_glass_form.break_the_glass_form_patient_name.value = '';
  document.forms.break_the_glass_form.break_the_glass_form_remarks.value = '';
}

function closeBreakTheGlassModal() {
  breakTheGlassDialog.hide();
}

function getNameString(name) {
  return ( name ? name + " " : '');
}

function getNameFromRecord(record) {
  return getNameString(record.patient_name) + getNameString(record.middle_name) + getNameString(record.last_name);
}

function initBreakTheGlassAutocomplete(inputElId, containerId) {
  var confidentialPatientLookupUrl = cpath + "/patients/lookupMrnoName.json";
  var ds = new YAHOO.util.DataSource(confidentialPatientLookupUrl);
  ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
  ds.responseSchema = {
    resultsList: "patients",
    fields: [
      { key: "patient_name" },
      { key: "middle_name" },
      { key: "last_name" },
      { key: "mr_no" },
      { key: "patient_group" }
    ]
  };

  var autoComp = new YAHOO.widget.AutoComplete(inputElId, containerId, ds);
  autoComp.generateRequest = function(filterText) {
    return "?filterText=" + filterText;
  };
  autoComp.minQueryLength = 1;
  autoComp.animVert = false;
  autoComp.maxResultsDisplayed = 20;
  autoComp.resultTypeList = false;
  autoComp.forceSelection = true;
  autoComp.formatResult = function(resultData) {
    return '<span>(' + resultData.mr_no + ') ' + getNameFromRecord(resultData) + ' - ' + resultData.patient_group + '</span>';
  };

  autoComp.itemSelectEvent.subscribe(function(e, args, data) {
    var record = args[2];
    document.forms.break_the_glass_form.break_the_glass_form_mr_no.value = record.mr_no;
    document.forms.break_the_glass_form.break_the_glass_form_patient_name.value = getNameFromRecord(record);
  });
  autoComp.selectionEnforceEvent.subscribe(function() {
    document.forms.break_the_glass_form.break_the_glass_form_mr_no.value = '';
    document.forms.break_the_glass_form.break_the_glass_form_patient_name.value = '';
  });

  return autoComp;
}

function submitBreakTheGlassForm() {
  if (!document.forms.break_the_glass_form.break_the_glass_form_mr_no.value) {
    alert('Please select a patient');
    return;
  }
  if (!document.forms.break_the_glass_form.break_the_glass_form_remarks.value || document.forms.break_the_glass_form.break_the_glass_form_remarks.value.trim().length < 30) {
    alert('Minimum 30 characters are required for remarks');
    return;
  }

  var saveUrl = cpath + '/master/breaktheglass/create.json';
  var data = {
    mr_no: document.forms.break_the_glass_form.break_the_glass_form_mr_no.value,
    remarks: document.forms.break_the_glass_form.break_the_glass_form_remarks.value.trim()
  };
  $.ajax({
    type: 'POST',
    url: saveUrl,
    data: JSON.stringify(data),
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(response){
      alert('Access to MR No ' + data.mr_no + ' has been provided with a limited period up to current logged in session');
      closeBreakTheGlassModal();
    },
    error: function(error) {
      let errorMessage = 'An error occurred!';
      Object.keys(error.responseJSON.validationErrors).forEach(key => {
        errorMessage = error.responseJSON.validationErrors[key][0];
      })
      alert(errorMessage);
    }
  });
}
