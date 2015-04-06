#删除group_members中的外键
alter table group_members drop foreign key fk_1;
alter table group_members drop foreign key fk_2;
#删除表user_friends中的外键
alter table user_friends drop foreign key fk_3;
alter table user_friends drop foreign key fk_4;
#删除表chatting_message中的外键
alter table chatting_message drop foreign key fk_5;
alter table chatting_message drop foreign key fk_6;
alter table chatting_message drop foreign key fk_7;

#删除表chatting_message
drop table if exists chatting_message;
#删除表group_members
drop table if exists group_members;
#删除表user
drop table if exists user;
#删除表user_friends
drop table if exists user_friends;
#删除表user_group
drop table if exists user_group;