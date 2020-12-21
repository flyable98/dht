package bupt.fnl.dht.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Node：dht节点对应的实体类
 *【注意】Node必须实现序列化接口
 */
public class Node implements Serializable {
//	public static final String city[] = {"北京", "南京", "武汉","重庆"};
//	public static final double WeiDu[] = { 40.22, 31.33, 30.58, 29.40};
//	public static final double JingDu[] = { 116.23, 118.89, 114.03, 106.54};
//	private static final Map<Double, Double> JingWei;
//	static
//	{
//		JingWei = new HashMap<Double, Double>();
//		JingWei.put(40.22,  116.23);
//		JingWei.put(31.33, 118.89);
//		JingWei.put( 30.58,  114.03);
//		JingWei.put(29.40, 106.54);
//	}

	private int myID;
	private String myIP;
	private String myPort;
	private String longitude;
	private String latitude;
	private String domainName;
	private int IdNums;
	private boolean isRun = false;//当前节点是否在运行，是为1涂上红色，否则为0涂上绿色

	public String getDomainName() {

		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public int getMyID() {
		return myID;
	}

	public void setMyID(int myID) {
		this.myID = myID;
	}


	public String getMyIP() {
		return myIP;
	}

	public void setMyIP(String myIP) {
		this.myIP = myIP;
	}

	public String getMyPort() {
		return myPort;
	}

	public void setMyPort(String myPort) {
		this.myPort = myPort;
	}

	public int getIdNums() {
		return IdNums;
	}

	public void setIdNums(int idNums) {
		IdNums = idNums;
	}

	public Node(int myID, String myIP, String myPort, String latitude, String longitude, String domainName, int idNums, boolean isRun) {

		this.myID = myID;
		this.myIP = myIP;
		this.myPort = myPort;
		this.longitude = longitude;
		this.latitude = latitude;
		this.domainName = domainName;
		IdNums = idNums;
		this.isRun = isRun;
	}

	public Node(int myID, String myIP, String myPort, String latitude, String longitude, int idNums, boolean isRun) {

		this.myID = myID;
		this.myIP = myIP;
		this.myPort = myPort;
		this.longitude = longitude;
		this.latitude = latitude;
		IdNums = idNums;
		this.isRun = isRun;
	}

	public Node(int myID, String myIP, String myPort, String latitude, String longitude, boolean isRun) {
		this.myID = myID;
		this.myIP = myIP;
		this.myPort = myPort;
		this.longitude = longitude;
		this.latitude = latitude;
		this.isRun = isRun;
	}

	@Override
	public int hashCode() {
		return 3*myID+4*myIP.hashCode()+5*myPort.hashCode() + (int)(longitude.hashCode() * 6 + latitude.hashCode() * 7);
	}
    @Override
    public boolean equals(Object obj) {
    	Node node=null;
    	if(obj instanceof Node)
    		node=(Node)obj;
    	if(node.getID()==myID&&node.getIP().equals(myIP)&&node.getPort().equals(myPort) && node.getLatitude().equals(longitude) && node.getLatitude().equals(latitude))
    		return true;
    	else return false;
    }

	public boolean isRun() {
		return isRun;
	}

	public void setRun(boolean run) {
		isRun = run;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setID (int id) {
		this.myID = id;
	}
	public void setIP (String ip) {
		this.myIP = ip;
	}
	public void setPort (String port) {
		this.myPort = port;
	}
	public int getID() {
		return this.myID;
	}
	public String getIP() {
		return this.myIP;
	}
	public String getPort() {
		return this.myPort;
	}
	public Node(int id, String ip, String port){
		myID = id;
		myIP = ip;
		myPort = port;
	}

	public Node(int myID, String myIP, String myPort, String latitude, String longitude) {
		this.myID = myID;
		this.myIP = myIP;
		this.myPort = myPort;
		this.longitude = longitude;
		this.latitude = latitude;
	}
}