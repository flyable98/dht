package bupt.fnl.dht.service.impl;

import bupt.fnl.dht.domain.FingerTable;
import bupt.fnl.dht.domain.Identity;
import bupt.fnl.dht.domain.Node;
import bupt.fnl.dht.domain.NodeInfo;
import bupt.fnl.dht.service.Print;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service("print")
public class PrintImpl implements Print {

    @Autowired
    NodeInfo nodeInfo;


    private int genOrder(String ip, String port, int NodeID){
       int result =  Math.abs(3* ip.hashCode() +  4 * port.hashCode() + 5* NodeID)/25;
       return result;

    }
    // 打印节点信息
    public void printNodeList(){
        List<Node> nodeList = nodeInfo.getNodeList();
        if (nodeList == null || nodeList.size() == 0) {
            System.out.println("列表为空！");
            return;
        }
        Iterator<Node> iterator = nodeList.iterator();
        int i = genOrder(nodeInfo.getMyIP(), nodeInfo.getMyPort(), nodeInfo.getMyID());
        System.out.println("*****节点列表*****");
        System.out.println("{");
        System.out.println("局部域" + nodeInfo.getDomainName() + "中边界节点节点ID: " + nodeInfo.getMyID());
        while(iterator.hasNext()) {
            Node node = iterator.next();
            System.out.println("node" + node.getID() + ":{");
            System.out.println("\"Latitude\"：" + " \"" + node.getLatitude() + "\"," );
            System.out.println("\"Longtitude\"：" + " \"" + node.getLongitude() + "\"," );
            System.out.println("\"City\"：" + " \"" + LatToCity.get(node.getLatitude())+ "\"," );
            System.out.println("\"NodeNums\"：" + " \"" + node.getIdNums() + "\"" );
            if(iterator.hasNext()) {
                System.out.println("},");
            }else{
                System.out.println("}");
                System.out.println("}");
            }


        }
        System.out.println();
    }


    private static final Map<String, String > LatToCity;
    static
    {
        LatToCity = new HashMap<String , String >();
        LatToCity.put("40.22", "北京");
        LatToCity.put("31.33", "南京");
        LatToCity.put("30.58",  "武汉");
        LatToCity.put("29.40", "重庆");
        LatToCity.put("22.33",  "深圳");
        LatToCity.put("32.20", "无锡");
    }

    //打印结果

    // 打印路由表信息
    public void printFingerInfo(){
        FingerTable[] finger = nodeInfo.getFinger();
        int m = nodeInfo.getM();
        System.out.println("*****路由表信息*****");
        for (int i = 1; i <= m; i++) {
            System.out.println("Index[" + finger[i].getStart() + "]       " + "后继节点ID: " + finger[i].getSuccessor().getID());
        }
        System.out.println();
    }

    // 打印节点信息
    public void printNodeInfo(){
        List<Node> nodeList = nodeInfo.getNodeList();
        if (nodeList == null || nodeList.size() == 0) {
            System.out.println("列表为空！");
            return;
        }
        Iterator<Node> iterator = nodeList.iterator();
        System.out.println("*****节点列表*****");
        while(iterator.hasNext()) {
            Node node = iterator.next();
            String string = "节点ID: " + node.getID() + "， IP地址: " + node.getIP() + "， 端口号: " + node.getPort() + "， 纬度： " + node.getLatitude() + "，经度：" + node.getLongitude() ;
            System.out.println(string);
        }
        System.out.println();
    }

    //打印节点所有标识
    public void printNodeId(Node node){
        List<Identity> identityList = nodeInfo.getIdentityList();
        if(identityList == null || identityList.size() == 0){
            System.out.println("列表为空！");
            return;
        }
        Iterator<Identity> iterator = identityList.iterator();
        System.out.println("**********标识列表*************");
        System.out.println("节点ID：" + node.getID() + "所存储的所有标识如下：");
        while (iterator.hasNext()){
            Identity identity = iterator.next();
            String string = "标识：" + identity.getIdentifier() + ", 标识映射数据：" + identity.getMappingData() + "， 标识哈希："+ identity.getHash();
            System.out.println(string);
        }
        return;
    }
}
