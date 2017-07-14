Microservice Kubernetes Sample
=====================

This sample is like the sample for my Microservices Book
 ([English](http://microservices-book.com/) /
 [German](http://microservices-buch.de/)) that you can find at
 https://github.com/ewolff/microservice .

However, this demo uses [Kubernetes](https://kubernetes.io/) as Docker
environment. Kubernetes also support service discovery and load
balancing. An Apache httpd as a reverse proxy routes the calls to the
services.

This project creates a complete micro service demo system in Docker
containers. The services are implemented in Java using Spring and
Spring Cloud.



It uses three microservices:
- `Order` to process orders.
- `Customer` to handle customer data.
- `Catalog` to handle the items in the catalog.


Kubernetes
----------

To run the example you will need a Kubernetes environment. An easy way
to get one is:

* Install
[minikube](https://github.com/kubernetes/minikube/releases). Minikube
is a Kubernetes environment in a virtual machine that is easy to use
and install. It is not meant for production but to test Kubernetes or
for developer environments.

* Install
  [kubectl](https://kubernetes.io/docs/tasks/kubectl/install/). This
  is the command line interface for Kubernetes.

* Create a Minikube instance with `minikube start --memory=4000`. This
  will set the memory of the Kubernetes VM to 4.000 MB - which should
  be enough for most experiments.

Build Docker Images (optional)
--------------------------

Kubernetes needs Docker images to run the system. These need to be
build and stored in a Docker Registry. Instead of installing and
running a Docker Registry yourself, you can just use the public
registry. The Docker images for the system are actually stored
there. However, you can also build and upload them yourself.

To build and upload the Docker images (optional step):

* Install Maven, see https://maven.apache.org/download.cgi

* Also you will a
[Docker installation](https://docs.docker.com/installation/).

* Compile the example. Execute `mvn package` in the directory
`microservice-kubernetes-demo`.

* Create an account at the public
[Docker Hub](https://hub.docker.com/).

* Set the environment variable `DOCKER_ACCOUNT` to the name of the
account you just created.

* Login to the Docker Hub with `docker login`.

* Run `docker-build.sh` in the directory
`microservice-kubernetes-demo`. It builds the images and uploads them to the
Docker Hub.

Run on Kubernetes
----------------

To run the system on Kubernetes:


* If your Set the environment variable `DOCKER_ACCOUNT` to the name of the
account you created.

* Run `kubernetes-deploy.sh` in the directory `microservice-kubernetes-demo` .

That deploys the images. It creates Pods. Pods might contain one or
many Docker containers - in this case each Pod contains just one
Docker container. The deployment takes care of the Pods -
i.e. it starts new Pods if a Pod crashes.

Also services are created. Services have a clusterwide unique IP
adress and a DNS entry. Service can use many Pods to do load
balancing. To actually view the services:

* Run `kubectl get services` and `kubectl describe services`for more
  details. This also works for pods and deployments.

* Run `minikube service apache` to open the web page of the Apache httpd
  server in the web browser. Notice how the service was bound to a
  port on the host Minikube runs on.

The service type is `LoadBalancer`. This should actually connect the
service to an external load balancer. This does not work on minikube
so it can only be accessed at a specific port on the minikube host.

* To remove all services and deployments tun `remove-kubernetes.sh`.

Apache HTTP Load Balancer
------------------------

Apache HTTP is used to provide the web page of the demo at
port 8080. It also forwards HTTP requests to the microservices. This
is not really necessary as each service has its own port on the
Minikube host but it provides a single point of entry for the whole system.
Apache HTTP is configured as a reverse proxy for this.
Load balancing is left to Kubernetes.

To configure this Apache HTTP needs to get all registered services from
Kubernetes. It just uses DNS for that.

Please refer to the subdirectory [microservice-kubernetes-demo/apache](microservice-kubernetes-demo/apache/) to see how this works.


Remarks on the Code
-------------------

The microservices are:

- [microservice-kubernetes-demo-catalog](microservice-kubernetes-demo/microservice-kubernetes-demo-catalog) is the application to take care of items.
- [microservice-kubernetes-demo-customer](microservice-kubernetes-demo/microservice-kubernetes-demo-customer) is responsible for customers.
- [microservice-kubernetes-demo-order](microservice-kubernetes-demo/microservice-kubernetes-demo-order) does order processing. It uses
  microservice-kubernetes-demo-catalog and microservice-kubernetes-demo-customer.
  Hystrix is used for resilience.

The microservices have a Java main application in `src/test/java` to
run them stand alone. `microservice-demo-order` uses a stub for the
other services then. Also there are tests that use _consumer-driven
contracts_. That is why it is ensured that the services provide the
correct interface. These CDC tests are used in microservice-demo-order
to verify the stubs. In `microservice-kubernetes-demo-customer` and
`microserivce-kubernetes-demo-catalog` they are used to verify the implemented
REST services.

Note that the code has no dependencies on Kubernetes. Only Spring
Cloud Hystrix is used to add resilience.
