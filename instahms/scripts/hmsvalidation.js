var digits = "0123456789";
var lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
var uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
var whitespace = " \t\n\r";
var decimalPointDelimiter = ".";

 // Allows char and spaces, won't allow any space at char(0)
 function SpaceAtWill(e,fieldName)
  {

   var key=0;
    if(window.event)
    {
        key = e.keyCode;
       //alert(key)
    }
    else
    {
       key = e.which
    }
   if((key>=65)&&(key<=90)||(key>=97)&&(key<=122)||key==8||key==0)
     {

        key=key;
        return true;

     }
     else
     {

     var src = eval("document.forms[0]." + fieldName );

     var strVal=new String(src.value);
     //alert("value----"+strVal.charAt(0));
     if(strVal.charAt(1)=="")
     {
       return false;
      }
      return true;
     }
  }

 // Converts to Uppercase
 function upperCase(obj)
 {
	obj.value=obj.value.toUpperCase();
 }


function capWords(obj) {
	var inputString = obj; // The input text field
	var tmpStr, tmpChar, preString, postString, strlen;
	tmpStr = inputString.value;
	stringLen = tmpStr.length;
	if (stringLen > 0){
	  for (i = 0; i < stringLen; i++){
	    if (i == 0){
	      tmpChar = tmpStr.substring(0,1).toUpperCase();
	      postString = tmpStr.substring(1,stringLen);
	      tmpStr = tmpChar + postString;
	    }else{
	      tmpChar = tmpStr.substring(i,i+1);
	      if (tmpChar == " " && i < (stringLen-1)){
		      tmpChar = tmpStr.substring(i+1,i+2).toUpperCase();
		      preString = tmpStr.substring(0,i+1);
		      postString = tmpStr.substring(i+2,stringLen);
		      tmpStr = preString + tmpChar + postString;
	      }
	    }
	  }
	}
	obj.value = tmpStr;
}


var sysdate=null;
var id1C;
var id2C;
  function load(id1,id2) {
  id1C=id1;
  id2C=id2;
  	 getServerTime();
}

function returnLoad(sys){


sysdate=sys;

 var months=new Array('JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC');


	  var date=sysdate.getDate();
	  var month=months[sysdate.getMonth()];
	  var year=sysdate.getFullYear();
	  if(date<10)
	   {
	    date="0"+date;
	   }
var hours=sysdate.getHours();
var minutes=sysdate.getMinutes();
var seconds=sysdate.getSeconds();


var amorpm = 'AM'
 	if(hours>12)
	{

  	hours=hours-12;
  	amorpm='PM';
	}
  if(hours==12)
  {
  amorpm='PM';
  }

	 if(hours<=9)
	{
	hours ='0' + hours;

	}
if(minutes<10) {
minutes="0"+minutes;
}

if(seconds<10) {
seconds="0"+seconds;
}

var d= date+'-'+month+'-'+year;
var t=hours+":"+minutes+":"+seconds + " " + amorpm;
document.getElementById(id1C).value=d;
document.getElementById(id2C).value=t;
startTimer();


}





	 // FUNCTION FOR ALLOW ALPHABETS AND SPACES,&,- (Only)

	function enterAlphanAndHip(e){
		var key=0;
		if(window.event){
			key = e.keyCode;
		}else{
			key = e.which;
		}
		if((key>=65)&&(key<=90)||(key>=97)&&(key<=122)||key==8||key==9||key==32 || key==0 || key==45 || key==38){
			key=key;
			return true;
		}else{
			return false;
		}
	}





 // FUNCTION FOR ALLOW ALPHABETS AND &,(,),and , ONLY
function enterAlphanSpecial(e)
{
     var key=0;
	if(window.event)
	{
		key = e.keyCode;

	}
	else
	{
		key = e.which;

	}

     if((key>=65)&&(key<=90)||(key>=97)&&(key<=122)||key==8||key==9||key==32 || key==38||key==40||key==41||key==44 || key==0)
     {

        key=key;
        return true;

     }
     else
     {
       return false;

     }
}


  //FUNCTION FOR ALLOW ALPHABETS AND NUMERICS AND '('  AND ')' AND '%'
  function enterAlphanNumericalsWithspec(e){

	    var key=0;
		if(window.event){
			key = e.keyCode;
		}
		else{
			key = e.which;
		}

	if((key>=65)&&(key<=90)||(key>=48)&&(key<=57)||(key>=97)&&(key<=122)||key==8||key==9||key==32 || key==38||key==40||key==41||key==45||key==37 || key==0)
     {

        key=key;
        return true;

     }
     else
     {
       return false;

     }



  }//end of the enterAlphanNumericalsWithspec






// FUNCTION FOR ALLOW ALPHABETS  ONLY


// This function Checks whether string s is empty.

function isEmpty(s)
{   return ((s == null) || (s.length == 0))
}


function checkPin(objValue)
{
  var pinval=eval(objValue.value.length)
  if(pinval>0)
  {
     if(pinval<6)
      {
          alert("Pincode cannot be less than 6 characters");
          objValue.focus();
          return false;
      }
      else if(pinval>6)
      {
          alert("Pincode cannot be greater than 6 characters");
          objValue.focus();
          return false;
      }
			else if( ( pinval == 6 ) && ( objValue.value.charAt(0)=="0" ) )
			  {
				  alert("Pincode first number should not be 0");
				  objValue.focus();
				  return false;
			  }
			 else if( ( pinval > 0 ) && ( parseInt(objValue.value) == 0 ) )
			  {
				alert("Pincode number should not be 0");
				objValue.focus();
				return false;
			  }

    }
          else return true;
}

// Registration Number,Voucher Number,Inspection Number,Chalan Number validation... call this functin in onkeypress

function checkAlshNumeric(e)
{
var key=0;
		if(window.event || !e.which)
		{
			key = e.keyCode;
	 	}
     	else
         {
		key = e.which;
         }

     if((key>=65)&&(key<=90)||(key>=97)&&(key<=122)||(key>=47)&&(key<=57)||key==45||key==8||key==9||key==39 || key==0)
     {
        key=key;
        return true;
     }
     else
     {
       key=0;
       return false;
     }

}



// function to Allow numbers and dot only

function enterNumOnlyANDdot(e)
{
   var key=0;
	if(window.event || !e.which)
	{
		key = e.keyCode;
   	}
	else
	{
		key = e.which;
	}

     if((key>=48)&&(key<=57)||key==8||key==9||key==46 || key==0)
     {
        key=key;
        return true;
     }
     else
     {
       key=0;
       return false;
     }

}

// function to Allow numbers and hypen (minus sign) only

function enterNumOnlyANDhypen(e)
{
   var key=0;
	if(window.event || !e.which)
	{
		key = e.keyCode;
   	}
	else
	{
		key = e.which;
	}

     if((key>=48)&&(key<=57)||key==8||key==9||key==45 || key==0)
     {
        key=key;
        return true;
     }
     else
     {
       key=0;
       return false;
     }

}




function enterFloatNumOnly(e)
{
   var checkDot=0;
   for(var i=0;i<e.srcElement.value.length;i++){
      if(e.srcElement.value.charAt(i)=='.'){
      checkDot=1;
      break;
      }
   }

   var key=0;
   if(window.event || !e.which)
	{
		key = e.keyCode;
	}
	else
	{
		key = e.which;
	}

    if((key>=48)&&(key<=57)||key==8||key==9||key==46 || key==0)
    {
        if(key==46){
           if(checkDot==0){
             key=key;
             return true;
           }else{
             key=0;
             return false;
           }
        }else{
           key=key;
           return true;
        }
     }
     else
     {
       key=0;
       return false;
     }

}




//Called in onkeypress. Allows everything except Single Quote

function enterDetails(e)
{
  var key=0;
  if(window.event || !e.which)
  {
    key = e.keyCode;

  }
  else
  {
    key = e.which;

  }
  if(key!=39)
  {
    key=key;
    return true;
  }
  else
  {
    key=0;
    return false;
  }
}

//Called in onkeypress. Allows Numbers and Alphabets and with out space.

function enterAlphaNumericNoSpace(e)
{
var key=0;

		if(window.event || !e.which)
		{
				key = e.keyCode;

		}
	   else
			{
					key = e.which;

			}

     if((key>=65)&&(key<=90)||(key>=97)&&(key<=122)||(key>=48)&&(key<=57)||key==8||key==45||key==47||key==95||key==9 || key==0)
     {
        key=key;
        return true;
     }
     else
     {
       key=0;
       return false;
     }

}


// This function Checks length of Text Area.

function checkTextArea(name,size)
{
	src = eval("document.forms[0]." + name );

	if (src.value.length > size)
	{
		src.value = src.value.substring(0,size)
	     alert("Length of this text can not exceed "+size+" characters");
	}
 }


 function setFocusInFirstField()
{
	var ele = eval("document.forms[0].elements");
	var len = ele.length;

	for(var i=0; i<len; i++)
	{
    //alert(ele[i].readOnly);
		if(ele[i].type == "text" ||  ele[i].type == "textarea" || ele[i].type == "select-one")
		{
      if(ele[i].readOnly == true || ele[i].disabled == true)
        continue;
      ele[i].focus();
      break;
		}
	}
}

//This function checks for null values.
function checkNull(objValue,message)
{
          //alert(objValue.name);
          if(eval(objValue.value.length) == 0)
          {
              alert("Enter the "+message);
              objValue.focus();
              return false;
           }
              else return true;

}
    function openeditframes(){
      window.top.left.location.href='editleftframe.jsp'
      window.top.rbottom.location.href='VisitDetails.jsp';
     }

	function isEmail(emailStr,msg) {
		var str = emailStr;
		var emails = str.replace(/\s/g, "").split(",");
		var filter=/^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i
		testresults=true
		for(var i = 0; i<emails.length; i++) {
			if(emails[i] == "" || !filter.test(emails[i])) {
				testresults = false;
			}
		}
		
		if (!testresults) {
			alert(msg);
		}
		return (testresults)
}


/*Phone Validation*/

      function isPhone(phno)
			{
			var len=phno.length;
			var c=0;
			var hyp=phno.indexOf('-');
			if(phno==""){
			alert("Enter the phone number");
			return false;
			}
			for(i=0;i<len;i++){
			ch=phno.substring(i,i+1);
			if(ch=="-")
			 c=c+1;
			if(c==2){
			 alert("Enter the valid phone number");
			 return false;
			}
			if((ch<"0"||ch>"9")&&(ch!="-")) {
			alert("Enter the valid phone number ex:080-27845162");
			return false;
			}
			}

			if((len!=11)&&(phno.substring(0,1)!=9)&&(hyp==-1)){
			alert("Enter the valid phone number");
			return false;
			}
			if((len==12)&&(phno.substring(0,1)!=0)&&(hyp==-1)){
			alert("Enter the valid phone number");
			return false;
			}
			if((hyp==-1)&&(len!=12)&&(phno.substring(0,1)=="0")){
			alert("Enter the valid phone number");
			return false;
			}
			if(hyp!=-1){
			if((hyp<3 || hyp>5)||(phno.substring(0,1)!=0)||(len!="12")){
			alert("Enter the valid phone number");
			return false;
			}
			}
			return true;
			}
			/*Mobile Validation*/

        function isMobile(mobile,msg){
			    if(mobile.search(/^\d{10}$/)==-1){
			    	alert(msg);
			    	return false;
			    }
			    return true;
			}

			/*PAN Number Validation*/

    	 function isPANNumber(PAN,msg){
			if(PAN.search(/^[A-Za-z0-9]+$/)==-1){
				alert(msg);
				return false;
			}
			return true;
		}


/*Passport Number Validation*/

     function isPassportNumber(passportNumber,msg){
		if(passportNumber.search(/^[a-z0-9]{8,10}$/i)==-1){
	    	alert(msg);
	    	return false;
	    }
	    return true;
	}
// Date Validation

// Future date Validation

   function isFutureDate(id)
	{

   var date1, date2;
   var month1, month2;
   var year1, year2;
   var today = new Date();
   var months=new Array('JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC');
   var selectedDate = document.getElementById(id).value;
   date2 = selectedDate.substring (0, selectedDate.indexOf ("-"));
   month2 = selectedDate.substring (selectedDate.indexOf ("-")+1, selectedDate.lastIndexOf ("-"));
   year2 = selectedDate.substring (selectedDate.lastIndexOf ("-")+1, selectedDate.length);
   month1 = today.getMonth()+1;
   date1 = today.getDate();
   year1 = today.getFullYear();

   var index=0;
    for(var i=0;i<months.length;i++)
    {
       if(months[i]==month2)
       {
         index=i;
        }
    }
   month2=index+1;
   if (year1 > year2) return 1;
   else if (year1 < year2) return -1;
   else if (month1 > month2) return 1;
   else if (month1 < month2) return -1;
   else if (date1 > date2) return 1;
   else if (date1 < date2) return -1;
   else return 0;
}
function UnFormatTextAreaValue(vText)
{
	vRtnText= vText;
	while(vRtnText.indexOf("~") > -1)
	{
		vRtnText = vRtnText.replace("~","\n");
	}
	while(vRtnText.indexOf("^") > -1)
	{
		vRtnText = vRtnText.replace("^","\r");
	}
	return  vRtnText;
}

function FormatTextAreaValue(vText)
{
	vRtnText= vText;
	while(vRtnText.indexOf("\n") > -1)
	{
		vRtnText = vRtnText.replace("\n","~");
	}
	while(vRtnText.indexOf("\r") > -1)
	{
		vRtnText = vRtnText.replace("\r","^");
	}
	return vRtnText;
}

	var phoneNumberDelimiters = "()- "
	var validWorldPhoneChars = phoneNumberDelimiters + "+"
	// Declaring required variables
	var digits = "0123456789";
	// non-digit characters which are allowed in phone numbers
	var phoneNumberDelimiters = "()- ";
	// characters which are allowed in international phone numbers
	// (a leading + is OK)
	var validWorldPhoneChars = phoneNumberDelimiters + "+";
	// Minimum no of digits in an international phone no.
	var minDigitsInIPhoneNumber = 10;

	function isInteger(s)
	{ var i;
	for (i = 0; i < s.length; i++)
	{
	// Check that current character is number.
	var c = s.charAt(i);
	if (((c < "0") || (c > "9"))) return false;
	}
	// All characters are numbers.
	return true;
	}

	function stripCharsInBag(s, bag)
	{

	var i;
	var returnString = "";
	// Search through string's characters one by one.
	// If character is not in bag, append to returnString.
	for (i = 0; i < s.length; i++)
	{
	// Check that current character isn't whitespace.
	var c = s.charAt(i);
	if (bag.indexOf(c) == -1) returnString += c;
	}
	return returnString;
	}

	function validatePhone(obj){
		var strPhone = obj.value;
		s=stripCharsInBag(strPhone,validWorldPhoneChars);
		return (isInteger(s) && s.length >= minDigitsInIPhoneNumber);
	}


	// This function is used to set values from database.

function setValuesFromDatabase()
{
  var eleLen = document.forms[0].elements.length;
  var ele = eval("document.forms[0].elements");
  for(var i=0, j=0; i<eleLen; i++)
  {
    if(ele[i].type == "text" ||  ele[i].type == "textarea")
    {
      arrayList[j] = ele[i].value;
      j++;
    }
    else if(ele[i].type == "select-one")
    {
      arrayList[j] = ele[i].selectedIndex;
      j++;
    }
    else if(ele[i].type == "checkbox")
    {
      arrayList[j] = ele[i].checked;
      j++;
    }

  }
}

// This function is used to check change in status.

function checkChangeStatus()
{
  var eleLen = document.forms[0].elements.length;
  var ele = eval("document.forms[0].elements");
  var flag = 0;
  for(var i=0, j=0; i<eleLen; i++)
  {
    if(ele[i].type == "text" ||  ele[i].type == "textarea" )
    {
      //alert(ele[i].value +" === "+ arrayList[j]);
      if(ele[i].value != arrayList[j])
      {
        flag = 1;
        break;
      }
      else
		{
        j++;
		}
    }
	else if(ele[i].type == "select-one")
	  {
      //alert(arrayList[j] +"!="+ ele[i].selectedIndex);
      if(arrayList[j] != ele[i].selectedIndex)
      {
        flag = 1;
        break;
      }
      else
      {
          j++;
      }
	  }
    else if(ele[i].type == "checkbox")
    {
      if(arrayList[j] != ele[i].checked)
      {
        flag = 1;
        break;
      }
      else
      {
          j++;
      }
    }

  }
  //alert(flag);
if(flag == 0)
{
	alert("No value has been changed. Nothing to update");
	return false;
}
else
	return true;
}


/*Name Validation*/

	function isNameWithoutNum(name,fieldname,size){

	  if (name==""){
			alert("Enter the "+fieldname);
			return false;
		}
	  if(name.length>size) {
			alert(fieldname+"should be less than " +size+ "characters");
	        return false;
	  }

	  ch=name.substring(0,1);

	  if ((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") )
	  {
		 alert(fieldname+" should start with alphabet");
		 return false;
	  }
	  for(i=0;i<name.length;i++){
	   ch1=name.substring(i,i+1);
	   ch2=name.substring(i+1,i+2);
	   if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1<"(" || ch1 > ")") && (ch1 != "&") && (ch1 != "_") && (ch1 != ".")&& (ch1 != " ") && (ch1 != "-")) {
		   alert(fieldname+" is an invalid name");
		   return false;
		}
	   if( ch1==".") {
		  if (ch2==".") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
	   if( ch1=="-") {
		  if (ch2=="-") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="_") {
		  if (ch2=="_") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1==" ") {
		  if (ch2==" ") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="&") {
		  if (ch2=="&") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="(") {
		  if (ch2=="(") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1==")") {
		  if (ch2==")") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}

	  }
	 return true;

	}


	function isNameWithNum(name,fieldname,size){

	  if (name==""){
			alert("Enter the "+fieldname);
			return false;
		}
	  if(name.length>size) {
			alert(fieldname+"should be less than" +size+ "characters");
	        return false;
	  }

	  ch=name.substring(0,1);

	  if ((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") )
	  {
		 alert(fieldname+" should start with alphabet");
		 return false;
	  }
	  for(i=0;i<name.length;i++){
	   ch1=name.substring(i,i+1);
	   ch2=name.substring(i+1,i+2);
	   if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1<"(" || ch1 > ")") && (ch1 != "&") && (ch1 != "_") && (ch1 != ".") && (ch1 != " ") && (ch1 != "-")) {
		   alert(fieldname+" is an invalid name");
		   return false;
		}
	   if( ch1==".") {
		  if (ch2==".") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="-") {
		  if (ch2=="-") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="_") {
		  if (ch2=="_") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1==" ") {
		  if (ch2==" ") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="&") {
		  if (ch2=="&") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="(") {
		  if (ch2=="(") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1==")") {
		  if (ch2==")") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}

	  }
	 return true;

	}

	//UOM Name Validation: allow numericals as first character of a name

function isNameWithSlashAndNumerics(name,fieldname,size){
	if(name==""){
		alert("Enter the "+fieldname);
		return false;
	}
	if(name.length>size){
		alert(fieldname+"should be less than" +size+ "characters");
		return false;
	}
	ch=name.substring(0,1);
	if((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") && (ch <"0" || ch > "9"))
	{
		alert(fieldname+" should start with alphabet");
		return false;
	}
	for(i=0;i<name.length;i++){
		ch1=name.substring(i,i+1);
		ch2=name.substring(i+1,i+2);
		if( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1<"(" || ch1 > ")") && (ch1 != "&") && (ch1 != "_") && (ch1 != ".") && (ch1 != " ") && (ch1 != "-") && (ch1 != "/") ) {
			alert(fieldname+" is an invalid name");
			return false;
		}
		if( ch1=="."){
			if(ch2=="."){
				alert(fieldname+" is an invalid name");
				return false;
			}
		}
		if( ch1=="-"){
			if (ch2=="-"){
				alert(fieldname+" is an invalid name");
				return false;
			}
		}
		if(ch1=="_"){
			if(ch2=="_"){
				alert(fieldname+" is an invalid name");
				return false;
			}
		}
		if(ch1==" "){
			if(ch2==" "){
				alert(fieldname+" is an invalid name");
				return false;
			}
		}
		if(ch1=="&"){
			if(ch2=="&"){
				alert(fieldname+" is an invalid name");
				return false;
			}
		}
		if(ch1=="("){
			if(ch2=="("){
				alert(fieldname+" is an invalid name");
				return false;
			}
		}
		if( ch1==")"){
			if(ch2==")"){
				alert(fieldname+" is an invalid name");
				return false;
			}
		}
		if( ch1=="/"){
			if(ch2=="/"){
				alert(fieldname+" is an invalid name");
				return false;
			}
		}

	}
	return true;

}



	function isNameWithSlash(name,fieldname,size){

	  if (name==""){
			alert("Enter the "+fieldname);
			return false;
		}
	  if(name.length>size) {
			alert(fieldname+"should be less than" +size+ "characters");
	        return false;
	  }

	  ch=name.substring(0,1);

	  if ((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") )
	  {
		 alert(fieldname+" should start with alphabet");
		 return false;
	  }
	  for(i=0;i<name.length;i++){
	   ch1=name.substring(i,i+1);
	   ch2=name.substring(i+1,i+2);
	   if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1<"(" || ch1 > ")") && (ch1 != "&") && (ch1 != "_") && (ch1 != ".") && (ch1 != " ") && (ch1 != "-") && (ch1 != "/") ) {
		   alert(fieldname+" is an invalid name");
		   return false;
		}
	   if( ch1==".") {
		  if (ch2==".") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="-") {
		  if (ch2=="-") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="_") {
		  if (ch2=="_") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1==" ") {
		  if (ch2==" ") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="&") {
		  if (ch2=="&") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="(") {
		  if (ch2=="(") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1==")") {
		  if (ch2==")") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}
		if( ch1=="/") {
		  if (ch2=="/") {
			 alert(fieldname+" is an invalid name");
		     return false;
		  }
		}

	  }
	 return true;

	}

function isNameWithSpecial(name,fieldname,size){

  if (name==""){
		alert("Enter the "+fieldname+" field");
		return false;
	}
  if(name.length>size) {
		alert(fieldname+" should be less than " +size+ " characters");
        return false;
  }

  ch=name.substring(0,1);

  if ((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") )
  {
	 alert(fieldname+" should start with alphabet!");
	 return false;
  }

  for(i=0;i<name.length;i++){
   ch1=name.substring(i,i+1);
   ch2=name.substring(i+1,i+2);
	if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1 != ' ') && (ch1 != ".")&& (ch1 != "&"))  {
	   alert(fieldname+" is an invalid name");
	   return false;
	}
    if( ch1==".") {
	  if (ch2==".") {
		 alert(fieldname+" is an invalid name");
		 return false;
	  }
	}
   if( ch1==" ") {
	  if (ch2==" ") {
		 alert(fieldname+" is an invalid name");
		 return false;
	  }
	}
	if( ch1=="&") {
	  if (ch2=="&") {
		 alert(fieldname+" is an invalid name");
		 return false;
	  }
	}

  }
  return true;
}

function checkPhone(phone){
	if(!(phone.substring(0,1)=="9"||phone.substring(0,1)=="0")){
		alert("Phone Number should start with 9 or 0 followed by phone no");
		return false;
	}
	if(phone.substring(0,1)=="9"){
    	if((phone.search(/^\d{10}$/)==-1)){
	    	alert("Enter the valid 10 digit number");
	    	return false;
    	}
    }
    if(phone.substring(0,1)=="0"){
        if((phone.search(/^\d{11}$/)==-1)){
	    	alert("Enter the valid 11 digit number");
	    	return false;
     	}
     }
     return true;
}

function checkFax(phno){
	var len=phno.length;
	var c=0;
	var hyp=phno.indexOf('-');

	for(i=0;i<len;i++){
		ch=phno.substring(i,i+1);
		if(ch=="-")
			 c=c+1;
		if(c==2){
			 alert("Enter the valid fax number");
			 return false;
		}
		if((ch<"0"||ch>"9")&&(ch!="-")) {
			alert("Enter the valid fax number");
			return false;
		}
	}

	if((len!=11)&&(phno.substring(0,1)!=9)&&(hyp==-1)){
		alert("Enter the valid fax number");
		return false;
	}
	if((len==11)&&(phno.substring(0,1)!=0)&&(hyp==-1)){
		alert("Enter the valid fax number");
		return false;
	}
	if((hyp==-1)&&(len!=11)&&(phno.substring(0,1)=="0")){
		alert("Enter the valid fax number");
		return false;
	}
	if(hyp!=-1){
		if((hyp<3 || hyp>5)||(phno.substring(0,1)!=0)||(len!="12")){
			alert("Enter the valid fax number");
			return false;
		}
	}
	return true;
}

function websiteCheck(website){
	var dotCount=0;
	 var lastBeforeString="";
	 var lastString="";
	 var firstString="";
	 var filter;

      // var filter=/^(www\.)?[a-z0-9\-\.]{3,}\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i
      //var filter=/^(www\.)?[a-zA-Z0-9\-\.]{3,}\.[a-z]{3}\.[a-z]{2,3}$/
      //alert(website.substring(0,4))
      if(website.substring(0,4)=="www." || website.substring(0,4)=="WWW."){}
      else{
      	alert("Enter the valid website");
		return false;
      }
      var lastString;
      if(website.substring(0,4)=="www." || website.substring(0,4)=="WWW."){
      	website1=website.substring(4,website.length);
      	var filter;
 	  	if(website!=""){
	 	 	if(website1.substring(0,1)=="."){
	 	 		alert("Enter the valid website");
		 		return false;
	 	 	}
	 	 	for(i=0;i<website1.length;i++){
	 	 		ch1=website1.substring(i,i+1);
	 	 		if(ch1=="."){
	 	 			dotCount++;
	 	 		}
		 	 	if(dotCount==0){
		 	 		firstString=firstString+website1.substring(i,i+1);
		 	 	}
		 	 	if(dotCount==1){
		 	 		if(website1.substring(i,i+1)=="."){} else
		 	 			lastBeforeString=lastBeforeString+website1.substring(i,i+1);
		 	 	}
		 	 	if(dotCount==2){
		 	 		lastString=lastString+website1.substring(i,i+1);
		 	 	}
		 	 	if(dotCount>2){
		 	 		alert("Enter the valid website");
				    return false;
	   			}
	 		}
	 		for(i=0;i<website1.length;i++){
	   			ch1=website1.substring(i,i+1);
	   			ch2=website1.substring(i+1,i+2);
	   			if( ch1==".") {
		  			if(ch2=="."){
		  				//alert("continuous two dots 2")
			 			alert("Enter the valid website");
			 			return false;
		  			}
		  		}
	   		}
	   		filter=/^[a-zA-Z0-9\-]{3,20}$/
	   		if (!filter.test(firstString)){
		   		//alert("firstString length>20 ")
				alert("Enter the valid website");
				return false;
			}
	   		filter=/^[a-zA-Z]{2,3}$/
	   		if (!filter.test(lastBeforeString)){
		   		alert("Enter the valid website");
				return false;
			 }
	   		if(dotCount==2){
	   			if(lastString.substring(0,1)!=="."){
	   				alert("Enter the valid website");
			     	return false;
	   			}
	   			lastString1=lastString.substring(1,lastString.length)
	   			filter=/^[a-zA-Z]{2}$/
	   			if (!filter.test(lastString1)){
			   		alert("Enter the valid website");
					return false;
			 	}
   			}
		}
	}
    return true;
}
/*Bank A/C Number Validation*/

function isBankAccountNumber(number,msg){
	if(number.search(/^[A-Za-z0-9]+$/)==-1){
		alert(msg);
		return false;
	}
	return true;
}

/*Currency Validation*/

function isCurrency(currency,msg){
	if(currency.search(/^\d+(\.\d\d)?$/)==-1){
		alert(msg);
		return false;
	}
	return true;
}

/*Name Validation*/

function isName(name,fieldname,size)
{

  if (name==""){
		alert("Enter the "+fieldname);
		return false;
	}
  if(name.length>size) {
		alert(fieldname+"should be less than "+size+" characters");
        return false;
  }

  ch=name.substring(0,1);

  if ((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") )
  {
	 alert(fieldname+" should start with alphabet");
	 return false;
  }
  for(i=0;i<name.length;i++){
   ch1=name.substring(i,i+1);
   ch2=name.substring(i+1,i+2);
	if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1 != ' ') && (ch1 != "."))  {
	   alert(fieldname+" is an invalid name");
	   return false;
	}
    if( ch1==".") {
	  if (ch2==".") {
		 alert(fieldname+" is an invalid name");
	     return false;
	  }
	}

  }
 return true;

}

// name validation with slash allowing for relation master screens
function isNameandSlash(name,fieldname,size){
	if (name==""){
			alert("Enter the "+fieldname);
			return false;
	}
    if(name.length>size){
		alert(fieldname+"should be less than" +size+ "characters");
        return false;
    }

    ch=name.substring(0,1);

	if ((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") ){
		alert(fieldname+" should start with alphabet");
		return false;
	}

  for(i=0;i<name.length;i++){
    ch1=name.substring(i,i+1);
    ch2=name.substring(i+1,i+2);
	if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z")  && (ch1 !='/') ){
	   alert(fieldname+" is an invalid name");
	   return false;
	}
    if( ch1==".") {
	  if (ch2==".") {
		 alert(fieldname+" is an invalid name");
	     return false;
	  }
	}
  }
 return true;
}// end of the isNameandSlash


/*Basic Validation*/

function basicValidation(which){
	var pass=true;
	if (document.images) {
	for (i=0;i<which.length;i++) {
	var tempobj=which.elements[i];
	if (tempobj.name.substring(0,8)!="required") {
		if (((tempobj.type=="text"||tempobj.type=="textarea")&&tempobj.value=='')||(tempobj.type.toString().charAt(0)=="s"&&tempobj.selectedIndex==0)) {
			pass=false;
			break;
         }
      }
   }
   }
   if (!pass) {
   	 shortFieldName=tempobj.name.substring(tempobj.name.lastIndexOf("_")+1).toUpperCase();
     alert("Make sure the "+shortFieldName+" field was properly completed");
     return false;
   }
   else
     return true;
}
/* Age Validation */

function isAge(number,msg,size){
if(number==""){
alert(msg);
return false;
}
	if(number.search(/^\d{1,}$/)==-1){
		alert("Invalid age");
		return false;
	}

if(number>115){
alert("Age must not exceed 115");
return false;
}
	return true;
}

/* Baby Gender */

function isBaby(value,msg){
    if(!value[0].checked && !value[1].checked){
alert(msg);
return false;
}
return true;
}



/*Baby Weight Validation*/

function isBabyWeight(number,msg){
	if(number.search(/^\d{1,}$/)==-1){
		alert(msg);
		return false;
	}
	return true;
}


/*Patient ID Validation*/

function isPatID(name,fieldname,size){

  if (name==""){
		alert("Enter the "+fieldname);;
		return false;
	}
  if(name.length>size) {
		alert(fieldname+"should be less than" +size+ "characters");
        return false;
  }

  ch=name.substring(0,1);

  if (ch <"0" || ch > "9")
  {
	 alert(fieldname+" should start with number");
	 return false;
  }
  for(i=0;i<name.length;i++){
   ch1=name.substring(i,i+1);
   ch2=name.substring(i+1,i+2);
	if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1 != "-"))  {
	   alert(fieldname+" is an invalid name");
	   return false;
	}
    if( ch1=="-") {
	  if (ch2=="-") {
		 alert(fieldname+" is an invalid name");
	     return false;
	  }
	}

  }
 return true;
}

		/*Code Validation*/

		function isCode(name,fieldname,size){

		if (name==""){
				alert("Enter the "+fieldname);
				return false;
			}

		if(name.length>size) {
				alert(fieldname+" should be less than " +size+ " characters");
		        return false;
		  }

			ch=name.substring(0,1);

		  if ((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") )
		  {
			 alert(fieldname+" should start with alphabet");
			 return false;
		  }
		  for(i=0;i<name.length;i++){
		   ch1=name.substring(i,i+1);
		   ch2=name.substring(i+1,i+2);
			if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") ){
			   alert(fieldname+" is Invalid");
			   return false;
			}

		  }

		}
		/* Address Validation*/

		function isAddress(name,fieldname,size){

		if (name==""){
				alert("Enter the "+fieldname);
				return false;
			}

		if(name.length>size) {
				alert(fieldname+" should be less than"+" "+size+ " characters");
		        return false;
		  }
		}

		/* City Validation*/

		function isCity(name,fieldname,size){

		if (name==""){
				alert("Enter the "+fieldname);
				return false;
			}

		if(name.length>size) {
				alert(fieldname+" should be less than " +size+ " characters");
		        return false;
		  }

		for(i=0;i<name.length;i++){
		   ch1=name.substring(i,i+1);
		   ch2=name.substring(i+1,i+2);
			if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1 != ' '))  {
			   alert(fieldname+" is an invalid name");
			   return false;
			}

			if( ch1==" ") {
			  if (ch2==" ") {
				 alert(fieldname+" is an invalid name");
			     return false;
			  }
			}


		  }

		}

		/* Pincode Validation*/

		function isPincode(name,fieldname,size){

		if (name==""){
				alert("Enter the "+fieldname);
				return false;
			}

		if(name.length>size) {
				alert(fieldname+" should be less than " +size+ " characters");
		        return false;
		  }

		if(name.search(/^\d{1,}$/)==-1){
				alert("Enter pincode in numbers only");
				return false;
			}


		}

               /* State Validation*/

				function isState(name,fieldname,size){

				if (name==""){
						alert("Enter the "+fieldname);
						return false;
					}

				if(name.length>size) {
						alert(fieldname+" should be less than " +size+ " characters");
				        return false;
				  }

				for(i=0;i<name.length;i++){
				   ch1=name.substring(i,i+1);
				   ch2=name.substring(i+1,i+2);
					if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1 != ' '))  {
					   alert(fieldname+" is an invalid name");
					   return false;
					}
				      if( ch1==" ") {
					  if (ch2==" ") {
						 alert(fieldname+" is an invalid name");
					     return false;
					  }
					}

				  }

				}
      /* Country Validation*/

		function isCountry(name,fieldname,size){

		if (name==""){
				alert("Enter the "+fieldname);
				return false;
			}

		if(name.length>size) {
				alert(fieldname+" should be less than " +size+ " characters");
		        return false;
		  }

		for(i=0;i<name.length;i++){
		   ch1=name.substring(i,i+1);
		   ch2=name.substring(i+1,i+2);
			if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1 != ' '))  {
			   alert(fieldname+" is an invalid name");
			   return false;
			}
		      if( ch1==" ") {
			  if (ch2==" ") {
				 alert(fieldname+" is an invalid name");
			     return false;
			  }
			}

		  }

		}

		/*Fax Validation*/

			function isFax(phno)
			{
			var len=phno.length;
			var c=0;
			var hyp=phno.indexOf('-');
			if(phno==""){
			alert("Enter the fax number");
			return false;
			}
			for(i=0;i<len;i++){
			ch=phno.substring(i,i+1);
			if(ch=="-")
			 c=c+1;
			if(c==2){
			 alert("Enter the valid fax number");
			 return false;
			}
			if((ch<"0"||ch>"9")&&(ch!="-")) {
			alert("Enter the valid fax number");
			return false;
			}
			}

			if((len!=11)&&(phno.substring(0,1)!=9)&&(hyp==-1)){
			alert("Enter the valid fax number");
			return false;
			}
			if((len==11)&&(phno.substring(0,1)!=0)&&(hyp==-1)){
			alert("Enter the valid fax number");
			return false;
			}
			if((hyp==-1)&&(len!=11)&&(phno.substring(0,1)=="0")){
			alert("Enter the valid fax number");
			return false;
			}
			if(hyp!=-1){
			if((hyp<3 || hyp>5)||(phno.substring(0,1)!=0)||(len!="12")){
			alert("Enter the valid fax number");
			return false;
			}
			}
			return true;
			}

			/*Medicine ID Validation*/
function isMedID(name,fieldname,size){


  if(name.length>size) {
		alert(fieldname+"should be less than" +size+ "characters");
        return false;
  }
x=name.substring(0,1);

  if ((x<"A"||x>"Z") && (x<"a"||x>"z") )
  {
	 alert(fieldname+" should start with alphabet");
	 return false;
  }
return true;
}
/*composition*/
function isComp(name,fieldname,size)
{

if(name.length>size) {
		alert(fieldname+"should be less than" +size+ "characters");
        return false;
  }
x=name.substring(0,1);

  if ((x<"A"||x>"Z") && (x<"a"||x>"z") )
  {
	 alert(fieldname+" should start with alphabet");
	 return false;
  }
  return true;
}

/*strength*/

function isStrength(name,fieldname,size)
{

if(name.length>size) {
		alert(fieldname+"should be less than" +size+ "characters");
        return false;
  }
x=name.substring(0,1);

  if ((x<"A"||x>"Z") && (x<"a"||x>"z") )
  {
	 alert(fieldname+" only allowed alphabet");
	 return false;
  }
    return true;
}







         /* =================Duplicate function=========*/

		/*pincode validation*/

 function ispincode(code)
 {
	 if(code.length>10)
	 {
		 alert("Enter valid pincode number");
		 return false;
	 }
	 if(code=="")
	 {
		 alert("Enter the Pincode");
		 return false;
	 }
ch=code.substring(0,1);

  if (ch<"0"||ch>"9")
  {
	 alert("Pincode should start with number only");
	 return false;
  }
  for(i=0;i<code.length;i++){
   ch1=code.substring(i,i+1);
   ch2=code.substring(i+1,i+2);
	if (ch1 <"0" || ch1 > "9")  {
	   alert("Pincode is  invalid ");
	   return false;
	}
    if( ch1==".") {
	  if (ch2==".") {
		 alert("Pincode is invalid");
	     return false;
	  }
	}
    }
	return true;
 }


 /*Website Validation*/

function iswebsite(name,fieldname,size){

    if(name.length>size) {
		alert(fieldname+"should be less than" +size+ "characters");
        return false;
  }

 for(i=0;i<name.length;i++){
   ch1=name.substring(i,i+1);
 	if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1 != '-') && (ch1 != "_")&& (ch1 != "."))  {
		 alert(fieldname+" is Invalid");
	   return false;
	}
 }



for(i=0;i<name.length;i++){
   ch1=name.substring(i,i+1);
   ch2=name.substring(i+1,i+2);
 	if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") )
		{
		if ( (ch2<"A" || ch2 > "Z") && (ch2<"a" || ch2 > "z") && (ch2 <"0" || ch2 > "9") )
		{
		 alert(fieldname+" is Invalid");
	   return false;
	}
 }
}
return true;
}

  /*============duplicate function=======*/
 /*Age Validation*/

 function isAge(age,msg)
{
	//alert("age");
	var len=age.length;
    if(age=="")
	{
	alert( "Enter the"+msg)
		return false;
	}

	if(age.search(/^\d{1,}$/)==-1)
	{
		alert(msg+"Invalid");
		return false;
	}
	if(age>105 )
	{
		alert(msg+"should not be greater than 105" );
		return false;
	}
	return true;
}



 /*Org Etc Validation*/
/*pincode validation*/
 function ispincode1(code)
 {
	 if(code.length>10)
	 {
		 alert("Enter Org valid pincode number");
		 return false;
	 }

ch=code.substring(0,1);

  if (ch<"0"||ch>"9")
  {
	 alert("Pincode should start with number only");
	 return false;
  }
  for(i=0;i<code.length;i++){
   ch1=code.substring(i,i+1);
   ch2=code.substring(i+1,i+2);
	if (ch1 <"0" || ch1 > "9")  {
	   alert("Pincode is  invalid ");
	   return false;
	}
    if( ch1==".") {
	  if (ch2==".") {
		 alert("Pincode is invalid");
	     return false;
	  }
	}
    }
	return true;
 }
/*Phone Validation*/

function isPhone2(phno)
{
var len=phno.length;
var c=0;
var hyp=phno.indexOf('-');

for(i=0;i<len;i++){
ch=phno.substring(i,i+1);
if(ch=="-")
 c=c+1;
if(c==2){
 alert("Enter the valid phone number");
 return false;
}
if((ch<"0"||ch>"9")&&(ch!="-")) {
alert("Enter the valid phone Number");
return false;
}
}

if((len!=11)&&(phno.substring(0,1)!=9)&&(hyp==-1)){
alert("Enter the valid phone number");
return false;
}
if((len==11)&&(phno.substring(0,1)!=0)&&(hyp==-1)){
alert("Enter the valid phone number");
return false;
}
if((hyp==-1)&&(len!=11)&&(phno.substring(0,1)=="0")){
alert("Enter the valid phone number");
return false;
}
if(hyp!=-1){
if((hyp<3 || hyp>5)||(phno.substring(0,1)!=0)||(len!="12")){
alert("Enter the valid phone number");
return false;
}
}
return true;
}

/*fax validation*/

 function isfax1(faxno)
{
var len=faxno.length;
var c=0;
var hyp=faxno.indexOf('-');

for(i=0;i<len;i++){
ch=faxno.substring(i,i+1);
if(ch=="-")
 c=c+1;
if(c==2){
 alert("Enter the valid fax number");
 return false;
}
if((ch<"0"||ch>"9")&&(ch!="-")) {
alert("Enter the valid fax number");
return false;
}
}

if((len!=11)&&(faxno.substring(0,1)!=9)&&(hyp==-1)){
alert("Enter the valid fax number");
return false;
}
if((len==11)&&(faxno.substring(0,1)!=0)&&(hyp==-1)){
alert("Enter the valid fax number");
return false;
}
if((hyp==-1)&&(len!=11)&&(faxno.substring(0,1)=="0")){
alert("Enter the valid fax number");
return false;
}
if(hyp!=-1){
if((hyp<3 || hyp>5)||(faxno.substring(0,1)!=0)||(len!="12")){
alert("Enter the valid fax number");
return false;
}
}
return true;
}

/*E-mail Validation*/
function isEmail1(emailStr,msg) {
var str = emailStr;
var filter=/^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;

if (filter.test(str)){
return true;
}
else{
alert(msg);
return false;
}
return true;
}


/*select como Box */
	function isSelect(field,msg){
	var v=field.selectedIndex;
	if(v==0){
	alert(msg);
	return false;
	}
	return true;
	}


	/*Comparing Validations */
	 function isCompare(doc1,doc2,msg){
	 var t1=doc1;
	 var t2=doc2;
	 if(t2<=t1){
	 alert(msg);
	 return false;
	 }
	 return true;

	}

	/*isSelectedTextBox como Box */
	function isSelectedTextBox(doc,msg){
	var v=doc;
	if(v==0){
	alert(msg);
	return false;
	}
	return true;
	}


	/* duplicate Age Validation */

	function isAge(number,msg){
	if(number==""){
	alert("Enter age")
	return false;
	}
		if(number.search(/^\d{1,}$/)==-1){
			alert(msg);
			return false;
		}

	if(number>115){
	alert("Age must not exceed 115");
	return false;
	}
		return true;
	}

	/* Select Button */

	function isButton(value,msg){
	    if(!value[0].checked && !value[1].checked){
	alert(msg);
	return false;
	}
	return true;
	}

	/* Weight Validation*/

	function isWeight(number,msg){
	if(number==""){
	alert("Enter "+msg);
	return false;
	}
		if(number.search(/^((\d+(\.\d*)?)|((\d*\.)?\d+))$/)==-1){
			alert("Invalid"+msg);
			return false;
		}
		return true;
	}

	/*AWeight*/

	function isAweight(number,msg){
	if(number.search(/^\d{1,}$/)==-1){
	alert(msg);
	return false;
		}
	if(number>130){
	alert("Weight must not excced 130");
		return false;
	}
	return true;
	}


	/* Height Validation*/
	function isHeight(number,msg){
	if(number==""){
	alert("Enter"+msg);
	return false;
	}

		if(number.search(/^((\d+(\.\d*)?)|((\d*\.)?\d+))$/)==-1){
			alert("Invalid "+msg);
			return false;
		}
		var x = number.split(".");
		var inches = x[1];
		if(inches >= 12){
		alert("Insert height in proper format");
		return false;
		}
		return true;
	}

	/*varchar Validation*/

	function isvarchar(name,fieldname){

	  ch=name.substring(0,1);

	  if ((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") && (ch<"0" || ch>"9"))
	  {
		 alert(fieldname+" should start with alphabet and numeric ");
		 return false;
	  }
	  for(i=0;i<name.length;i++){
	   ch1=name.substring(i,i+1);
	   ch2=name.substring(i+1,i+2);
		if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1 != ' ') && (ch1 != "."))  {
		   alert(fieldname+" is an Invalid ");
		   return false;
		}
	  }
	 return true;
	}


	/*comments Validation*/

	function iscomment(name,fieldname,size){

	  if (name==""){
			alert("Enter the "+fieldname);
			return false;
		}
	  if(name.length>size) {
			alert(fieldname+"should be less than" +size+ "characters");
	        return false;
	  }

	  ch=name.substring(0,1);

	  if ((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") )
	  {
		 alert(fieldname+" should start with alphabet");
		 return false;
	  }
	  for(i=0;i<name.length;i++){
	   ch1=name.substring(i,i+1);
	   ch2=name.substring(i+1,i+2);
		if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1 != ' ')) {
		   alert(fieldname+" is an invalid name");
		   return false;
		}


	  }
	 return true;

	}

	function trimAll(sString)
	{

	while (sString.substring(0,1) == ' ')
	{
	sString = sString.substring(1, sString.length);

	}
	while (sString.substring(sString.length-1, sString.length) == ' ')
	{
	sString = sString.substring(0,sString.length-1);
	}

	return sString;
	}

	/*organisation validation from omyvalidation.js*/


	function ValidateOrganization()
{
var abbreviation=document.getElementById("abbrev").value;
var name=document.getElementById("orgname").value;
var Address=document.getElementById("address").value;
var city=document.getElementById("orgcity").value;
var state=document.getElementById("orgstate").value;
var country=document.getElementById("orgcountry").value;
var code=document.getElementById("orgcode").value;
var phno1=document.getElementById("ph1").value;
var phno2=document.getElementById("ph2").value;
var fax=document.getElementById("faxs").value;
var website=document.getElementById("web").value;
var contactperson=document.getElementById("cp").value;
var emailStr=document.getElementById("mail").value;
document.forms[0].time.value=FormatTextAreaValue(document.forms[0].time.value);

    // for valid url




   //For OrgName
   if(name=="")
   {
			alert("Enter the organization name");
			document.getElementById("orgname").focus();
			return false;
	} else {
	//alert("else");
	if(isName(name,"Name",30)==false)
	{
	document.getElementById("orgname").focus();
		return false;
	}
	}

	//For Abbreviation
    if(abbreviation!="")
    {
   if(isStrength(abbreviation,"Abbreviation",50)==false)
	{
	document.getElementById("abbrev").focus();
	return false;
	}
	}


    // For City
    if(city=="")
    {
			alert("Enter the city");
			document.getElementById("orgcity").focus();
			return false;
	} else {
    if(isName(city,"City",20)==false)
	{
	document.getElementById("orgcity").focus();
		return false;
	}
    }

	//For State
    if(state=="")
    {
			alert("Enter the state");
			document.getElementById("orgstate").focus();
			return false;
	} else {
    if(isName(state,"State",20)==false)
	{
		document.getElementById("orgstate").focus();
		return false;
	}
    }

    //For Country
    if(country=="")
    {
		alert("Specify the country ");
			document.getElementById("orgcountry").focus();
			return false;
	} else {
	if(isName(country,"Country",20)==false)
	{
	document.getElementById("orgcountry").focus();
		return false;
	}
	}

   //For pincode

   if(code=="")
   {
   	alert('Enter the pincode');
   	document.getElementById("orgcode").focus();

   	return false;
   }

   if(code!="")
   {

   if (ispincode1(code)==false)
   {
   document.getElementById("orgcode").focus();
   return false;
   }
   }




   //For Phone1
   if(phno1==""){
   alert("Enter the phone1 number");
   document.getElementById("ph1").focus();
   return false;
   }else{
   if(isPhone(phno1)==false)
	{
	document.getElementById("ph1").focus();
	return false;
	}
	}

   //For Phone2
    if(phno2!="")
    {
    if(isPhone2(phno2)==false)
	{
	document.getElementById("ph2").focus();
	return false;
	}
	}
	//For fax
	if(fax!="")
    {
	if(isfax1(fax)==false)
	{
	document.getElementById("faxs").focus();
	return false;
	}
	}
	//For WebSite

	if(website=="")
	{
		alert('Enter the web address');
		document.getElementById("web").focus();
		return false;

	}

	if(website!="")
    {
	if(iswebsite(website,"website address",30)==false)
	{
	document.getElementById("web").focus();
	return false;
	}
	}

	//For ContactPerson
	if(contactperson=="")
   {
			alert("Enter the contact person");
			document.getElementById("cp").focus();
			return false;
	} else {
	if(isName(contactperson,"contact person",30)==false)
	{		document.getElementById("cp").focus();
			return false;
	}
	}

	//For Email

	if(emailStr=="")
	{
		alert('Enter the email id');
		document.getElementById("mail").focus();
		return false;
	}


	if(emailStr!="")
    {
	if(isEmail1(emailStr,"Enter the valid mailID")==false)
	{
	document.getElementById("mail").focus();
	return false;
	}
	}

	//For Address
	if(isAddress(Address,"Address",100)==false)
	{
	document.getElementById("address").focus();
	return false;
	}
	function isAddress(name,fieldname,size){
	  if (name==""){
		alert("Enter the "+fieldname);
		return false;
	 }
  	if(name.length>size) {
		alert(fieldname+"should be less than" +size+ "characters");
        return false;
     }

return true;
}





function FormatTextAreaValue(vText)
{ //alert(vText);
	vRtnText= vText;
	while(vRtnText.indexOf("\n") > -1)
	{
		vRtnText = vRtnText.replace("\n","~");
	}
	while(vRtnText.indexOf("\r") > -1)
	{
		vRtnText = vRtnText.replace("\r","^");
	}
	//alert(vRtnText);
	return vRtnText;
}


	return true;
}


function ValidateService()
{
var serviceId=document.getElementById("service").value
var department=document.getElementById("department").value
var servicename=document.getElementById("name").value
var unitvalue=document.getElementById("units").value
var unitcharge=document.getElementById("unit").value

var conductingcharge=document.getElementById("conducting").value

if(document.inputForm.dept.options[document.inputForm.dept.selectedIndex].value==""){
	alert("Select the Department");
	document.getElementById("department").focus();
	return false;
}
if(servicename=="")
{
	alert("Please Enter The Service Name");
	return false;
}
if(isName(servicename,"Service Name",30)==false)
{
	document.getElementById("name").focus()
	return false;
}
if(document.inputForm.units.options[document.inputForm.units.selectedIndex].value==""){
	alert("Select the Unit");
	document.getElementById("units").focus();
	return false;
}
if(isNumber(unitcharge,"Unit Charge","Please Enter The Unit Charge")==false)
	{
	document.getElementById("unit").focus()
		return false;
	}
if(isNumber(conductingcharge,"Conducting Charge","Please Enter The Conducting charge")==false)
{
	document.getElementById("conducting").focus()
		return false;
}

if(confirm("Do you want to Save"))
	return true;
	else
	return false;

return true;
}

/*Duplicate Number*/

function isNumber(number,fieldname,msg){

if(number=="")
{
alert("Enter the " + fieldname);
return false;

}



	if(number.search(/^\d{1,}$/)==-1){
		alert(msg);
		return false;
	}

return true;
}

/* from docvalidate.js*/
function DocForm_Validator()
{
var abbreviation=document.getElementById("abbrev").value;
var name=document.getElementById("orgname").value;
var Address=document.getElementById("address").value;
var city=document.getElementById("orgcity").value;
var state=document.getElementById("orgstate").value;
var country=document.getElementById("orgcountry").value;
var code=document.getElementById("orgcode").value;
var phno1=document.getElementById("ph1").value;
var phno2=document.getElementById("ph2").value;
var fax=document.getElementById("faxs").value;
var website=document.getElementById("web").value;
var contactperson=document.getElementById("cp").value;
var emailStr=document.getElementById("mail").value;
document.forms[0].time.value=FormatTextAreaValue(document.forms[0].time.value);

if(isStrength(abbreviation,"Abbreviation",50)==false)
	{
	document.getElementById("abbrev").focus();
	return false;
	}

if(isName(name,"Name",30)==false)
	{
	document.getElementById("orgname").focus();
		return false;
	}
if(isName(city,"City",20)==false)
	{
	document.getElementById("orgcity").focus();
		return false;
	}

if(isName(state,"State",20)==false)
	{
	document.getElementById("orgstate").focus();
		return false;
	}

if(isName(country,"Country",20)==false)
	{
	document.getElementById("orgcountry").focus();
		return false;
	}

if (ispincode1(code)==false)
{
document.getElementById("orgcode").focus();
return false;
}

if(isPhone(phno1)==false)
	{
	document.getElementById("ph1").focus();
	return false;

	}
if(isPhone2(phno2)==false)
	{
	document.getElementById("ph2").focus();
	return false;

	}
if(isfax1(fax)==false)
	{
	document.getElementById("faxs").focus();
	return false;
	}
if(iswebsite(website,"website address",30)==false)
	{
	document.getElementById("web").focus();
	return false;
	}
if(isName(contactperson,"contact person",30)==false)
	{document.getElementById("cp").focus();
		return false;
	}
if(isEmail1(emailStr,"Enter valid MailID")==false)
	{
	document.getElementById("mail").focus();
	return false;
	}
if(isAddress(Address,"Address",100)==false)
	{
	document.getElementById("address").focus();
	return false;
	}

}
function isAddress(name,fieldname,size){

  if (name==""){
		alert("Enter the "+fieldname);
		return false;
	}
  if(name.length>size) {
		alert(fieldname+"should be less than" +size+ "characters");
        return false;
  }

return true;
}


/* this is from single field validate.js */

 function ValidateName()
{
	//alert(document.forms[0].name.value);
	 var uname=document.getElementById("name").name.value;

if (uname == "")
  {
    alert("Enter the name");
   document.forms[0].name.focus();
    return (false);
  }

 /*var len=document.getElementById("name").name.value.length;
 alert(len);  */

 if (document.forms[0].name.value.length > 30)
  {
    alert("Please enter at most 30 characters in the \"Doctor Name\" field.");

    document.forms[0].name.focus();

    return (false);
  }
  var checkOK = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789. ";
  var checkStr = document.forms[0].name.value;
  var allValid = true;
//  var validGroups = true;
  for (i = 0;  i < checkStr.length;  i++)
  {
    ch = checkStr.charAt(i);
    for (j = 0;  j < checkOK.length;  j++)
      if (ch == checkOK.charAt(j))
        break;
    if (j == checkOK.length)
    {
      allValid = false;
      break;
    }
  }

  //Please enter only letter,digit,.,space characters in the \"Name\" field.
  if (!allValid)
  {
    alert("not a valid Name");
    document.forms[0].name.focus();
    document.forms[0].name.select();
    return (false);
  }

 return true;
}

function ValidateAddress()
{

  var checkOK = "#ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789. ";
  var checkStr = document.forms[0].address.value;
  var allValid = true;

  ch=checkStr.substring(0,1);
  if(ch == "\n" || ch == " "  || ch == "\t")
  {
  	alert("not a valid address");
    document.forms[0].address.focus();
    return(false);
  }

  for (i = 0;  i < checkStr.length;  i++)
  {
    ch = checkStr.charAt(i);
    for (j = 0;  j < checkOK.length;  j++)
      if (ch == checkOK.charAt(j))
        break;
    if (j == checkOK.length)
    {
      allValid = false;
      break;
    }
  }
  //Please enter only letter,digit,.,space,@,# characters in the \"Address\" field
  if (!allValid)
  {
    alert("not a valid address");
    document.forms[0].address.focus();
    return(false);
  }

  return true;
 }

 function ValidateCity(field,msg)
 {
 if (field.value.length == "")
  {
    alert("Enter the the city");
    field.value.city.focus();
    return (false);
  }

   if (field.value.length > 30)
  {
    alert("Please enter at most 30 characters in the \"City\" field.");
    field.focus();
    return (false);
  }
   var checkOK = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789. ";
  var checkStr = field.value;
  var allValid = true;
//  var validGroups = true;
  for (i = 0;  i < checkStr.length;  i++)
  {
    ch = checkStr.charAt(i);
    for (j = 0;  j < checkOK.length;  j++)
      if (ch == checkOK.charAt(j))
        break;
    if (j == checkOK.length)
    {
      allValid = false;
      break;
    }
  }

  //Please enter only letter,digit,.,space characters in the \"Name\" field.
  if (!allValid)
  {
    alert("not a valid "+ msg);
   field.focus();
    return (false);
  }
  return true;
 }

 /* duplicate name*/
 function ValidatePhone()
 {
 	var len=document.forms[0].phoneNo.value.length;
	var c=0;
	var hyp=document.forms[0].phoneNo.value.indexOf('-');
/*	if(document.forms[0].phoneNo.value == "")
	{
		alert("Enter the Phone number");
		document.forms[0].phoneno.focus();
		return false;
	}  */

	for(i=0;i<len;i++)
	{
	ch=document.forms[0].phoneNo.value.substring(i,i+1);
	if(ch=="-")
 	c=c+1;
	if(c==2)
	{
		 alert("Enter the valid phone number(first word is -");
		 document.forms[0].phoneNo.focus();
		 return false;
	}
	if (((ch<"0"||ch>"9"))  &&(ch!="-"))
	{
		alert("Enter the valid phone number, not in 0-9 range ");
		document.forms[0].phoneNo.focus();
		return false;
	}


	if((len!=11)&&(document.forms[0].phoneNo.value.substring(0,1)!=9)&&(hyp==-1))
	{
		alert("Enter the valid phone number,len!=11,!=9,-1");
		document.forms[0].phoneNo.focus();
		document.forms[0].phoneNo.selected();
		return false;
	}
	if((len==11)&&(document.forms[0].phoneNo.value.substring(0,1)!=0)&&(hyp==-1))
	{
		alert("Enter the valid phone number,len==11,!=0,-1");
		document.forms[0].phoneNo.focus();
		return false;
	}
	if((hyp==-1)&&(len!=11)&&(document.forms[0].phoneNo.value.substring(0,1)=="0"))
	{
		alert("Enter the valid phone number,len!=11,hyp==-1,(0,1)==0 ");
		document.forms[0].phoneNo.focus();
		return false;
	}
	if(hyp!=-1)
	{
		if((hyp<3 || hyp>5)||(document.forms[0].phoneNo.value.substring(0,1)!=0)||(len!="12"))
		{
			alert("Enter the valid phone number");
			document.forms[0].phoneNo.focus();
			return false;
		}
	}

  }
	return true;
 }



/* Float validation */

function isFloat(number,msg){

	if(number.search(/^((\d+(\.\d*)?)|((\d*\.)?\d+))$/)==-1){
		alert(msg+ " is invalid");
		return false;
	}
	return true;
}


/*Baby Weight Validation*/

function isBabyWeight(number,msg){
	if(number.search(/^\d{1,}$/)==-1){
		alert(msg);
		return false;
	}
	return true;
}

  /* duplicate isempty */

function isEmpty(name,value)
{
	var str=name;
	if(name.value="")
	{
		alert(value+"should not empty");
		return false;
	}
	return true;
}


/*Name Validation*/

function isName(name,fieldname){

  if (name==""){
		alert("Enter the "+fieldname);
		return false;
	}

  if(name.length>60) {
		alert(fieldname+"should be less than" +60+ "characters");
        return false;
  }

  ch=name.substring(0,1);

  if ((ch<"A"||ch>"Z") && (ch<"a"||ch>"z") )
  {
	 alert(fieldname+" should start with alphabet");
	 return false;
  }
  for(i=0;i<name.length;i++){
   ch1=name.substring(i,i+1);
   ch2=name.substring(i+1,i+2);
	if ( (ch1<"A" || ch1 > "Z") && (ch1<"a" || ch1 > "z") && (ch1 <"0" || ch1 > "9") && (ch1 != ' ') && (ch1 != "."))  {
	   alert(fieldname+" is an invalid name");
	   return false;
	}
    if( ch1==".") {
	  if (ch2==".") {
		 alert(fieldname+" is an invalid name");
	     return false;
	  }
	}

  }
 return true;

}

/* phone number validation*/

function isPhoneNumber(phno)
			{
			var len=phno.length;
			var c=0;
			var hyp=phno.indexOf('-');
			if(phno==""){
			alert("Enter the phone number ex:08503-278451");
			return false;
			}
			for(i=0;i<len;i++){
			ch=phno.substring(i,i+1);
			if(ch=="-")
			 c=c+1;
			if(c==2){
			 alert("Enter the valid phone number ex:08503-278451");
			 return false;
			}
			if((ch<"0"||ch>"9")&&(ch!="-")) {
			alert("Enter the valid phone number ex:08503-278451");
			return false;
			}
			}
			if((len==12)&&(phno.substring(0,1)!=0)&&(hyp==-1)){
			alert("Enter the valid phone number ex:080-23020477,08503-200586");
			return false;
			}
			if((hyp==-1)&&(len!=15)){
			alert("Enter the valid phone number ex:080-23020477,08503-200586");
			return false;
			}
			if(hyp!=-1){
			if((hyp<2 || hyp>5)||(phno.substring(0,1)!=0)){
			alert("Enter the valid phone number ex:080-23020477,08503-200586");
			return false;
			}
			}
			return true;
			}

	/*  author : Sumathi Kotthuri */
	/* function to truncate the values after digit to desired numbers in a float value   */
	/* arg1: specifies the float value to be truncated  */
	/* arg1: strictly it should be a document value */
	/* function won't work uf arg1 is arg1 is any javascript variable  */
	/* arg2: specifies the number of digits that should present after . in float value  */

	function truncateToDesiredDigits(toBeTruncatedData,noOfDigitsWishingAfterDot){

		var splittedValue = toBeTruncatedData.split(".");
		var beforeDotValue = splittedValue[0];
		var afterDotValue = splittedValue[1];
		var countOfdigitsAfterDot = 0;
		var dotCrossed = false;
		var truncatedDataAfterDot = 0;
		var completeTruncatedResult = 0;

		for(k=0; k<toBeTruncatedData.length;k++){
			if(dotCrossed==true){
				countOfdigitsAfterDot++;
			}
			if(toBeTruncatedData.charAt(k)=="."){
				dotCrossed = true;
				truncatedDataAfterDot=toBeTruncatedData.substring(k,parseInt(k,10)+(parseInt(noOfDigitsWishingAfterDot,10)+1));
			}
		}

		if(parseInt(countOfdigitsAfterDot,10)>parseInt(noOfDigitsWishingAfterDot,10)){
			completeTruncatedResult = beforeDotValue+truncatedDataAfterDot;
		}else{
			completeTruncatedResult = (parseFloat(toBeTruncatedData,10)).toFixed(noOfDigitsWishingAfterDot);
		}
		return completeTruncatedResult;
	}


 function openurl(){

   window.open("http://www.instahealthsolutions.com");

 }

 function isPhoneNumberWithSpecialCharacter(field){
	    with(field){
		var numbers =/^[0-9(),\-\u0020]+$/;
		if(field.match(numbers)){
			return true;
		}
		else{
			return false;
		}
	}
}

/* This function shows/hides the tooltip for Combo Box */

function checkEvent(e) {
 if (!e) var e = window.event;
 if (e.target) targ = e.target;
 else if (e.srcElement) targ = e.srcElement;
 showHideToolTip(targ, e, e.type)
}

function showHideToolTip (theDropDown, e, eType)
{
 var toolTipObj = new Object();
 toolTipObj = document.getElementById("tooltip");
 toolTipObj.innerHTML = theDropDown.options[theDropDown.selectedIndex].text;
 if(eType == "mouseout"){
  toolTipObj.style.display = "none";
   } else
 {
  toolTipObj.style.display = "inline";
  toolTipObj.style.top = e.y + 15;
  toolTipObj.style.left = e.x + 10;
 }
}

/* This function shows/hides the tooltip for Text Field */

function checkEvent1(e) {
 if (!e) var e = window.event;
 if (e.target) targ = e.target;
 else if (e.srcElement) targ = e.srcElement;
 showHideToolTip1(targ, e, e.type)
}

function showHideToolTip1(textBox, e, eType)
{
 var toolTipObj = new Object();
 toolTipObj = document.getElementById("tooltip");
 toolTipObj.innerHTML = textBox.value;
 if(eType == "mouseout"){
  toolTipObj.style.display = "none";
   } else
 {
  toolTipObj.style.display = "inline";
  toolTipObj.style.top = e.y + (-20);
  toolTipObj.style.left = e.x + 0;
 }
}


//fuction for Report Generator Screen in Diagnostic Module

function reportName(e){
	var key=0;
	if(window.event || !e.which){
				key = e.keyCode;

	}else{
				key = e.which;
	 }

     if((key>=65)&&(key<=90)||(key>=97)&&(key<=122)||(key>=48)&&(key<=57)||key==8||key==45||key==47||key==9||key==32||key==40||key==41||key==46||key==38 || key==0)
     {
        key=key;
        return true;
     }else
     {
       key=0;
       return false;
     }

}//end of the reportName

function onfocus(fieldName){
//alert("hello");
    if (document.getElementById(fieldName)) {
        document.getElementById(fieldName).focus();
    }
}


/*USED TO SEARCH ITEMNAMES START WITH TEXT.searchString:String which u enter.selectBoxId:select box id to which u add names. codeIndex:attribute index of item code.nameIndex:attribute index of item name. */

	function searchNames(searchString,selectBoxId,codeIndex,nameIndex){
 		var obj=document.getElementById(selectBoxId);
 		document.getElementById(selectBoxId).length=0;
 		var len=searchString.length;
 		if(x==null){
 			alert("There are no items");
 			return false;
 		}
 		var xmlLen=x.childNodes[0].childNodes.length;
 		var k = 0;
 		for(var i=0;i<xmlLen;i++){

 			var itemName=x.childNodes[0].childNodes[i].attributes(nameIndex).nodeValue;
 			if(searchString==itemName.substring(0,len)){
 				document.getElementById(selectBoxId).length=k+1;
 				document.getElementById(selectBoxId).options[k].value=x.childNodes[0].childNodes[i].attributes(codeIndex).nodeValue;
				document.getElementById(selectBoxId).options[k].text=x.childNodes[0].childNodes[i].attributes(nameIndex).nodeValue;
				k++;
 			}

 		}
 	}

	function searchNames1(searchString,selectBoxId,codeIndex,nameIndex){
 		var obj=document.getElementById(selectBoxId);
 		document.getElementById(selectBoxId).length=0;
 		var len=searchString.length;
 		if(x==null){
 			alert("There are no items");
 			return false;
 		}
 		var xmlLen=x.childNodes.length;
 		var k = 0;
 		for(var i=0;i<xmlLen;i++){

 			var itemName=x.childNodes[i].attributes(nameIndex).nodeValue;
 			if(searchString==itemName.substring(0,len)){
 				document.getElementById(selectBoxId).length=k+1;
 				document.getElementById(selectBoxId).options[k].value=x.childNodes[i].attributes(codeIndex).nodeValue;
				document.getElementById(selectBoxId).options[k].text=x.childNodes[i].attributes(nameIndex).nodeValue;
				k++;
 			}

 		}
 	}

 /* For expiry date validation that expiry date should be alteast one month ahead of current date  */

 	function isFutureDateOneMonthAhead(id){
	   var date1, date2;
	   var month1, month2;
	   var year1, year2;
	   var today = new Date();
	   var months=new Array('01','02','03','04','05','06','07','08','09','10','11','12');
	   var selectedDate = document.getElementById(id).value;
	   date2 = selectedDate.substring (0, selectedDate.indexOf ("-"));
	   month2 = selectedDate.substring (selectedDate.indexOf ("-")+1, selectedDate.lastIndexOf ("-"));
	   year2 = selectedDate.substring (selectedDate.lastIndexOf ("-")+1, selectedDate.length);
	   month1 = today.getMonth()+2;
	   date1 = today.getDate();
	   year1 = today.getFullYear();
	   var index=0;
	   for(var i=0;i<months.length;i++){
	   		if(months[i]==month2){
	        	index=i;
	        }
	   }
	   month2=index+1;
	   if (year1 > year2) return 1;
	   else if (year1 < year2) return -1;
	   else if (month1 > month2) return 1;
	   else if (month1 < month2) return -1;
	   else if (date1 > date2) return 1;
	   else if (date1 < date2) return -1;
	   else return 0;
	}


 /* This code will  identify user on which browser he  is working */
   function getBrowser(){
  	var ua=navigator.userAgent.toLowerCase();
	var browserName = "";
	var ua = navigator.userAgent.toLowerCase();
	if ( ua.indexOf( "opera" ) != -1 ) {
	browserName = "opera";
	} else if ( ua.indexOf( "msie" ) != -1 ) {
	browserName = "msie";
	} else if ( ua.indexOf( "safari" ) != -1 ) {
	browserName = "safari";
	} else if ( ua.indexOf( "mozilla" ) != -1 ) {
	if ( ua.indexOf( "firefox" ) != -1 ) {
	browserName = "firefox";
	} else {
	browserName = "mozilla";
	}
	}
 	return browserName;
};

/* To clear content in the grid */
function clearGrid(obj){

		obj.clearCellModel();
		obj.clearRowModel();
		obj.clearScrollModel();
		obj.clearSelectionModel();
		obj.clearSortModel();
		obj.refresh();
}

	/* check whether valid Number or not, which contains(0123456789., ) */
	function numberCheck(numObj){
		var checkok="0123456789.,- ";
		var checkStr = numObj.value;
	    var allValid = true;
	    for (i = 0;  i < checkStr.length;  i++)  {
		    ch = checkStr.charAt(i);
		    for (j = 0;  j < checkok.length;  j++)
		      if (ch == checkok.charAt(j)) break;
		      if (j == checkok.length) {
		      	allValid = false;
		      	break;
		      }
	  	}
	  	if (!allValid) {
	    	alert("Invalid Input");
	    	return false;
	  	}
	  	return true;
	}

/**
 *  If fractional part is non-zero then display it, optionally suppressing the
 *  paise part, if it is .00 or .000. Pass the second argument as false if you don't want
 *  this behaviour (that is, you always want to display the paise part).
 *  Also uses the global var decDigits to determine how many digits to show after decimal
 *
 *  Note that this returns a string, and is not suitable for calculations. For
 *  calculations, please use getPaise and getAmount.
 *
 *  Input is an object (HTML input element)
 */
function formatAmountObj(obj, suppressZeroDecimal) {

	if (suppressZeroDecimal == null) {
		suppressZeroDecimal = false;
	}

	if (null != obj) {
		if ( "" != obj.value ) {
			var objValue = parseFloat(obj.value).toFixed(decDigits);
			var splittedValue = objValue.split(".");
			var beforeDotValue = splittedValue[0];
			var afterDotValue = splittedValue[1];

			if ( (afterDotValue == 0) && (suppressZeroDecimal) ) {
				obj.value = beforeDotValue;
				return beforeDotValue;
			} else {
				obj.value = objValue;
				return objValue;
			}
		} else {
			if (suppressZeroDecimal) {
				obj.value = '0';
				return '0';
			} else {
				if (decDigits == 2){
					obj.value = '0.00';
					return '0.00';
				} else if (decDigits == 3){
					obj.value = '0.000';
					return '0.000';
				}
			}
		}
	}
}

/**
 * Same as above, but deals with the value instead of the object.
 * The objValue is expected to be text.
 */
function formatAmountValue(objValue, suppressZeroDecimal) {

	if (suppressZeroDecimal == null) {
		suppressZeroDecimal = false;
	}

	if ( (null != objValue) && ("" != objValue) && (objValue != 0) ) {
		var objValue = parseFloat(objValue).toFixed(decDigits);
	 	var splittedValue = objValue.split(".");
		var beforeDotValue = splittedValue[0];
		var afterDotValue = splittedValue[1];

		if ( (afterDotValue == 0) && (suppressZeroDecimal) )
			return beforeDotValue;
		else
			return objValue;
	} else if (null != objValue && objValue == 0) {
		if (suppressZeroDecimal) {
			return '0';
		} else {
			if (decDigits == 2)
				return '0.00';
			else if(decDigits == 3)
				return '0.000';
		}
	}
}

// To use this function "separator_type" & "currency_format" from generic preferences needs to be set in every jsp

function applySeparator(displayText) {
	var commaSeparator;
	var beforeDecimalValue;
	var afterDecimalValue;
	var isNegative = "";

	if(separator_type == "comma")
		commaSeparator = true;
	else if(separator_type == "dot")
		commaSeparator = false;
	else
		return displayText;

	displayText = displayText.toString();
	var len = displayText.length;
	if(displayText[0] == '-') {
		isNegative = '-';
		displayText = displayText.substr(1, len-1);
	}
	if ( (null != displayText) && ("" != displayText)) {
		len = displayText.length;
        if(commaSeparator) {
        	if(currency_format == "Millions")
        		beforeDecimalValue = displayText.substr(0, len-decDigits-1).replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,");
        	else {
        		beforeDecimalValue = displayText.substr(0, len-decDigits-2).replace(/(\d)(?=(\d{2})+(?!\d))/g, "$1,");
        		beforeDecimalValue = beforeDecimalValue + displayText.substr(len-decDigits-2, 1);
        	}
        }
        else {
        	if(currency_format == "Millions")
        		beforeDecimalValue = displayText.substr(0, len-decDigits-1).replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1.");
        	else {
        		beforeDecimalValue = displayText.substr(0, len-decDigits-2).replace(/(\d)(?=(\d{2})+(?!\d))/g, "$1.");
        		beforeDecimalValue = beforeDecimalValue + displayText.substr(len-decDigits-2, 1);
        	}
        }
        afterDecimalValue = displayText.substr(len-decDigits, decDigits);
        if(commaSeparator)
    		return isNegative + beforeDecimalValue + '.' + afterDecimalValue;
    	else
    		return isNegative + beforeDecimalValue + ',' + afterDecimalValue;
	}
	else
		return displayText;
}

//displayText for this function should always be an amount with decimals.
function removeSeparator(displayText) {

	var len = displayText.length;
	var beforeDecimalValue = displayText.substr(0, len-decDigits-1);
	var afterDecimalValue = displayText.substr(len-decDigits, decDigits);
	beforeDecimalValue = beforeDecimalValue.replace(/,/g, "");
	beforeDecimalValue = beforeDecimalValue.replace(/\./g, "");
	return beforeDecimalValue + '.' + afterDecimalValue;

}


function formatAmountPaise(paiseValue, suppressZeroDecimal) {
	if (suppressZeroDecimal == null) {
		suppressZeroDecimal = false;
	}

	paiseValue = Math.round(paiseValue);
	var prefix = "";
	if (paiseValue < 0) {
		prefix = "-";
		paiseValue = -(paiseValue);
	}

	paiseValue = paiseValue.toString();

	if ( (null != paiseValue) && ("" != paiseValue) && (paiseValue != 0) ) {
		var len = paiseValue.length;
		if (len > decDigits) {
			var beforeDotValue = paiseValue.substr(0, len-decDigits);
			var afterDotValue = paiseValue.substr(len-decDigits, decDigits);

			if ((afterDotValue == 0) && (suppressZeroDecimal))
				return prefix + beforeDotValue;
			else
				return prefix + beforeDotValue + '.' + afterDotValue;
		} else {
			var leading = '0.';
			for (var i=len; i<decDigits; i++) {
				leading += '0';
			}
			return prefix + leading + paiseValue;
		}

	} else if (null != paiseValue && paiseValue == 0) {
		if (suppressZeroDecimal) {
			return '0';
		} else {
			if (decDigits == 2)
				return '0.00';
			else if (decDigits == 3)
				return '0.000';
		}
	}
}

/*
 * Return the round-off amount in paise. Eg, 1025 will return -25 and 1075 will return 25.
 */
function getRoundOffPaise(paiseValue) {
	var divisor = (decDigits == 2) ? 100 : 1000
	var change = paiseValue % divisor;		// 25 or 75

	if (change >= divisor/2)
		return divisor - change;			// 100 - 75
	else
		return 0 - change;					// 0 -25
}

/*
 * Get an amount given an input value. This returns a number by rounding off to the
 * nearest paise. Calculations using the return value can be erroneous since this
 * returns floating point numbers. Especially, == comparison is really problematic
 * as 10.20 != 10.199999999
 */
function getAmount(value) {
	if (decDigits == 2){
		return getPaise(value)/100;
	} else if (decDigits == 3){
		return getPaise(value)/1000;
	}
}

function getAmountDecimal(value, numDigits) {
	var returnAmt = 0;
	if (numDigits == 2){
		returnAmt = getPaise(value, numDigits)/100;
	} else if (numDigits == 3){
		returnAmt = getPaise(value, numDigits)/1000;
	}
	return returnAmt;
}


/*
 * Get the paise (amount * 100) or (amount * 1000) given a value in text and whether
 * preference is to show 2 or 3 decimal places. This is the safest bet for
 * all calculations. The result needs to be displayed using formatAmountPaise(value).
 */
function getPaise(value) {
	var fValue = parseFloat(value);
	if (!isNaN(fValue)) {
		if (decDigits == 2){
			return Math.round(fValue*100);
		} else if (decDigits == 3){
			return Math.round(fValue*1000);
		}
	} else {
		return 0;
	}
}
/* new getPaise function, accepts num of decimal places*/

function getPaiseDecimal(value,numDigits) {
	var fValue = parseFloat(value);
	var returnVal = 0;
	if (!isNaN(fValue)) {
		if (numDigits == 2){
			returnVal = Math.round(fValue*100);
		} else if (numDigits == 3){
			returnVal = Math.round(fValue * 1000);
		}
		return returnVal;
	} else {
		return returnVal;
	}
}

function getElementIdPaise(elId) {
	var el = document.getElementById(elId);
	if (el && el.value && el.value != "") {
		return getPaise(el.value);
	}
	return 0;
}

function getElementIdAmount(elId) {
	var el = document.getElementById(elId);
	if (el && el.value && el.value != "") {
		return getAmount(el.value);
	}
	return 0;
}

function getElementPaise(el) {
	if (el && el.value && el.value != "") {
		return getPaise(el.value);
	}
	return 0;
}

function setElementPaise(el, paise, suppressZeroDecimal) {
	if (!paise)
		paise = 0;
	if (el) {
		el.value = formatAmountPaise(paise, suppressZeroDecimal);
	}
}

function setElementAmount(el, amt, suppressZeroDecimal) {
	if (!amt)
		amt = 0;
	if (el) {
		el.value = formatAmountValue(amt, suppressZeroDecimal);
	}
}

/* Below method accepts a value in paise and returns rupee amt*/
function getPaiseReverse(value){
	var returnAmt = 0;
	if (decDigits == 2){
		returnAmt = parseFloat(value) / 100;
	} else if (decDigits == 3){
		returnAmt = parseFloat(value) / 1000;
	}
	return returnAmt;
}


function getElementAmount(el) {
	if (el && el.value && el.value != "") {
		return getAmount(el.value);
	}
	return 0;
}

function getRowAmount(row, name) {
    return getElementAmount(getElementByName(row,name));
}

function setRowAmount(row, name, amount) {
	setElementAmount(getElementByName(row, name), amount);
}

function getRowPaise(row, name) {
    var el = getElementByName(row, name)
	return el ? getElementPaise(el) : 0;
}

function setRowPaise(row, name, paise) {
	setElementPaise(getElementByName(row, name), paise);
}

function breakContent(vText){
	vRtnText= vText;
	while(vRtnText.indexOf("~") > -1){
		vRtnText = vRtnText.replace("~","<br/>");
	}
	while(vRtnText.indexOf("^") > -1){
		vRtnText = vRtnText.replace("^","\r");
	}
	return vRtnText;
}

/*
 * Get the character based on an event. Note that a character is a true character,
 * and not a key (eg, arrow keys are not characters).
 *
 * In Mozilla, the keypress event captures arrow keys in addition to character keys.
 * If true character, e.charCode is set. Else, keyCode is set. Note that the left arrow
 * gives a keyCode of 37, and the % symbol gives a charCode of 37. Cannot normalize the
 * two to return a single "character". Since this function is supposed to return a char,
 * we return 0 for all control keys and the actual ASCII value for all characters.
 *
 * In IE, only typeable characters and Enter are captured by keypress. Only keyCode is
 * set to the appropriate value.
 *
 */
function getEventChar(e) {
	e = (e) ? e : event;
	var charCode;
	//debug("charcode: " + e.charCode);
	//debug("keycode: " + e.keyCode);
	if (e.charCode != null) {
		// Mozilla: charCode is 0 for control Keys, or the real character.
		// Returning 0 for control Keys is OK, we don't care.
		if (e.altKey || e.ctrlKey)
			return 0;
		return e.charCode;
	} else if (e.which != null) {
		// Opera: which is 0 for control Keys, or the real character.
		// Returning 0 for control Keys is OK, we don't care.
		if (e.altKey || e.ctrlKey)
			return 0;
		return e.which;
	} else {
		// IE does not support charCode or which. Also, keypress event does not get
		// triggered if it is a control character other than Enter key.
		return e.keyCode;
	}

	/*
	 * In any case, the whole thing is pointless, because, Mozilla allows to
	 * Paste via right-click -> paste, pretty much anything into a text box.
	 * We really need to validate stuff onchange, or even better, onSubmit.
	 * The following functions should be used only for user convenience, and
	 * definitely NOT for reliable validation.
	 */
}

function isCharNumber(c) {
	return ( (c >=48) && (c <= 57) );
}

function isCharControl(c) {
	return (c<32);
}

function isCharAlpha(c) {
	if (enlanguage != 'en') {
		return true;
	}
	// lower case
	if (c>=65 && c<=90) return true;
	// upper case
	if (c>=97 && c<=122) return true;
	return false;
}

var charPlus = 43;
var charComma = 44;
var charHyphen = 45;
var charDot = 46;
var charSpace = 32;
var charPlus = 43;
var charPercent = 37;
var charApostrophe = 39;
var charunderScore = 95;

/*
 * Validators are defined below
 */

// Allows Numbers, Comma(key==44) and Hyphen.(key==45)
function enterPhone(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharNumber(c) || (charComma == c) || (charHyphen == c) || (charPlus == c) );
}

//function for Allows Numbers,Comma(key==44) and Hiphon.(key==45) an dot

function enterPhoneANDdot(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharNumber(c) ||
			(charComma == c) || (charHyphen == c) ||
			(charDot ==c) );

}

// Number, dot, apostrophe, percent
function enterNumOnly(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharNumber(c) ||
			(charDot ==c) || (charApostrophe == c) || (charPercent == c) );
}

// alpha and space
function enterAlpha(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharAlpha(c) ||
			(charSpace == c) );
}

// alpha and space and dot
function enterAlphaANDdot(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharAlpha(c) ||
			(charHyphen == c) || (charSpace == c) || (charDot == c) || (charApostrophe == c) );
}

// ALPHABETS, NUMERICS &, and ',' and 'space' and '+' and '.'and '-'
function enterAlphanNumericals(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharAlpha(c) || isCharNumber(c) ||
			(charComma == c) || (charSpace == c) || (charDot == c) || (charHyphen == c) || (charPlus == c) || (charApostrophe == c)
			|| (charunderScore == c));
}

function enterAlphaOnly(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharAlpha(c) );
}

// only digits 0-9.
function enterNumOnlyzeroToNine(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharNumber(c) );
}
// digits 0-9 and comma
function enterDigitAndComma(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharNumber(c) || charComma == c);
}
function enterNumAndDot(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharNumber(c) || (charDot == c));
}
function enterNumAndDotAndMinus(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharNumber(c) || (charDot == c) || (charHyphen == c));
}

// ALPHABETS, NUMERICS &, and '_' and '.'and '-'
function enterAlphanNumerical(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharAlpha(c) || isCharNumber(c) ||
			 (charDot == c) || (charHyphen == c)  || (charApostrophe == c) || (charunderScore == c));
}


// ALPHABETS, NUMERICS &, and '_' and '.'and '-'
function enterAlphaNumericals(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharAlpha(c) || isCharNumber(c) ||
			 (charDot == c) || (charHyphen == c)  || (charunderScore == c) || (charPlus==c) || (charSpace==c));
}

// Allows Numbers and Alphabets and space.

function enterAlphaNumeric(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharAlpha(c) || isCharNumber(c) ||
			(charSpace == c));
}

function replaceXmlContent (a){
  if (a==null)
     return a;
  while(a.indexOf("&amp;") > -1)
  {
	a = a.replace("&amp;","&");
  }
  while(a.indexOf("&lt;") > -1)
  {
	a = a.replace("&lt;","<");
  }
  while(a.indexOf("&gt;") > -1)
  {
	a = a.replace("&gt;",">");
  }
  while(a.indexOf("&apos;") > -1)
  {
	a = a.replace("&apos;","'");
  }
  while(a.indexOf("&quot;") > -1)
  {
	a = a.replace("&quot;","\\");
  }
return a;
}


function validateNum(obj) {
	var objValue = obj.value;
		var fractionExists = false;

		var numericVal = objValue;
		for ( var i = 0; i < objValue.length; i++ ) {
			if ( objValue.charAt(i) == "." ) {
				var splittedValue = objValue.split(".");
				numericVal = splittedValue[0];
				var fractionalVal = splittedValue[1];
				fractionExists = true;
				break;
			}
		}

		if (fractionExists) {
			if ( fractionalVal.length > 2 ) {
				obj.value =  parseFloat(obj.value).toFixed(2);
			}
		}
		if ( numericVal.length > 10 ) {
			alert("Numeric field should not exceed 10 digits");
			obj.focus();
			return false;
		}
		return true;
}

/*
 *  Get an ExpiryPeriod given an input value. This returns a number depends on expiry date.
 *  expPeriod : eg :- May-2009
 *  it returns 0 when a medicine is already expired,
 *             1 when a medicine going to expire in a month
 * 			   2 in an else case
 *
 */
	function isExpired(expPeriod){
	   var curMonth, expMonth;
	   var curYear, expYear;
	   var today = new Date();
	   var months=new Array('JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC');

	   expMonth = expPeriod.substring (0, expPeriod.lastIndexOf ("-"));
	   expYear = expPeriod.substring (expPeriod.indexOf ("-")+1, expPeriod.length);

	   curMonth = today.getMonth()+1;
	   curYear = today.getFullYear();

	   var index=0;
	   for(var i=0;i<months.length;i++){
	   		if(months[i]==expMonth){
	        	index=i;
	        }
	   }
	   expMonth=index+1;

	   if (expYear < curYear) {
	   	 	return 0;
	   }else if (expYear == curYear) {
	   		if (expMonth <= curMonth) return 0;
	   		else if ( (curMonth+1) == expMonth ) {return 1;}
	   } else return 2;
	}

function validateQtyField (e,qtyDecimal) {
	e = (e) ? e : event;
	return qtyDecimal == 'Y' ? enterNumAndDot(e) : enterNumOnlyzeroToNine(e);

}

function makeingDecValidate(objValue,obj,id){
	if (objValue == '' || isNaN(objValue)) objValue = 0;
    if (isAmount(objValue)) {
		document.getElementById(obj.name+id).value = parseFloat(objValue).toFixed(decDigits);
	}else document.getElementById(obj.name+id).value = parseFloat(0).toFixed(decDigits);
}

function makeingRoundoff(objValue,obj){
    if (objValue == '' || isNaN(objValue)) objValue = 0;
    if (isSignedAmount(objValue)) {
		document.getElementById(obj.name).value = parseFloat(objValue).toFixed(decDigits);
	} else document.getElementById(obj.name).value = parseFloat(0).toFixed(decDigits);
}

function makeingDecPer(objValue,obj){
		if (objValue == '' || isNaN(objValue)) objValue = 0;
	   	document.getElementById(obj.name).value = parseFloat(objValue).toFixed(2);
}

function validatePriorAuthMode(priorAuthEl, priorAuthModeEl, priorAuthId, priorAuthModeId) {
	var priorAuthValue ="";
	var priorAuthModeVal = "";
	var thePriorAuthModeEl = priorAuthModeEl;

	if(priorAuthEl == null && priorAuthId == null){
		return false;
	}

	if(priorAuthModeEl == null && priorAuthModeId == null){
		return false;
	}

	if(priorAuthEl == null && priorAuthId != null){
		priorAuthValue = document.getElementById(priorAuthId).value;
	} else {
		priorAuthValue = priorAuthEl.value;
	}

	if(priorAuthModeEl == null && priorAuthModeId != null){
		priorAuthModeVal = document.getElementById(priorAuthModeId).value;
		thePriorAuthModeEl = document.getElementById(priorAuthModeId);
	} else {
		priorAuthModeVal = priorAuthModeEl.value;
	}

	var isPriorAuthEmpty = !priorAuthValue == null || priorAuthValue == "";
	var isPriorAuthModeEmpty = priorAuthModeVal == null || priorAuthModeVal == "";

	if(!isPriorAuthEmpty && isPriorAuthModeEmpty) {
		alert("Prior Auth Mode is required...\nPlease select the Prior Auth Mode");
		thePriorAuthModeEl.focus();
		return false;
	}

	if(isPriorAuthEmpty && !isPriorAuthModeEmpty) {
		setSelectedIndex(thePriorAuthModeEl, "");
	}

	return true;
}

// regular expression for validating the government_identifier/emirates id
	function getRegularExp(pattern){
		 var regexp="";
		 for(var i=0;i<pattern.length;i++){
			 var str1="";
			 str1=str1+pattern.charAt(i);
			 str1.trim();
			 if(str1 == '9'){
				 regexp=regexp+"(\\d{1})";
			 }else if(str1 == 'x' || str1 == 'X'){
				 regexp=regexp+"([A-Za-z]{1})";
			 }else{
				 regexp=regexp+"(["+str1.toLowerCase()+""+str1.toUpperCase()+"]{1})";
			 }
		 }
		 return "^"+regexp+"$";
	}
	//Restrict special characters like }{<>/\" 
	function restrictSpecialCharacter(e) {
		var chr = String.fromCharCode(e.which);
		if ("}{></\"".indexOf(chr) >= 0) {
			return false;
		}
	}
