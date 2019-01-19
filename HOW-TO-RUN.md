# How to Run

This is a step-by-step guide how to run the example:

## Installation

* Install
[minikube](https://github.com/kubernetes/minikube/releases). Minikube
is a Kubernetes environment in a virtual machine that is easy to use
and install. It is not meant for production but to test Kubernetes or
for developer environments.

* Install
  [kubectl](https://kubernetes.io/docs/tasks/kubectl/install/). This
  is the command line interface for Kubernetes.

## Build the Docker images (optional)

This step is *optional*. There are Docker images on the public Docker
Hub that are used if you do not build your own.

* The example is implemented in Java. See
   https://www.java.com/en/download/help/download_options.xml . The
   examples need to be compiled so you need to install a JDK (Java
   Development Kit). A JRE (Java Runtime Environment) is not
   sufficient. After the installation you should be able to execute
   `java` and `javac` on the command line.

* The example run in Docker Containers. You need to install Docker
  Community Edition, see https://www.docker.com/community-edition/
  . You should be able to run `docker` after the installation.

Change to the directory `microservice-kubernetes-demo` and run `./mvnw clean
package` or `mvnw.cmd clean package` (Windows). This will take a while:

```
[~/microservice-kubernetes/microservice-kubernetes-demo]./mvnw clean package
....
[INFO] 
[INFO] --- maven-jar-plugin:2.6:jar (default-jar) @ microservice-kubernetes-demo-order ---
[INFO] Building jar: /Users/wolff/microservice-kubernetes/microservice-kubernetes-demo/microservice-kubernetes-demo-order/target/microservice-kubernetes-demo-order-0.0.1-SNAPSHOT.jar
[INFO] 
[INFO] --- spring-boot-maven-plugin:1.4.5.RELEASE:repackage (default) @ microservice-kubernetes-demo-order ---
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] microservice-kubernetes-demo ....................... SUCCESS [  0.986 s]
[INFO] microservice-kubernetes-demo-customer .............. SUCCESS [ 16.953 s]
[INFO] microservice-kubernetes-demo-catalog ............... SUCCESS [ 18.016 s]
[INFO] microservice-kubernetes-demo-order ................. SUCCESS [ 18.512 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 57.633 s
[INFO] Finished at: 2017-09-08T09:36:32+02:00
[INFO] Final Memory: 56M/420M
[INFO] ------------------------------------------------------------------------
```

If this does not work:

* Ensure that `settings.xml` in the directory `.m2` in your home
directory contains no configuration for a specific Maven repo. If in
doubt: delete the file.

* The tests use some ports on the local machine. Make sure that no
server runs in the background.

* Skip the tests: `./mvnw clean package -Dmaven.test.skip=true` or
  `mvnw.cmd clean package -Dmaven.test.skip=true` (Windows).

* In rare cases dependencies might not be downloaded correctly. In
  that case: Remove the directory `repository` in the directory `.m2`
  in your home directory. Note that this means all dependencies will
  be downloaded again.

Now the Java code has been compiles. The next step is to create Docker
images and upload them to the public Docker Hub:

* Create an account at the public
[Docker Hub](https://hub.docker.com/).

* Log in to your Docker Hub account by entering `docker login` on the command
line.

* Set the environment variable `DOCKER_ACCOUNT` to the name of the account.

* Run `docker-build.sh` in the directory
`microservice-kubernetes-demo`. It builds the images and uploads them to the
Docker Hub using your account. Of course uploading the images takes
some time:

```
[~/microservice-kubernetes/microservice-kubernetes-demo]export DOCKER_ACCOUNT=ewolff
[~/microservice-kubernetes/microservice-kubernetes-demo]echo $DOCKER_ACCOUNT
ewolff
[~/microservice-kubernetes/microservice-kubernetes-demo]./docker-build.sh 
...
Removing intermediate container 36e9b0c2ac0e
Successfully built b76261d1e4ee
f4ffcb9c643d: Pushed 
14c5bfa09694: Mounted from ewolff/microservice-kubernetes-demo-order 
41a5c76632fc: Mounted from ewolff/microservice-kubernetes-demo-order 
5bef08742407: Mounted from ewolff/microservice-kubernetes-demo-order 
latest: digest: sha256:36d87ea5c8628da9a6677c1eafb9009c8f99310f5376872e7b9a1edace37d1a0 size: 1163
```

## Run the containers

* Create a Minikube instance with `minikube start --memory=4000`. This
  will set the memory of the Kubernetes VM to 4.000 MB - which should
  be enough for most experiments:

```
[~/microservice-kubernetes]minikube start --memory=4000
Starting local Kubernetes v1.7.5 cluster...
Starting VM...
Getting VM IP address...
Moving files into cluster...
Setting up certs...
Connecting to cluster...
Setting up kubeconfig...
Starting cluster components...
Kubectl is now configured to use the cluster.
```

* If you created your own Docker images: Ensure that the environment
variable `DOCKER_ACCOUNT` is set to the name of the account on Docker
Hub you created.

* Run `kubernetes-deploy.sh` in the directory
`microservice-kubernetes-demo` :

```
[~/microservice-kubernetes/microservice-kubernetes-demo]./kubernetes-deploy.sh
deployment "apache" created
service "apache" exposed
deployment "catalog" created
service "catalog" exposed
deployment "customer" created
service "customer" exposed
deployment "order" created
service "order" exposed
```

An alternative is to use the command `kubectl apply -f
microservices.yaml` . This command takes the description of the
services and deployments from the file `microservices.yaml` and
creates them if they do not already exist. The YAML uses the images
from the Docker Hub account `ewolff`. You will need to modify the YAML
if you want to use different images.

That deploys the images. It creates Pods. Pods might contain one or
many Docker containers. In this case each Pod contains just one
Docker container.

Also services are created. Services have a clusterwide unique IP
adress and a DNS entry. Service can use many Pods to do load
balancing. To actually view the services:

* Run `kubectl get services` to see all services:

```
[~/microservice-kubernetes/microservice-kubernetes-demo]kubectl get services
NAME                CLUSTER-IP   EXTERNAL-IP   PORT(S)          AGE
apache              10.0.0.90    <pending>     80:31214/TCP     46s
catalog             10.0.0.219   <pending>     8080:30161/TCP   46s
customer            10.0.0.163   <pending>     8080:30620/TCP   45s
kubernetes          10.0.0.1     <none>        443/TCP          3m
order               10.0.0.21    <pending>     8080:30616/TCP   45s
```


* Run `kubectl describe services` for more
  details. This also works for pods (`kubectl describe pods`) and
  deployments (`kubectl describe deployments`).

```
[~/microservice-kubernetes/microservice-kubernetes-demo]kubectl describe services
...

Name:			order
Namespace:		default
Labels:			run=order
Annotations:		<none>
Selector:		run=order
Type:			LoadBalancer
IP:			10.0.0.21
Port:			<unset>	8080/TCP
NodePort:		<unset>	30616/TCP
Endpoints:		172.17.0.7:8080
Session Affinity:	None
Events:			<none>
```

* You can also get a list of the pods:

```
[~/microservice-kubernetes/microservice-kubernetes-demo]kubectl get pods
NAME                                READY     STATUS    RESTARTS   AGE
apache-3412280829-k5z5p             1/1       Running   0          2m
catalog-269679894-60dr0             1/1       Running   0          2m
customer-1984516559-1ffjk           1/1       Running   0          2m
order-2204540131-nks5s              1/1       Running   0          2m
```

* ...and you can see the logs of a pod:

```
[~/microservice-kubernetes/microservice-kubernetes-demo]kubectl logs catalog-269679894-60dr0 

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.4.5.RELEASE)
...
2017-09-08 08:11:06.128  INFO 7 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
2017-09-08 08:11:06.158  INFO 7 --- [           main] o.s.c.support.DefaultLifecycleProcessor  : Starting beans in phase 0
2017-09-08 08:11:06.746  INFO 7 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
2017-09-08 08:11:06.803  INFO 7 --- [           main] c.e.microservice.catalog.CatalogApp      : Started CatalogApp in 53.532 seconds (JVM running for 54.296)
...
```

* You can also run commands in a pod:

```
[~/microservice-kubernetes/microservice-kubernetes-demo]kubectl exec catalog-269679894-60dr0  /bin/ls
bin
dev
etc
home
lib
lib64
media
microservice-kubernetes-demo-catalog-0.0.1-SNAPSHOT.jar
mnt
proc
root
run
sbin
srv
sys
tmp
usr
var
```

* You can even open a shell in a pod:

```
[~/microservice-kubernetes/microservice-kubernetes-demo]kubectl exec catalog-269679894-60dr0  -it /bin/sh
/ # ls
bin                                                      proc
dev                                                      root
etc                                                      run
home                                                     sbin
lib                                                      srv
lib64                                                    sys
media                                                    tmp
microservice-kubernetes-demo-catalog-0.0.1-SNAPSHOT.jar  usr
mnt                                                      var
/ # 
```

* Run `minikube service apache` to open the web page of the Apache httpd
  server in the web browser. Notice how the service was bound to a
  port on the host Minikube runs on.

The service type is `LoadBalancer`. This should actually connect the
service to an external load balancer. This does not work on minikube
so it can only be accessed at a specific port on the minikube host.

* To remove all services and deployments run `kubernetes-remove.sh`:

```
[~/microservice-kubernetes/microservice-kubernetes-demo]./kubernetes-remove.sh 
service "apache" deleted
service "catalog" deleted
service "customer" deleted
service "order" deleted
deployment "apache" deleted
deployment "catalog" deleted
deployment "customer" deleted
deployment "order" deleted
```

This skript must be executed before a new version of the pods can be deployed.
