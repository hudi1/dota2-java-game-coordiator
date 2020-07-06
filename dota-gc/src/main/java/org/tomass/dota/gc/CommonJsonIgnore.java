
package org.tomass.dota.gc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = { "idqs", "init_", "initAssociations_", "null_", "nullOp_",
        "operators_" }, ignoreUnknown = true)
public class CommonJsonIgnore {
}
