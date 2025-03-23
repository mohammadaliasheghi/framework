# Framework

This project contains bunch of utility
classes and abstraction which helps you
to speed up your development while building
Java projects using Spring framework.

#### Fork repository from ( omid-biz )

##### Features and changes

* change security version
* writing test cases
* try to use multithreading
* improved code
* implement new method
* ...

### Reference Documentation

For further reference, please consider the following sections:

* [lombok](https://mvnrepository.com/artifact/org.projectlombok/lombok)
* [slf4j](https://mvnrepository.com/artifact/org.slf4j/slf4j-api)
* [hibernate](https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-core)
* [dbutils](https://mvnrepository.com/artifact/commons-dbutils/commons-dbutils)
* [beanutils](https://mvnrepository.com/artifact/commons-beanutils/commons-beanutils)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

#### Jar path to use another aap `(.m2\repository\com\m2a\rep\0.0.1)`