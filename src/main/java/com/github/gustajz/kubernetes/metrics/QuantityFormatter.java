package com.github.gustajz.kubernetes.metrics;

import io.kubernetes.client.custom.Quantity;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.Function;

/** @author gustavojotz */
public class QuantityFormatter implements Function<Quantity, String> {

    @Override
    public String apply(Quantity quantity) {
        if (quantity != null) {
            if (quantity.getFormat() == Quantity.Format.BINARY_SI) {
                var ki = quantity.toSuffixedString();
                if (ki.endsWith("Ki")) {
                    var mi =
                            quantity.getNumber()
                                    .divideToIntegralValue(BigDecimal.valueOf(1024L * 1024L));
                    return String.format("%sMi", mi);
                }
                return ki;
            } else if (quantity.getFormat() == Quantity.Format.DECIMAL_SI) {
                var c = quantity.getNumber().multiply(BigDecimal.valueOf(1000L));
                return new DecimalFormat("#0'm'").format(c);
            }
            return new DecimalFormat("#0.000").format(quantity.getNumber());
        }
        return null;
    }
}
