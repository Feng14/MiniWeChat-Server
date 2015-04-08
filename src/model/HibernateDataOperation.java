package model;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class HibernateDataOperation {
	static Logger logger = Logger.getLogger(HibernateDataOperation.class);
	
	public static void add(Object o,ResultCode code){
		logger.info("Hibernate:start add to database");
		logger.info("Hibernate:add Object:"+o.getClass()+":"+o.toString());
		try{
			Session session = HibernateSessionFactory.getSession();
		    Transaction trans = session.beginTransaction();
		    session.save(o);
		    trans.commit();
		    session.close();
		    
		    code.setCode(ResultCode.SUCCESS);
		    logger.info("Hibernate:add to database success");
		}catch(Exception e){
			code.setCode(ResultCode.FAIL);
			logger.error("Hibernate:add to database fail");
			logger.error("Hibernate error:"+e.getStackTrace());
		}
	}
	
	public static List query(String s,Object o,Class c,ResultCode code){
		logger.info("Hibernate:start query from database");
		logger.info("Hibernate:query parameters:"+s+" , "+o.toString());
		try{
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(c.getClass());
			criteria.add(Restrictions.eq(s, o));
			 
			List list = criteria.list();
			session.close();
			logger.info("Hibernate:query from database success");
			logger.info("Hibernate:query result list size:"+list.size());
			code = ResultCode.SUCCESS;
			return list;
		}catch(Exception e){
			code = ResultCode.FAIL;
			logger.error("Hibernate:query from database fail");
			logger.error("Hibernate error:"+e.getStackTrace());
			return null;
		}
	}
	
	public static void update(Object o,ResultCode code){
		logger.info("Hibernate:start update database");
		logger.info("Hibernate:update Object:"+o.getClass()+":"+o.toString());
		try{
			Session session = HibernateSessionFactory.getSession();
		    Transaction trans = session.beginTransaction();
		    session.update(o);
		    trans.commit();
		    session.close();
		    
		    code = ResultCode.SUCCESS;
		    logger.info("Hibernate:update database success");
		}catch(Exception e){
			code = ResultCode.FAIL;
			logger.error("Hibernate:update database fail");
			logger.error("Hibernate error:"+e.getStackTrace());
		}
	}
	
	public static void delete(Object o,ResultCode code){
		logger.info("Hibernate:start delete from database");
		logger.info("Hibernate:delete Object:"+o.getClass()+":"+o.toString());
		try{
			Session session = HibernateSessionFactory.getSession();
		    Transaction trans = session.beginTransaction();
		    session.delete(o);
		    trans.commit();
		    session.close();
		    
		    code = ResultCode.SUCCESS;
		    logger.info("Hibernate:delete database success");
		}catch(Exception e){
			code = ResultCode.FAIL;
			logger.error("Hibernate:delete from database fail");
			logger.error("Hibernate error:"+e.getStackTrace());
		}
	}
	
	
	public static void main(String args[]){
		HibernateDataOperation test = new HibernateDataOperation();
		
		ResultCode code = ResultCode.NULL	 ;
		User u = new User();
		
		u.setUserId("newuser1");
		u.setUserName("username1");
		u.setUserPassword("123");
		test.add(u, code);
		
		System.out.println(code.getCode());
	}
	

}
