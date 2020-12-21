package bupt.fnl.dht.service.impl;

import bupt.fnl.dht.domain.*;

import bupt.fnl.dht.network.MakeConnection;
import bupt.fnl.dht.dao.DataBase;
import bupt.fnl.dht.network.MakeCrossDomainConnection;
import bupt.fnl.dht.service.IdentityService;
import bupt.fnl.dht.service.NodeService;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
//import org.app.chaincode.invocation.InvokeChaincode;
//import org.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

import static bupt.fnl.dht.utils.Decryption.decrypt;
import static bupt.fnl.dht.utils.Decryption.digest;
import static bupt.fnl.dht.utils.Hash.HashFunc;

/**
 * 标识的业务层逻辑处理：
 *      1、权限校验
 *      2、缓存判断
 */
@Service("identityService")
public class IdentityServiceImpl implements IdentityService {
    @Autowired
    NodeInfo nodeInfo;
    @Autowired
    NodeService nodeService;
    @Autowired
    MakeConnection makeConnection;
    @Autowired
    MakeCrossDomainConnection makeCrossDomainConnection;
    @Autowired
    DataBase dataBase;
    @Autowired
    JedisPool jedisPool;

    private static final Map<String, String > LatToCity;
    static
    {
        LatToCity = new HashMap<String, String >();
        LatToCity.put("40.22", "北京");
        LatToCity.put("31.33", "南京");
        LatToCity.put("30.58",  "武汉");
        LatToCity.put("29.40", "重庆");
//        JingWei.put("36.07", "120.33");
//        JingWei.put("43.88", "125.35");
        LatToCity.put("22.33",  "深圳");
        LatToCity.put("32.20", "无锡");
    }
    private int genOrder(String ip, String port, int NodeID){
            int result =  Math.abs(3* ip.hashCode() +  4 * port.hashCode() + 5* NodeID)/25;
            return result;

    }

    private boolean isUpdate;

    private boolean isForward;

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    /**
     * 收到消息后进行一系列权限校验
     * @param received_message
     * @return
     */
    public Message authentication(Message received_message) {

        Message response_message = received_message;
        //System.out.println("收到消息的内容为："+ received_message.toString()+":"+received_message.getMappingData());
        /**
         * flag == 1，是节点初始化配置指令，来自其他dht节点
         */
        if(received_message.isCrossDomain_flag()){
            System.out.println("此解析请求为跨域解析，本局部域将请求转发给标识所在的局部域.........");
            /* 若是解析请求，直接走缓存 */
            if (received_message.getType() == 1) {
                // 从pool中获取jedis连接
                try (Jedis jedis = jedisPool.getResource()) {
                    String identity = received_message.getIdentity();
                    System.out.println("【系统提示】- 收到标识 " + identity + " 的解析请求...");
                    String mappingData = jedis.get(identity);
                    // 缓存命中，直接返回
                    if (null != mappingData && isUpdate == false) {
                        response_message.setMappingData(mappingData);
                        System.out.println("标识解析成功！（ 通过缓存读取 ）");
                        response_message.setFeedback("标识解析成功！（ 通过缓存读取 ）");
                    }
                    // 没有缓存，则从数据库读取
                    else {
                        response_message = resolveIdentity(received_message);
                        mappingData = response_message.getMappingData();
                        // 将映射数据加入缓存
                        jedis.set(identity, mappingData);
                    }
                }
            }
//            String ip="10.108.144.21";
//            String port="9999";
//            received_message.setCrossDomain_flag(false);
//            Message message2 = new Message();
//            message2.setIdentity(received_message.getIdentity());
//            message2.setType(received_message.getType());
//            Message m = new MakeConnection().makeConnectionByObject(ip,port,message2);
//            String result=m.getMappingData();
//            System.out.println(result);
        }
        else {
            if (received_message.getInitNode_flag() == 1) {
                String response_initInfo = makeConnection.considerInput(received_message.getInitInfo());
                response_message.setInitInfo(response_initInfo);
            }
            /**
             * 接收Web后台发送的received_message，可能包括：
             * 发送者前缀：orgName  操作类型：type  标识：identity  密文：cipherText
             */
            else {
                /* 若是解析请求，直接走缓存 */
                if (received_message.getType() == 1) {
                    // 从pool中获取jedis连接
                    try (Jedis jedis = jedisPool.getResource()) {
                        String identity = received_message.getIdentity();
                        System.out.println("【系统提示】- 收到标识 " + identity + " 的解析请求...");
                        String mappingData = jedis.get(identity);
                        // 缓存命中，直接返回
                        if (null != mappingData && isUpdate() == false) {
                            response_message.setMappingData(mappingData);
                            System.out.println("标识解析成功！通过缓存读取 " + identity + "的解析结果为：" + mappingData);
                            response_message.setFeedback("标识解析成功！通过缓存读取 " + identity + "的解析结果为：" + mappingData);
                        }
                        // 没有缓存，则从数据库读取
                        else {
                            response_message = resolveIdentity(received_message);
                            if(received_message.getStatus()==1) {
                                mappingData = response_message.getMappingData();
                                // 将映射数据加入缓存
                                jedis.set(identity, mappingData);
                            }else {
                                System.out.println("查询失败！");
                            }

                        }
                    }
                }
                /* 其他类型请求（增、删、改），需要鉴权流程 */
                else {
                    // 查询区块链里是否存有发送者的前缀
                    String orgName = received_message.getOrgName();
                    int bNode = received_message.getNodeID();
                    System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&"+ bNode);
                    //JSONArray jsonArrayFromBlockChain = QueryAuthority.query(orgName);
                    System.out.println();
                    boolean flag = false;
                    if (false) {
                        response_message.setFeedback("该企业未注册，没有操作权限！");
                        System.out.println("该企业未注册，没有操作权限！");
                    } else {
                        System.out.println("成功读取企业" + orgName + "注册信息!");
                        /*
                         * 控制模块，逻辑判断
                         */
                        // 从区块链获取公钥
//                    String publicKey = jsonArrayFromBlockChain.getJSONObject(0).getJSONObject("Record").getString("public_key");
                        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzITPLCPScoMO14gKgUsiHGcWwY0Q94eFzFARBXHsKv5zMxDq1jvM7TzpiauzL4Ig8/w+FvaHVhwUpYUjySk+i777BfH71HruAVTuKnG2wkZT15nAn3biwevNCCewCV0Zj1HFghNZc/Gmo7KL7tAWYpvikOoZByp/XyYJUwXxeL92etV5+7ZEG5FKDjh9BuSoD1PaapeyeF6HZOW4JOgpoA/9GD4dya5PsJuV8Ddfk+HCi8jjj2x4EUPTWg16nkz4fQWvbyISZD7JgQTVskyz0pkDYkuRPlnBW11pQ5zh02qPSYVvnHIpd9EztXOytsqk6c4VEho/45NZFiEJNA096wIDAQAB";
                        // 公钥解密，获得明文
//                    String cipherText = received_message.getCipherText(); // 密文
                        String cipherText = received_message.getCipherText(); // 密文
                        String plainText = null; // 明文-String格式
                        String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDMhM8sI9Jygw7X" +
                                "iAqBSyIcZxbBjRD3h4XMUBEFcewq/nMzEOrWO8ztPOmJq7MvgiDz/D4W9odWHBSl" +
                                "hSPJKT6LvvsF8fvUeu4BVO4qcbbCRlPXmcCfduLB680IJ7AJXRmPUcWCE1lz8aaj" +
                                "sovu0BZim+KQ6hkHKn9fJglTBfF4v3Z61Xn7tkQbkUoOOH0G5KgPU9pql7J4Xodk" +
                                "5bgk6CmgD/0YPh3Jrk+wm5XwN1+T4cKLyOOPbHgRQ9NaDXqeTPh9Ba9vIhJkPsmB" +
                                "BNWyTLPSmQNiS5E+WcFbXWlDnOHTao9JhW+ccil30TO1c7K2yqTpzhUSGj/jk1kW" +
                                "IQk0DT3rAgMBAAECggEAA0+fHwLRdGMkyV59dQxnV/hfSPDktm0uQFmHfQQUI7oM" +
                                "2WXLt34uWiTjTwRFmV5M9EZAJxUEqeM4flmCc85EIfUMkVMSlaUL11+tn2hJ4ilL" +
                                "UDFAChTdpPARWLFzyyMDt+tF/E4d45+k5/+K/mnGAwCtEVWI5DHO8BZojjyJZUAE" +
                                "3VZ92EYjc41mcMzf9iCqCHSMKYVHjs9o8xoV4ogWEI1sjtg34ya5cLr5YvMlyb1p" +
                                "yTyBkqffLa2yMraOcd9m6wWvUR3L9iGMIfTV78j1EHTaYgraVo1QUy0WcGQf8Cb5" +
                                "zMDqYcDZ9+ApznLSgKXGGJGfdbxTG1N9aZT2MmU7MQKBgQDlvgEpu+yp8iJYvcsV" +
                                "RzC0isypvLlJldal2Emyf2P6yW1icT0r2nX1dcZjVspLw1+Ys15qxf5fKGE78FZq" +
                                "98gZ/37UiHfVkhVydCyIjCiBnUal7OMfLV0ndwSTbX2FXjw8IKKmMGQMXi/cyldx" +
                                "0tlOrPF+YTSHTRpTFTxYLxU+uQKBgQDj5MvTFJoizUyurAnQOShznAicCHvyoplP" +
                                "sQjX5TjCWUrAOj1Uh5AJSmqF3CHcWMi7bEBLJUBLNG/FZieznlo2dLH2dILjzi2e" +
                                "lt2ip354zdTv8SDvMaTN5AGVijlXFSvscF+89JB5GCaakiq+AJatQf8VwpAaoekK" +
                                "kg4BCe6vwwKBgQCotfpmqnFmb8DXSDEdpBTZUGBxeXzb0+Q4D/g206QZI9hnBV91" +
                                "l80t/1o70x1lu8i0+2unn1Rojt+ww5LCpMlWhjCeZMUTml6TmUqmz75jSJr7+FTl" +
                                "rUuOUrGSjkIyMXysbw7iWgDusDAXxKOom70nMIt7UmjfvzhIsPyKibDhSQKBgQCB" +
                                "a1OdQdvu26wqniTMjUk9rmTtR5wsRM7QBPfCs7gyFsdutyRRNtNDk3E/J65LFliY" +
                                "p3cztan3i4XWEpeFV+5fcpIGJlCW0mXx5Ddlwbz/GdVNliBf9k9jZZLIu8Cohat2" +
                                "ELMt+a16N47kxRFk6ayoJAya7O0tUrmneR5e1KcqRQKBgBnpzwg5jiM6JbZwMDCu" +
                                "2MtXr38aNNK+wZzTbHuSUMF2DMOmvFsODaToIJEvFx8q5LwEa5qLWCzYkUrfGmnr" +
                                "VS8p7vC9d6AMAdD0T+5CcKAhqkjv0MASAs4u6etq/hHnAT4fFItAmbdqqUD4yrUt" +
                                "eSFjYrqvxKGWkLKoJ3I0yvV0";
                        try {
                            plainText = decrypt(cipherText, privateKey);
                        } catch (Exception e) {
                            System.out.println("解密失败！");
                            ;
                        }
                        //JSONObject plainTextJson = JSONObject.fromObject(plainText); // 明文-Json格式
                        // 设置message的值
                        //response_message.setPlainText(plainText);
//                    response_message.setIdentity(plainTextJson.getString("identity"));
                        //                   System.out.println("收到消息的内容为："+ received_message.toString()+":"+received_message.getMappingData());
                        //                  System.out.println(plainTextJson.getString("mappingData"));
                        response_message.setMappingData(received_message.getMappingData());

                        // 遍历所有前缀并对照
//                        String[] tokens = response_message.getIdentity().split("/");
//                        String prefix = tokens[0];
//                        String authority = "";
                        //  for (int i = 0; i < jsonArrayFromBlockChain.size(); i++) {
//                        JSONObject tmpJson = jsonArrayFromBlockChain.getJSONObject(i).getJSONObject("Record");
//                        if (tmpJson.getString("identity_prefix").equals(prefix)) {
//                            authority = tmpJson.getString("authority");
//                            break;
//                        }
                    }
//                    if (authority.equals("")) {
//                        response_message.setFeedback("该标识前缀 "+prefix+" 未分配！");
//                        System.out.println("该标识前缀 "+prefix+" 未分配！");
//                    } else {
//                        // 比较操作权限
//                        int type = received_message.getType();
//                        if ((Integer.parseInt(authority) & type) == type) {
//                            // 验证成功
//                            // 可以进行下一步的增删改查操作

                    response_message = considerType(received_message);

                    //                        } else {
//                            response_message.setFeedback("对该前缀没有足够的操作权限！");
//                            System.out.println("对该前缀没有足够的操作权限！");
//                        }
                }
            }
//            return response_message;
//            }
//        }
        }
        return response_message;
    }



    /* 对于不同的操作类型，返回不同的 Message 对象 */
    public Message considerType(Message message){
        Message result = message;
        switch (message.getType()) {
//            case "getNodeList":
//                result.setNodeList(getNodeList());
//                result.setFeedback("成功获取所有节点信息！");
//                break;
            case 8:
                try {
                    result = registerIdentity(message);
                } catch (Exception e) {
                    System.out.println("注册失败！");
                }
                break;
            case 4:
                try {
                    result = deleteIdentity(message);
                } catch (Exception e) {
                    System.out.println("删除失败！");
                }
                break;
            case 2:
                try {
                    result = updateIdentity(message);
                } catch (Exception e) {
                    System.out.println("更新失败！");
                }
                break;
            case 1:
                try {
                    result = resolveIdentity(message);
                } catch (Exception e) {
                    System.out.println("解析失败！");
                }
                break;

            case 0:
                try {
//                    System.out.println("到case 0了");
                    result = findAllId(message);
                }catch (Exception e){
                    System.out.println("查询所有标识信息失败！");
                }
                break;
            case 9:
                try {
//                    System.out.println("到case 9了");
                    result = printNodeList(message);
                }catch (Exception e){
                    System.out.println("查询所有节点信息失败！");
                }
                break;


        }
        return result;
    }

    /* 增 */
    public Message registerIdentity(Message message) {
        String identity = message.getIdentity();
//        System.out.println("收到标识"+ identity+"的注册信息：");
        String mappingData = message.getMappingData();
        setUpdate(message.isUpdate_flag());
//        System.out.println("该标识的映射数据为："+mappingData+".");
        System.out.println("【系统提示】- 有新标识 "+identity+" 请求注册...");
        int kid;
        if(!message.isForward()) {
           kid = HashFunc(identity, nodeInfo.getNumDHT()) * 976;//标识的哈希

        } else{
            kid = HashFunc(identity, nodeInfo.getNumDHT())* 1234;
        }
        Node targetNode = nodeService.find_successor(kid);//应该存储的位置
        Node me = nodeInfo.getMe();
        message.setNodeID(nodeInfo.getMyID());
        if (targetNode.getID() == me.getID()) {
            // 判断标识是否已被注册
            if (dataBase.ifExist(me.getID(), identity)) {
                System.out.println("当前标识已被注册！");
                message.setStatus(0);
                message.setFeedback("当前标识已被注册！");
            } else {
                // 添加到本地节点数据库列表
                dataBase.registerData(me.getID(), kid, identity, mappingData);
                System.out.println("标识映射 " + identity + "->" + mappingData + " 已存入本地数据库");
                message.setStatus(1);
                // 将标识、映射数据hash写入区块链
//                String mappingDataHash = digest(mappingData);
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("identifier",identity);
//                jsonObject.put("mappingData_hash",mappingDataHash);
//                new InvokeHash("写入").invoke(jsonObject);
                me.setIdNums(me.getIdNums() + 1);


                message.setFeedback("标识 " + identity + " 注册成功！");
            }
            return message;
        } else {
            System.out.println("注册请求已经转发至节点 " + targetNode.getID());
            message.setForward(true);
            return makeConnection.makeConnectionByObject(targetNode.getIP(), targetNode.getPort(), message);
        }
    }
    /* 删 */
    public Message deleteIdentity(Message message) {
        String identity = message.getIdentity();
        setUpdate(message.isUpdate_flag());
        System.out.println("【系统提示】- 收到标识 "+identity+" 的删除请求...");
        int kid = HashFunc(identity, nodeInfo.getNumDHT());//标识的哈希
        Node targetNode = nodeService.find_successor(kid);//应该存储的位置

        Node me = nodeInfo.getMe();
        message.setNodeID(nodeInfo.getMyID());
        if (targetNode.getID() == me.getID()) {
            // 判断预删除标识是否存在
            if (!dataBase.ifExist(me.getID(), identity)) {
                System.out.println("该标识还未注册，无法删除！");
                message.setStatus(0);
                message.setFeedback("该标识还未注册，无法删除！");
            } else {
                // 删除本地数据库的标识及映射数据
                dataBase.deleteData(me.getID(), identity);
                System.out.println("标识 " + identity + " 删除成功！");
                me.setIdNums(me.getIdNums() - 1);
                message.setStatus(1);
                // 删除区块链状态数据库中标识的映射数据hash
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("identifier",identity);
//                jsonObject.put("mappingData_hash",null); // 【注意】这里设为null会有异常，未解决
////                new InvokeHash("删除").invoke(jsonObject);

                message.setFeedback("标识 " + identity + " 删除成功！");
            }
            return message;
        } else {
            System.out.println("删除请求已经转发至节点 " + targetNode.getID());
            return makeConnection.makeConnectionByObject(targetNode.getIP(), targetNode.getPort(), message);
        }
    }
    /* 改 */
    public Message updateIdentity(Message message) {
        String identity = message.getIdentity();
        String mappingData = message.getMappingData();
        setUpdate(message.isUpdate_flag());
        System.out.println("Update_flag:" + isUpdate());
        System.out.println("【系统提示】- 标识 "+identity+" 请求更新映射数据...");
        int kid = HashFunc(identity, nodeInfo.getNumDHT());//标识的哈希
        Node targetNode = nodeService.find_successor(kid);//应该存储的位置
        Node me = nodeInfo.getMe();
        message.setNodeID(nodeInfo.getMyID());
        if (targetNode.getID() == me.getID()) {
            // 判断标识是否已被注册
            if (!dataBase.ifExist(me.getID(), identity)) {
                System.out.println("该标识还未注册，无法更新！");
                message.setStatus(0);
                message.setFeedback("该标识还未注册，无法更新！");
            } else {
                // 更新本地节点数据库列表
                dataBase.updateData(me.getID(), identity, mappingData);
                System.out.println("标识映射 " + identity + "->" + mappingData + " 已更新至本地数据库");
                message.setStatus(1);
                // 更新区块链状态数据库中标识的映射数据hash
//                String mappingDataHash = mappingData;
////                JSONObject jsonObject = new JSONObject();
////                jsonObject.put("identifier",identity);
////                jsonObject.put("mappingData_hash",mappingDataHash);
//                new InvokeHash("更新").invoke(jsonObject);

                message.setFeedback("标识 " + identity + " 更新映射数据成功！");
            }
            return message;
        } else {
            System.out.println("更新请求已经转发至节点 " + targetNode.getID());
            return makeConnection.makeConnectionByObject(targetNode.getIP(), targetNode.getPort(), message);
        }
    }



    /* 查 */
    public Message resolveIdentity(Message message) {
        String identity = message.getIdentity();
        System.out.println("【系统提示】- 收到标识 "+identity+" 的解析请求...");
        Node targetNode = nodeService.find_successor(HashFunc(identity, nodeInfo.getNumDHT()));
        Node me = nodeInfo.getMe();
        message.setNodeID(nodeInfo.getMyID());
        if (targetNode.getID() == me.getID()) {
            // 从本地数据库获取内容
//            System.out.println((resolveData("select *" + " from node" + me.getID() + " where Identity='" + identity + "';"))
//                    .replaceAll("#", "\n"));
            String result = dataBase.resolveData(me.getID(), identity);
            if (result.equals("数据库中未找到该表示！")) {
                System.out.println("该标识不存在！");
                message.setStatus(0);
                message.setFeedback("该标识不存在！");
            } else {
                System.out.println("标识解析成功！正在进行防篡改校验...");
//                String mappingDataHash = QueryHash.query(identity);
//                if (digest(result).equals(mappingDataHash)) {
//                    System.out.println("经验证，解析结果未被篡改");
//                    // message写入解析结果
                message.setMappingData(result);
                message.setStatus(1);
                message.setFeedback("标识解析成功！经验证未被篡改");
//                } else {
//                    System.out.println("【注意！！】解析结果已被篡改！");
//                    message.setFeedback("【注意！！】解析结果已被篡改！");
//                }
            }
            System.out.println(identity+ "解析结果为：" + message.getMappingData());
            return message;
        } else {
            System.out.println("解析请求已经转发至节点 " + targetNode.getID());
            // 从其他节点数据库获取内容
//            System.out.println(makeConnection(targetNode.getIP(), targetNode.getPort(), "geturl/" + identity)
//                    .replaceAll("#", "\n"));
            return makeConnection.makeConnectionByObject(targetNode.getIP(), targetNode.getPort(),message);
        }
    }

    public Message printNodeList(Message message) {
        Node me = nodeInfo.getMe();
        message.setNodeID(me.getID());
        List<Node> nodeList = nodeInfo.getNodeList();
        if (nodeList == null || nodeList.size() == 0) {
            System.out.println("列表为空！");
            return null;
        }

//                "局部域\"" + i + "\"中边界节点节点ID:\"" + nodeInfo.getMyID() + "\"\n";
//        while (iterator.hasNext()) {
        Iterator<Node> iterator = nodeList.iterator();
        Node node = iterator.next();

        message.setLongtitude(node.getLongitude());
        message.setLatitude(node.getLatitude());
        message.setCity(LatToCity.get(node.getLatitude()));
        message.setIdNums(node.getIdNums());
//        System.out.println("局部域" + nodeInfo.getDomainName() + "已创建。");
        message.setDomainID(nodeInfo.getDomainID());
        message.setbNodeID(nodeInfo.getbNodeID());

        //            node_List.setID(node.getID());
//            node_List.setIdNums(node.getIdNums());
//            node_List.setLatitude(node.getLatitude());
//            node_List.setLongtitude(node.getLongitude());
//            node_List.setCity(LatToCity.get(node.getLatitude()));
        Iterator<Node> iterator2 = nodeList.iterator();
        String result = "";
        result = result +
                "{";
//                "局部域\"" + i + "\"中边界节点节点ID:\"" + nodeInfo.getMyID() + "\"\n";

//        List<Node> nodeList = nodeInfo.getNodeList();
//        if (nodeList == null || nodeList.size() == 0) {
//            System.out.println("列表为空！");
//            return;
//        }
//        Iterator<Node> iterator = nodeList.iterator();
//        System.out.println("*****节点列表*****");
//        while(iterator.hasNext()) {
//            Node node = iterator.next();
//            String string = "节点ID: " + node.getID() + "， IP地址: " + node.getIP() + "， 端口号: " + node.getPort() + "， 纬度： " + node.getLatitude() + "，经度：" + node.getLongitude() ;
//            System.out.println(string);
//        }
        while (iterator2.hasNext()) {
            Node node2 = iterator2.next();
            result = result + "\"node" + node2.getID() + "\":{" +
                    "\"latitude\":" + " \"" + node2.getLatitude() + "\"," +
                    "\"longtitude\":" + " \"" + node2.getLongitude() + "\"," +
                    "\"city\":" + " \"" + LatToCity.get(node2.getLatitude()) + "\"," +
                    "\"idNums\":" + " \"" + node2.getIdNums() + "\"";
            if (iterator2.hasNext()) {
                result += "},";
            } else {
                result += "}";
                result += "}";
            }
        }

        message.setFeedback(result);
        return message;
    }


    //    查指定节点的所有标识
    public Message findAllId(Message message){
        Node me = nodeInfo.getMe();
        Message Message = new Message();
        Message.setNodeID(me.getID());
        if(nodeInfo.getNodeList().size() == 1) {
            Message.setInforamation(dataBase.listAllIdentity(me.getID()));
            System.out.println(dataBase.listAllIdentity(me.getID()));
            return Message;
        }else {
            List<List<String>> result = new LinkedList<>();
            List<Node> nodeList = nodeInfo.getNodeList();
            for(int i = 0; i < nodeInfo.getNodeList().size();i++) {
                result.add(dataBase.listAllIdentity(nodeList.get(i).getID()));
                System.out.println(dataBase.listAllIdentity(nodeList.get(i).getID()));
                System.out.println(result.get(i));
            }

//            List<Weather> weatherList=new ArraryList<Weather>;
//
//            String json=JSON.toJSONString(weatherList);
//            message.setNodejson(JSONObject.fromObject(result));
            Message.setDHTInformation(result);
            return Message;
        }

    }

}
