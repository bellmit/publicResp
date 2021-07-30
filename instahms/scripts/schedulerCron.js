//Storing previous last index in order to prevent traversing and manually removing class.
var previousRowIndex = 0;

//Fill data in Right-Panel
function populateData(rowId) {
    var job_index = null != rowId ? rowId : 0;
    var job_id = $('#job_id'+job_index).val();
    var job_name = $('#job_name'+job_index).val();
    var job_group = $('#job_group'+job_index).val();
    var job_status = $('#job_status'+job_index).val();
    var job_time = $('#job_time'+job_index).val();
    var job_params = $('#job_params'+job_index).val();
    var job_allow_disable = $('#job_allow_disable'+job_index).val();
    var job_next_runtime = $('#job_next_runtime'+job_index).val();
    var job_last_runtime = $('#job_last_runtime'+job_index).val();
    var job_last_status = $('#job_last_status'+job_index).val();
    var job_description = $('#job_description'+rowId).val();

    $('#job-index').val(job_index);
    $('#job-id').val(job_id);
    $('#job-name').val(job_name);
    $('#job-group').val(job_group);
    $('#job-params').val(job_params);
    $('#job-status').val(job_status);
    $('#job-time').val(job_time);
    $('#job-last-run-time').val(job_last_runtime);
    $('#job-next-run-time').val(job_next_runtime);
    $('#job-pre-status').val(job_last_status);
    $('#job-allow-disable').val(job_allow_disable);
    $('#job-description').val(job_description);
  
    if(job_allow_disable == 'N') {
        $("#job-params").prop("readonly", true);
        $("#job-time").prop("readonly", true);
        $("#job-status").prop("readonly", true);
        $("#job-name").prop("readonly", true);
        $("#job-group").prop("readonly", true);
        $("#job-description").prop("readonly", true);
      } 
    else {
        $("#job-params").prop("readonly", false);
        $("#job-time").prop("readonly", false);
        $("#job-status").prop("readonly", false);
        $("#job-name").prop("readonly", true);
        $("#job-group").prop("readonly", true);
        $("#job-description").prop("readonly", false);
    }

}

function updateJobName() {
    var value = $('#job-name').val();
    $('#job_name').val(value);
}

function updateJobParams() {
    var value = $('#job-params').val();
    $('#job_params').val(value);
}

function updateJobTime() {
    var value = $('#job-time').val();
    $('#job_time').val(value);
}

function updateJobStatus() {
    var value = $('#job-status').val();
    $('#job_status').val(value);
}

function getQueryParameter(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
            results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function getRowIndexFromURL(url) {
    return getQueryParameter('job_index', url) || 0;
}

function highlightRow(rowIndex) {
    unHighlightRow(rowIndex);
    var row = document.getElementById('toolBarRow' + rowIndex);
    row.classList.add('selected-job-card');
}

function unHighlightRow(currentRowIndex) {
    var previousRow = document.getElementById('toolBarRow' + previousRowIndex);
    previousRow.classList.remove('selected-job-card');
    previousRowIndex = currentRowIndex;
}

