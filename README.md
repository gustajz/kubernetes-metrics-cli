# Kubernetes Metrics Cli

### Development
```
./gradlew clean build
```

### Run
```
➜  java -jar build/libs/kubernetes-metric-cl-0.0.1-SNAPSHOT.jar --help
Usage: <main class> [-h] [-no] [-po] -n=<namespace>
Prints information about current usage, limits and request of resources.
  -h, --help         Show this help message and exit.
  -n, --namespace=<namespace>
                     Namespace name.
      -no, --nodes   Nodes metrics.
      -po, --pods    Pods metrics and configuration.
```

## License

This project is licensed under the Apache License Version 2.0 (see
[LICENSE](./LICENSE)).
