[TOC]

# 使用nettty来实现一个简单的RPC

## TODO
-[ ] 客户端获取结果有没有一个更好的办法，去看看掘金小册。
-[ ] future的实现原理？有没有更好的future的实现？
-[ ] 代理工厂那个地方实现的还是不够优雅
-[ ] zookeeper注册中心等
-[ ] 加入更多的rpc的功能？

## 原理
![img](https://gitee.com/dongzhonghua/zhonghua/raw/master/img/blog/rpc%E5%8E%9F%E7%90%86.png)

1. client 会调用本地动态代理 proxy
2. 这个代理会将调用通过协议转序列化字节流
3. 通过 netty 网络框架，将字节流发送到服务端
4. 服务端在受到这个字节流后，会根据协议，反序列化为原始的调用，利用反射原理调用服务方提供的方法
5. 如果请求有返回值，又需要把结果根据协议序列化后，再通过 netty 返回给调用方
## 协议

## 服务器端

## 客户端



## 参考
> 主要参考：https://juejin.cn/post/6844903957622423560
> https://www.w3cschool.cn/architectroad/architectroad-rpc-framework.html
> https://github.com/pjmike/springboot-rpc-demo