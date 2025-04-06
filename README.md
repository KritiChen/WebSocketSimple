# WebSocketSimple-参照黑马聊天室
​	超级简单的聊天室小Demo，包括后端代码和前端代码。前端代码用AI生成，用户数据写死，可能不够灵活，后端登录模块也写得比较简单，固定两个用户。

​	拉取代码后直接运行即可，不用添加任务配置，比如数据库配置、Redis配置等。保证代码最简。

**账号：赵敏 密码：123456**

**账号：张无忌 密码：654321**

![image-20250406220404857](https://github.com/user-attachments/assets/4dabf146-5fc1-4662-881b-7f4caa964aab)
![image-20250406220517842](https://github.com/user-attachments/assets/33461b79-0721-4d06-8281-b4a61fa686dc)


# 一、用WebSocket协议实现聊天室的原因

## ✅ 为什么用 WebSocket 实现聊天室？

聊天室的本质需求是：**实时、双向通信、延迟低**。

------

## 🔁 其他方案分析：HTTP 轮询 和 长轮询

### 1. **HTTP 轮询（Polling）**

#### ✅ 工作方式：

- 客户端每隔几秒发一次请求给服务端：“有新消息吗？”
- 服务端立刻返回，无论有没有新消息

#### ⚠️ 优点：

- 实现简单，兼容性最好（所有浏览器都支持）

#### ❌ 缺点：

- **高延迟**：可能几秒后才能拿到消息
- **低效率**：绝大多数请求都是无效的，浪费资源
- **服务器压力大**：每个用户都在频繁请求

------

### 2. **HTTP 长轮询（Long Polling）**

#### ✅ 工作方式：

- 客户端发请求后，服务端**不会立刻响应**，而是**等有新消息时才返回**
- 客户端拿到消息后，再次发起下一个请求

#### ⚠️ 优点：

- 比普通轮询实时性高很多
- 不频繁发请求，服务端压力小一点

#### ❌ 缺点：

- 仍然是**一次性连接**：发 -> 等 -> 响应 -> 再发
- 服务端线程需要保持等待，**并发多时依然会占用很多资源**
- **不是真正的双向通信**，只能服务端响应，客户端不能“推送”

------

## 🌐 WebSocket：聊天室的理想方案

### ✅ 工作方式：

- 浏览器和服务端之间建立一个**持久连接**（握手一次后，连接常驻）
- **双向通信**：服务端、客户端都可以主动发消息
- 消息是通过 TCP 的一种轻量协议传输（比 HTTP Header 小很多）

------

### ✅ 优点：

| 优点           | 说明                                     |
| -------------- | ---------------------------------------- |
| ⚡ 实时性强     | 服务端可随时推送消息到客户端，毫秒级延迟 |
| 🔁 双向通信     | 客户端发，服务端发都可以，不用等请求     |
| 📉 开销小       | 握手一次就不再重复建连，无头部冗余       |
| 📈 支持高并发   | 一个线程可支持数千个连接（基于事件驱动） |
| ✅ 浏览器支持好 | 所有现代浏览器都支持 WebSocket           |

------

### ❌ 缺点：

- 需要服务器和浏览器支持 WebSocket（现代环境基本没问题）
- 对防火墙、代理可能要特别配置（但常规都能搞定）

------

## 🎯 总结：为什么聊天室选 WebSocket？

| 方案          | 实时性 | 服务端压力 | 双向通信 | 推荐程度 |
| ------------- | ------ | ---------- | -------- | -------- |
| HTTP 轮询     | ❌ 差   | ❌ 大       | ❌ 无     | ❌        |
| 长轮询        | ⭕ 一般 | ⭕ 较高     | ❌ 无     | ⭕        |
| **WebSocket** | ✅ 极好 | ✅ 小       | ✅ 有     | ✅✅✅      |

> 所以你会发现：**即时通讯、聊天室、协同编辑、在线客服、游戏对战**这类实时场景，基本都采用 WebSocket。

## 🔥 什么是 SSE（Server-Sent Events）？

SSE 是 HTML5 提供的一种服务端主动向客户端推送消息的技术。

- 基于 HTTP 协议，单向通道（Server ➡ Client）
- 客户端用 `EventSource` 对象接收消息
- 连接是**持久的**，服务端通过流式方式持续发送数据

------

## ✅ SSE 的特点

| 特性                 | 说明                                                       |
| -------------------- | ---------------------------------------------------------- |
| 📡 单向通信           | 只能服务端向客户端推送，客户端不能主动发消息               |
| 🧱 基于 HTTP          | 使用普通 HTTP，不需要额外协议或握手（比 WebSocket 更兼容） |
| ⏳ 自动重连           | SSE 原生支持断线重连（浏览器帮你重试）                     |
| 🧼 文本流传输         | 只能传输 UTF-8 编码的文本数据，不支持二进制                |
| 📦 内置心跳机制       | SSE 支持 `:heartbeat` 来保持连接活性                       |
| ✅ 兼容浏览器事件模型 | 可以监听不同类型的事件（`message`, `open`, `error`）       |

------

## ❌ SSE 的限制

| 限制             | 说明                                                         |
| ---------------- | ------------------------------------------------------------ |
| ❌ 只能服务端推送 | 客户端不能主动发消息（不像 WebSocket 是双向的）              |
| ❌ 不支持 IE      | IE 和一些老浏览器不支持 `EventSource`                        |
| ❌ 不支持二进制   | 不适合传输文件、图片、音频等                                 |
| ❌ 并发连接限制   | 浏览器对每个域名的 EventSource 连接数量有限制（Chrome 默认是 6 个） |

------

## ✅ SSE vs WebSocket vs HTTP轮询对比总结

| 特性       | HTTP轮询        | 长轮询          | SSE                  | WebSocket                |
| ---------- | --------------- | --------------- | -------------------- | ------------------------ |
| 实时性     | ❌ 差            | ⭕ 一般          | ✅ 好                 | ✅ 极好                   |
| 通信方向   | 客户端 ➡ 服务端 | 客户端 ➡ 服务端 | **服务端 ➡ 客户端**  | **双向通信**             |
| 开销       | ❌ 大            | ⭕ 中            | ✅ 小                 | ✅ 最小                   |
| 支持二进制 | ❌ 否            | ❌ 否            | ❌ 否                 | ✅ 是                     |
| 浏览器支持 | ✅ 全部          | ✅ 全部          | ⭕ 现代浏览器         | ✅ 现代浏览器             |
| 编程复杂度 | ✅ 简单          | ⭕ 中            | ✅ 简单               | ⭕ 稍复杂                 |
| 断线重连   | ❌ 无            | ❌ 无            | ✅ 有                 | ❌ 需要手动实现           |
| 场景       | 普通查询        | 实时监控        | 新闻推送、股票、日志 | 聊天室、协作、游戏、客服 |

------

## 🧠 聊天室选 WebSocket 的理由（SSE 不太适合）：

- 聊天需要**双向通信**（用户发消息、服务器广播） ➡ SSE 不支持
- WebSocket 支持**任意类型数据**（文本、JSON、二进制） ➡ SSE 只能 UTF-8 文本
- 聊天涉及**多人交互和高并发**，WebSocket 更高效
- SSE 适合**一对多、服务端推送场景**，比如：新闻订阅、监控日志流

# 二、后端代码配置

### ✅ 一、引入依赖（Maven）

```
xml复制编辑<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

------

### ✅ 二、配置类：`ServerEndpointExporter`

Spring Boot 中需要显式注册 WebSocket 端点：

```
java复制编辑import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

> 注意：`ServerEndpointExporter` 只有在你使用的是 Spring Boot 的内嵌容器（比如 Tomcat）时才需要手动配置。

------

### ✅ 三、获取 `HttpSession` 的配置类

因为 `@ServerEndpoint` 的类不受 Spring 管理，要想获取 `HttpSession`，需要通过 `Configurator` 实现：

```
java复制编辑import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class GetHttpSessionConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        config.getUserProperties().put(HttpSession.class.getName(), httpSession);
    }
}
```

------

### ✅ 四、WebSocket 服务端实现类

```
java复制编辑import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@ServerEndpoint(value = "/chat", configurator = GetHttpSessionConfigurator.class)
@Component
public class ChatEndpoint {
    private static final Map<String, Session> onlineSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 获取 HttpSession 中的用户信息等
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 消息处理逻辑
    }

    @OnClose
    public void onClose(Session session) {
        // 用户下线逻辑
    }
}
```

------

### ✅ 五、前端连接示例

虽然你的 PDF 没完整列出前端内容，但前端一般通过以下方式连接：

```
javascript复制编辑const socket = new WebSocket("ws://localhost:8080/chat");

socket.onopen = () => {
    console.log("Connected");
};

socket.onmessage = (event) => {
    console.log("Message from server:", event.data);
};

socket.onclose = () => {
    console.log("Disconnected");
};
```

# 三、后端设计时序图
![image-20250406222320605](https://github.com/user-attachments/assets/0093ba11-bdec-4283-8410-d9912549e166)



# 四、前端代码（AI生成，可以自己优化）

