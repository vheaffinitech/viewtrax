<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<%@ page import="viewTrax.data.Title" %>
<%@ page import="viewTrax.HtmlHelper" %>
<%@ page import="viewTrax.SingletonWrapper" %>
<%@ page import="viewTrax.QueryHelper" %>


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

	
	<form action="/addTitle" method="post">
		<fieldset id="showInformation">
			<legend>Show Information</legend>
			<label for="tname">Title: </label>
			<input type="text" name=<%= HtmlHelper.surroundWithQuotes(Title.NAMES)%> size=40 />
			
			<br>
			<label for="desc">Description: </label>
			<textarea name="description" COLS=40 ROWS=4></textarea>
			
			<br>
			<label for='submitSpacer'> &nbsp; </label>
			<input type="submit" value="Post Title" />
		</fieldset>
	</form>
	
	
	<form action="titleDetails.jsp">
		<fieldset>
			<legend>Query</legend>
			<label for="tname">Title: </label>
			<input type="text" name="names" size=40 />
	    	<input type="submit" value="Query" />
		</fieldset>
	</form>
	
	
	<style type="text/css" media="screen">
			body {
				font: 11px arial;
			}
			.suggest_link {
				background-color: #000000;
				padding: 2px 6px 2px 6px;
			}
			.suggest_link_over {
				background-color: #3366CC;
				padding: 2px 6px 2px 6px;
			}
			#search_suggest {
				position: absolute; 
				text-align: left; 
				border: 1px solid #FFFFFF;
			}
			div.left{
				float:left;
			}
		</style>
	
		<script language="JavaScript" type="text/javascript" src="scripts/common.js"></script>
	<script language="JavaScript" type="text/javascript" src="scripts/ajax_search.js"></script>
	
	<form action="titleDetails.jsp">
			<label>Search Suggest: </label>
			
		<input type="text" id="txtSearch" name=<%= "'"+Title.NAMES+"'" %> alt="Search Criteria" 
			onkeyup="searchSuggest();" onblur="//searchSuggestLost()" autocomplete="off" />
			<div id="search_suggest">	</div>
	    	<input type="submit" value="Query" />
	</form>
	<div id="debug">debug div</div>
	<script language="JavaScript" type="text/javascript" src="scripts/common.js">
		debugEle = document.getElementById("debug");
	</script>
	<p>###############################################################
	</p>
	<p>fadfadddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
	</p>
	<p>fadfadddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
	</p>
	<p>fadfadddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
	</p>
	<p>fadfadddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
	</p>
	<p>fadfadddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
	</p>
	<p>fadfadddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
	</p>
	<p>fadfadddddddddddddddddddddddddddddddddddddddddddddddddddddddddd
	</p>

  </body>
</html>
