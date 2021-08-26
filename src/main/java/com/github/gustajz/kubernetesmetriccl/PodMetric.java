package com.github.gustajz.kubernetesmetriccl;

import io.kubernetes.client.custom.Quantity;
import lombok.Data;

/** @author gustavojotz */
@Data
public class PodMetric {

    private String podName;
    private String containerName;
    private Quantity usageCpu;
    private Quantity usageMemory;
    private Quantity limitsCpu;
    private Quantity limitsMemory;
    private Quantity requestsCpu;
    private Quantity requestsMemory;
}
