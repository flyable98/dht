package bupt.fnl.dht.domain;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 封装节点各种信息的实体类
 */
@Component("nodeInfo")
public class NodeInfo {

    private String knownHostIP;     // 已知节点IP
    private int myIdNums;
    private String knownHostPort;   // 已知节点端口
    private Node me;                // 当前节点
    private int myID;               // 当前节点ID
    private String myIP;            // 当前节点IP
    private String myPort;          // 当前节点端口
    private String myLongtitude; //当前节点的经度
    private String myLatitude; //当前节点的纬度
    private boolean isRun = false; //当前节点的颜色
    private Node pred;              // 前驱节点
    private List<Node> nodeList;    // 节点列表
    private int numDHT;             // 网络最大节点数
    private FingerTable[] finger;   // 路由表
    private int m;                  // 路由表条数
    private List<Identity> identityList;
    private String domainName;
    private String domainID;
    private int bNodeID;

    public int getbNodeID() {
        return bNodeID;
    }

    public void setbNodeID(int bNodeID) {
        this.bNodeID = bNodeID;
    }

    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public int getMyIdNums() {
        return myIdNums;
    }

    public void setMyIdNums(int myIdNums) {
        this.myIdNums = myIdNums;
    }

    public List<Identity> getIdentityList() {
        return identityList;
    }

    public void setIdentityList(List<Identity> identityList) {
        this.identityList = identityList;
    }

    public String getKnownHostIP() {
        return knownHostIP;
    }

    public void setKnownHostIP(String knownHostIP) {
        this.knownHostIP = knownHostIP;
    }

    public String getKnownHostPort() {
        return knownHostPort;
    }

    public void setKnownHostPort(String knownHostPort) {
        this.knownHostPort = knownHostPort;
    }

    public Node getMe() {
        return me;
    }

    public void setMe(Node me) {
        this.me = me;
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

    public Node getPred() {
        return pred;
    }

    public void setPred(Node pred) {
        this.pred = pred;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public int getNumDHT() {
        return numDHT;
    }

    public void setNumDHT(int numDHT) {
        this.numDHT = numDHT;
    }

    public FingerTable[] getFinger() {
        return finger;
    }

    public void setFinger(FingerTable[] finger) {
        this.finger = finger;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public String getMyLongtitude() {
        return myLongtitude;
    }

    public void setMyLongtitude(String myLongtitude) {
        this.myLongtitude = myLongtitude;
    }

    public String getMyLatitude() {
        return myLatitude;
    }

    public void setMyLatitude(String myLatitude) {
        this.myLatitude = myLatitude;
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean run) {
        isRun = run;
    }
}
