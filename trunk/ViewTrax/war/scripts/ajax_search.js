/*
	This is the JavaScript file for the AJAX Suggest Tutorial

	You may use this code in your own projects as long as this 
	copyright is left	in place.  All code is provided AS-IS.
	This code is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
	
	For the rest of the code visit http://www.DynamicAJAX.com
	
	Copyright 2006 Ryan Smith / 345 Technical / 345 Group.	

*/


//Our XmlHttpRequest object to get the auto suggest
var searchReq = getXmlHttpRequestObject();

//Called from keyup on the search textbox.
//Starts the AJAX request.
function searchSuggest() {
	var input = document.getElementById('txtSearch');
	
	var key = getkey(input);
	var Esc = 27;
	if( key == Esc )
	{
		dbgClear();
		dbgOut('searchSuggest');
		searchSuggestLost();
		return;
	}
	
	if (searchReq.readyState == 4 || searchReq.readyState == 0) {
		var str = escape(input.value);
		searchReq.open("GET", 'search?name=' + str, true);
		searchReq.onreadystatechange = handleSearchSuggest; 
		searchReq.send(null);
	}		
}

//Called when the AJAX response is returned.
function handleSearchSuggest() {
	if (searchReq.readyState == 4) {
		dbgClear();
		
		var ss = document.getElementById('search_suggest');
		var ts = document.getElementById('txtSearch');
		align(ts, ss);
		ss.style.width = ts.offsetWidth;
		dbgOut( searchReq.responseText );
		
		ss.innerHTML = '';
		var str = searchReq.responseText.split("\n");
		for(i=0; i < str.length - 1; i++) {
			//Build our element string.  This is cleaner using the DOM, but
			//IE doesn't support dynamically added attributes.
			var suggest = '<div onmouseover="javascript:suggestOver(this);" ';
			suggest += 'onmouseout="javascript:suggestOut(this);" ';
			suggest += 'onclick="javascript:setSearch(this.innerHTML);" ';
			suggest += 'class="suggest_link">' + str[i] + '</div>';
			ss.innerHTML += suggest;
		}
	}
}

// Align the element provided to the target
function align(target, element){
	var pos = getOffsetParent(target);
	element.style.left = pos[0];
	// target.style.top = pos[1]; // Don't need this currently
}

function getOffsetParent(obj) {
	var curleft = curbottom = 0;
	
	if (obj.offsetParent) {
		curbottom += obj.offsetHeight; 
	
		do {
			curleft += obj.offsetLeft;
			curbottom += obj.offsetTop;
			
		} while (obj = obj.offsetParent);
	}
	return [curleft,curbottom];
}

function searchSuggestLost() {
	var element = document.getElementById('search_suggest');
	element.innerHTML = '';
	
	dbgOut( 'searchSuggestLost' );
}



//Mouse over function
function suggestOver(div_value) {
	div_value.className = 'suggest_link_over';
}
//Mouse out function
function suggestOut(div_value) {
	div_value.className = 'suggest_link';
}
//Click function
function setSearch(value) {
	document.getElementById('txtSearch').value = value;
	document.getElementById('search_suggest').innerHTML = '';
}


function getkey(e)
{
	if (window.event)
		return window.event.keyCode;
	else if (e)
		return e.which;
	else
		return null;
}