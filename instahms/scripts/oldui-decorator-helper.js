var lastSelAddToHomePageOptionValue = null;
var lastSelAddToHomePageTypeValue = null;
var lastSelAddToHomePage = document.createElement("p");
var startTime;
var isScrolled = true;

function isOnHomePage() {
    if (currentURL.search('home.do') !== -1)
        return true;

    /*In case user is on main page i.e. "/", still the main activity is on Home Screen.*/
    return (currentURL.split("/")[4].length === 0);
}
function isOnSettingsPage() {
    return isSettingsPage
         || currentURL.search('/sequences/') !== -1
         || currentURL.search('/ippreference.do') !== -1
         || currentURL.search('/systempreferences.htm') !== -1
         || currentURL.search('/ItemInsuranceCategoryMaster.do') !== -1
         || currentURL.search('/resourcemanagement/contractsType.do') !== -1
         || currentURL.search('/pages/masters/') !== -1
         || currentURL.search('/vitalparameter.htm') !== -1
         || currentURL.search('/UserDashBoard.do') !== -1
         || currentURL.search('/messages/message/Message.do') !== -1
         || currentURL.search('/MessageLog.do') !== -1
         || currentURL.search('/CenterProfileMapping.do') !== -1
         || currentURL.search('/DoctorProfileMapping.do') !== -1
         || (currentURL.search('/master/') !== -1
                && currentURL.search('resourceschedulers.htm') === -1
                && currentURL.search('resourceoverrides.htm') === -1
                && currentURL.search('FixedAssetMaster.do') === -1);
}

function removeMask() {
    $('#default-mask').css("display","none");
    $('.usernav-content').toggleClass('hide').toggleClass('show');
    $('.usernav-content').toggleClass('hide-overflow');
}

function subscribeKeyListeners(dialog, type) {
    var escKeyListener = new YAHOO.util.KeyListener(document, {
        keys : 27
    }, {
        fn : closeDialog,
        scope : dialog,
        correctScope : true
    });

    dialog.cfg.setProperty("keylisteners", [ escKeyListener ]);
}

function closeDialog() {
    this.cancel();
}

function addToHomePageHover(selEl) {
    if (selEl !== lastSelAddToHomePage) {
        selEl.style.backgroundColor = "#EAF6FF";
    }
}

function addToHomePageNormal(selEl) {
    if (selEl !== lastSelAddToHomePage) {
        selEl.style.backgroundColor = "#FFFFFF";
    }
}

// This function is to show selected option of Work List in AddToHomePageDialog Box.
function selAddToHomePageWorkList(selEl) {
    lastSelAddToHomePage.style.cssText += 'background-color: #FFFFFF; font-weight:normal; color:#646668';
    selEl.style.cssText += 'background-color: #49596E; font-weight:bold; color:#FFFFFF';

    document.getElementById("sel-add-to-home-page").innerHTML = selEl.text;
    document.getElementById("value-option").innerHTML = $(selEl).attr('value');
    document.getElementById("type-option").innerHTML = 'W';

    lastSelAddToHomePage = selEl;
    lastSelAddToHomePageOptionValue = $(selEl).attr('value');
    lastSelAddToHomePageTypeValue = 'W';

    $('#save-add-to-home-page').prop('disabled', false);
    for(i=0;i<tabListHeader.length;i++) {
        var item = tabListHeader[i];
        if((item[0].length ==0) && item[1] == selEl.text ) {
            $('#save-add-to-home-page').prop('disabled', true);
        }
    }
}

// This function is to show selected option of saved search in AddToHomePageDialog Box.
function selAddToHomePageSavedSearch(selEl) {
    lastSelAddToHomePage.style.cssText += 'background-color: #FFFFFF; font-weight:normal; color:#646668';
    selEl.style.cssText += 'background-color: #49596E; font-weight:bold; color:#FFFFFF';

    document.getElementById("sel-add-to-home-page").innerHTML = selEl.text;
    document.getElementById("value-option").innerHTML = $(selEl).attr('value');
    document.getElementById("type-option").innerHTML = 'S';

    lastSelAddToHomePage = selEl;
    lastSelAddToHomePageOptionValue = $(selEl).attr('value');
    lastSelAddToHomePageTypeValue = 'S';

    $('#save-add-to-home-page').prop('disabled', false);
    for(i=0;i<tabListHeader.length;i++) {
        var item = tabListHeader[i];
        if((item[0].length)!=0 && item[1] == selEl.text ) {
            $('#save-add-to-home-page').prop('disabled', true);
        }
    }
}

//This function is to show selected option of Fav Report in AddToHomePageDialog Box.
function selAddToHomePageFavReport(selEl) {
    lastSelAddToHomePage.style.cssText += 'background-color: #FFFFFF; font-weight:normal; color:#646668';
    selEl.style.cssText += 'background-color: #49596E; font-weight:bold; color:#FFFFFF';

    document.getElementById("sel-add-to-home-page").innerHTML = selEl.text;
    document.getElementById("value-option").innerHTML = $(selEl).attr('value');
    document.getElementById("type-option").innerHTML = 'F';

    lastSelAddToHomePage = selEl;
    lastSelAddToHomePageOptionValue = $(selEl).attr('value');
    lastSelAddToHomePageTypeValue = 'F';

    $('#save-add-to-home-page').prop('disabled', false);
    for(i=0;i<tabListHeader.length;i++) {
        var item = tabListHeader[i];
        if((item[0].length ==0) && item[1] == selEl.text ) {
            $('#save-add-to-home-page').prop('disabled', true);
        }
    }
}

/*
 * F denotes favourite Reports.
 * S denotes Saved Search.
 * W denotes Work List.
 */
function addTofavScreen(selValue, optionValue, typeValue) {
    if (typeValue == 'S') {
        var screen_name=selValue.text;
        if (empty(screen_name) || empty(optionValue))  {
            return false;
        } else {
            var form = document.createElement('form');
            form.setAttribute('action', cpath + '/homePage.do');
            form.setAttribute('name', 'addToHomePage');
            form.setAttribute('method', 'GET');

            var method = makeHidden('method', 'method', 'saveSearch');
            var screen_name = makeHidden('screen_name', 'screen_name', screen_name);
            var search_id = makeHidden('search_id', 'search_id', optionValue);
            var tab = makeHidden('_tab', '_tab', '1');

            form.appendChild(screen_name);
            form.appendChild(method);
            form.appendChild(search_id);
            form.appendChild(tab);

            document.body.appendChild(form);
            eventTracking('Homepage Shortcut Clicks','AddToHomePage Shortcut','AddToHomePage Shortcut');
            document.addToHomePage.submit();
            return true;
        }


    } else if (typeValue == 'F') {
        var screen_name=selValue.text;
        if (empty(screen_name) || empty(optionValue))  {
            return false;
        } else {
            var form = document.createElement('form');
            form.setAttribute('action', cpath + '/homePage.do');
            form.setAttribute('name', 'addToHomePage');
            form.setAttribute('method', 'GET');

            var method = makeHidden('method', 'method', 'saveFavReports');
            var screen_name = makeHidden('screen_name', 'screen_name', screen_name);
            var search_id = makeHidden('report_id', 'report_id', optionValue);
            var tab = makeHidden('_tab', '_tab', '1');

            form.appendChild(screen_name);
            form.appendChild(method);
            form.appendChild(search_id);
            form.appendChild(tab);

            document.body.appendChild(form);
            eventTracking('Homepage Shortcut Clicks','AddToHomePage Shortcut','AddToHomePage Shortcut');
            document.addToHomePage.submit();
            return true;
        }

    } else if (typeValue == 'W' ) {
        var screen_name=selValue.text;
        if (empty(screen_name) || empty(optionValue))  {
            alert("field are empty");
            return false;
        } else {
            var form = document.createElement('form');
            form.setAttribute('action', cpath + '/homePage.do');
            form.setAttribute('name', 'addToHomePage');
            form.setAttribute('method', 'GET');

            var method = makeHidden('method', 'method', 'saveWorklistReport');
            var screen_name1 = makeHidden('screen_name', 'screen_name', screen_name);
            var action_id =null;
            var query = null;

            if(typeValue=='W') {
                for(i=0; i< navList.length ; i++) {
                    if(navList[i].id !== 'grp_settings') {
                        if(navList[i].id !== 'grp_reports') {
                            var test = navList[i].subNavList;
                            for(j=0;j<test.length;j++) {
                                if(screen_name === test[j].labelName) {
                                    action_id = test[j].action_id;
                                    query = test[j].query;
                                }
                            }
                        } else {
                            var test = navList[i].subNavList;
                            for(j=0;j<test.length;j++) {
                                var submenuNavListItem = test[j].submenuNavList;
                                for(k=0;k<submenuNavListItem.length;k++) {
                                     if(screen_name === submenuNavListItem[k].labelName) {
                                         action_id = submenuNavListItem[k].action_id;
                                         query = submenuNavListItem[k].query;
                                     }
                                }
                            }
                        }
                    }
                }
            }

            var actionId = makeHidden('action_id', 'action_id', action_id);
            var Query = makeHidden('query', 'query', query);
            var tab = makeHidden('_tab', '_tab', '1');

            form.appendChild(screen_name1);
            form.appendChild(method);
            form.appendChild(actionId);
            form.appendChild(Query);
            form.appendChild(tab);

            document.body.appendChild(form);
            eventTracking('Homepage Shortcut Clicks','AddToHomePage Shortcut','AddToHomePage Shortcut');
            document.addToHomePage.submit();
            return true;
        }
    }
}
