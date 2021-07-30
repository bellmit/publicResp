/*
 * This page is to send mail to customer-support
 */
$(document).ready(function(){
    $('#send-mail').on('click',function(){
       window.location.href = "mailto:insta-support@practo.com?subject=Query From Hospital: "+hospitalid; 
    });
});
