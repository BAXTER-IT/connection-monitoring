package com.baxter.lpm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class LPMonitor
{
  /**
   * Logger
   */
  //  private static final Logger logger = LoggerFactory.getLogger(LPMonitor.class);

  private static final String CONNECTION_FILE = "/var/log/connections.txt";
  private static final String SEPARATOR = "%";
  private static final String SERVER_ID = "price-engine-liquidity-provider-BAML";

  /**
   * Register the connection into the connections map
   *
   * @param lp The identify of the Liquidity Provider
   * @param ip The IP address of the caller
   * @param port Port number for JMX communication
   * @param pcName The name of the caller's computer
   */
  public void setConnection(String lp, String ip, String port, String pcName)
  {
	//	logger.info("setConnection(lp=\"" + lp + "\", ip=\"" + ip + "\", port=\"" + port + "\", pcName=\"" + pcName + "\")");
	log(LPMonitor.class.getName(), "INFO",
	    "setConnection(lp=\"" + lp + "\", ip=\"" + ip + "\", port=\"" + port + "\", pcName=\"" + pcName + "\")");
	Map<String, LPConnection> connections = loadConnections();
	connections.put(lp, new LPConnection(lp, ip, port, pcName, new Date()));
	writeConnections(connections);
  }

  /**
   * Get information of the connection of the given LP
   *
   * @param lp The identify of the Liquidity Provider
   * @return null if there is no live connection for LP, else the IP address and name of the LP user
   */
  public String getConnection(String lp)
  {
	Map<String, LPConnection> connections = loadConnections();
	LPConnection conn = connections.get(lp);
	if (conn == null)
	{
	  //	  logger.info("getConnection(lp=\"{}\"), returns: null. No such connection.", lp);
	  log(LPMonitor.class.getName(), "INFO", "getConnection(lp=\"" + lp + "\"), returns: null. No such connection.");
	  return null;
	}
	try
	{
	  JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + conn.getIp() + ":" + conn.getPort() + "/jmxrmi");
	  JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
	  MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
	  ObjectName objectName = new ObjectName("com.baxter.pe:type=" + SERVER_ID);
	  if ((Boolean) mbsc.getAttribute(objectName, "Connected"))
	  {
		String ret = conn.getPcName() + " (" + conn.getIp() + ")";
		//		logger.info("getConnection(lp=\"{}\"), returns: \"{}\"", lp, ret);
		log(LPMonitor.class.getName(), "INFO", "getConnection(lp=\"" + lp + "\"), returns: \"" + ret + "\"");
		return ret;
	  }
	}
	catch (IOException | MalformedObjectNameException | AttributeNotFoundException | InstanceNotFoundException | MBeanException
	    | ReflectionException e)
	{
	  //	  logger.error(e.getMessage());
	  log(LPMonitor.class.getName(), "ERROR", e.getMessage());
	}
	//	logger.info("getConnection(lp=\"{}\"), returns: null", lp);
	log(LPMonitor.class.getName(), "INFO", "getConnection(lp=\"" + lp + "\"), returns: null");
	return null;
  }

  /**
   * Remove LP from connections
   *
   * @param lp The identify of the Liquidity Provider
   */
  public void removeConnection(String lp)
  {
	//	logger.info("removeConnection(lp=\"{}\")", lp);
	log(LPMonitor.class.getName(), "INFO", "removeConnection(lp=\"" + lp + "\")");
	Map<String, LPConnection> connections = loadConnections();
	connections.remove(lp);
	writeConnections(connections);
  }

  /**
   * @return the all registered connection in a Map
   */
  public LPConnection[] getMap()
  {
	Map<String, LPConnection> connections = loadConnections();
	Set<String> lps = connections.keySet();
	LPConnection[] list = new LPConnection[lps.size()];
	int i = 0;
	for (String lp : connections.keySet())
	{
	  list[i++] = connections.get(lp);
	}
	//	logger.info("getMap(), returns: \"{}\"", getString(list));
	log(LPMonitor.class.getName(), "INFO", "getMap(), returns: \"" + getString(list) + "\"");
	return list;
  }

  private String getString(LPConnection[] list)
  {
	StringBuffer sb = new StringBuffer();
	for (LPConnection lpConn : list)
	{
	  if (sb.length() > 0)
	  {
		sb.append(", ");
	  }
	  sb.append("{").append("lp=\"").append(lpConn.getLp()).append("\",ip=\"").append(lpConn.getIp()).append("\",port=\"")
	      .append(lpConn.getPort()).append("\",pcName=\"").append(lpConn.getPcName()).append("}");
	}
	return sb.toString();
  }

  private Map<String, LPConnection> loadConnections()
  {
	Properties prop = new Properties();
	try
	{
	  InputStream stream = new FileInputStream(CONNECTION_FILE);
	  try
	  {
		prop.load(stream);
	  }
	  catch (IOException e)
	  {
		//		logger.error("loading error. {}", e.getMessage());
		log(LPMonitor.class.getName(), "ERROR", "loading error. " + e.getMessage());
	  }
	  finally
	  {
		stream.close();
	  }
	}
	catch (final IOException e)
	{
	  //	  logger.error("There is no connection file: {}", CONNECTION_FILE);
	  log(LPMonitor.class.getName(), "ERROR", "There is no connection file: " + CONNECTION_FILE);
	  return new HashMap<String, LPConnection>();
	}
	Map<String, LPConnection> connections = new HashMap<String, LPConnection>();
	for (String lp : prop.stringPropertyNames())
	{
	  String value = prop.getProperty(lp);
	  String[] values = value.split(SEPARATOR);
	  String ip = values.length > 0 ? values[0] : "";
	  String port = values.length > 1 ? values[1] : "";
	  String pcName = values.length > 2 ? values[2] : null;
	  String date = values.length > 3 ? values[3] : null;
	  connections.put(lp, new LPConnection(lp, ip, port, pcName, date));
	}
	return connections;
  }

  private void writeConnections(Map<String, LPConnection> connections)
  {
	try
	{
	  Properties envProperties = new Properties();
	  for (String lp : connections.keySet())
	  {
		LPConnection msg = connections.get(lp);
		String s = msg.getIp() + SEPARATOR + msg.getPort() + SEPARATOR + msg.getPcName() + SEPARATOR + msg.getDateString();
		envProperties.setProperty(lp, s);
	  }
	  OutputStream out = new FileOutputStream(CONNECTION_FILE);
	  envProperties.store(out, "Active LP usage");
	}
	catch (IOException e)
	{
	  //	  logger.error("writeConnections error: {}", e.getMessage());
	  log(LPMonitor.class.getName(), "ERROR", "writeConnections error: " + e.getMessage());
	}
  }

  public static void log(String className, String type, String msg)
  {
	String logFileName = "/var/log/lpmon.log";
	File logFile = new File(logFileName);
	try
	{
	  if (!logFile.exists())
	  {
		logFile.createNewFile();
	  }
	  StringBuffer out = new StringBuffer().append(className).append("[").append(new Date().toString()).append("][").append(type)
	      .append("]").append(msg).append("\n");
	  Files.write(Paths.get(logFileName), out.toString().getBytes(), StandardOpenOption.APPEND);
	  System.out.println("lpmonitor log: " + msg);
	}
	catch (IOException e)
	{
	  e.printStackTrace();
	  System.out.println("lpmonitor IOError: " + e.getMessage());
	}

  }
}
