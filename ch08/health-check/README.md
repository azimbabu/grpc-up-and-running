# Health Check
Install grpc-health-probe: https://github.com/grpc-ecosystem/grpc-health-probe#installation
````
% grpc_health_probe --addr=localhost:50051
status: SERVING

% grpc_health_probe --addr=localhost:50051 --service=ecommerce.ProductInfo
status: SERVING

% grpc_health_probe --addr=localhost:50051 --service=ecommerce.ProductInfo2
error: health rpc failed: rpc error: code = NotFound desc = unknown service ecommerce.ProductInfo2

% grpc_health_probe --addr=localhost:50051 --service=grpc.reflection.v1alpha.ServerReflection
status: SERVING

% grpc_health_probe --addr=localhost:50051 --service=grpc.health.v1.Health                   
error: health rpc failed: rpc error: code = NotFound desc = unknown service grpc.health.v1.Health
````

# gRPC Health Probe in Kubernetes

## Build docker image and test it

```
% docker image build -t azimbabu/grpc-productinfo-server-healthcheck .

% docker run -it --name=productinfo-server-healthcheck --hostname=productinfo-server-healthcheck -p 50051:50051  azimbabu/grpc-productinfo-server-healthcheck
```

## Load image into minikube

```
% minikube start

% minikube image load azimbabu/grpc-productinfo-server-healthcheck:latest
```

## Create kubernetes deployment

```
kubectl apply -f kubernetes/grpc-productinfo-server-deployment.yaml
```