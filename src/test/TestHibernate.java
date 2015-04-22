package test;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import protocol.Data.ChatData.ChatItem.ChatType;
import protocol.Data.ChatData.ChatItem.TargetType;

import model.Chatting;
import model.Group;
import model.HibernateSessionFactory;
import model.User;

public class TestHibernate {
	public static void main(String args[]){
		add();
		add2();
		query();
		query2();
	}
	
	public static void add(){
		Group p = new Group("group1");
//		p.setGroupName("group1");
		
		User u1 = new User();
		User u2 = new User();
		u1.setUserId("user1");
    	u1.setUserName("name1");
    	u1.setUserPassword("121");
    	u2.setUserId("user2");
    	u2.setUserName("name2");
    	u2.setUserPassword("122");
    	List<User> memberList = new ArrayList<User>();
    	memberList.add(u1);
    	memberList.add(u2);
    	p.setMemberList(memberList);
    	
    	Session session = HibernateSessionFactory.getSession();
    	Transaction trans = session.beginTransaction();
    	session.save(u1);
    	session.save(u2);
    	session.save(p);
    	
    	trans.commit();
    	session.close();
	}
	
	public static void add2(){
		Chatting chat = new Chatting("user1","user2",ChatType.TEXT,"this is a message",new Long(2015040611),0, TargetType.GROUP);
		Session session = HibernateSessionFactory.getSession();
    	Transaction trans = session.beginTransaction();
    	session.save(chat);
    	
    	trans.commit();
    	session.close();
	}
	
	public static void query(){
		Session session = HibernateSessionFactory.getSession();
		Criteria criteria = session.createCriteria(Group.class);
		criteria.add(Restrictions.eq("groupName", "group1"));
		List<Group> list = criteria.list();
		if(null != list && list.size()>0){
			Group group = list.get(0);
			System.out.println(group.getGroupId()+" "+group.getGroupName());
			for(int i=0;i<group.getMemberList().size();i++){
				System.out.println(group.getMemberList().get(i));
			}
		}
	}
	
	public static void query2(){
		Session session = HibernateSessionFactory.getSession();
		Criteria criteria = session.createCriteria(Chatting.class);
		criteria.add(Restrictions.eq("id",new Long(1)));
		List list =criteria.list();
		if(null != list && list.size()>0){
			Chatting chat = (Chatting)list.get(0);
			System.out.println(chat.getSenderUserId()+" "+chat.getReceiverUserId()+" "+chat.getMessage());
		}
	}
	
	public static void query3(){
		Session session = HibernateSessionFactory.getSession();
	       Query query = session.createQuery("SELECT c.id, c.senderUserId,c.receiverUserId,c.chattingType,c.message,c.time"+
",c.isGroup,c.groupId   from Chatting c where id ="+1);
	       List list = query.list();
	       for(int i=0;i<list.size();i++){
	    	   System.out.println("index:"+i);
	    	   Object []o = (Object[])list.get(i);
	    	   for(int j=0;j<o.length;j++){
	    		   System.out.print(o[j]+" ");
	    	   }
	       }
	       session.close();
	}

}
