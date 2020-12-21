package bupt.fnl.dht.runnable;

import bupt.fnl.dht.domain.Message;
import bupt.fnl.dht.network.MakeCrossDomainConnection;
import bupt.fnl.dht.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketThread implements Runnable {

    private Socket connection;
    private IdentityService identityService;

    public SocketThread(Socket connection, IdentityService identityService) {
        this.connection = connection;
        this.identityService = identityService;
    }

    @Override
    public void run() {
        try (
                // 【注意】对于Object IO流，要先创建输出流对象，再创建输入流对象，不然程序会死锁
                ObjectOutputStream outToOtherNodes = new ObjectOutputStream(connection.getOutputStream());
                ObjectInputStream inFromOtherNodes = new ObjectInputStream(connection.getInputStream()))
        {
            Message received_message = (Message)inFromOtherNodes.readObject(); // 阻塞
            /* 收到Web消息后进行一系列权限校验 */
            if(received_message.isCrossDomain_flag()!=true && received_message.isInRightDomain_flag()) {
                outToOtherNodes.writeObject(identityService.authentication(received_message));

            }else if(received_message.isCrossDomain_flag() == true && received_message.isInRightDomain_flag()){
                String identity = received_message.getIdentity();
                System.out.println("【系统提示】- 收到标识 " + identity + " 的解析请求...");
                System.out.println("此解析请求为跨域解析，本局部域将请求转发给标识所在的局部域.........");
                //全局域IP
                String ip="47.95.216.60";
//                全局域边界节点端口
                String port="10211";
                received_message.setInRightDomain_flag(false);
                System.out.println("妆发给全局域 ip + port:" + ip + ", " +port);
                Message m = new MakeCrossDomainConnection().makeConnectionByObject(ip, port,received_message);
                String result = m.getMappingData();
                System.out.println(identity+ "解析结果为：" + result);
                received_message.setMappingData(result);
                received_message.setFeedback("标识解析成功！");
                outToOtherNodes.writeObject(received_message);
            }else if(received_message.isCrossDomain_flag()==true && received_message.isInRightDomain_flag() !=true){
                outToOtherNodes.writeObject(identityService.authentication(received_message));
            }
//            else if(received_message.getType() == 9){
//                outToOtherNodes.writeObject(identityService.authentication2(received_message));
//            }
            connection.close(); // 释放连接

        } catch (Exception e) {
            System.out.println("【系统提示】- "+"线程无法服务连接");
            e.printStackTrace();
        }
    }
}
