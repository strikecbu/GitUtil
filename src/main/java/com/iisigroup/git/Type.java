package com.iisigroup.git;

/**
 * @author AndyChen
 * @version <ul>
 * <li>2020/11/13 AndyChen,new
 * </ul>
 * @since 2020/11/13
 */
public enum Type {

    DEV_TYPE("DEV"),
    SIT_TYPE("SIT"),
    UAT_TYPE("UAT"),
    PROD_TYPE("PROD"),
    ACCUMULATE_TYPE("ACCUMULATE");

    private final String type;

    Type(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
