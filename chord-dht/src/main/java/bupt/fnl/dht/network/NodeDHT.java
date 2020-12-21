package bupt.fnl.dht.network;

import bupt.fnl.dht.config.Config;
import bupt.fnl.dht.domain.NodeInfo;
import bupt.fnl.dht.runnable.CommandThread;
import bupt.fnl.dht.runnable.SocketThread;
import bupt.fnl.dht.service.FingerService;
import bupt.fnl.dht.service.IdentityService;
import bupt.fnl.dht.service.NodeService;
import bupt.fnl.dht.service.Print;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 入口类，用于搭建dht网络
 */
public class NodeDHT {

    /**
     * -----【 Main 函数 】-----
     * Spring在启动时会在主程序中注入bean，但是线程类是不会注入，只能通过getBean获取
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {
        // Spring容器
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class); // 基于注解配置
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("bean.xml"); // 基于xml配置

        // 节点的各种信息
        NodeInfo nodeInfo = (NodeInfo) context.getBean("nodeInfo");
        // 用于操作节点的类
        NodeService nodeService = (NodeService) context.getBean("nodeService");
        // 用于操作路由表的类
        FingerService fingerService = (FingerService) context.getBean("fingerService");
        // 用于权限校验和增删改查的类
        IdentityService identityService = (IdentityService) context.getBean("identityService");
        // 用于打印信息的类
        Print print = (Print) context.getBean("print");

        //获取本地IP地址
        String localHost = null;
        try {
            InetAddress address = InetAddress.getLocalHost();
            localHost = address.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println("*************启动DHT网络************");
        nodeService.initNode(args); // 初始化节点信息
        System.out.println("本节点 ID: " + nodeInfo.getMe().getID() + "  前继节点 ID: " + nodeInfo.getPred().getID());
        fingerService.initTable(args); // 初始化路由表信息

        // 线程池
        ExecutorService threadPool = Executors.newCachedThreadPool();

        // 监听后台键盘输入
        threadPool.execute(new CommandThread(nodeService, print));

        int port;
        if (args.length == 3) {
            System.out.println("**********************************新建一个局部域网络************************************");
            System.out.println("该局部域的边界节点IP地址和端口为："+ localHost + ":" + args[0] + " .");
            System.out.println("该局部域的边界节点ID号为：" + nodeInfo.getMe().getID()+".");
            System.out.println();
            port = Integer.parseInt(args[0]);
        }
        else
            port = Integer.parseInt(args[3]);

                // 监听其他节点连接
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(port);
                } catch (IOException e) {
                    System.out.println("监听端口绑定失败！");
                    System.exit(1);
                }
                while (true) {
                    Socket newCon = null;
                    try {
                newCon = serverSocket.accept(); // 阻塞，程序释放cpu资源
            } catch (IOException e) {
                System.out.println("Socket接收失败！");
            }
            threadPool.execute(new SocketThread(newCon, identityService));
        }
    }
















}