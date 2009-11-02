<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>


<%@ page import="org.w3c.dom.Node;" %>

<%@ page import="viewTrax.data.Title" %>
<%@ page import="viewTrax.data.TitleName" %>
<%@ page import="viewTrax.data.TitleEntry" %>
<%@ page import="viewTrax.SingletonWrapper" %>
<%@ page import="viewTrax.QueryHelper" %>
<%@ page import="viewTrax.HtmlHelper" %>


<html>
<head>
	<link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
</head>


  <body>
	  <div id='menu'>
	  	<div id='navigation'>
	  		<a href='/'><span>Home</span></a>
	  	</div>
	
	  	<div id='loginContainer'>
			<%
			    UserService userService = UserServiceFactory.getUserService();
			    User user = userService.getCurrentUser();
			    if (user != null) {
			%>
			Hello, <%= user.getNickname() %>! (You can
			<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>.)
			<%
			    } else {
			%>
			Hello!
			<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
			to include your name with greetings you post.
			<%
			    }
			%>
		</div>
      </div>
      <div class='spacer'>
      
<% 
	String titleKey = request.getParameter(Title.KEY);
	String titleName = request.getParameter(Title.NAMES);
	Title t;
	if( titleName != null ) {
		t = QueryHelper.getTitle(titleName);
	} else {
		t = QueryHelper.getTitleById(titleKey);
	}
%>

<h1><%= t.getPrimaryName().getName() %></h1>

<div class="titleInfo" id="description">
	<h2>Description
	</h2>
	
	<%
		String[] details = HtmlHelper.getDetails(t); 
		for( String para : details ) { 
	%>		<p> <%= para %> </p>	<%
		} 
	%>
	<div id="source">
		Source: 
			<div id='wikiSourceEditHolder' style="display:none">
				<input type='text' id='wikiSourceInput' size=60 name=<%= HtmlHelper.surroundWithQuotes(Title.DETAILS_PAGE)%> value=<%= "'"+t.getDetailsPage().getValue()+"'" %> 
					onblur='toggleVisibilityUsingId("wikiSourceEditHolder", "inline");toggleVisibilityUsingId("wikiSource", "inline");'/>
				<a href='javascript:;' onclick='updateTitleDetailsPage(this)'>Submit</a>
			</div>
			<%= HtmlHelper.createTagA(t.getDetailsPage().getValue(), "wikiSource", "wiki page") %>
			<a href="javascript:;" onmousedown='toggleVisibilityUsingId("wikiSourceEditHolder", "inline");toggleVisibilityUsingId("wikiSource", "inline");'> Edit </a>
			
	</div>
</div>


<div class="titleInfo" id="details">
	<div id="titleImage">Image Goes here</div>
	
	<div class="titleNamesContainer">
		Other Names: 
		<a href="javascript:;" onmousedown="toggleVisibilityUsingId('namesListHolder', 'block')">Toggle Visibility</a> 
		<div id="namesListHolder" style="display:block">
			<ul id="titleNamesList">
				<%  { // set scope for name vars
					String nameAttr = HtmlHelper.surroundWithQuotes(Title.REMOVE_NAMES);
					for( TitleName name : t.getNames() ) {
						String quotedName = HtmlHelper.surroundWithQuotes(name.getName());
				%>		<li class="titleNames" >
							<%= name.getName() %>
							
							<input type='hidden' name=<%= nameAttr %> value=<%= quotedName %>  />
							<a href="javascript:;" onclick="removeTitleName(this)">X</a>
						</li>
				<%	} 
					}// set scope for name var
				%>
			</ul>
	
			<div id="addTitleNameHolder"></div>	
			<a href="javascript:addTitleNameEntry()"> Add Title Name</a>
		</div>
			
	</div>
</div>

<div class='spacer'>


<div id='entriesInfo'>
	<h2>Entries</h2>
	<div id='entriesListHolder'>
		<ul id='entriesList'>
		<%  
			String nameAttr = HtmlHelper.surroundWithQuotes(Title.REMOVE_ENTRIES);
			for( TitleEntry entry : t.getEntries() ) {
				String quotedName = HtmlHelper.surroundWithQuotes(entry.getName());
		%>		<li class="titleEntries" >
					<%= entry.getName() %>
					
					<input type='hidden' name=<%= nameAttr %> value=<%= quotedName %>  />
					<a href="javascript:;" onclick="removeTitleEntry(this)">X</a>
				</li>
		<%	} 		%>
		</ul>
		<div id="addTitleEntryHolder"></div>	
		<a href="javascript:addTitleEntryInput()"> Add Title Name</a>
	</div>
</div>

<div id="debug"></div>

<script language="JavaScript" type="text/javascript" src="scripts/common.js"></script>
<script language="JavaScript" type="text/javascript">

updateFields = {
	list : document.getElementById("titleNamesList"),
	listEntryClass : "titleNames",
	addHolder : document.getElementById("addTitleNameHolder"),
	namesHolder : document.getElementById("titleNamesList"),
	curElement : null,
}


var wikiLink;

// param link should be the submit link
function updateTitleDetailsPage(link) {
	dbgClear();
	dbgOut("Link Change");
	wikiLink = link;
	dbgOut(link.innerHTML);
	submitTitleUpdate(link.previousElementSibling, handleDetailsPageUpdate);
}

function handleDetailsPageUpdate() {
	if (updateReq.readyState == 4) {
		var input = wikiLink.previousElementSibling;
		var str = updateReq.responseText.split("\n");
		var found = false;
		
		for(updated in str) {
			if( str[updated] == input.name ) {
				found = true
				break;
			}
		}

		if(!found) {
			// Change link to say retry
			wikiLink.innerHTML = 'Retry';
		} else {
			toggleVisibilityUsingId("wikiSourceEditHolder", "inline");
			toggleVisibilityUsingId("wikiSource", "inline");
			
			wikiLink.innerHTML = 'Submit';
			var link = document.getElementById('wikiSource');
			link.href = input.value;
		}
	}
}



function removeTitleName(link) {
	updateFields.curElement = link.previousElementSibling;
	submitTitleUpdate(link.previousElementSibling, handleTitleNameRemove);
}

function handleTitleNameRemove() {
	if (updateReq.readyState == 4) {
		var str = updateReq.responseText.split("\n");
		var found = false;
		for(updated in str) {
			if( str[updated] == updateFields.curElement.name ) {
				found = true;
				break;
			}
		}

		if(!found) {
			// Change link to say retry
			updateFields.curElement.nextElementSibling.innerHTML = 'Retry';
		} else {
			updateFields.namesHolder.removeChild(updateFields.curElement.parentNode);
		}
	}
}

function addTitleEntryInput() {
	var loc = document.getElementById("addTitleEntryHolder");
	var submitAction = "addTitleEntry(this.firstElementChild)";
	addSimpleTextFormInput(loc, submitAction, <%= HtmlHelper.surroundWithQuotes(Title.ENTRIES) %>, "Submit");
}


var titleEntry = null;

function addTitleEntry(input) {
	var name = input.value;
	
	if(name.length == 0 ) {
		alert("Nothing to submit!");
	} else {
		titleEntry = input;
		submitTitleUpdate(input, handleTitleEntryUpdate);
	}
}

function handleTitleEntryUpdate() {
	if (updateReq.readyState == 4) {
		var str = updateReq.responseText.split("\n");
		var found = false;
		for(updated in str) {
			if( str[updated] == titleEntry.name ) {
				found = true;
				break;
			}
		}

		if(!found) {
			// Change link to say retry
			titleEntry.nextElementSibling.innerHTML = 'Retry';
		} else {
			var entriesHolder = document.getElementById("entriesList");
			removeInputAndAddAsListItem(entriesHolder, titleEntry, "titleEntries", "removeTitleEntry(this)");
		}
	}
}

function removeTitleEntry(link) {
	dbgClear();
	dbgOut("rm link.innerHTML: " + link.innerHTML);
	titleEntry = link.previousElementSibling;
	submitTitleUpdate(titleEntry, handleTitleEntryRemove);
}

function handleTitleEntryRemove () {
	if (updateReq.readyState == 4) {
		var str = updateReq.responseText.split("\n");
		var found = false;
		for(updated in str) {
			if( str[updated] == titleEntry.name ) {
				found = true;
				break;
			}
		}

		if(!found) {
			// Change link to say retry
			titleEntry.nextElementSibling.innerHTML = 'Retry';
		} else {
			var entriesHolder = document.getElementById("addTitleEntryHolder");
			titleEntry.parentNode.parentNode.removeChild(titleEntry.parentNode);
		}
	}
}

function addTitleNameEntry() {
	var loc = document.getElementById("addTitleNameHolder");
	var submitAction = "addTitleName(this.firstElementChild)";
	addSimpleTextFormInput(loc, submitAction, <%= HtmlHelper.surroundWithQuotes(Title.NAMES) %>, "Submit");
}

function addTitleName(element)
{
	var name = element.value;
		
	if(name.length == 0 ) {
		alert("Nothing to submit!");
	} else {
		updateFields.curElement = element;
		submitTitleUpdate(element, handleTitleNameUpdate);
	}
}


var updateReq = getXmlHttpRequestObject();

function submitTitleUpdate(input, handler) {
dbgOut("input.value: "+input.value);

	if (updateReq.readyState == 4 || updateReq.readyState == 0) {
		var url = 'updateTitle?';
		url += <%= HtmlHelper.surroundWithQuotes(Title.KEY) %> + '=';
		url += <%= HtmlHelper.surroundWithQuotes(t.getKey().getId()) %>;
dbgOut("URL: "+ url +" <br>");
		var body = buildTitleNameSubmitBody(input.name, input.value);
dbgOut("body: "+ body +" ");		
		
		updateReq.open("POST", url, true);
		updateReq.onreadystatechange = handler; 
		updateReq.send(body);
	}
}

function buildTitleNameSubmitBody(name, value) {
	var body = name + '\n';
	body += value + '\n';
	return body;
}

function handleTitleNameUpdate() {
	if (updateReq.readyState == 4) {
		var str = updateReq.responseText.split("\n");
		var found = false;
		for(updated in str) {
			if( str[updated] == updateFields.curElement.name ) {
				found = true;
				break;
			}
		}

		if(!found) {
			// Change link to say retry
			updateFields.curElement.nextElementSibling.innerHTML = 'Retry';
		} else {
			removeInputAndAddAsListItem(updateFields.list, updateFields.curElement, updateFields.listEntryClass, 'removeTitleName(this)'); 
		}
	}
}

</script>


  </body>
</html>
