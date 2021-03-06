# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.6.1/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.6.1/maven-plugin/reference/html/#build-image)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/2.6.1/reference/htmlsingle/#using-boot-devtools)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.6.1/reference/htmlsingle/#production-ready)
* [Graphite](https://docs.spring.io/spring-boot/docs/2.6.1/reference/html/production-ready-features.html#production-ready-metrics-export-graphite)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

# To do list
* Monitoring
    * ~~Grafana~~
    * ~~VisualVM~~
    * Actuator
    * Flight recorder
    * JMX
* Reactor
    * ~~metrics in Grafana~~
* Tracing
  * Zipkin
  * Spring Sleuth
* Control
  * JMX
  * CLI
* Logging
  * Logback 
  * MDC
  * Splunk
* Kafka
  * Kafka
  * Kafka Streams
  * KSQL
* JMS messaging
* Reactive Postgres
* Web with React
* Transactional processing of all inputs
* Event sourcing / CQRS
* Security
* Java 11
* Kubernetes
* Azure

# Docker
## Splunk

`docker run -d -e "SPLUNK_START_ARGS=--accept-license" -e "SPLUNK_USER=root" -p "8000:8000" --name splunk store/splunk/enterprise`