<%@ page import="com.piragua.sakila.Film" %>
<html>
<head>
    <title>Search Solr</title>
    <meta name="layout" content="main" />
</head>
<body style="width:800px;">
<div id="pageBody" style="padding:10px 10px 10px 50px;">
    <h1>Search </h1>
    <g:form name="search" action="index" method="get">
        <g:textField name="q" value="${q}"/> <g:submitButton name="search" value="search"/>
        <g:hiddenField name="max" value="15"/>
    </g:form>

    <div id="results" style="width:500px; float:left; display:block; padding-right:20px;">
        <g:each in="${result.resultList}" var="item">
            <p style="padding: 5px 0 5px 0; border-bottom:1px dashed #DDDDDD;">
                <solr:resultLink result="${item}">${item.title}</solr:resultLink>
            </p>
        </g:each>
        <br/>
        <span class="paging">
            <g:paginate total="${result.total}" max="15" params="[q:q, fq:fq, facetfield:params.list('facetfield')]"/>
        </span>
        <span>Total: ${result.total}</span>
        <g:if test="${result.total == 0}">
            No Results found
        </g:if>
    </div>

    <g:if test="${result.total}">
        <div style="width:250px; float: left; display:block;">

            <g:each in="${params.list('facetfield')}" var="facetfield" >
                <solr:facet field="${facetfield}" result="${result}" fq="${fq}" q="${q}" min="1">
                    <h3>Filter by ${facetfield}</h3>
                </solr:facet>

            </g:each>

        </div>

        <div style="float:left; background-color:#eeeeee; padding: 10px;">
            <h3>Raw Solr URL Parameters</h3>
            <g:each in="${solrQueryUrl.split('&')}">
                ${it.decodeURL()}<br/>
            </g:each>
        </div>
    </g:if>
</div>
</body>
</html>

