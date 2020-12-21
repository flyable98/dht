package bupt.fnl.dht.network;

import bupt.fnl.dht.domain.Message;
import bupt.fnl.dht.domain.NodeInfo;
import bupt.fnl.dht.service.FingerService;
import bupt.fnl.dht.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
@Service
public class MakeCrossDomainConnection {
    @Autowired
    NodeInfo nodeInfo;
    @Autowired
    NodeService nodeService;
    @Autowired
    FingerService fingerService;
    // 节点间通过序列化对象Object来传输数据
    public Message makeConnectionByObject(String ip, String port, Message message) {
        /**
         * 改成,直接传输message,传输给已知的全局域节点
         * */
        try(
                Socket sendingSocket = new Socket(ip,Integer.parseInt(port));
                // 【注意】对于Object IO流，要先创建输出流对象，再创建输入流对象，不然程序会死锁
                ObjectOutputStream outToOtherNodes = new ObjectOutputStream(sendingSocket.getOutputStream());
                ObjectInputStream inFromOtherNodes = new ObjectInputStream(sendingSocket.getInputStream()))
        {
            outToOtherNodes.writeObject(message);

            return (Message)inFromOtherNodes.readObject();

        } catch (IOException | ClassNotFoundException e){
            System.out.println("Socket建立连接失败！");
            return null;
        }

    }

}
