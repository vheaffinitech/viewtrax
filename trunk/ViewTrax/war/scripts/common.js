
var debugEle = document.getElementById("debug");

function dbgOut(str) {
	debugEle.innerHTML += str +"<br />";
}

function dbgClear() {
	debugEle.innerHTML = '';
}



//Gets the browser specific XmlHttpRequest Object
function getXmlHttpRequestObject() {
	if (window.XMLHttpRequest) {
		return new XMLHttpRequest();
	} else if(window.ActiveXObject) {
		return new ActiveXObject("Microsoft.XMLHTTP");
	} else {
		alert("Your Browser Sucks!\nIt's about time to upgrade don't you think?");
	}
}


/**
 * Toggles the visibility of the element with the matching id.
 */
function toggleVisibilityUsingId(name, state) {
	var target = document.getElementById(name);
	toggleVisibility(target, state);
}

/**
 * Toggles the visibility of the element with the matching id.
 */
function toggleVisibility(target, state) {
	if (target.style.display == 'none') {
		target.style.display = state;
	} else {
		target.style.display = 'none'; 
	}
}



function removeInputAndAddAsListItem(listItemHolder, input, itemClassName, removeOnclick) {
	var entry = document.createElement("li");
	entry.class = itemClassName;
	entry.innerHTML = input.value;

	// Remove input from user view
	input.parentNode.parentNode.removeChild(input.parentNode);
	
	// Add the input but hide it.
	input.type = "hidden";
	// input.name = <%= HtmlHelper.surroundWithQuotes(Title.REMOVE_PREFIX) %> + input.name;
	input.name = 'rm' + input.name;
	entry.appendChild(input);
	
	// Add a close tag
	var removeLink = document.createElement("a");
	removeLink.setAttribute("onclick", removeOnclick);
	removeLink.setAttribute("href","javascript:;");
	removeLink.innerHTML = "X";
	entry.appendChild(removeLink);
	
	// Add new title entry to view
	listItemHolder.appendChild(entry);
}



