package com.hitales.entity;

import lombok.Data;

import java.util.List;

/**
 * 入库基本信息
 *
 * @author aron
 */
@Data
public class StockInfo {
    private String batchNo;
    private String hospitalId;
    private String userId;
    private List<String> odCategories;//病种
    private String department;//科室
    private String status;
    private String prefix;
    private Double version;
}
