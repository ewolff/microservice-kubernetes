#!/bin/sh
kubectl delete service apache catalog customer order hystrix-dashboard
kubectl delete deployments apache catalog customer order hystrix-dashboard
