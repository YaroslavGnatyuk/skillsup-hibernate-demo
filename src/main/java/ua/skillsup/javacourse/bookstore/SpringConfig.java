package ua.skillsup.javacourse.bookstore;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * @author leopold
 * @since 13/03/16
 */
@Configuration // аннотация, означающая, что этот класс является спринговой конфигурацией
@ComponentScan({
    "ua.skillsup.javacourse.bookstore.application",
    "ua.skillsup.javacourse.bookstore.persistence",
    "ua.skillsup.javacourse.bookstore.demo"
})
// включаем автоматическое сканирование компонентов (классаов, пемеченных @Component, @Service, @Repository)
@EnableTransactionManagement
// включаем менеджмент транзакций (теперь спринг будет учитывать аннотации @Transactional)
@PropertySource("classpath:db.properties") // импортрруем проперти файл db.properties
public class SpringConfig {

  @Inject
  Environment env; // отсуюда мы будем брать проперти, которые загрузили через @PropertySource

  @Bean // этой аннотацией помечаем методы, которые конфигурируют бины
  public DataSource dataSource() {
    // датасорс. мы используем коннекшн-пул Hikari-CP, поэтому создаем HikariDataSource.
    final HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl("jdbc:h2:mem:books_db;DB_CLOSE_DELAY=-1");
    hikariConfig.setAutoCommit(env.getProperty("hikari.autoCommit", Boolean.class));
    hikariConfig.setConnectionTimeout(env.getProperty("hikari.connectionTimeout", Long.class));
    return new HikariDataSource(hikariConfig);
  }

  @Bean
  public LocalSessionFactoryBean sessionFactory() {
    // хибернейтовский sessionFactory.
    // у спринга есть удобный класс-фабрика LocalSessionFactoryBean, который мы и будем использовать.
    // он позволяет настроить автоматическое сканирование классов-сущностей.
    final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
    sessionFactory.setDataSource(dataSource());
    sessionFactory.setPackagesToScan("ua.skillsup.javacourse.bookstore.domain");
    sessionFactory.setConfigLocation(new ClassPathResource("hibernate_spring.cfg.xml"));

    return sessionFactory;
  }

  @Bean
  public PlatformTransactionManager txManager() {
    // менеджер транзакций, который необходим для того, чтобы аннотация @Transactional работала.
    // это класс, который знает как стартовать/завершать транзакции. спринг будет использовать его
    // всякий раз, когда мы вызываем метод помеченный @Transactional.
    // в нашем случаем используется реализация транзакшн-менеджера для хибернейта. под капотом он
    // он будет дергать sessionFactory.openSession(), session.getTransaction().begin() итд.
    return new HibernateTransactionManager(sessionFactory().getObject());
  }
}
