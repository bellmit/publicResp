$(document).ready(function() {

  $(".foottertxt").parent().css("padding", "0px");

  $("#zohoSync").click(function(){
    $(".sync-block .cross").attr("src","assets/images/loader.gif")
    for (var i = 0; i < $(".sync-block a").length; i++) {
      if($(".sync-block a").eq(i).html() == "Unsynced")
        $(".sync-block a").eq(i).html("Syncing..")
    }
  })

  $('.message .close')
  .on('click', function() {
    $(this)
      .closest('.message')
      .transition('fade')
    ;
  });

  $('.task-table tbody tr').addClass('clickable-row')

  $(".clickable-row").click(function() {
    window.location = $(this).data("href");
  });

  $('#toast-danger').on('click', function(){
    $("#snackbar").addClass('show');
  })

  $('#close-toast').on('click', function(){
    $("#snackbar").slideUp(300);
  })

  $('#close-data-range-toast').on('click', function(){
    $("#date-range-snackbar").slideUp(300);
    $("#date-range-snackbar").removeClass('show');
  });


  $('#voucher-types').dropdown({
    sortSelect: true
  });

  $('.ui.dropdown').dropdown();

  $('.ui.checkbox').checkbox();

  $('.exported').popup({
    transition: 'vertical flip'
  });

  jQuery('.task-table tbody tr').mousedown(function() {
    $('.task-table tbody tr').removeClass('active')
     $('.task-table tbody tr:active').addClass('active');
    });

  jQuery(function(){
     $('.fullscreen.audit-log-modal').modal({
         transition: 'fade up',
        }).modal('show');
     $('.fullscreen.audit-log-modal').modal({
      closable: true
     });
     $('.ui.accordion').accordion();
  });

  jQuery(function(){
	    $('#credential-modal').click(function(){
	      $('.tiny.credential-modal').modal('show');
	    });
	     $('.tiny.credential-modal').modal({
	      closable: true
	    });
 });

  $('.task-accordion').css({display: 'none', cursor: 'pointer'})

  //  CHANGE THIS LATER
  $('#first-head').on('click',function(){
    $('#first-body').toggle("bounce")
  });
  $('#second-head').on('click',function(){
    $('#second-body').toggle("bounce")
  })
  $('#third-head').on('click',function(){
    $('#third-body').toggle("bounce")
  })
  $('#fourth-head').on('click',function(){
    $('#fourth-body').toggle("bounce")
  })
  $('#fifth-head').on('click',function(){
    $('#fifth-body').toggle("bounce")
  })
/*
  jQuery(function(){
        var stickyHeaderTop = $('#stickyfilter').offset().top;
        $(window).scroll(function(){
                if( $(window).scrollTop() > stickyHeaderTop ) {
                      $('.search-header').css({borderBottom: 'none', paddingBottom: '0', marginBottom: '0'})
                        $('#stickyfilter').css({position: 'fixed', width: '100%', top: '0px',left: 0, boxShadow: '7px -2px 13px 0px', transition: '0.3 ease'});

                } else {
                if($(window).width() <= 576){
                    $('.search-header').css({borderBottom: '1px solid #ccc', paddingBottom: '10px'})
                  }
                    $('#stickyfilter').css({position: 'static', top: '0px', boxShadow: 'none', padding: '0'});
                }
        });
    });
*/
    jQuery('.toggle-search').on('click', function(e){
      e.preventDefault();
      jQuery(function(){
        if($('#toggle-label').text() == "Show"){
          $('#toggle-label').text("Hide");
        }
        else
      $('#toggle-label').text("Show");
          $('.search-body').toggle();
          if (!$('.search-body').is(':hidden')){
            $('.search-body').css({scroll: 'none'});
          }
      })
    });

        $(".datepicker").datepicker({
            autoclose: true,
            todayHighlight: true,
            format: 'dd-mm-yyyy',
            dateFormat: 'yy-mm-dd',
//            onSelect: function(date, instance){
//
//            		$.ajax({
//            	        url: 'logModal',
////            	        data: 'fromDate=' + from_date + '&toDate=' + to_date,
//            	        data: 'date=' + date,
//            	        type: 'POST',
//            	        dataType: 'json'
//            	    })
//
//            }
         });

    $('.datepicker').on('changeDate', function(e){
    	var from_date = $('.fd').val();
    	var to_date = $('.td').val();
    	if(from_date != '' && to_date != ''){
    		$("#formdata").submit();
    	}

    });
});

function validateSearchForm() {
	var fromDateObject = $("#voucher_date_0").val().split("-");
	var toDateObject = $("#voucher_date_1").val().split("-");
	var fromDate = new Date(fromDateObject[2], fromDateObject[1] - 1, fromDateObject[0]);
	var toDate = new Date(toDateObject[2], toDateObject[1] - 1, toDateObject[0]);
	if (toDate < fromDate) {
		$("#date-range-snackbar").addClass('show');
		return false;
	}
	return true;
}
