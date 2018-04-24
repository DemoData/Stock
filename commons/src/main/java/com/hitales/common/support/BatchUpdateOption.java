package com.hitales.common.support;

import lombok.Data;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Data
public class BatchUpdateOption {
    private Query query;
    private Update update;
    private boolean upsert = false;
    private boolean multi = false;
}
