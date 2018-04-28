package com.hitales.entity;

import lombok.Data;

@Data
public class LogRecord {
    private Double version;
    private String recordType;
    private String subRecordType;
}
