<%-- 
    Document   : main
    Created on : 23-Jan-2017, 15:15:51
    Author     : Nate
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>

    <head>
        <title>Nated</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/resources/nd-logo.bmp" />
        <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/css/natedStyle.css">
        <script type="text/javascript" src="${pageContext.request.contextPath}/javascript/jquery/jquery-3.1.1.min.js" charset="utf-8"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/javascript/d3/d3.min.js" charset="utf-8"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/javascript/timeseries.js" charset="utf-8"></script>
    </head>

    <body>
        <%@ include file="/resources/header.jsp" %>
        <jsp:include page="${requestScope.included}" flush="true" />
        
        <script>
            //Set selected menu link
            $(function () {
                $('.menu a').filter(function () {
                    return this.href == location.href
                }).parent().addClass('active').siblings().removeClass('active')
                $('.menu a').click(function () {
                    $(this).parent().addClass('active').siblings().removeClass('active')
                })
            })
        </script>
        
    </body>
</html>
