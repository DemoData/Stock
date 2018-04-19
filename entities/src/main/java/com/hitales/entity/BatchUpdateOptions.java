package com.hitales.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Data
public class BatchUpdateOptions {
    private Query query;
    private Update update;
    private boolean upsert = false;
    private boolean multi = false;
}
