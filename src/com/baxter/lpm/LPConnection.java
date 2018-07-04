package com.baxter.lpm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LPConnection
{
  /**
   * Logger
   */
  //  private static final Logger logger = LoggerFactory.getLogger(LPConnection.class);

  private static final String DATE_FORMAT = "yyyyMMdd-HHmmss";

  /**
   * Identify of the Liquidity Provider
   */
  private final String lp;

  /**
  * IP address of the caller
  */
  private final String ip;

  /**
   * Port number for JMX communication
   */
  private final String port;

  /**
   * The name of the caller's computer
   */
  private final String pcName;

  /**
   * Message date/time
   */
  private final Date date;

  public LPConnection()
  {
	this(null, null, null, null, (Date) null);
  }

  public LPConnection(String lp, String ip, String port, String pcName, String date)
  {
	this.lp = lp;
	this.ip = ip;
	this.port = port;
	this.pcName = pcName;
	DateFormat df = new SimpleDateFormat(DATE_FORMAT);
	Date dateParam = null;
	try
	{
	  dateParam = df.parse(date);
	}
	catch (ParseException e)
	{
	  //	  logger.error("writeConnections error: {}", e.getMessage());
	  LPMonitor.log(LPConnection.class.getName(), "ERROR", "writeConnections error: " + e.getMessage());
	}
	this.date = dateParam;
  }

  public LPConnection(String lp, String ip, String port, String pcName, Date date)
  {
	this.lp = lp;
	this.ip = ip;
	this.port = port;
	this.pcName = pcName;
	this.date = date;
  }

  /**
   * @return the lp
   */
  public String getLp()
  {
	return lp;
  }

  /**
   * @return the ip
   */
  public String getIp()
  {
	return ip;
  }

  /**
   * @return the port
   */
  public String getPort()
  {
	return port;
  }

  /**
   * @return the name of the caller computer
   */
  public String getPcName()
  {
	return pcName;
  }

  /**
   * @return the date
   */
  public Date getDate()
  {
	return date;
  }

  /**
   * @return the date
   */
  public String getDateString()
  {
	DateFormat df = new SimpleDateFormat(DATE_FORMAT);
	return df.format(date);
  }

}
