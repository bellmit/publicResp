var templates = {
  "simple" : null,
  "table" : null,
  "uploader" : null
}
var reportRefetchTimeoutId = null;
$(function(){
  _.templateSettings = {
    interpolate: /\{\%\=(.+?)\%\}/g,
    evaluate: /\{\%(.+?)\%\}/g,
    escape: /\{\%\-(.+?)\%\}/g
  };
  templates.simple = _.template($("#simple_template").html());
  templates.table = _.template($("#table_template").html());
  templates.uploader = _.template($("#uploader_template").html());
  templates.signoff = _.template($("#signoff_template").html());
	$("#reportingYear").change(function() {
		showLoader("Loading Report");
    var year = $("#reportingYear").val();
    if (!year) {
      $("#reportContainer").empty();
      $("#reportContainer").append($("<p>").html("Select a reporting year to view/submit report").addClass("messageRelCentered"));
      $("#loader").hide();
      $("#reportingYear").removeAttr("disabled");
      $("#reportContainer").show();
      return;
    }
		getReport(year);
	});

  $("#signoffOhsrsReport").click(function() {
    showLoader("Report signoff is in progress. This screen will auto-load the report once the report is signed off");
    var year = $("#reportingYear").val();
    signoffReport(year);
  });
  $("#regenerate").click(function() {
    showLoader("Report generation is in progress. This screen will auto-load the report once the report is ready");
    var year = $("#reportingYear").val();
    generateReport(year);
  });
  $("#submitToOHSRS").click(function() {
    showLoader("Report submission is in progress. This screen will auto-load the report with updated status once the report is submitted");
    var year = $("#reportingYear").val();
    submitReport(year);
  });
  $(document).on("change", "#ohsrsFunctionSelect" , function() {
    var ohsrsFunction = $("#ohsrsFunctionSelect").val();
    var url = !ohsrsFunction ? "javascript:void();" : (cpath + "/reports/ohsrsdohgovph/report/" + $("#reportingYear").val() + "/download/" + ohsrsFunction + ".json");
    if (!ohsrsFunction) {
      $("#csvFile").attr("disabled", "disabled");
      $("#uploadCsv").attr("disabled", "disabled");
      $("#downloadCSVLink").hide();
    } else {
      $("#csvFile").removeAttr("disabled");
      $("#uploadCsv").removeAttr("disabled");
      $("#downloadCSVLink").show();
    }
    $("#downloadCSVLink").attr("href", url);
  });
  $(document).on("click", "#uploadCsv" , function() {
    var ohsrsFunction = $("#ohsrsFunctionSelect").val();
    var year = $("#reportingYear").val();
    uploadCsv(year, ohsrsFunction);
  });
});

function showLoader(message){
  $("#loader").html(message).show();
  $("#reportContainer").hide();
  $("#reportingYear").attr("disabled", "disabled");
  $("#actionbar button").hide();
};

function signoffReport(year) {
  var postData = {
    "reportedby": $("input[name='reportedby']").val(),
    "designation": $("input[name='designation']").val(),
    "department": $("input[name='department']").val(),
    "section": $("input[name='section']").val(),
  }
  $.ajax({
    "url": cpath + "/reports/ohsrsdohgovph/report/" + year + "/signoff.json",
    "dataType": "json",
    "data": postData,
    "method": "POST",
    success: function(data) {
      if (data.status) {
        getReport($("#reportingYear").val());
      }
    },
    error: function(data) {
      alert("Failed to signoff report. Please retry");
      getReport($("#reportingYear").val());
    }
  })
}

function submitReport(year) {
  $.ajax({
    "url": cpath + "/reports/ohsrsdohgovph/report/" + year + "/send.json",
    success: function(data) {
      if (data.status) {
        window.setTimeout(function() { getReport($("#reportingYear").val());}, 10000);
      }
    }
  })
}

function generateReport(year) {
  $.ajax({
    "url": cpath + "/reports/ohsrsdohgovph/report/" + year + "/generate.json",
    success: function(data) {
      if (data.status) {
        window.setTimeout(function() { getReport($("#reportingYear").val());}, 10000);
      }
    }
  })
}

function getReport(year) {
	$.ajax({
		"url": cpath + "/reports/ohsrsdohgovph/report/" + year + ".json",
    success: function(data) {
      if (data.submission_job_status) {
          showLoader("Report submission is in progress. This screen will auto-load the report with updated status once the report is submitted");
          window.setTimeout(function() { getReport($("#reportingYear").val());}, 10000);
      } else if (data.generation_job_status) {
          showLoader("Report generation is in progress. This screen will auto-load the report once the report is ready");
          window.setTimeout(function() { getReport($("#reportingYear").val());}, 10000);
      } else {
        renderReport(data);
      }
    }
  })
}

function renderReport(reportData) {
  $("#reportContainer").empty();
  if (Object.keys(reportData).indexOf("signoff") === -1) {
    $("#reportContainer").append($(templates["uploader"]({
      "schemas" : ohsrsdohgovphRendererMeta,
      "isFirstYear": $("#reportingYear").val() == reportingYears[0]
    })));
  }
  var fieldset = $("<fieldset>").append($("<legend>").html("Report").addClass("fieldSetLabel")).addClass("fieldSetBorder");
  $.each(ohsrsdohgovphRendererMeta, function(ohsrsFunction, functionMeta) {
    var template = templates[functionMeta.representation];
    var data = {
      "schema": functionMeta,
      "data": reportData.report[ohsrsFunction],
      "isFirstYear": $("#reportingYear").val() == reportingYears[0]
    };
    var markup = template(data);
    fieldset.append($(markup));
  });
  $("#reportContainer").append($(fieldset));
  if (Object.keys(reportData).indexOf("signoff") !== -1) {
    $("#reportContainer").append(templates.signoff({"signoff": reportData.signoff, "signoffError": false}));
  } else if (reportData.allow_signoff) {
	var tmplData = {"signoff" : false, "signoffError": false};
    if (Object.keys(reportData).indexOf("signoff_error") !== -1) {
    	tmplData["signoffError"] = reportData.signoff_error;
    }
    $("#reportContainer").append(templates.signoff(tmplData));
  }
  $("#loader").hide();
  $("#reportContainer").show();
  $("#reportingYear").removeAttr("disabled");
  if (reportData.allow_submit) {
    $("#submitToOHSRS").show();
  }
  if (reportData.allow_generate) {
    $("#regenerate").show();
  }
  if (reportData.allow_signoff) {
    $("#signoffOhsrsReport").show();
  }
}

function uploadCsv(year, ohsrsFunction) {
  var form = $('#csvUploadForm')[0];
  var data = new FormData(form);
  $("#csvFile").attr("disabled", "disabled");
  $("#uploadCsv").attr("disabled", "disabled");
  $("#downloadCSVLink").hide();

  $.ajax({
    type: "POST",
    enctype: 'multipart/form-data',
    url: cpath + "/reports/ohsrsdohgovph/report/" + year + "/upload/" + ohsrsFunction + ".json",
    data: data,
    processData: false,
    contentType: false,
    cache: false,
    timeout: 600000,
    success: function (data) {
      $("#csvFile").removeAttr("disabled");
      $("#uploadCsv").removeAttr("disabled");
      $("#downloadCSVLink").show();
      window.alert("Upload successful. Click Generate / Regenerate Report to process data from uploaded CSV.");
      getReport(year);
    },
    error: function (e) {
      $("#csvFile").removeAttr("disabled");
      $("#uploadCsv").removeAttr("disabled");
      $("#downloadCSVLink").show();
      window.alert("Upload failed. Please Retry.");
    }
  });
}