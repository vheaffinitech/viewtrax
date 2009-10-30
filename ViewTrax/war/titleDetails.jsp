<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>


<%@ page import="org.w3c.dom.Node;" %>

<%@ page import="viewTrax.data.Title" %>
<%@ page import="viewTrax.data.TitleName" %>
<%@ page import="viewTrax.SingletonWrapper" %>
<%@ page import="viewTrax.QueryHelper" %>
<%@ page import="viewTrax.HtmlHelper" %>


<html>
<head>
	<link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
</head>


  <body>

<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {
%>
<p>Hello, <%= user.getNickname() %>! (You can
<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>.)</p>
<%
    } else {
%>
<p>Hello!
<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
to include your name with greetings you post.</p>
<%
    }
%>
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

<h1><%= t.getPrimaryName() %></h1>

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
				<input type='text' id='wikiSourceInput' size=60 name=<%= HtmlHelper.surroundWithQuotes(Title.DETAILS_PAGE)%> value=<%= "'"+t.getDetailsPage().getValue()+"'" %> />
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
		<div id="debug"></div>
			
	</div>
</div>

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
	dbgOut("WTF");
	wikiLink = link;
	dbgOut(link.innerHTML);
	submitTitleName(link.previousElementSibling, handleDetailsPageUpdate);
}

function handleDetailsPageUpdate() {
	if (updateReq.readyState == 4) {
		var input = wikiLink.previousElementSibling;
		var str = updateReq.responseText.split("\n");
		var found = false;
		for(updated in str) {
			if( str[updated] == input.name ) {
				found = true;
				break;
			}
		}

		if(!found) {
			// Change link to say retry
			wikiLink.innerHTML = 'Retry';
		} else {
			wikiLink.innerHTML = 'Submit';
		}
	}
}



function removeTitleName(link) {
	updateFields.curElement = link.previousElementSibling;
	submitTitleName(link.previousElementSibling, handleTitleNameRemove);
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

function addTitleNameEntry() {
	var loc = document.getElementById("addTitleNameHolder");
	
	var input = document.createElement("input");
	input.setAttribute("type","text");
	input.setAttribute("name", <%= HtmlHelper.surroundWithQuotes(Title.NAMES) %>);
	
	var action = document.createElement("a");
	action.setAttribute("href","javascript:;");
	action.setAttribute("onclick","addTitleName(this.previousSibling)");
	action.innerHTML = "Submit";
	
	var container = document.createElement("div");
	container.appendChild(input);
	container.appendChild(action);
	
	loc.appendChild(container);
}

function addTitleName(element)
{
	var name = element.value;
		
	if(name.length == 0 ) {
		alert("Nothing to submit!");
	} else {
		updateFields.curElement = element;
		submitTitleName(element, handleTitleNameUpdate);
	}
}


var updateReq = getXmlHttpRequestObject();

function submitTitleName(input, handler) {
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
			var entry = document.createElement("li");
			entry.setAttribute("class", updateFields.listEntryClass);
			entry.innerHTML = updateFields.curElement.value;
			
			updateFields.list.appendChild(entry);
			updateFields.addHolder.removeChild(updateFields.curElement.parentNode);
		}
	}
}

</script>


  </body>
</html>
