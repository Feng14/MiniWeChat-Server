package model;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.logicalcobwebs.proxool.ProxoolFacade;

public class HibernateDataOperation {
	static Logger logger = Logger.getLogger(HibernateDataOperation.class);

	public static void add(Object object, ResultCode code) {
		add(new Object[] { object }, code);
	}

	public static void add(Object[] objects, ResultCode code) {
		Session session = HibernateSessionFactory.getSession();
		Transaction trans = session.beginTransaction();
		add(objects, code, session);
		trans.commit();
		session.close();
		ProxoolFacade.shutdown(0);
	}

	public static void add(Object object, ResultCode code, Session session) {
		add(new Object[] { object }, code, session);
	}

	public static void add(Object[] objects, ResultCode code, Session session) {
		logger.info("Hibernate:start add to database");
		try {
			for (Object object : objects) {
				logger.info("Hibernate:add Object:" + object.getClass() + ":" + object.toString());
				session.save(object);
			}

			code.setCode(ResultCode.SUCCESS);
			logger.info("Hibernate:add to database success");
		} catch (Exception e) {
			code.setCode(ResultCode.FAIL);
			logger.error("Hibernate:add to database fail");
			logger.error("Hibernate error:" + e.getStackTrace());
			logger.error("Hibernate error:" + e.getMessage());
		}
	}

	public static List query(String paramName, Object paramValue, Class outputClass, ResultCode resultCode) {
		logger.info("Hibernate:start query from database");
		logger.info("Hibernate:query parameters:" + paramName + " , " + paramValue.toString());
		try {
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(outputClass);
			// 使用缓存
			criteria.setCacheable(true);
			criteria.add(Restrictions.eq(paramName, paramValue));
			List list = criteria.list();
			session.close();
			ProxoolFacade.shutdown(0);

			logger.info("Hibernate:query from database success");
			logger.info("Hibernate:query result list size:" + list.size());
			resultCode.setCode(ResultCode.SUCCESS);

			return list;
		} catch (Exception e) {
			resultCode.setCode(ResultCode.FAIL);
			logger.error("Hibernate:query from database fail");
			logger.error("Hibernate error:" + e.getStackTrace());
			logger.error("Hibernate error:" + e.getMessage());
			return null;
		}
	}

	/**
	 * 重载query方法 增加Session参数 当需要使用延迟加载的时候 Session不能提前关闭
	 * 
	 * @param str
	 * @param obj
	 * @param cls
	 * @param code
	 * @param session
	 * @return List
	 * @author WangFei
	 */
	public static List query(String paramName, Object paramValue, Class outputClass, ResultCode resultCode, Session session) {
		logger.info("Hibernate:start query from database");
		logger.info("Hibernate:query parameters:" + paramName + " , " + paramValue.toString());
		try {
			Criteria criteria = session.createCriteria(outputClass);
			// 使用缓存
			criteria.setCacheable(true);
			criteria.add(Restrictions.eq(paramName, Integer.parseInt((String)paramValue)));
			List list = criteria.list();

			logger.info("Hibernate:query from database success");
			logger.info("Hibernate:query result list size:" + list.size());
			resultCode.setCode(ResultCode.SUCCESS);

			return list;
		} catch (Exception e) {
			e.printStackTrace();
			resultCode.setCode(ResultCode.FAIL);
			logger.error("Hibernate:query from database fail");
			logger.error("Hibernate error:" + e.getStackTrace());
			logger.error("Hibernate error:" + e.getMessage());
			return null;
		}
	}

	public static void update(Object obj, ResultCode code) {
		try {
			Session session = HibernateSessionFactory.getSession();
			Transaction trans = session.beginTransaction();
			update(obj, code, session);
			trans.commit();
			session.close();
			ProxoolFacade.shutdown(0);

		} catch (Exception e) {
			code.setCode(ResultCode.FAIL);
			logger.error("Hibernate:update database fail");
			logger.error("Hibernate error:" + e.getStackTrace());
			logger.error("Hibernate error:" + e.getMessage());
		}
	}

	public static void update(Object o, ResultCode code, Session session) {
		logger.info("Hibernate:start update database");
		logger.info("Hibernate:update Object:" + o.getClass() + ":" + o.toString());
		
		session.update(o);
		
		code.setCode(ResultCode.SUCCESS);
		logger.info("Hibernate:update database success");
	}

	public static void delete(Object o, ResultCode code) {
		logger.info("Hibernate:start delete from database");
		logger.info("Hibernate:delete Object:" + o.getClass() + ":" + o.toString());
		try {
			Session session = HibernateSessionFactory.getSession();
			Transaction trans = session.beginTransaction();
			session.delete(o);
			trans.commit();
			session.close();
			ProxoolFacade.shutdown(0);

			code.setCode(ResultCode.SUCCESS);
			logger.info("Hibernate:delete database success");
		} catch (Exception e) {
			code.setCode(ResultCode.FAIL);
			logger.error("Hibernate:delete from database fail");
			logger.error("Hibernate error:" + e.getStackTrace());
			logger.error("Hibernate error:" + e.getMessage());
		}
	}

}
