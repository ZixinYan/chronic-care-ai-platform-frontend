# 站内信系统 (Message System)

## 📚 模块架构

```
message-system/
├── message-system-api/          # API定义层
│   ├── MessageAPI.java         # Dubbo服务接口
│   ├── dto/                    # 数据传输对象
│   ├── po/                     # 持久化对象
│   └── enums/                  # 枚举类
├── message-system-provider/     # 服务提供者 (Dubbo RPC)
│   ├── service/                # Dubbo服务实现
│   │   └── MessageServiceImpl.java (@DubboService)
│   └── mapper/                 # 数据访问层
└── message-system-consumer/     # 服务消费者 (C端接口)
    └── controller/             # C端HTTP接口
        └── MessageController.java
```

## 🎯 架构设计原则

### 1. **Provider层 (纯Dubbo服务)**
- **位置**: `message-system-provider/service/MessageServiceImpl.java`
- **端口**: `20883` (Dubbo端口)
- **返回类型**: `BaseResponse`及其子类
- **调用方式**: **Dubbo RPC** (不提供HTTP接口)
- **功能**:
  - 所有业务逻辑实现
  - 通过`@DubboService`暴露RPC接口

### 2. **Consumer层 (C端HTTP接口)**
- **位置**: `message-system-consumer/controller/MessageController.java`
- **端口**: `8084` (HTTP端口)
- **返回类型**: `Result<T>`
- **调用方式**: 通过`@DubboReference`调用Provider
- **功能**:
  - 查询收件箱/发件箱
  - 获取消息详情
  - 标记已读
  - 删除消息
  - 获取未读数量

## 🔌 API接口

### Dubbo RPC接口 (Provider提供)

Provider只提供Dubbo服务,不暴露HTTP接口。所有方法通过Dubbo RPC调用:

```java
// MessageAPI接口定义
public interface MessageAPI {
    // 发送消息
    SendMessageResponse sendMessage(Long senderId, SendMessageRequest request);
    
    // 群发消息
    SendMessageResponse broadcastMessage(Long senderId, SendMessageRequest request);
    
    // 查询收件箱
    QueryMessageResponse queryInbox(Long userId, QueryMessageRequest request);
    
    // 查询发件箱
    QueryMessageResponse querySentBox(Long userId, QueryMessageRequest request);
    
    // 获取消息详情
    GetMessageDetailResponse getMessageDetail(Long userId, Long messageId);
    
    // 标记已读
    MarkAsReadResponse markAsRead(Long userId, Long messageId);
    
    // 批量标记已读
    MarkAsReadResponse batchMarkAsRead(Long userId, List<Long> messageIds);
    
    // 撤回消息
    RevokeMessageResponse revokeMessage(Long userId, Long messageId);
    
    // 删除消息
    DeleteMessageResponse deleteMessage(Long userId, Long messageId);
    
    // 批量删除消息
    DeleteMessageResponse batchDeleteMessage(Long userId, List<Long> messageIds);
    
    // 获取未读数量
    UnreadCountResponse getUnreadCount(Long userId);
    
    // 推送消息 (B端)
    PushMessageResponse pushMessage(PushMessageRequest request);
    
    // 批量推送消息 (B端)
    BatchPushMessageResponse batchPushMessage(BatchPushMessageRequest request);
}
```

### HTTP接口 (Consumer - 8084)

#### 1. 查询收件箱
```bash
GET /message/inbox?unreadOnly=true
Header: X-User-Id: 123

Response (Result包装):
{
    "code": 200,
    "message": "success",
    "data": {
        "messages": [...],
        "total": 10
    }
}
```

#### 2. 获取消息详情
```bash
GET /message/detail?messageId=789
Header: X-User-Id: 123

Response (Result包装):
{
    "code": 200,
    "message": "success",
    "data": {
        "messageId": 789,
        "title": "系统通知",
        "content": "...",
        ...
    }
}
```

#### 3. 标记已读
```bash
POST /message/read?messageId=789
Header: X-User-Id: 123

Response (Result包装):
{
    "code": 200,
    "message": "success",
    "data": true
}
```

#### 4. 批量标记已读
```bash
POST /message/batch-read
Header: X-User-Id: 123
Content-Type: application/json

[789, 790, 791]

Response (Result包装):
{
    "code": 200,
    "message": "success",
    "data": true
}
```

#### 5. 删除消息
```bash
POST /message/delete?messageId=789
Header: X-User-Id: 123

Response (Result包装):
{
    "code": 200,
    "message": "success",
    "data": true
}
```

#### 6. 批量删除
```bash
POST /message/batch-delete
Header: X-User-Id: 123
Content-Type: application/json

[789, 790, 791]

Response (Result包装):
{
    "code": 200,
    "message": "success",
    "data": true
}
```

#### 7. 获取未读数量
```bash
GET /message/unread-count
Header: X-User-Id: 123

Response (Result包装):
{
    "code": 200,
    "message": "success",
    "data": 5
}
```

## 🔐 调用说明

### Provider (Dubbo服务)
- **调用方式**: Dubbo RPC
- **调用者**: Consumer或其他微服务
- **不提供**: HTTP接口
- **端口**: 20883 (Dubbo端口)

### Consumer (HTTP接口)
- **调用方式**: HTTP REST API
- **调用者**: 前端、Gateway
- **权限**: 需要用户登录
- **端口**: 8084 (HTTP端口)
- **用户ID**: 通过Gateway注入`X-User-Id`头

## ⚠️ 功能限制

### 1. **不支持修改功能**
- 站内信一旦发送,不支持修改内容
- 只能撤回消息(发送后5分钟内)

### 2. **撤回限制**
- 只有发送者或系统管理员可以撤回
- 撤回时间限制: 5分钟

### 3. **删除方式**
- 用户删除: 软删除(只标记状态)
- 系统不会真正删除消息数据

## 🚀 启动顺序

### 1. 启动Provider (B端服务)
```bash
cd message-system/message-system-provider
mvn spring-boot:run
```
- 端口: 8083
- 注册到Nacos
- 提供Dubbo服务

### 2. 启动Consumer (C端服务)
```bash
cd message-system/message-system-consumer
mvn spring-boot:run
```
- 端口: 8084
- 注册到Nacos
- 调用Provider的Dubbo服务

## 📊 数据库

### 消息表 (message)
```sql
- message_id: 消息ID
- message_type: 消息类型
- sender_id: 发送者ID (0表示系统)
- receiver_id: 接收者ID
- title: 消息标题
- content: 消息内容
- status: 状态 (1-未读, 2-已读, 3-已删除, 4-已撤回)
- is_broadcast: 是否群发
- create_time: 创建时间
- read_time: 阅读时间
- revoke_time: 撤回时间
```

### 消息接收者表 (message_recipient)
```sql
- recipient_id: 接收者记录ID
- message_id: 消息ID
- receiver_id: 接收者ID
- status: 状态
- create_time: 创建时间
- read_time: 阅读时间
```

## 💡 最佳实践

### 其他微服务调用Provider (Dubbo RPC)

```java
// 在其他微服务中注入MessageAPI
@DubboReference(check = false)
private MessageAPI messageAPI;

// 推送单条消息
public void sendNotification(Long userId, String title, String content) {
    PushMessageRequest request = new PushMessageRequest();
    request.setMessageType(1);  // SYSTEM
    request.setReceiverId(userId);
    request.setTitle(title);
    request.setContent(content);
    
    PushMessageResponse response = messageAPI.pushMessage(request);
    if (response.getCode().name().equals("SUCCESS")) {
        log.info("推送消息成功, messageId: {}", response.getMessageId());
    }
}

// 批量推送消息
public void batchSendNotification(List<Long> userIds, String title, String content) {
    BatchPushMessageRequest request = new BatchPushMessageRequest();
    request.setMessageType(3);  // BROADCAST
    request.setReceiverIds(userIds);
    request.setTitle(title);
    request.setContent(content);
    
    BatchPushMessageResponse response = messageAPI.batchPushMessage(request);
    log.info("批量推送成功, 成功数量: {}", response.getSuccessCount());
}
```

### Consumer提供给前端的HTTP接口

```javascript
// 前端调用Consumer的HTTP接口
// 查询收件箱
fetch('/message/inbox?unreadOnly=true', {
    headers: {
        'X-User-Id': userId
    }
})
.then(res => res.json())
.then(data => {
    console.log(data.data.messages);
});

// 标记已读
fetch('/message/read?messageId=123', {
    method: 'POST',
    headers: {
        'X-User-Id': userId
    }
})
.then(res => res.json())
.then(data => {
    console.log('标记成功:', data.data);
});
```

## 🎯 核心特性

✅ **纯Dubbo服务**: Provider只提供RPC接口,不暴露HTTP  
✅ **分层架构**: Provider(Dubbo RPC) + Consumer(HTTP)  
✅ **统一返回**: Provider用BaseResponse, Consumer用Result  
✅ **批量操作**: 支持批量推送、批量已读、批量删除  
✅ **消息撤回**: 5分钟内可撤回  
✅ **软删除**: 数据不丢失  
✅ **不可修改**: 保证消息完整性  
✅ **系统推送**: 支持系统推送消息  
✅ **群发支持**: 支持向多个用户发送消息  
✅ **微服务调用**: 其他服务通过Dubbo调用Provider  

## 📐 架构图

```
┌─────────────┐
│   前端用户   │
└──────┬──────┘
       │ HTTP
       ↓
┌─────────────────────┐
│  Gateway (8080)     │
│  注入 X-User-Id     │
└──────┬──────────────┘
       │ HTTP
       ↓
┌─────────────────────────────┐
│  message-system-consumer    │
│  端口: 8084                  │
│  MessageController          │
│  返回: Result<T>            │
└──────┬──────────────────────┘
       │ Dubbo RPC
       ↓
┌─────────────────────────────┐
│  message-system-provider    │
│  端口: 20883 (Dubbo)        │
│  MessageServiceImpl         │
│  返回: BaseResponse子类     │
│  注解: @DubboService        │
└──────┬──────────────────────┘
       │
       ↓
┌─────────────────────────────┐
│  MySQL Database             │
│  - message                  │
│  - message_recipient        │
└─────────────────────────────┘

其他微服务可以直接通过Dubbo调用Provider:

┌─────────────────────┐
│  其他微服务          │
│  @DubboReference    │
│  MessageAPI         │
└──────┬──────────────┘
       │ Dubbo RPC
       ↓
┌─────────────────────────────┐
│  message-system-provider    │
│  MessageServiceImpl         │
└─────────────────────────────┘
```  
