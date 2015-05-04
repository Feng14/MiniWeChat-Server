![项目Logo](http://images.cnblogs.com/cnblogs_com/xiaozefeng/686123/o_Logo.png)

#迷你微信(服务器端)
客户端的[传送门][1]

##简介
《迷你微信》是一款仿制微信的手机跨平台应用，前端使用Unity3D（包括UGUI）实现，后端使用J2EE（包括Mina框架、Hibernate、Spring、Protobuf）实现，包含了通讯录，个人资料，单对单聊天，群聊等功能。

这里.是服务器端的介绍，先向大家提供客户端的[传送门][1]。

##在Linux上的配置
主要是数据库的配置和程序启动方式：
###1.数据库配置
在hibernat.cfg.xml中查看、更改数据库连接的端口号、数据库名、用户名和密码
```
<!-- Database connection settings -->
		<property name="connection.driver_class">com.mysql.jdbc.Driver</property>
		<property name="connection.url">jdbc:mysql://127.0.0.1:3306/MiniWechat?useUnicode=true&amp;characterEncoding=UTF-8</property>
		<property name="connection.username">root</property>
		<property name="connection.password">root</property>
```
- 在/WebContent/WEB-INF路径下有createTable.sql和dropTable两个建表和删表的sql语句
- 在mysql数据库中运行文件进行建表，因为其中涉及到外键，所以mysql在Linux下的默认数据库引擎可能不支持外键 需要自行更换相应的数据库引擎

###2.程序启动方式

将项目代码打jar包，并把根目录下的applicationContext.xml和Log4JConfig.properties两个配置文件放在jar包相同路径下。
通过java -jar方式启动程序
启动后相关的日志会保存在根路径的logs文件夹内









[1]: https://github.com/MrNerverDie/MiniWeChat-Client
