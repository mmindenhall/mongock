package com.github.cloudyrock.mongock;

import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SpringMongock extends Mongock implements InitializingBean {
  private static final Logger logger = LoggerFactory.getLogger(SpringMongock.class);

  private Environment springEnvironment;
  private MongoTemplate mongoTemplate;

  protected SpringMongock(ChangeEntryRepository changeEntryRepository, MongoClient mongoClient, ChangeService changeService, LockChecker lockChecker) {
    super(changeEntryRepository, mongoClient, changeService, lockChecker);
  }

  /**
   * For Spring users: executing mongock after bean is created in the Spring context
   *
   * @throws Exception exception
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    execute();
  }

  @Override
  protected void executeChangeSetMethod(Method changeSetMethod, Object changeLogInstance)
      throws IllegalAccessException, InvocationTargetException {
    if (changeSetMethod.getParameterTypes().length == 1
        && changeSetMethod.getParameterTypes()[0].equals(MongoTemplate.class)) {
      logger.debug("method with MongoTemplate argument");

      changeSetMethod.invoke(changeLogInstance, this.mongoTemplate);
    } else if (changeSetMethod.getParameterTypes().length == 2
        && changeSetMethod.getParameterTypes()[0].equals(MongoTemplate.class)
        && changeSetMethod.getParameterTypes()[1].equals(Environment.class)) {
      logger.debug("method with MongoTemplate and environment arguments");

      changeSetMethod.invoke(changeLogInstance, this.mongoTemplate, this.springEnvironment);
    } else {
      super.executeChangeSetMethod(changeSetMethod, changeLogInstance);
    }
  }
  /**
   * Set Environment object for Spring Profiles (@Profile) integration
   *
   * @param environment org.springframework.core.env.Environment object to inject
   * @return SpringMongock object for fluent interface
   */
  public SpringMongock setSpringEnvironment(Environment environment) {
    this.springEnvironment = environment;
    return this;
  }

  /**
   * Sets pre-configured {@link MongoTemplate} instance to use by the Mongock
   *
   * @param mongoTemplate instance of the {@link MongoTemplate}
   * @return SpringMongock object for fluent interface
   */
  public SpringMongock setMongoTemplate(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
    return this;
  }
}
