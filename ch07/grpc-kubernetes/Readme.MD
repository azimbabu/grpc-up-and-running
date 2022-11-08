1. Build docker images in the same terminal.

```
$ docker image build -t azimbabu/grpc-productinfo-server ../grpc-docker/product-info-server
$ docker image build -t azimbabu/grpc-productinfo-client ../grpc-docker/product-info-client
```

2. Load docker images to minikube.

```
$ minikube image load azimbabu/grpc-productinfo-server:latest
$ minikube image load azimbabu/grpc-productinfo-client:latest
```

3. Create server deployment.

```
$ kubectl apply -f server/grpc-productinfo-server-deployment.yaml
$ kubectl apply -f server/grpc-productinfo-server-service.yaml
```

4. Create client job.

```
$ kubectl apply -f client/grpc-productinfo-client-job.yaml
```

5. Create ingress

```
$ kubectl apply -f ingress/grpc-productinfo-ingress.yaml
```

6. Wait for a minute for ADDRESS to show up for the ingress.

```
azim@Khaleques-MBP grpc-kubernetes % kubectl get ingress
NAME                       CLASS   HOSTS                ADDRESS        PORTS     AGE
grpc-productinfo-ingress   nginx   productinfo-server   192.168.49.2   80, 443   2m2s
```

7. Create minikube tunnel from another terminal.

```
$ minikube service productinfo-server
```

8. From current terminal, add this line to the end of /etc/hosts file if you are using minikube on mac M1.

```
azim@Khaleques-MBP grpc-kubernetes % tail -1 /etc/hosts                                 
127.0.0.1 productinfo-server
```

9. Get tunnel port number

```
azim@Khaleques-MBP kubernetes % ps -ef | grep docker@127.0.0.1
  501 70796 68725   0  9:28PM ttys003    0:00.00 grep docker@127.0.0.1
  501 70643 70630   0  9:06PM ttys004    0:00.01 ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -N docker@127.0.0.1 -p 65459 -i /Users/azim/.minikube/machines/minikube/id_rsa -L 50702:10.109.94.101:80 -L 50703:10.109.94.101:443 -L 50704:10.109.94.101:50051
```

From the above output tunnel port is 50704 as can be seen at the end

```
-L 50704:10.109.94.101:50051
```

10. Get list of all services.

```
$ grpcurl -plaintext productinfo-server:50704 list ecommerce.ProductInfo
ecommerce.ProductInfo.addProduct
ecommerce.ProductInfo.getProduct
```

11. Call addProduct and getProduct methods using grpccurl

```
$ grpcurl -plaintext -d @ productinfo-server:50704 ecommerce.ProductInfo/addProduct <<EOM
{
  "name": "Apple iPhone 11",
  "description": "Meet Apple iPhone 11. All-new dual-camera system with Ultra Wide and Night mode.",
  "price": "1000.0"
}
EOM
{
  "value": "030e85a5-9452-416e-b9d6-9b28ddfdb96a"
}

$ grpcurl -plaintext -d @ productinfo-server:50704 ecommerce.ProductInfo/getProduct <<EOM
{
  "value": "030e85a5-9452-416e-b9d6-9b28ddfdb96a"
}
EOM
{
  "name": "Apple iPhone 11",
  "description": "Meet Apple iPhone 11. All-new dual-camera system with Ultra Wide and Night mode.",
  "price": 1000
}
```