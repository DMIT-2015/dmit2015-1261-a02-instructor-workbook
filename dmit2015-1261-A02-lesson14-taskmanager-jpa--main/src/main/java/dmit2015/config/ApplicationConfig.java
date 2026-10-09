package dmit2015.config;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sql.DataSourceDefinitions;
import jakarta.enterprise.context.ApplicationScoped;

@DataSourceDefinitions({

        @DataSourceDefinition(
                name = "java:app/datasources/H2DatabaseDS",
                className = "org.h2.jdbcx.JdbcDataSource",
                 url="jdbc:h2:file:~/jdk/databases/h2/lesson14-taskmanager-jpa-db", //we udpated databse name from part 1
               // url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
                user = "user2015",
                password = "Password2015"),

})

@ApplicationScoped
public class ApplicationConfig {

}