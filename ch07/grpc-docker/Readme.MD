Create network productinfo-network

```
docker network create productinfo-network
```

Build and run the server docker image

```
docker image build -t azimbabu:grpc-productinfo-server .
docker run -it --network=productinfo-network --name=productinfo-server --hostname=productinfo-server -p 50051:50051  azimbabu:grpc-productinfo-server
```

Build and run the client docker image

```
docker image build -t azimbabu:grpc-productinfo-client .
docker run -it --network=productinfo-network --hostname=productinfo-client azimbabu:grpc-productinfo-client
```

Delete network productinfo-network

```
docker network rm productinfo-network
```
