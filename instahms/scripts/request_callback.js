var requestCallBackDialog;
var requestCallBackPatientAutocomplete;
window.addEventListener('load', function(e) {
  initRequestCallbackDialog();
});

function initRequestCallbackDialog() {
  var requestCallBackDiv = document.getElementById('request-callback-modal');
  if (requestCallBackDiv) {
      requestCallBackDiv.style.display = 'block';
  }
  requestCallBackDialog = new YAHOO.widget.Dialog('request-callback-modal',
  {
      width: '350px',
      height: '200px',
      close: true,
      context : ['request-a-callback-modal', 'tr', 'br'],
      visible: false,
      modal: true,
      fixedcenter: true,
      constraintoviewport: true
  });
  var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
    {
      fn: closeRequestCallbackModal,
      scope: requestCallBackDialog,
      correctScope: true
    }
  );
  requestCallBackDialog.cfg.queueProperty("hideaftersubmit", false);
  requestCallBackDialog.cfg.queueProperty("keylisteners", escKeyListener);
  requestCallBackDialog.render();

}

function openRequestCallbackModal() {
  requestCallBackDialog.show();
  document.forms.requestcallbackform.request_callback_requestee.value = '';
  document.forms.requestcallbackform.request_callback_number.value = '';
}

function closeRequestCallbackModal() {
  requestCallBackDialog.hide();
}

function getNameString(name) {
  return ( name ? name + " " : '');
}

function getNameFromRecord(record) {
  return getNameString(record.patient_name) + getNameString(record.middle_name) + getNameString(record.last_name);
}

function submitRequestCallbackForm() {
  var validE164 = /^\+\d{9,18}$/g;
  if (!document.forms.requestcallbackform.request_callback_requestee.value) {
    alert('Please Enter your name');
    return;
  }
  if (!document.forms.requestcallbackform.request_callback_number.value || !validE164.test(document.forms.requestcallbackform.request_callback_number.value)) {
    alert('Enter valid phone number');
    return;
  }

  var saveUrl = cpath + '/help/requestcallback.json';
  var data = {
    requestee: document.forms.requestcallbackform.request_callback_requestee.value,
    callback_number: document.forms.requestcallbackform.request_callback_number.value.trim(),
    current_url: window.location.href
  };
  $.ajax({
    type: 'POST',
    url: saveUrl,
    data: data,
    success: function(response){
      alert('Your request is submitted. You\'ll receive a callback');
      closeRequestCallbackModal();
    },
    error: function(error) {
      let errorMessage = 'An error occurred!';
      /*Object.keys(error.responseJSON.validationErrors).forEach(key => {
        errorMessage += error.responseJSON.validationErrors[key][0];
      })*/
      alert(errorMessage);
    }
  });
}
