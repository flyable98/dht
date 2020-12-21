package bupt.fnl.dht.domain;

import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.List;


/**
 * Message：传输信息对应的实体类
 *【注意】控制组件和DHT节点中的Message类所在的目录名必须相同
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 3288400331417976843L;

    // 节点配置标志位（设置为 1 则是传输节点的初始化信息）
    private int status = 1;

    private int initNode_flag;
    // 节点配置信息
    private String initInfo;

    // 发送者前缀
    private String orgName;
    // 私钥加密后的信息
    private String cipherText;
    // 公钥解密后的信息
    private String plainText;
//
//    private JSONObject Nodejson;

    // 五种类型：getNodeList, register, delete, update, resolve
    private int type;

    //跨域解析标识符，默认为false，非跨域解析
    private boolean crossDomain_flag = false;
    //判断消息请求是否在可解析域
    private boolean inRightDomain_flag = true;
    //通知缓存标识被更新
    private boolean isUpdate_flag = false;

    private boolean isForward = false;

//    public JSONObject getNodejson() {
//        return Nodejson;
//    }
//
//    public void setNodejson(JSONObject nodejson) {
//        Nodejson = nodejson;
//    }

    public boolean isForward() {
        return isForward;
    }

    public void setForward(boolean forward) {
        isForward = forward;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isUpdate_flag() {
        return isUpdate_flag;
    }

    public void setUpdate_flag(boolean update_flag) {
        isUpdate_flag = update_flag;
    }

    public boolean isInRightDomain_flag() {
        return inRightDomain_flag;
    }

    public void setInRightDomain_flag(boolean inRightDomain_flag) {
        this.inRightDomain_flag = inRightDomain_flag;
    }

    public boolean isCrossDomain_flag() {
        return crossDomain_flag;
    }

    public void setCrossDomain_flag(boolean crossDomain_flag) {
        this.crossDomain_flag = crossDomain_flag;
    }

    // 标识
    private String identity;
    // 映射数据
    private String mappingData;
    // dht节点信息
    private Node[] nodeList;
    // 反馈信息
    private String feedback;

    private int nodeID;
    private int bNodeID;
    private String DomainID;
    private String Latitude;
    private String Longtitude;
    private String City;
    private int IdNums;

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongtitude() {
        return Longtitude;
    }

    public void setLongtitude(String longtitude) {
        Longtitude = longtitude;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public int getIdNums() {
        return IdNums;
    }

    public void setIdNums(int idNums) {
        IdNums = idNums;
    }

    public int getbNodeID() {
        return bNodeID;
    }

    public void setbNodeID(int bNodeID) {
        this.bNodeID = bNodeID;
    }

    public String  getDomainID() {
        return DomainID;
    }

    public void setDomainID(String domainID) {
        DomainID = domainID;
    }

    private List<String> Inforamation;
    private List<List<String>> DHTInformation;

    public Message() {
    }

    public Message(int nodeID, List<String> inforamation) {
        this.nodeID = nodeID;
        Inforamation = inforamation;
    }

    public Message(int nodeID, List<String> inforamation, List<List<String>> DHTInformation) {
        this.nodeID = nodeID;
        Inforamation = inforamation;
        this.DHTInformation = DHTInformation;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public List<String> getInforamation() {
        return Inforamation;
    }

    public void setInforamation(List<String> inforamation) {
        Inforamation = inforamation;
    }

    public List<List<String>> getDHTInformation() {
        return DHTInformation;
    }

    public void setDHTInformation(List<List<String>> DHTInformation) {
        this.DHTInformation = DHTInformation;
    }
    public void setInitNode_flag(int initNode_flag){
        this.initNode_flag = initNode_flag;
    }
    public void setInitInfo(String initInfo){
        this.initInfo = initInfo;
    }
    public void setType(int type) {
        this.type = type;
    }
    public void setIdentity(String identity) {
        this.identity = identity;
    }
    public void setMappingData(String mappingData) {
        this.mappingData = mappingData;
    }
    public void setNodeList(Node[] nodeList) {
        this.nodeList = nodeList;
    }
    public void setFeedback(String feedback){
        this.feedback = feedback;
    }

    public int getInitNode_flag(){
        return initNode_flag;
    }
    public String getInitInfo(){
        return initInfo;
    }
    public int getType(){
        return type;
    }
    public String getIdentity(){
        return identity;
    }
    public String getMappingData(){
        return mappingData;
    }
    public Node[] getNodeList(){
        return nodeList;
    }
    public String getFeedback(){
        return feedback;
    }

    public String getCipherText() {
        return cipherText;
    }
    public void setCipherText(String cipherText) {
        this.cipherText = cipherText;
    }
    public String getPlainText() {
        return plainText;
    }
    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }
    public String getOrgName() {
        return orgName;
    }
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
}

