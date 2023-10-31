package io.metersphere.cdc.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SinkUpgradeElement<T> extends SinkElement {

    private static final long serialVersionUID = 1L;

    private T before;

    private T after;

}

