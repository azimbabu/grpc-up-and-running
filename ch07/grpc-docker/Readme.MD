1. Create network productinfo-network
```
docker network create productinfo-network
```

2. Build and run the server docker image

```
$ docker image build -t azimbabu/grpc-productinfo-server product-info-server
$ docker run -it --network=productinfo-network --name=productinfo-server --hostname=productinfo-server -p 50051:50051  azimbabu/grpc-productinfo-server
```

3. From another terminal, build and run the client docker image

```
$ docker image build -t azimbabu/grpc-productinfo-client product-info-client
$ docker run -it --network=productinfo-network --hostname=productinfo-client azimbabu/grpc-productinfo-client
```

4. Delete network productinfo-network
```
docker network rm productinfo-network
```
