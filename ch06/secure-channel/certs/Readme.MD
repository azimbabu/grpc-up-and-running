Generate Server Private RSA Key server.key

```
$ openssl genrsa -out server.key 2048
```

Generate Server Public Certificate server.crt. Use localhost as Common Name.

```
$ openssl req -new -x509 -sha256 -key server.key -out server.crt -days 3650
```

Convert Server Private Key to PEM format

```
$ openssl pkcs8 -topk8 -inform pem -in server.key -outform pem -nocrypt -out server.pem
```

