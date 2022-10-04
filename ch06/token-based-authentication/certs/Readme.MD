Generate Server Private RSA Key server.key

```
$ openssl genrsa -out server.key 2048
```

Generate RSA Private Key with a password for CA. Password is abcd1234

```
$ openssl genrsa -aes256 -out ca.key 4096
```

Generate Root CA certificate with a validity of two years using the SHA256 hash algorithm. Use
localhost as Common Name.

```
$ openssl req -new -x509 -sha256 -days 730 -key ca.key -out ca.crt
```

Create a certificate signing request.

```
$ openssl req -new -sha256 -key server.key -out server.csr
```

Use root CA to sign the CSR and create server certificate.

```
$ openssl x509 -req -days 365 -sha256 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 1 -out server.crt
```

Generate client private key.

```
$ openssl genrsa -out client.key 2048
```

Generate client certificate signing request or CSR. Use localhost as Common Name.

```
$ openssl req -new -key client.key -out client.csr
```

Generate client certificate.

```
$ openssl x509 -req -days 365 -sha256 -in client.csr -CA ca.crt -CAkey ca.key -set_serial 2 -out client.crt
```

Convert server/client keys to pem format.

```
$ openssl pkcs8 -topk8 -inform pem -in server.key -outform pem -nocrypt -out server.pem
$ openssl pkcs8 -topk8 -inform pem -in client.key -outform pem -nocrypt -out client.pem
```