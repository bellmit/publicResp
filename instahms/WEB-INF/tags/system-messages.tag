<%@tag pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
com.insta.hms.master.SystemMessageMaster.SystemMessagesDAO dao = new com.insta.hms.master.SystemMessageMaster.SystemMessagesDAO();
java.util.List beans = dao.listAll();
request.setAttribute("tagbeans", beans);
%>

<table  align="center" >

<tr style="height: 4em">
<td id="infobox" class="message" >
		<c:forEach items="${tagbeans}" var="record" >
		<li style="list-style:none">${record.map.messages} </li>
		</c:forEach>
</td></tr></table>

<script type="text/javascript">
showinfo = function() {
      var infobox = new INSTA.Infobox("infobox", true);
    infobox.run();

}
</script>

<script>
var INSTA = YAHOO.namespace("INSTA");

INSTA.Infobox = function(listElem, cycle, delay) {
    delay = delay || 3000;
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
        var easing = YAHOO.util.Easing.easeOutStrong;
        var callbacks = this.getCallbacks();
        var item;
        for (i=0; i<items.length; i++) {
            item = items[i];
            animin = new Anim(item, { opacity: { to: 1 } }, 4.0, easing);
            animout = new Anim(item, { opacity: { from: 1, to: 0 } }, 2.5, easing);
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
</script>
