package com.github.gustajz.kubernetesmetriccl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KubernetesMetricClApplication {

    public static void main(String[] args) {
        SpringApplication.run(KubernetesMetricClApplication.class, args).close();
    }
}
