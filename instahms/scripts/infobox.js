var INSTA = YAHOO.namespace("INSTA");

showinfo = function() {
    var infobox = new INSTA.Infobox("infobox", true);
    infobox.run();

	// alternate implementation using CSS3. Need to check if the CPU utilization
	// is significantly different between the two options.
	//INSTA.fader = new INSTA.FadeInfoBox("infobox");
	//INSTA.fader.fadeInCurrent(INSTA.fader);
}

INSTA.FadeInfoBox = function(box) {

	this.listElem = document.getElementById(box);
	this.items = this.listElem.getElementsByTagName('li');
	this.cur = 0;    // start with the last guy

	this.fadeInCurrent = function(o) {
		// show and set the guy's opacity, it will fade in due to the transition style (see the css).
		YAHOO.util.Dom.setStyle(o.items[o.cur], 'opacity', '1');
		// schedule the next event
		setTimeout(function() {o.fadeOutCurrent(o)}, 15500);	// .2s to fade in, 15s to stay shown
	}

	this.fadeOutCurrent = function(o) {
		// set the opacity: it will fade out
		YAHOO.util.Dom.setStyle(o.items[o.cur], 'opacity', '0');
		setTimeout(function() {o.showNext(o)}, 500);	// .2s to fade out
	}

	this.showNext = function(o) {
		YAHOO.util.Dom.setStyle(o.items[o.cur], 'display', 'none');
		o.cur = (o.cur+1)%o.items.length;
		YAHOO.util.Dom.setStyle(o.items[o.cur], 'display', 'block');
		setTimeout(function() {o.fadeInCurrent(o)}, 500); // .2s to keep the message hidden for some time
	}

}


INSTA.Infobox = function(listElem, cycle, delay) {
    delay = delay || 11000;
	YAHOO.util.AnimMgr.fps = 10;
	YAHOO.util.AnimMgr.delay = 50;
    this.init(listElem, cycle, delay);
}

INSTA.Infobox.prototype = {

    init: function(listElem, cycle, delay) {
        this.cycle = cycle;
        this.delay = delay;
        this.current = 0;
        var listElem = document.getElementById(listElem);
        var items = listElem.getElementsByTagName('li');
        var anims = new Array(2*items.length);
        var Anim = YAHOO.util.Anim;
        var setStyle = YAHOO.util.Dom.setStyle;
        var easing = YAHOO.util.Easing.easeNone;
        var callbacks = this.getCallbacks();
        var item;
        for (i=0; i<items.length; i++) {
            item = items[i];
            animin = new Anim(item, { opacity: { to: 1 } }, 1.0, easing);
            animout = new Anim(item, { opacity: { from: 1, to: 0 } }, 1.0, easing);
            animin.onStart.subscribe(callbacks.start);
            animin.onComplete.subscribe(callbacks.next);
            animout.onStart.subscribe(callbacks.start);
            animout.onComplete.subscribe(callbacks.next);
            anims[2*i] = animin;
            anims[2*i+1] = animout;
            setStyle( item , 'list-item-type', 'none');
            if (i>0) {
                setStyle( item , 'opacity', 0);
                setStyle( item, 'display', 'none');
            }
        }
        this.anims = anims;
    },

    getCallbacks : function() {
        var instance = this;
        return {
            start: function() {
                YAHOO.util.Dom.setStyle(this.getEl(), 'display', 'block');
            },
            next: function() {
                var nextindex = instance.current + 1;
                var anims = instance.anims;
                if (instance.cycle) {
                    if (nextindex == anims.length) {
                        nextindex = 0;
                    }
                }
                else {
                    if (nextindex == anims.length-1) {
                        //don't fade the last message
                        return;
                    }
                }
                var elem = this.getEl();
                var anim = anims[nextindex];
                if ( nextindex % 2 == 0 ) {
                    instance.show(elem, anim);
                }
                else {
                    setTimeout(function() {instance.show(elem, anim);}, instance.delay);
                }
                instance.current = nextindex;
            }
        }
    },

    run: function() {
        if (this.anims.length > 0) {
            this.anims[0].animate();
        }
    },

    show: function(prev, anim) {
        YAHOO.util.Dom.setStyle(prev, 'display', 'none');
        anim.animate();
    }
}

