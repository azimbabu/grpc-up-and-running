# Start Prometheus

````
% cd /Users/azim/Learning/observability
% ./prometheus-2.40.5.darwin-arm64/prometheus --config.file=./prometheus.yml
````

# View metrics in Prometheus

To view metrics in prometheus, navigate to:

http://localhost:9090/graph?g0.expr=product_management_server_rpc_count_total%7Brpc_method%3D%22ecommerce.ProductInfo%2FaddProduct%22%7D&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h

http://localhost:9090/graph?g0.expr=product_management_server_rpc_count_total%7Brpc_method%3D%22ecommerce.ProductInfo%2FgetProduct%22%7D&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h

To view application metrics in prometheus format, navigate to:

http://localhost:19090/metrics

# Start Jaegar

````
docker run --rm -it --name jaeger\
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 4317:4317 \
  -p 16686:16686 \
  jaegertracing/all-in-one:1.39
````

# Open the Jaeger UI

Navigate to http://localhost:16686