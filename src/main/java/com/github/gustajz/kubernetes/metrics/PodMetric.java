package com.github.gustajz.kubernetes.metrics;

import io.kubernetes.client.custom.Quantity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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

    public String percentOfRequests() {
        return percentOfRequests(usageCpu, requestsCpu);
    }

    public String getPercentOfMemoryRequests() {
        return percentOfRequests(usageMemory, requestsMemory);
    }

    private String percentOfRequests(Quantity usage, Quantity requests) {
        if (requests != null) {
            var percent =
                    usage.getNumber()
                            .multiply(BigDecimal.valueOf(100L))
                            .divide(requests.getNumber(), RoundingMode.HALF_UP);

            return new DecimalFormat("#0.00'%'").format(percent);
        }
        return null;
    }
}
