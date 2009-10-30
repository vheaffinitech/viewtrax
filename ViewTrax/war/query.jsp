<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<%@ page import="viewTrax.data.Title" %>
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


<p>Anime in DB:</p>
<%	String title = "Vandread";
	Title t = QueryHelper.getTitle(title);
	if( t != null ) {
%>
<p>Anime: <%= t.getNames().get(0) %> 
</p>
<%
	}
%>


<form action="/sign" method="post">
    <div><textarea name="title" rows="1" cols="60"></textarea></div>
    <div><textarea name="description" rows="1" cols="25"></textarea></div>
    <div><input type="submit" value="Post Title" /></div>
  </form>



  </body>
</html>
