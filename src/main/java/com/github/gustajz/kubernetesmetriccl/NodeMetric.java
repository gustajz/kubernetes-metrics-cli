package com.github.gustajz.kubernetesmetriccl;

import io.kubernetes.client.custom.Quantity;
import lombok.Data;

/** @author gustavojotz */
@Data
public class NodeMetric {

    private String name;
    private Quantity usageCpu;
    private Quantity usageMemory;
}
