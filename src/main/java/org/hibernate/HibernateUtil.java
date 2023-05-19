package org.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.data.Charges;
import org.data.Holiday;
import org.data.Rental;
import org.data.Tool;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {

  private static SessionFactory sessionFactory;
  private static Session session;

  private static synchronized SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
      Configuration configuration = new Configuration();
      InputStream inputStream = HibernateUtil.class.getClassLoader().
          getResourceAsStream("hibernate-h2.properties");
      Properties hibernateProperties = new Properties();

      try {
        hibernateProperties.load(inputStream);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      configuration.setProperties(hibernateProperties);

      configuration.addAnnotatedClass(Holiday.class);
      configuration.addAnnotatedClass(Tool.class);
      configuration.addAnnotatedClass(Charges.class);
      configuration.addAnnotatedClass(Rental.class);

      ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().
          applySettings(configuration.getProperties()).build();

      sessionFactory = configuration.buildSessionFactory(serviceRegistry);
      return sessionFactory;
    }
    return sessionFactory;
  }

  public static synchronized Session getSession() {
    if(session == null || !session.isConnected()) {
      try {
        session = getSessionFactory().getCurrentSession();
      } catch (HibernateException ex) {
        session = getSessionFactory().openSession();
      }
    }

    return session;
  }
}
