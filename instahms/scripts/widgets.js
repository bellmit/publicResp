/***************************************
  Generic toggle functions. 
****************************************/

/* 
  1. Finds <type>:id and page:id
  2. Sets display for node identified by page:id to block or none
  3. Appends or removes "-open" in style for <type>:id
*/
function toggle(type, id)
{
    var ctrlNode = document.getElementById(type + ":" + id);
    var curStyle = ctrlNode.className;
    var closedStyle = curStyle.replace("-open", "");
    var openStyle = closedStyle + "-open";

    var isClosed = (curStyle == closedStyle);
    var page = document.getElementById("page:" + id);

    if (isClosed)
    {
        ctrlNode.className = openStyle;
        page.style.display = "block";
    }
    else
    {
        ctrlNode.className = closedStyle;
        page.style.display = "none";
    }
}

function doOpen(type, id, shouldOpen)
{
    var ctrlNode = document.getElementById(type + ":" + id);
    var curStyle = ctrlNode.className;
    var closedStyle = curStyle.replace("-open", "");
    var openStyle = closedStyle + "-open";

    var isClosed = (curStyle == closedStyle);
    var page = document.getElementById("page:" + id);

    if (isClosed && shouldOpen)
    {
        ctrlNode.className = openStyle;
        page.style.display = "block";
    }
    else if (!isClosed && !shouldOpen)
    {
        ctrlNode.className = closedStyle;
        page.style.display = "none";
    }
}


/************************
  twiddle functions
**************************/
function twOpen(id)
{
    doOpen("tw", id, true);
}

function twClose(id)
{
    doOpen("tw", id, false);
}

function twToggle(id)
{
    toggle("tw", id);
}

function stwToggle(hdr)
{
    var open;
    if (hdr.className.indexOf("stwClosed") != -1)
        open = true;
    else
        open = false;

    stwOpen(hdr, open); 
}

function stwOpen(hdr, open)
{
    var content = document.getElementById(hdr.id + "_content");
    if (open)
    {
        removeName(hdr, "stwClosed", ' ');
        removeName(content, "stwHidden", ' ');
    }
    else
    {
        addName(hdr, "stwClosed", ' ');
        addName(content, "stwHidden", ' ');
    }
}


/*************************************
  Normal tree functions (same as twiddle)
*************************************/
function treeOpen(id)
{
    doOpen("tree", id, true);
}

function treeToggle(id)
{
    toggle("tree", id);
}

function treeClose(id)
{
    doOpen("tree", id, false);
}

/*
   expand a tree, starting at a certain point to a certain level 
   Note: this assumes you have ids which are like paths, separated by /
*/
function treeExpand(id, level)
{
    /* 
       Strategy: Find all nodes with class name treeNode, or class name treeLastNode.
       Iterate, if the id has N number of slashes more than level, close else, open.
       TODO: change this to use getElementsByIdPrefix instead of classname.
     */
    var root = document.getElementById(id);
    var allTreeNodes = getElementsByClassName(root, '*', "treeNode");
    allTreeNodes = allTreeNodes.concat(getElementsByClassName(root, '*', "treeLastNode"));

    for(var i=0; i<allTreeNodes.length; i++)
    {
        var node = allTreeNodes[i];
        var baseid = node.id.replace("tree:", "");
        var paths = baseid.split("/");
        if (paths.length <= level)
        {
            treeOpen(baseid);
        }
        else
        {
            treeClose(baseid);
        }
    }
}

/*************************
  extRow functions
**************************/
function exrOpenShow(parent, tab)
{
    twOpen(parent);
    tabShow(parent, tab);
}

function exrToggle(id)
{
    toggle("exr", id);
}

/****************************************
  Normal tab hide/show/init functions
*****************************************/
function tabInitialize(tabMainId, newActiveId)
{
    var tabMain = document.getElementById("tabs:" + tabMainId);

    // hide all tabs
    var tabPages = getElementsByIdPrefix(tabMain, '*', "page:");
    for (var i=0; i < tabPages.length; i++)
    {
        tabPages[i].style.display = "none";
    }

    // add onclick to all tab buttons
    var tabButtons = getElementsByIdPrefix(tabMain, '*', "btn:");
    for (var i=0; i < tabButtons.length; i++)
    {
        var btn = tabButtons[i];

        // init the tab buttons
        var btnId = btn.id;
        btnId = btnId.split(":")[1];
        // set the onclick to show ourselves
        if (! btn.getAttribute("onclick"))
        {
            // btn.setAttribute("onclick", "tabShow('"+tabMainId+"','"+btnId+"');");
            // setAttribute does not work for IE, we need to set the onclick
            // directly like this.
            btn.onclick = function(){tabAutoShow(this);}; 
        }
    }

    // show the given one
    tabActivate(tabMain, newActiveId, true);
}

function tabAutoShow(el)
{
    /* automatically figure out containing block, as well as the id
       Insert code like this: onclick="tabAutoShow(this)" in the tab button element
       ensure that the id of this is set to btn:something */

    // get the actual ID based on 
    btnId = el.id.split(':')[1];
    // move up the hierarchy till we find an el with id = tabs:<id>
    p = el.parentNode;
    while(p)
    {
        if (p.id && p.id.match(/^tabs:/))
            break;
        p = p.parentNode;
    }
    if (p)
    {
        tabMainId = p.id.split(':')[1];
        tabShow(tabMainId, btnId);
    }
}

function tabShow(tabMainId, newActiveId)
{
    var tabMain = document.getElementById("tabs:" + tabMainId);

    /* hide the currently active tab */
    var activeId = tabMain.getAttribute("active");
    if (activeId)
    {
        tabActivate(tabMain, activeId, false);
    }

    /* show the newly active tab */
    tabActivate(tabMain, newActiveId, true);
}

function tabActivate(tabMain, Id, show)
{
    var page = document.getElementById("page:" + Id);
    if (!page)
    {
        alert("tabActivate: Tab page not found. Id: " + Id + 
              "; tabId: " + tabMain.id + "; show: " + show);
        return;
    }
    var button = document.getElementById("btn:" + Id);
    if (show)
    {
        page.style.display = "block";
        addName(button, "active");
    }
    else
    {
        page.style.display = "none";
        removeName(button, "active");
    }
    tabMain.setAttribute("active", Id);
}

function showHashPage()
{
    /* The URL's hash part indicates which tab hierarchy is
       currently active. Activate this tab hierarchy during load */

    var hash = window.location.hash;
    if (hash)
    {
        tabActivatePath(hash.substr(1));  // remove the # in front
    }
}

function tabActivatePath(path)
{
    /* path is like /vsite-general or /web-firewall/web-acls */
    var dirname = dirName(path);
    tabShow(dirname, path);

    /* recurse */
    if (dirname != "/")
        tabActivatePath(dirname);
}

/*************************************
  specialized tree open and pin functions
*************************************/
function toggleTree()
{
    var tree = document.getElementById("s:tree");
    if (tree.style.display == "block")
      tree.style.display = "none";
    else
      tree.style.display = "block";
}

function togglePin()
{
    var tree = document.getElementById("s:tree");
    if (tree.style.position == "absolute")
    {
        tree.style.position = "relative";
        tree.style.height = "auto";
        tree.style.display = "block";
        var btn = document.getElementById("btn:tree");
        addName(btn, 's-open');
    }
    else
    {
        tree.style.position = "absolute";
        tree.style.display = "none";
        var btn = document.getElementById("btn:tree");
        removeName(btn, 's-open');
    }
}

/***********************************
  menu functions
***********************************/
/*
  At any time, active attr of the menu node in the HTML DOM
  contains the chain of currently open menus. Eg, "/vsite1/app1"
*/

function menuToggle(menuId, itemId)
{
    var node = document.getElementById("menu:" + menuId + ":" + itemId);
    if (!node)
        alert("Node not found: " + itemId);
    var isClosed = !node.className.match("menu-open");
    var menu = document.getElementById("menu:" + menuId);

    if (isClosed)
    {
        menuCloseOthers(menuId, itemId);
        var page = document.getElementById("mpage:" + menuId + ":" + itemId);
        addName(node, "menu-open");
        page.style.display = "block";
        menu.active=itemId;
    }
    else
    {
        /* close all children and self only. Leave parents open */
        var parent = dirName(itemId);
        menuCloseOthers(menuId, parent);
        menu.active=parent;
    }
}

function menuClose(menuId, itemId)
{
    var node = document.getElementById("menu:" + menuId + ":" + itemId);
    var isClosed = !node.className.match("menu-open");
    var menu = document.getElementById("menu:" + menuId);

    /* alert("close: " + itemId);  */
    var page = document.getElementById("mpage:" + menuId + ":" + itemId);
    removeName(node, "menu-open");
    page.style.display = "none";
}

/*
  close menu items not matching new path
*/
function menuCloseOthers(menuId, itemId)
{
    var menu = document.getElementById("menu:" + menuId);
    var active = menu.active;
    if (!active)
        return;

    var match = matchPath(active, itemId);

    iter = 0;   /* sanity */
    while (match != active)
    {
        menuClose(menuId, active);
        active = dirName(active);
        iter = iter + 1;
        if (iter > 10) break;
    }
}

/***********************************
  hrefs in dropdowns, select handler
***********************************/
function selGoto(select,target)
{
    var selIndex = select.selectedIndex;
    var href = select.options[selIndex].value;
    if (select.options[selIndex].getAttribute("target"))
        target=select.options[selIndex].getAttribute("target");
    goTo(href,target);
    select.selectedIndex=0;
}

function hrefGoto(a,target)
{
    goTo(a.href,target);
}

function goTo(url,target)
{
    if (url && (url.length > 0) )
    {
        if (target == "_dialog")
        {
            return openDialogFor(url);
        }
        if (target == "_blank")
        {
            return openNewWindowFor(url);
        }
        if (parent && target && (target.length > 0) )
        {
            if (target == "_parent")
            {
                parent.location.href = url;
            }
            else if (target == "_gparent")
            {
                parent.parent.location.href = url;
            }
            else if (parent.frames && parent.frames[target])
            {
                parent.frames[target].location.href = url;
            }
            else
            {
                window.location.href = url;
            }
        }
        else
        {
            window.location.href = url;
        }
    }
}

/********************************
  Open a dialog (new window)
********************************/
function openDialog(a)
{
    return openDialogFor(a.href);
}

function openDialogFor(href)
{
    window.open (href, 'nc_dialog', 
      config='height=500, width=400, top=200, left=200, toolbar=no, menubar=no, scrollbars=yes, resizable=yes, location=no, directories=no, status=no');
    return false;
}

function openNewWindowFor(href)
{
    window.open (href, 'nc_newwin');
    return false;
}

/*********************************
  Utils
**********************************/
function removeName(el, name, separator) 
{
    var i, curList, newList;
    if (!separator) separator=" ";

    if (el.className == null)
        return;

    // Remove the given class name from the element's className property.
    newList = new Array();
    curList = el.className.split(separator);
    for (i = 0; i < curList.length; i++)
        if (curList[i] != name)
            newList.push(curList[i]);

    el.className = newList.join(separator);
}

function addName(el, name, separator)
{
    if (!separator) separator=" ";
    if (!el.className.match(name))
        el.className = el.className + separator + name;
}

/*
  returns parent dir of given path.
   /a/b -> /a
   /a   -> /
   /    -> ""
*/

function dirName(path)
{
    var newpath;
    if (path == "/")
        newpath = "";
    else
    {
        var lastSlash = path.lastIndexOf("/");
        if (lastSlash == 0)
            newpath = "/";
        else
            newpath = path.substr(0,lastSlash);
    }
    /* alert("dirName: " + path + ": " + newpath);  */
    return newpath;
}

/*
  returns base name of given path.
   /a/b -> b
   /a   -> a
   /    -> /
*/

function baseName(path)
{
    var basename;
    if (path == "/")
        basename = "/";
    else
    {
        var lastSlash = path.lastIndexOf("/");
        basename = path.substr(lastSlash+1);
    }
    /* alert("baseName: " + path + ": " + basename);  */
    return basename;
}

/*
  find the longest matching path of two paths
    /a/b/c, /a/b/x -> /a/b
    /a/b/c, /a/x/y -> /a
    /a/b , /x/y  -> /
*/
function matchPath(a, b)
{
    /* pick one, keep shortening it till we find a match */
    while (b != "")
    {
        /* alert("matchPath: " + a + "-" + b + " index: " + a.indexOf(b)); */
        if (a.indexOf(b) != 0)
            b = dirName(b);
        else
            break;
    }
    /* alert("foundMatch: " + b); */
    return b;
}

function getElementsByClassName(oElm, strTagName, strClassName)
{
    var arrElements = (strTagName == "*" && oElm.all) ? 
                oElm.all : oElm.getElementsByTagName(strTagName);
    var arrReturnElements = new Array();
    strClassName = strClassName.replace(/\-/g, "\\-");
    var oRegExp = new RegExp("(^|\\s|-)" + strClassName + "(-|\\s|$)");
    var oElement;

    for(var i=0; i<arrElements.length; i++)
    {
        oElement = arrElements[i];      
        if(oRegExp.test(oElement.className))
        {
            arrReturnElements.push(oElement);
        }   
    }
    return (arrReturnElements)
}

function getElementsByIdPrefix(oElm, strTagName, strPrefix)
{
    var arrElements = (strTagName == "*" && oElm.all) ? 
                oElm.all : oElm.getElementsByTagName(strTagName);
    var arrReturnElements = new Array();

    var oRegExp = new RegExp("^" + strPrefix);
    var oElement;

    for(var i=0; i<arrElements.length; i++)
    {
        oElement = arrElements[i];      
        if(oRegExp.test(oElement.id))
        {
            arrReturnElements.push(oElement);
        }   
    }
    return (arrReturnElements)
}

/*********************************
  Debugging
**********************************/
function displayDomTree(startNode, indentLevel)
{
  var children = startNode.childNodes;
  var outString = "";

  //alert("Level: " + indentLevel + "; length: " + children.length);

  for (var i=0; i < children.length; i++)
  {
    child = children.item(i);
    outString = outString + toString(child, indentLevel);
    outString = outString + displayDomTree(child, indentLevel+1);
  }
  //alert("Outstring is: " + outString);

  return outString;
}

function toString(node, indentLevel)
{
  if (node == undefined)
    return "undefined";
  var indent = "";
  for (var i=0; i< indentLevel ; i++)
  {
    indent = indent + "..";
  }
  return (indent + " Name: " + node.tagName + 
          "; ID: " + node.id + "; active: " + node.getAttribute("active") +
          "; class: " + node.className + "\n");

}

function whereAmI(node)
{
  alert(toString(node, 0));
}


