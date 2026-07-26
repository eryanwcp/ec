package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class BaseDataCube extends BaseModel {

    @JsonProperty(""ref_date", format = "yyyy-MM-dd")
    private Date refDate;

    public Date getRefDate() {
        return refDate;
    }

    public BaseDataCube setRefDate(Date refDate) {
        this.refDate = refDate;
        return this;
    }
}
