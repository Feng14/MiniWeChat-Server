![项目Logo](http://images.cnblogs.com/cnblogs_com/xiaozefeng/686123/o_Logo.png)


#MiniWeChat

迷你微信客户端：[MiniWeChat-Client](https://github.com/MrNerverDie/MiniWeChat-Client)

后端介绍博客：http://www.cnblogs.com/xiaozefeng/p/mina_wechat_Java.html

《迷你微信》是一款仿制微信的手机跨平台应用，服务器端使用J2EE（包括Mina框架、Hibernate、Spring、Protobuf）实现，包含了通讯录，个人资料，单对单聊天，群聊等功能。

###项目需求

JDK版本 = 1.7 MySql版本 = 5.1 

###如何部署

- 数据库配置

在hibernat.cfg.xml中查看、更改数据库连接的端口号、数据库名、用户名和密码
```
<!-- Database connection settings -->
<property name="connection.driver_class">com.mysql.jdbc.Driver</property>
<property name="connection.url">
	jdbc:mysql://127.0.0.1:3306/MiniWechat?useUnicode=true&amp;characterEncoding=UTF-8
</property>
<property name="connection.username">root</property>
<property name="connection.password">root</property>
```
- 创建数据库

在Java项目中的/WebContent/WEB-INF路径下有createTable.sql和dropTable两个建表和删表的sql语句，直接运行即可

- 启动服务器端程序

将项目代码打jar包（要将第三方包一起打入），并把根目录下的applicationContext.xml和Log4JConfig.properties和LoggerRule.xml三个配置文件放在jar包相同路径下。

接着，在控制台中输入启动Java项目指令：

```
	java -jar MiniwWeChat.jar
```

启动后相关的日志会保存在根路径的logs文件夹内

###架构

![](http://7xiwp6.com1.z0.glb.clouddn.com/服务器主体架构.png)

[1]: https://github.com/MrNerverDie/MiniWeChat-Client
