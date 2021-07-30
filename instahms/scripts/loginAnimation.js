/*
 * This page sets the animation : 
 * 	a)	sending label up while clicking on input field.
 * 	b) 	Changing button color on hover.
 *  c) drawing a line across the input field on click.
 *  d) Opening page with animation. 
 */

// This function sends the label up when it is auto filled by browser.
function listner() {
	$('#password-Label').css({
		'line-height' : '18px',
		'font-size' : '10px',
		'top' : '20px'
	});
}

function hospitalListner() {
	$('#hospital-Label').css({
		'line-height' : '18px',
		'font-size' : '10px',
		'top' : '20px'
	});
}

$(document).ready(function() {
	var hospital = $('#hospital').val();
	var userId = $('#userId').val();

	if (hospital != '') {
		$('#hospital-Label').css({
			'line-height' : '18px',
			'font-size' : '10px',
			'top' : '20px'
		})
	}

	$('#userId').change(function() {

		$('#userId-Label').css({
			'line-height' : '18px',
			'font-size' : '10px',
			'top' : '20px'
		})
		$('#password-Label').css({
			'line-height' : '18px',
			'font-size' : '10px',
			'top' : '20px'
		})
	});

	$('#oldpwd').change(function() {
		$('#oldpwd-Label').css({
			'line-height' : '18px',
			'font-size' : '10px',
			'top' : '20px'
		})
	});

	$('.login-button').hover(function() {
		$(this).css('background-color', '#F59A00');
		$(this).css('color', '#FFFFFF');
	}, function() {
		$(this).css('background-color', '#FFA000');
		$(this).css('color', '#FFFFFF');
	});

	$('.save-new-pass-button').hover(function() {
		$(this).css('background-color', '#F59A00');
		$(this).css('color', '#FFFFFF');
	}, function() {
		$(this).css('background-color', '#FFA000');
		$(this).css('color', '#FFFFFF');
	});
	
	$('.remind-button').hover(function() {
		$(this).css('background-color', '#B4B4BE');
		$(this).css('color', '#FFFFFF');
		$(this).css('border', '1px solid transparent');
	}, function() {
		$(this).css('background-color', '#F6F6F9');
		$(this).css('color', '#787887');
		$(this).css('border', '1px solid #787887');
	});
	
	$('.cancel-button').hover(function() {
		$(this).css('background-color', '#B4B4BE');
		$(this).css('color', '#FFFFFF');
		$(this).css('border', '1px solid transparent');
	}, function() {
		$(this).css('background-color', '#F6F6F9');
		$(this).css('color', '#787887');
		$(this).css('border', '1px solid #787887');
	});
	
	$('.changePassword-button').hover(function() {
		$(this).css('background-color', '#F59A00');
		$(this).css('color', '#FFFFFF');
	}, function() {
		$(this).css('background-color', '#FFA000');
		$(this).css('color', '#FFFFFF');
	});

	$('.login-card').animate({
		'opacity' : '1'
	});

	$('.login-card-footer').animate({
		'opacity' : '1'
	});

	$('.form-input input').focus(function() {
		$(this).parent('.form-input').each(function() {
			$('label', this).css({
				'line-height' : '18px',
				'font-size' : '10px',
				'top' : '20px'
			})
			$('.spin', this).css({
				'width' : '100%'
			})
		});
	}).blur(function() {
		$('.spin').css({
			'width' : '0px'
		})
		if ($(this).val() == '') {
			$(this).parent('.form-input').each(function() {
				$('label', this).css({
					'line-height' : '35px',
					'font-size' : '12px',
					'top' : '35px'
				})
			});

		}
	});

	//When user press tab then button should get highlighted.
	$('#login').focus(function() {
		$(this).css({
			'background-color' : '#F59A00',
			'color' : '#FFFFFF'
		});
	}).blur(function() {
		$(this).css({
			'background-color' : '#FFA000',
			'color' : '#FFFFFF'
		})
	});
	
	$('#changePassword').focus(function() {
		$(this).css({
			'background-color' : '#F59A00',
			'color' : '#FFFFFF'
		});
	}).blur(function() {
		$(this).css({
			'background-color' : '#FFA000',
			'color' : '#FFFFFF'
		})
	});
	
	$('#submit').focus(function() {
		$(this).css({
			'background-color' : '#F59A00',
			'color' : '#FFFFFF'
		});
	}).blur(function() {
		$(this).css({
			'background-color' : '#FFA000',
			'color' : '#FFFFFF'
		})
	});
	
	$('#submit-expire').focus(function() {
		$(this).css({
			'background-color' : '#F59A00',
			'color' : '#FFFFFF'
		});
	}).blur(function() {
		$(this).css({
			'background-color' : '#FFA000',
			'color' : '#FFFFFF'
		})
	});
	
	
	$('#cancel').focus(function() {
		$(this).css({
			'color' : '#3E3E3E'
		});
	}).blur(function() {
		$(this).css({
			'color' : '#AEAEAE'
		})
	});
	
	$('.remind-button').focus(function() {
		$(this).css({
			'color' : '#3E3E3E'
		});
	}).blur(function() {
		$(this).css({
			'color' : '#AEAEAE'
		})
	});
	
	//This function is to set the margin of footer so it remain at bottom. 
	setTimeout(function() { 
		var screenHeight =window.innerHeight;
		var loginCardHeight = $('.login-card').height();
		var loginCardFooterHeight = $('.login-card-footer').height() ;
		var fontSizeFooter = parseInt($('.footer').css('font-size') || 10);
		var bodyMarginBottom = parseInt($('body').css('margin-bottom') || 10);
		var loginCardMarginHeight = 84;
		var paddingHight = 24;
		var result= screenHeight - loginCardHeight - loginCardFooterHeight - fontSizeFooter - bodyMarginBottom - loginCardMarginHeight -paddingHight;
		if(result>0) {
			$('.footer').css({
				'margin-top' : result,
				'display' : 'block'
			});
		}
	}, 50);
	 
	$( window ).resize(function() {
		$('.footer').css({
			'display' : 'none'
		});
		
		setTimeout(function() { 
			var screenHeight =window.innerHeight;
			var loginCardHeight = $('.login-card').height();
			var loginCardFooterHeight = $('.login-card-footer').height() ;
			var fontSizeFooter = parseInt($('.footer').css('font-size'), 10);
			var bodyMarginBottom = parseInt($('body').css('margin-bottom'), 10);
			var loginCardMarginHeight = 84;
			var paddingHeight = 24;
			var result= screenHeight - loginCardHeight - loginCardFooterHeight - fontSizeFooter 
					- bodyMarginBottom - loginCardMarginHeight - paddingHeight;
			if(result > 0) {
				$('.footer').css({
					'margin-top' : result,
					'display' : 'block'
				});
			}
		}, 50);
	});
});
