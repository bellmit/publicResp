//Event tracking google-analytics
function eventTracking(category,action,label) {
    if(typeof ga !== 'undefined' && ga) {
        ga('send', 'event', category,action,label);
    } 
}

//tracking user timings
function timeTracking(category,variable,value) {
    if(typeof ga !== 'undefined' && ga) {
        ga('send','timing',category,variable,value);
    } 
}
