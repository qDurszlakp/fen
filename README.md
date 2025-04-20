[![Integration Tests](https://github.com/qDurszlakp/fen/actions/workflows/maven.yml/badge.svg)](https://github.com/qDurszlakp/k8s/actions/workflows/maven.yml)

````
Hey you!

Stack: 
Java
Spring Boot
PostgresSql
Terraform
Docker
Kafka
k8s
````
**Commands**
````
./run.sh                      - Build and run the project on local docker server.
./remove_containers.sh        - Remove containers.
./k8s_run.sh                  - Build and run the project on local k8s cluster.
````

```
k8s:
terraform apply -var 'registry_password=<YOUR_DOCKER_HUB_PASSWORD>' -auto-approve
```