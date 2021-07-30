var adddialog = null;
var deletedialog = null;
var deleteHeaderTab = null;
var tabListHeader= [];
var type=null;

//Add YUI params to AddToHomePage Dialog Box 
function addToHomePageDialog() {
    var dialog = document.getElementById('add-to-home-page-dialog');
    dialog.style.display = 'block';
    adddialog = new YAHOO.widget.Dialog('add-to-home-page-dialog', {
        width : '760px',
        visible : false,
        context :['tour-area', 'tl', 'tl'],
        modal : true,
        constraintoviewport : true
    });
    YAHOO.util.Event.addListener('close-add-to-home-page-dialog', 'click',
            closeAddToHomePageDialog, adddialog, true);

    subscribeKeyListeners(adddialog, 'custom');
    adddialog.render();
}

// Display AddtoHomePage Dialog Box
function showAddToHomePageDialog(obj) {
    var row = getThisRow(obj);
    if (adddialog != null) {
        adddialog.cfg.setProperty('context', [ obj, 'tr', 'tl' ], false);
        adddialog.show();
        $('#save-add-to-home-page').prop('disabled', true);
    }
    return false;
}

//To cancel the AddtoHomePageDialog Box
function closeAddToHomePageDialog() {
    this.cancel();
}

// Delete box YUI box 
function deleteDialog() {
    var dialog = document.getElementById('delete-dialog');
    dialog.style.display = 'block';
    deletedialog = new YAHOO.widget.Dialog('delete-dialog', {
        width : '300px',
        visible : false,
        context :['tour-area', 'tl', 'tl'],
        modal : true,
        constraintoviewport : true
    });
    YAHOO.util.Event.addListener('closeDeleteDialog', 'click',closeDeleteDialog, deletedialog, true);
    subscribeKeyListeners(deletedialog, 'custom');
    deletedialog.render();
}

// To show delete box to delete header
function showDeleteDialog(obj) {
    deleteHeaderTab = obj;
    if (deletedialog != null) {
        deletedialog.cfg.setProperty('context', [ obj, 'tr', 'tl' ], false);
        var home_screen_name= $(deleteHeaderTab).parent().children('div').eq(0).text();
        
        if(headerLength > 1) {
        document.getElementById('deleteContent').innerHTML='Are you sure want to remove' + home_screen_name +
            'from home screen ?';
        } else {
            document.getElementById('deleteContent').innerHTML='In Home Page, minimum one category is required.';
            $('#removeDeleteDialog').css('display','none');
            document.getElementById('closeDeleteDialog').innerHTML='<b>OK</b>';
        }
        deletedialog.show();
    }
    return false;
}

//To cancel the delete box 
function closeDeleteDialog() {
    this.cancel();
}

//Delete the selected tab 
function deleteTab() {
    var home_screen = $(deleteHeaderTab).parent().children('div').eq(0).attr('name');
    var form = document.createElement('form');
    form.setAttribute('action', cpath + '/homePage.do');
    form.setAttribute('name', 'delHeaderTab');
    form.setAttribute('method', 'GET');
    
    var method = makeHidden('method', 'method', 'deleteTab');
    var home_screen_id = makeHidden('home_screen_id', 'home_screen_id', home_screen);
    
    form.appendChild(method);
    form.appendChild(home_screen_id);
    
    document.body.appendChild(form);
    eventTracking('Homepage Shortcut Clicks','Delete Shortcut','Delete Shortcut');
    document.delHeaderTab.submit();
    return true;
}

//Load the content of Default List in Add worklist Section of Add To Home Page Dialog Box.
function loadWorkListBox(selectBox, itemList, dispNameVar, valueVar, title, titleValue) {
    if (itemList == null) {
        selectBox.length = 0;
        selectBox.disabled = true;
        return;
    }

    // clear the select box
    selectBox.length = 0;
    selectBox.disabled = false;
    selectBox.selectedIndex = -1;

    var index = 0;

    // add items from the itemList to the select box
    for (var i = 0; i < itemList.length; i++) {
        if(itemList[i].labelName !== 'SETTINGS') {
            if(itemList[i].labelName !== 'REPORTS') {
                var item = itemList[i];
                for (var j = 0; j < itemList[i].subNavList.length; j++) {
                        var subItem = item.subNavList[j];
                        if (dispNameVar == null) {
                            selectBox.options[index] = new Option(subItem, subItem);
                            index++;
                        } else if (typeof subItem[dispNameVar] != 'undefined' 
                                   && subItem !== 'separator' 
                                   && subItem['type'] == 'WorkList') {

                            var option = new Option(subItem[dispNameVar], subItem[valueVar]);
                            selectBox.options[index] = option;
                            selectBox.options[index].setAttribute('onclick','selAddToHomePageWorkList(this)');
                            selectBox.options[index].setAttribute('onmouseenter','addToHomePageHover(this)');
                            selectBox.options[index].setAttribute('onmouseleave','addToHomePageNormal(this)');
                            selectBox.options[index].setAttribute('title',subItem[dispNameVar]);
                            index++;
                        }
                }
            } else {
                var item = itemList[i];
                for (var j = 0; j < itemList[i].subNavList.length; j++) {
                        var subItem = item.subNavList[j];
                        for(var k=0;k<subItem.submenuNavList.length; k++) {
                            var submenuNavListItem = subItem.submenuNavList[k];
                             if (dispNameVar == null) {
                                 selectBox.options[index] = new Option(submenuNavListItem, submenuNavListItem);
                                 index++;
                             } else if (typeof submenuNavListItem[dispNameVar] != 'undefined' 
                                        && submenuNavListItem !== 'separator' 
                                        && submenuNavListItem['type'] == 'WorkList') {
                                    
                                 var option = new Option(submenuNavListItem[dispNameVar], submenuNavListItem[valueVar]);
                                 selectBox.options[index] = option;
                                 selectBox.options[index].setAttribute('onclick','selAddToHomePageWorkList(this)');
                                 selectBox.options[index].setAttribute('onmouseenter','addToHomePageHover(this)');
                                 selectBox.options[index].setAttribute('onmouseleave','addToHomePageNormal(this)');
                                 selectBox.options[index].setAttribute('title',submenuNavListItem[dispNameVar]);
                                 index++;
                             }
                        }
                }
            }
        }
    }
}

//Refresh the report
function refreshReport() {
    var linkToUrl = $('#fav-screen').attr('src');
    $('#fav-screen').attr('src',linkToUrl);
}

//Load the contents in AddToHomePage worklist section.
function loadAddToHomePageDialog() {
    loadWorkListBox(document.getElementById('add-to-home-page-work-list-content'),
                             navList, 'labelName', 'action_id', null, null);
}

$(document).ready(function() {
    var url = window.location.search.substring(1);
    var homePageTab = url.indexOf('_homePagetab');
    if(homePageTab != -1) {
        var homePageTabNumber = url[homePageTab + 13];
        var type = $('.home-page-header .home-page-tab:nth-child('+homePageTabNumber+') .screen-name').attr('type');
        if(type === 'fav') {
            var link= $('.home-page-header .home-page-tab:nth-child('+homePageTabNumber+') .screen-name .screen-link').attr('value');
            var linkToUrl= link;
            linkToUrl=link.replace('method=getScreen', 'method=getReport');
            linkToUrl=linkToUrl + "&home_page_redirect=true"
            if(typeof linkToUrl != 'undefined') {
                $('#fav-screen').attr('src',linkToUrl);
                $('#fav-screen').css({
                    'height' : 1192,
                    'display': 'block'
                })  
                $('.report-title').append('<h1>'+$('.home-page-header .home-page-tab:nth-child('+homePageTabNumber+') .screen-name a').html()+'</h1>');
            }
        }
        $('.home-page-header .home-page-tab:nth-child('+homePageTabNumber+') .topnav-tooltip-header').css({
            'display': 'block',
            'margin-left' : ($('.home-page-header .home-page-tab:nth-child('+homePageTabNumber+') .screen-name').width())/2 - 9
        })
        
        $('.home-page-header .home-page-tab:nth-child('+homePageTabNumber+') .screen-name').css({
            'color': 'rgb(73, 89, 110)'
        })
        
        $(".home-btn").css("background-color","#F2F9FF");
        $(".home-btn").children().children("img").attr("src",cpath+ "/icons/HomeSelected.png");
        $(".home-btn").children().children("span").css("color","#49596E");
        
    } else if(isHomePage === 'true') {
        var type = $('.home-page-header .home-page-tab:nth-child(1) .screen-name').attr('type');
        if(type === 'fav') {
            var link= $('.home-page-header .home-page-tab:nth-child(1) .screen-name .screen-link').attr('value');
            var linkToUrl= link;
            linkToUrl=link.replace('method=getScreen', 'method=getReport');
            linkToUrl=linkToUrl + "&home_page_redirect=true"
            if(typeof linkToUrl != 'undefined') {
                $('#fav-screen').attr('src',linkToUrl);
                $('#fav-screen').css({
                    'height' : 1192,
                    'display': 'block'
                })  
                $('.report-title').append('<h1>'+$('.home-page-header .home-page-tab:nth-child(1) .screen-name a').html()+'</h1>');
            }
        }
        $('.home-page-header .home-page-tab:nth-child(1) .topnav-tooltip-header').css({
            'display': 'block',
            'margin-left' : ($('.home-page-header .home-page-tab:nth-child(1) .screen-name').width())/2 - 9
        })
        
         $('.home-page-header .home-page-tab:nth-child(1) .screen-name').css({
            'color': 'rgb(73, 89, 110)'
        })
        
        $(".home-btn").css("background-color","#F2F9FF");
        $(".home-btn").children().children("img").attr("src",cpath+ "/icons/HomeSelected.png");
        $(".home-btn").children().children("span").css("color","#49596E");
        
    }
    
    //changing the image of add to home page on hover.
    $('.add-to-home-page-button').mouseenter(function() {
        $('.add-to-home-page-img').attr('src',
                cpath + '/icons/plusAddToHomePage_Color.png');
        }).mouseleave(function() {
            $('.add-to-home-page-img').attr('src',
                    cpath + '/icons/plusAddToHomePage.png');
        });
    
    //changing the image of cancel button on hover.  
    $('.home-page-tab .cancel').mouseenter(function() {
        $('.home-page-tab .cancel img').attr('src',
                cpath + '/images/closeButtonColor.png');
        }).mouseleave(function() {
            $('.home-page-tab .cancel img').attr('src',
                    cpath + '/images/close-button1.png');
        });
})
