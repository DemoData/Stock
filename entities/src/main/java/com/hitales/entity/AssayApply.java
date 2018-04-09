package com.hitales.entity;

import lombok.Data;

/**
 * 化验申请单
 *
 * @author aron
 */
@Data
public class AssayApply {

    private String id;//id
    private String groupRecordName;//一次就诊号
    private String assayName;//项目名称
    private String subItemEnName;//检验子项英文名
    private String subItemEnCode;//检验子项编码
    private String applyId;//检验申请号
    private String stateName;//状态名称
    private String applyDate;//申请时间
    private String specimen;//标本

    public enum ColumnMapping {
        ASSAY_NAME("项目名称"),
        SUB_ITEM_EN_NAME("检验子项英文名"),
        SUB_ITEM_EN_CODE("检验子项编码"),
        APPLY_ID("检验申请号"),
        STATE_NAME("状态名称"),
        APPLY_DATE("申请时间"),
        SPECIMEN("标本");

        private final String value;

        ColumnMapping(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }
}