#创建表chatting_message
create table chatting_message (id bigint  COMMENT '聊天消息Id' not null auto_increment, chatting_type int(8)  COMMENT '聊天类型', group_id int(8)  COMMENT '时间', is_group tinyint  COMMENT '是否群聊', message char(100)  COMMENT '消息', receiver_user_id char(20)  COMMENT '接收者id', sender_user_id char(20)  COMMENT '发送者id', time bigint(20)  COMMENT '时间', primary key (id));
#创建表group_members
create table group_members (group_id int(8)  COMMENT '聊天群Id' not null, user_id char(20)  COMMENT '微信号' not null);
#创建表user
create table user (user_id char(20)  COMMENT '微信号' not null, user_headIndex int(4) not null default 0 COMMENT '头像编号', user_name char(20) not null  COMMENT '昵称', user_password char(20) not null COMMENT '密码', primary key (user_id));
#创建表user——friends
create table user_friends (user_id char(20)  COMMENT '微信号' not null, friend_id char(20)  COMMENT '微信号' not null);
#创建表user_group
create table user_group (group_id int(8)  COMMENT '聊天群Id' not null auto_increment, group_name char(20)  COMMENT '聊天群昵称',create_id char(20) COMMENT '创建者微信号', primary key (group_id));
#新增联合主键
alter table user_friends add primary key (user_id, friend_id);
alter table group_members add primary key (user_id, group_id);
#group_member新增外键
alter table group_members add constraint fk_1 foreign key (user_id) references user (user_id);
alter table group_members add constraint fk_2 foreign key (group_id) references user_group (group_id);
#user_friends新增外键
alter table user_friends add constraint fk_3 foreign key (friend_id) references user (user_id);
alter table user_friends add constraint fk_4 foreign key (user_id) references user (user_id);
#chatting_message新增外键
alter table chatting_message add constraint fk_5 foreign key (receiver_user_id) references user (user_id);
alter table chatting_message add constraint fk_6 foreign key (sender_user_id) references user (user_id);
alter table chatting_message add constraint fk_7 foreign key (group_id) references user_group (group_id);
#user_group新增外键
alter table user_group add constraint fk_8 foreign key (create_id) references user (user_id) ;