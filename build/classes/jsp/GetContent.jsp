<%@page contentType="text/html;charset=UTF-8"%>
<HTML>
 <HEAD>
  <TITLE>LP Connections</TITLE>
    <style type="text/css">
    /*<![CDATA[*/
	body {
          color: #000000;
          background-color: #FFFFFF;
		  font-family: Arial, "Times New Roman", Times, serif;
          margin: 10px 0px;
      }

    table {
		border: 2px solid;
		margin: 6px 12px;
		border-collapse: collapse;
	}
	
	tr:nth-child(even) {background-color: #f2f2f2;}
	tr:nth-child(odd) {background-color: lightgrey;}
	
    th, td {
		padding: 3px 8px;
	}
	
    th {
        font-family: Verdana, "Times New Roman", Times, serif;
        font-size: 110%;
        font-weight: normal;
        font-style: italic;
        background: #D2A41C;
        text-align: center;
    }

    td {
        color: #000000;
		font-family: Arial, Helvetica, sans-serif;
	}
	
	.pcname {
		font-weight: bold;
		font-style: italic;
	}
    /*]]>*/
   </style> 
   </HEAD>
 <BODY>
  <H1>Last Connections</H1>
  <jsp:useBean id="lpmon" scope="session" class="com.baxter.lpm.LPMonitor" />
<%
  com.baxter.lpm.LPConnection[] maps = lpmon.getMap();
  if (maps == null) {
%>
There is no connection information.
<%
  } else {
%>
<TABLE>
<TR>
<TH>LP</TH>
<TH>PC name</TH>
<TH>Ip address</TH>
<TH>Port</TH>
<TH>Date-time</TH>
</TR>
<TR>
<%
    for (com.baxter.lpm.LPConnection conn: maps){
%>
<TD>
<%=conn.getLp()%>
</TD>
<TD class="pcname">
<%=conn.getPcName()%>
</TD>
<TD>
<%=conn.getIp()%>
</TD>
<TD>
<%=conn.getPort()%>
</TD>
<TD>
<%=conn.getDateString()%>
</TD>
</TR>
<%
    }
  }
%>
 </BODY>
</HTML>