package com.github.gustajz.kubernetes.metrics;

import io.kubernetes.client.custom.Quantity;
import lombok.Data;

/** @author gustavojotz */
@Data
public class NodeMetric {

    private String name;
    private Quantity usageCpu;
    private Quantity usageMemory;
}
