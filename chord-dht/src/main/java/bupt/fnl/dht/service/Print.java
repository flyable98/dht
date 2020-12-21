package bupt.fnl.dht.service;

import bupt.fnl.dht.domain.Node;

/**
 * 用于打印各种信息
 */
public interface Print {
    /**
     * 打印路由表信息
     */
    void printFingerInfo();

    void printNodeList();
    /**
     * 打印节点信息
     */
    void printNodeInfo();

    void printNodeId(Node node);

}
