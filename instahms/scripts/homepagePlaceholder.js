/*
 * This file sets configuration for placeholder slider.
 * The configuration is used to create slider using jssor slider.
 */
var placeholderSlider = null;
jQuery(document).ready(function ($) {
    var options = {
        $AutoPlay: true,
        $AutoPlaySteps: 1,
        $Idle: 3000,
        $PauseOnHover: 1,
        $ArrowKeyNavigation: true,
        $SlideEasing: $JssorEasing$.EaseOutQuad,
        $SlideDuration: 300,
        $MinDragOffsetToSlide: 20,
        $SlideSpacing: 0,
        $Cols: 1,
        $ParkingPosition: 0,
        $UISearchMode: 1,
        $PlayOrientation: 1,
        $DragOrientation: 1,

        $BulletNavigatorOptions: {
            $Class: $JssorBulletNavigator$,
            $ChanceToShow: 2,
            $AutoCenter: 1,
            $Steps: 1,
            $Rows: 1,
            $SpacingX: 12,
            $SpacingY: 4,
            $Orientation: 1,
            $Scale: false
        }
    };

    $('.usernav').mouseenter(function() {
        if(typeof placeholderSlider === 'object')
        {
        	placeholderSlider.$Pause();
        }
    }).mouseleave(function() {
        if(typeof placeholderSlider === 'object')
        {
        	placeholderSlider.$Play();
        }
    });

    $('.helpnav').mouseenter(function() {
        if(typeof placeholderSlider === 'object')
        {
        	placeholderSlider.$Pause();
        }
    }).mouseleave(function() {
        if(typeof placeholderSlider === 'object')
        {
        	placeholderSlider.$Play();
        }
    });

    $('.home-btn').mouseenter(function() {
        if(typeof placeholderSlider === 'object')
        {
        	placeholderSlider.$Pause();
        }
    }).mouseleave(function() {
        if(typeof placeholderSlider === 'object')
        {
        	placeholderSlider.$Play();
        }
    });

    $('.msgnav').mouseenter(function() {
        if(typeof placeholderSlider === 'object')
        {
        	placeholderSlider.$Pause();
        }
    }).mouseleave(function() {
        if(typeof placeholderSlider === 'object')
        {
        	placeholderSlider.$Play();
        }
    });

	$('.leftnav').mouseover(function() {
        if(typeof placeholderSlider === 'object')
        {
        	placeholderSlider.$Pause();
        }
    }).mouseleave(function() {
        if(typeof placeholderSlider === 'object')
        {
        	placeholderSlider.$Play();
        }
    });

    placeholderSlider = new $JssorSlider$("homepage-placeholder", options);
});
