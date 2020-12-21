package bupt.fnl.dht.service.impl;

import bupt.fnl.dht.domain.FingerTable;
import bupt.fnl.dht.domain.Message;
import bupt.fnl.dht.domain.Node;
import bupt.fnl.dht.domain.NodeInfo;
import bupt.fnl.dht.network.MakeConnection;
import bupt.fnl.dht.dao.DataBase;
import bupt.fnl.dht.service.NodeService;
import bupt.fnl.dht.service.Print;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service("nodeService")
public class NodeServiceImpl implements NodeService {

    @Autowired
    NodeInfo nodeInfo;
    @Autowired
    MakeConnection makeConnection;
    @Autowired
    DataBase dataBase;
    @Autowired
    JedisPool jedisPool;
    @Autowired
    Print print;
    public String WeiDu[] = { "40.22", "31.33", "30.58", "29.40", "22.33","32.20"};
    private static final Map<String, String> JingWei;
    static
    {
        JingWei = new HashMap<String, String>();
        JingWei.put("40.22", "116.23");
        JingWei.put("31.33", "118.89");
        JingWei.put("30.58", "114.03");
        JingWei.put("29.40", "106.54");
        JingWei.put("22.33", "114.05");
        JingWei.put("32.20", "120.38");

    }


//    static
//    {
//        LatToCity = new HashMap<String, String >();
//        LatToCity.put("40.22", "北京");
//        LatToCity.put("31.33", "南京");
//        LatToCity.put("30.58",  "武汉");
//        LatToCity.put("29.40", "重庆");
//            LatToCity.put("22.33",  "深圳");
//        LatToCity.put("43.88", "无锡");
//    }
    /**
     * initNode - 初始化节点信息
     *
     * 通过输入参数的个数区分第一个节点和之后其他节点的加入
     *
     *      args参数为2位: [当前节点监听端口] [numNodes]
     *      说明是第一个加入的节点 args[0] args[1]
     *
     *      args参数为4位: [已知节点IP] [已知节点监听端口] [当前节点监听端口] [numNodes]
     *      说明不是第一个加入的节点 args[0]  args[1]  args[2]  args[3]
     */
    public void initNode(String... args) {

        if (args.length == 3){
            String myPort = args[0];
            nodeInfo.setMyPort(myPort);
            nodeInfo.setDomainID(args[2]);
            System.out.println("局部域" + nodeInfo.getDomainID() + "已创建。");
            Node me = getCurrentNode(Integer.parseInt(args[1]));
            nodeInfo.setMe(me);
            nodeInfo.setPred(me);

//            System.out.println("2.局部域" + nodeInfo.getDomainName() + "已创建。");


            int laIndex;
            switch (args[2]){
                case "086.001.000001":
                    laIndex = 3;
                    break;
                default:
                    laIndex = 0;

            }

//            System.out.println("laIndex:" + laIndex);
            String latitude = WeiDu[laIndex];
//            System.out.println("WeiDu[laIndex]:" + WeiDu[laIndex]);
            String longtitude = JingWei.get(latitude);
//            System.out.println("latitude:" + latitude +", longtitude: " + longtitude);
            me.setLatitude(latitude);
//            System.out.println("" + nodeInfo.getMyLatitude());
            me.setLongitude(longtitude);
            me.setIdNums(0);
//            System.out.println("" + nodeInfo.getMyLongtitude());

        } else if(args.length == 5 ){
            System.out.println("当前已知节点IP为：" + args[0]);
            nodeInfo.setKnownHostIP(args[0]);
            System.out.println("当前已知节点的端口为：" + args[1]);
            nodeInfo.setKnownHostPort(args[1]);
            System.out.println("当前要加入节点的端口为：" + args[3]);
            String myPort = args[3];
            nodeInfo.setMyPort(myPort);
            nodeInfo.setDomainName(args[2]);
            System.out.println("当前要加入节点的标识为："+args[4]);
// 第四个参数确定了当前网络中的最大节点数
            Node me = getCurrentNode(Integer.parseInt(args[4]));
            nodeInfo.setMe(me);

            int laIndex = 0;
            switch (args[4]){
                case "105":
                    laIndex = 4;
                    break;
                case "104":
                    laIndex = 5;
                    break;
                case "103":
                    laIndex = 2;
                    break;
                case "102":
                    laIndex = 1;
                    break;
            }
//            System.out.println("laIndex:" + laIndex);
            String latitude = WeiDu[laIndex];
//            System.out.println("WeiDu[laIndex]:" + WeiDu[laIndex]);
            String longtitude = JingWei.get(latitude);
//            System.out.println("latitude:" + latitude +", longtitude: " + longtitude);
            me.setLatitude(latitude);
//            System.out.println("" + nodeInfo.getMyLatitude());
            me.setLongitude(longtitude);
            Message request = new Message();
            request.setInitNode_flag(1);
            request.setInitInfo("findPred/" + nodeInfo.getMyID());
            int bNodeId = getbNodeID(args[0], args[1]);
//            request.set("边界节点id:" + bNodeId + ", 局部域编号:"  + args[2] );
            System.out.println("边界节点ID：" + bNodeId);
            nodeInfo.setbNodeID(bNodeId);
            System.out.println("局部域ID:" + args[2]);
            nodeInfo.setDomainID(args[2]);
            Message result = makeConnection.makeConnectionByObject(args[0], args[1], request);
            String[] tokens = result.getInitInfo().split("/");
            Node pred = new Node(Integer.parseInt(tokens[0]),tokens[1],tokens[2]);
            nodeInfo.setPred(pred);
            me.setIdNums(0);

        } else {
            System.out.println("Syntax one - NodeDHT-First [LocalPortNumber] [numNodes] [domainName]");
            System.out.println("Syntax two - NodeDHT-Others [Known-HostIP]  [Known-HostPortNumber] [domainName] [LocalPortNumber] [numNodes]");
            System.out.println("[LocalPortNumber] = is the port number which the main.java.bupt.fnl.dht.domain.Node will be listening waiting for connections.");
            System.out.println("[Known-HostName] = is the hostIP of one DHTNode already in the net.");
            System.out.println("[Known-HostPortNumber] = is the port which the Known-Host listening waiting for connections.");
            System.exit(1);
        }
    }

    /**
     * getCurrentNode - 获取当前节点
     * @param maxNumNodes 用户输入的最大节点数
     * @return 返回当前节点
     */
    public Node getCurrentNode(int maxNumNodes) {
        // 路由表条数
        int m = (int) Math.ceil(Math.log(maxNumNodes) / Math.log(2));
        nodeInfo.setM(m);
        // 路由表
        nodeInfo.setFinger(new FingerTable[m+1]);
        // 网络最大节点数
        nodeInfo.setNumDHT((int)Math.pow(2,m));
        // 当前节点 IP
        InetAddress mIP = null;
        try {
            mIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("ip获取失败...");
            System.exit(1);
        }
        String myIP = mIP.getHostAddress();
        nodeInfo.setMyIP(myIP);
        System.out.println("IP地址: " + myIP);
        // 当前节点端口
        String myPort = nodeInfo.getMyPort();
        // 当前节点 ID
        int myID = getNodeID(myIP,myPort);
//        System.out.println("%%%%%%%%%%%%%%%!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        System.out.println("边界节点ID:" + myID + "Ip:" + myIP + ", Port:" + myPort);
        nodeInfo.setMyID(myID);
//        int laIndex = 0 + (int)(Math.random() * (4 - 0 ));
//        System.out.println("laIndex:" + laIndex);
//        String latitude = WeiDu[laIndex];
//        System.out.println("WeiDu[laIndex]:" + WeiDu[laIndex]);
//        String longtitude = JingWei.get(latitude);
//        System.out.println("latitude:" + latitude +", longtitude: " + longtitude);
        String domainName = nodeInfo.getDomainName();
//        nodeInfo.setMyLatitude(latitude);
//            System.out.println("" + nodeInfo.getMyLatitude());
//        nodeInfo.setMyLongtitude(longtitude);
        return new Node(myID,myIP,myPort, nodeInfo.getMyLatitude(), nodeInfo.getMyLongtitude(),domainName,0,false);
    }

    public int getbNodeID(String nodeIP, String nodePort){
        int nodeID;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.reset();

            if(nodeIP.equals("39.107.232.47")){
                nodeIP = "172.20.0.23";
            }else{
                nodeIP = "172.20.0.18";
            }

            String hashString = nodeIP + nodePort;
            md.update(hashString.getBytes());
            byte[] hashBytes = md.digest();
            BigInteger hashNum = new BigInteger(1, hashBytes);
            nodeID = Math.abs(hashNum.intValue()) % nodeInfo.getNumDHT() * 976;

            return nodeID;
        }catch (NoSuchAlgorithmException e) {
            System.out.println("获取节点ID失败...");
            return -1;
        }
    }

    // 获取新加入网络的节点ID
    public int getNodeID(String nodeIP, String nodePort) {

        int nodeID;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.reset();
            String hashString = nodeIP + nodePort;
            md.update(hashString.getBytes());
            byte[] hashBytes = md.digest();
            BigInteger hashNum = new BigInteger(1, hashBytes);
            nodeID = Math.abs(hashNum.intValue()) % nodeInfo.getNumDHT() * 976;

            if (nodeInfo.getKnownHostIP() != null) {
                Message request = new Message();
                request.setInitNode_flag(1);
                request.setInitInfo("findSucOfPred/"+nodeID);
                // 判断节点hash是否重复
//                while(Integer.parseInt(makeConnection.makeConnectionByObject(nodeInfo.getKnownHostIP(), nodeInfo.getKnownHostPort(), request).getInitInfo())==nodeID) {
//                    md.reset();
//                    md.update(hashBytes);
//                    hashBytes = md.digest();
//                    hashNum = new BigInteger(1,hashBytes);
//                    nodeID = Math.abs(hashNum.intValue()) % nodeInfo.getNumDHT() * 976;
//                    request.setInitInfo("findSucOfPred/"+nodeID);
//                }
            }

            System.out.println("新节点加入... ");
            return nodeID;

        } catch (NoSuchAlgorithmException e) {
            System.out.println("获取节点ID失败...");
            return -1;
        }
    }

    // 创建节点列表
    public void buildNodeList() {
        Node me = nodeInfo.getMe();

//        System.out.pr me.getLongitude();
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(me);
        nodeInfo.setNodeList(nodeList);
        Message request = new Message();
        request.setInitNode_flag(1);
        request.setInitInfo("load/");
        Message result = makeConnection.makeConnectionByObject(nodeInfo.getKnownHostIP(), nodeInfo.getKnownHostPort(), request);
        getNode(result.getInitInfo());
    }

    // 从其他节点列表获取节点信息
    public String loadNode(){
        List<Node> nodeList = nodeInfo.getNodeList();
        StringBuilder results = new StringBuilder();
        nodeList.forEach(node -> results.append(node.getID()).append("/").append(node.getIP()).append("/").append(node.getPort()).append("/").append(node.getLatitude()).append("/").append(node.getLongitude()));
        return results.toString();
    }
    // 处理返回的m个node信息并生成list(路由表中最多只有m个node)
    public void getNode(String str) {
        String[] tokens = str.split("/");
        List<Node> nodeList = nodeInfo.getNodeList();
        for (int i = 1; i <= (tokens.length / 5); i++) {
            Node newNode = new Node(Integer.parseInt(tokens[5 * (i - 1)]), tokens[1 + 5* (i - 1)], tokens[2 + 5 * (i - 1)], tokens[3 + 5 * (i - 1)], tokens[4 + 5 * (i - 1)]);
            nodeList.add(newNode);
        }
    }


    // 更新nodeList
    public void updateList(Node node) {
        nodeInfo.getNodeList().add(node);
        System.out.println();
        System.out.println("【系统提示】- "+"新节点 "+node.getID()+"加入DHT网络");
        print.printNodeInfo();
    }

    // 更新其它节点的nodeList
    public void updateOthersList() {
        Node me = nodeInfo.getMe();
        List<Node> nodeList = nodeInfo.getNodeList();
        Message request = new Message();
        request.setInitNode_flag(1);
        request.setInitInfo("updateList/"+me.getID()+"/"+me.getIP()+"/"+me.getPort() + "/" + me.getLatitude() + "/" + me.getLongitude());
        for (Node node : nodeList) {
            if (node == me) {
                continue;
            }
            makeConnection.makeConnectionByObject(node.getIP(), node.getPort(), request);
        }
    }

    // 通过当前节点的路由表查询某个NID的前继节点
    public Node find_predecessor(int id){
        Node me = nodeInfo.getMe();
        FingerTable[] finger = nodeInfo.getFinger();
        int myID = me.getID();
        int succID = finger[1].getSuccessor().getID();
        int normalInterval = 1;
        if (myID >= succID)
            normalInterval = 0;

        while ((normalInterval==1 && (id <= myID || id > succID)) ||
                (normalInterval==0 && (id <= myID && id > succID))) {

            Message request = new Message();
            request.setInitNode_flag(1);
            request.setInitInfo("closetPred/" + id);
            Message result = makeConnection.makeConnectionByObject(me.getIP(),me.getPort(),request);
            String[] tokens = result.getInitInfo().split("/");

            me = new Node(Integer.parseInt(tokens[0]),tokens[1],tokens[2]);

            myID = me.getID();

            request.setInitInfo("getSuc/");
            Message result2 = makeConnection.makeConnectionByObject(me.getIP(),me.getPort(),request);
            String[] tokens2 = result2.getInitInfo().split("/");

            succID = Integer.parseInt(tokens2[0]);

            if (myID >= succID)
                normalInterval = 0;
            else
                normalInterval = 1;
        }
        return me;
    }
    // 通过当前节点的路由表查询某个NID的后继节点
    public Node find_successor(int id) {
        Node n = find_predecessor(id);

        Message request = new Message();
        request.setInitNode_flag(1);
        request.setInitInfo("getSuc/");
        Message result = makeConnection.makeConnectionByObject(n.getIP(),n.getPort(),request);
        String[] tokens = result.getInitInfo().split("/");
        return new Node(Integer.parseInt(tokens[0]),tokens[1],tokens[2]);
    }

    // 设置当前节点的前继节点
    public void setPredecessor(Node n) // throws RemoteException
    {
        nodeInfo.setPred(n);
    }

    // 获取当前节点的前继节点
    public Node getPredecessor() //throws RemoteException
    {
        return nodeInfo.getPred();
    }

    // 获取当前节点的后继节点
    public Node getSuccessor() {
        FingerTable[] finger = nodeInfo.getFinger();
        return finger[1].getSuccessor();
    }

    // 获取当前节点路由表中距离目标id最近的节点
    public Node closet_preceding_finger(int id)
    {
        Node me = nodeInfo.getMe();
        int normalInterval = 1;
        int myID = me.getID();
        if (myID >= id) {
            normalInterval = 0;
        }
        FingerTable[] finger = nodeInfo.getFinger();
        for (int i = nodeInfo.getM(); i >= 1; i--) {
            int nodeID = finger[i].getSuccessor().getID();
            if (normalInterval == 1) {
                if (nodeID > myID && nodeID < id)
                    return finger[i].getSuccessor();
            } else {
                if (nodeID > myID || nodeID < id)
                    return finger[i].getSuccessor();
            }
        }
        return me;
    }

    // 广播消息
    public void noticeOthers(String message) {
        Node me = nodeInfo.getMe();
        List<Node> nodeList = nodeInfo.getNodeList();
        Message request = new Message();
        request.setInitNode_flag(1);
        request.setInitInfo(message);
        Iterator<Node> iterator = nodeList.iterator();
        while(iterator.hasNext()) {
            Node node =iterator.next();
            if (node == me) {
                continue;
            }
            makeConnection.makeConnectionByObject(node.getIP(),node.getPort(),request);
        }
        System.out.println("已通知所有节点");
    }


    /**
     * beforeExit() - 节点退出网络
     * 分 单个节点、两个节点、多个节点 退出3种情况
     */
    public void beforeExit() {
        Node me = nodeInfo.getMe();
        List<Node> nodeList = nodeInfo.getNodeList();
        // 清空缓存
        try(Jedis jedis = jedisPool.getResource()){
            jedis.flushAll();
        }
        if (nodeList.size() == 1) {
            // 节点退出时，删除数据表
            System.out.println("准备删除数据表...");
            dataBase.deleteTable(me.getID());
            System.out.println("成功删除数据表【node" + me.getID() + "】");
            System.out.println("【系统提示】- 节点 " + me.getID() + " 已经退出DHT网络");
            System.out.println("【系统提示】- 网络已关闭");
        } else {
            System.out.println("准备删除数据表,并更新其他节点路由表...");
            FingerTable[] finger = nodeInfo.getFinger();
            Node pred = nodeInfo.getPred();
            // 数据迁移到后继节点
            dataBase.transferAll(me.getID(), finger[1].getSuccessor().getID());
            Message request = new Message();
            request.setInitNode_flag(1);
            if (nodeList.size() == 2)
                request.setInitInfo("quitOfTwoNodes/");
            else
                request.setInitInfo("quitOfManyNodes/" + pred.getID() + "/" + pred.getIP() + "/" + pred.getPort());
            makeConnection.makeConnectionByObject(finger[1].getSuccessor().getIP(), finger[1].getSuccessor().getPort(), request);
            // 删除数据表
            dataBase.deleteTable(me.getID());
            nodeList.remove(me);

            System.out.println("成功删除数据表【node" + me.getID() + "】");
            System.out.println("【系统提示】- 节点 " + me.getID() + " 已经退出DHT网络");
        }
    }
}
