package com.example.demo.ws;

import com.alibaba.fastjson2.JSON;
import com.example.demo.config.GetHttpSessionConfigurator;
import com.example.demo.pojo.MessageReceive;
import com.example.demo.pojo.MessageSend;
import com.example.demo.utils.MessageUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/chat", configurator = GetHttpSessionConfigurator.class)
@Component
public class ChatEndPoint {
    private static Map<String, Session> onlineSessions = new ConcurrentHashMap<>();
    private HttpSession httpSession;
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        try {
            //通过httpSession来获取当前登录的用户信息
            HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
            this.httpSession = httpSession;
            String username = httpSession.getAttribute("username").toString();
            //将当前登录用户的webSocketSession保存在map集合中
            onlineSessions.put(username, session);
            //发送系统消息，广播当前登录的用户信息
            String message = MessageUtil.getMsgStr(true, null, JSON.toJSONString(onlineSessions.keySet()));
            for (Map.Entry<String, Session> entry : onlineSessions.entrySet()) {
                Session session1 = entry.getValue();
                session1.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @OnMessage
    public void onMessage(String message) {
        try {
            //发送系统消息
            MessageReceive msg = JSON.parseObject(message, MessageReceive.class);
            if (msg.getSystem()) {
                if (!onlineSessions.isEmpty()) {
                    for (Map.Entry<String, Session> entry : onlineSessions.entrySet()) {
                        Session session1 = entry.getValue();
                        session1.getBasicRemote().sendText(message);
                    }
                }

            } else {
                //非系统消息获取接收人信息，并从map中获取接收人的WebSocketSession对象
                String  to = msg.getTo();
                Session toSession = onlineSessions.get(to);
                //获取发送人信息
                String username = httpSession.getAttribute("username").toString();
                //构造消息并通过接收人webSocketSession对象将消息发送给到客户端
                String sendMsg = MessageUtil.getMsgStr(false, username, msg.getMessage());
                toSession.getBasicRemote().sendText(sendMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @OnClose
    public void onClose(Session session) {
        try {
            //连接断开后将当前用户从活跃用户中剔除。
            String username = httpSession.getAttribute("username").toString();
            onlineSessions.remove(username);
            //广播当前活跃的用户信息
            if (!onlineSessions.isEmpty()) {
                String message = MessageUtil.getMsgStr(true, null, JSON.toJSONString(onlineSessions.keySet()));
                for (Map.Entry<String, Session> entry : onlineSessions.entrySet()) {
                    Session session1 = entry.getValue();
                    session1.getBasicRemote().sendText(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
