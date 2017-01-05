# jdg-rolling-demo

This application is intended to demo rolling update and failover functionality of JBoss Data Grid in OpenShift.
The deployment consists of two components
* [Red Hat JBoss Data Grid](https://www.redhat.com/en/technologies/jboss-middleware/data-grid)
* [Spring Boot](https://projects.spring.io/spring-boot/) application using HotRod protocol to store and read from the the remote cache

## How to run
* Clone the repository
* Use `oc login` to log to an OpenShift instance
* Deploy the application
```shell
oc process -v PROJECT_NAMESPACE=<project> -f template/jdg-rolling.yaml -o yaml| oc create -f -
```
* Configure the service account for JDG cache
```shell
oc policy add-role-to-user admin system:serviceaccount:$(oc project -q):jdg-service-account -n $(oc project -q)
```
The role `admin` is used because we need role `view` for clustering and an access to a resource `pods/proxy` for imporoved readiness probe.

## Components
### Improved readiness probe
We need to enhance the readiness probe to marke the pod to be ready only after the initial state transfer is completed. Tis information is available only via JMX in JDG 6.5. To get this information we are using embedded Jolokia to query the required MBean. This means we need access to the `pods/proxy` resource. JDG 7.0 exposes the necessary information over JBoss DMR so a plain http call to DMR REST endpoint will be sufficient in future,

### Client application
The client application is a Spring Boot application using [Infinispan Spring Boot Starter](https://github.com/infinispan/infinispan-spring-boot) to confiure the connection between the application and the remote cache.
The application itself exposes four REST endpoints
* `/single/write`
* `/single/read`
* `/bulk/write`
* `/bulk/read`

## Demo workflow
* Run [stern](https://github.com/wercker/stern) to monitor client application pod logs
```shell
stern -n ... 'jdg-client-app-*'
```
### Dying cache instances
* Start writing to cache from two client application instances
```shell
curl -v 'http://jdg-client-app-<host>/bulk/write?from=0&count=25000'
curl -v 'http://jdg-client-app-<host>/bulk/write?from=100000&count=25000'
```
* Kill all JDG pods but one with command `oc delete pod datagrid-app-... --grace-period=0`
* When writing is finished verify the contents of the cache using
```shell
curl -v 'http://jdg-client-app-<host>/bulk/read?from=100000&count=25000'
curl -v 'http://jdg-client-app-<host>/bulk/read?from=0&count=25000'
```
### Dying cache instances
* Start writing to cache from two client application instances
```shell
curl -v 'http://jdg-client-app-<host>/bulk/write?from=200000&count=25000'
curl -v 'http://jdg-client-app-<host>/bulk/write?from=300000&count=25000'
```
* Trigger an update of JDG instance `oc deploy datagrid-app --latest`
* When writing is finished verify the contents of the cache using
```shell
curl -v 'http://jdg-client-app-<host>/bulk/read?from=300000&count=25000'
curl -v 'http://jdg-client-app-<host>/bulk/read?from=200000&count=25000'
```
