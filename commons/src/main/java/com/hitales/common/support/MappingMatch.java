package com.hitales.common.support;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.*;

/**
 * 类型映射支持类
 *
 * @author aron
 */
public class MappingMatch {

    private static String[] excludeArray = {"宣教", "清单", "报告卡", "计划", "告知", "计划", "通知书", "评分", "健康教育", "全院通用", "责任书", "心电图、TCD、脑电图", "病历名称",
            "输血反应报告", "体检封面", "检测表", "自费", "饮食-低脂普食", "入院介绍表", "同意书", "承诺书", "记录单", "志愿书",
            "申请单", "审批表", "评估单", "评估表", "登记表", "报告单", "观察表", "监控表", "指肠切除术", "胰腺癌化疗", "胰腺肿瘤手术", "输血", "镇痛记录", "高危患者", "冠脉造影", "病史摘要",
            "专用单", "评价表", "调查表", "拒绝书", "讨论记录", "术前小结", "输血前评估", "谈话记录", "死亡", "死亡小结", "出院小结", "出院小节", "住院小结", "经超声胃镜超声造影记录", "病案首页", "小时内入出院", "24小时", "入出院", "甲状腺腺癌"};

    public static void addMappingRule(MongoTemplate mongoTemplate) {
        List<JSONObject> mapping = new ArrayList<>();
        JSONObject other = new JSONObject();
        List<JSONObject> subList = new ArrayList<>();
        other.put("type", "其他记录");
        other.put("level", 1);
        other.put("rank", 1);
        other.put("subType", subList);
        //其他记录
        String[] containArray = new String[]{"宣教", "清单", "报告卡", "计划", "告知", "计划", "通知书", "评分", "健康教育", "全院通用", "责任书", "心电图、TCD、脑电图", "病历名称",
                "输血反应报告", "体检封面", "检测表", "自费", "饮食-低脂普食", "入院介绍表", "同意书", "承诺书", "记录单", "志愿书",
                "申请单", "审批表", "评估单", "评估表", "登记表", "报告单", "观察表", "监控表", "指肠切除术", "胰腺癌化疗", "胰腺肿瘤手术", "输血", "镇痛记录", "高危患者", "冠脉造影", "病史摘要",
                "专用单", "评价表", "调查表", "拒绝书", "讨论记录", "术前小结", "输血前评估", "谈话记录"};
        //TODO:改进成Object类型 然后通过哦按段类型做不同的处理方式
        subList.add(generateItem(containArray, new String[]{"查房", "病程"}, "其他", null, 2, 1));
        subList.add(generateItem(new String[]{"会诊单", "会诊记录"}, null, "会诊单", null, 2, 2));
        subList.add(generateItem(new String[]{"体温单"}, null, "体温单", null, 2, 3));
        subList.add(generateItem(new String[]{"告知书"}, null, "告知书", null, 2, 4));
        subList.add(generateItem(new String[]{"知情", "知情书", "知情同意书"}, null, "知情文件", null, 2, 5));
        subList.add(generateItem(new String[]{"护理记录", "入院护理评估"}, null, "护理文书", null, 2, 6));
        subList.add(generateItem(new String[]{"临时"}, null, "临时文件", null, 2, 7));
        subList.add(generateItem(new String[]{"协议书"}, null, "协议书", null, 2, 8));
        //治疗方案
        JSONObject bingcheng = new JSONObject();
        subList = new ArrayList<>();
        bingcheng.put("type", "治疗方案");
        bingcheng.put("level", 1);
        bingcheng.put("rank", 2);
        bingcheng.put("subType", subList);
        subList.add(generateItem(new String[]{"ICU出室记录"}, excludeArray, "ICU出室记录", null, 2, 1));
        subList.add(generateItem(new String[]{"ICU入室记录"}, excludeArray, "ICU入室记录", null, 2, 2));
        subList.add(generateItem(new String[]{"病程", "查房", "转出", "转入", "危急值记录", "危急值报告", "拔管记录", "术后记录", "术后第"}, excludeArray, "病程", null, 2, 3));
        subList.add(generateItem(new String[]{"医嘱单"}, excludeArray, "医嘱单", null, 2, 4));
        //化验记录
        /*JSONObject assay = new JSONObject();
        subList = new ArrayList<>();
        assay.put("type", "化验记录");
        assay.put("level", 1);
        assay.put("rank", 3);
        assay.put("subType", subList);
        subList.add(generateItem(new String[]{"微生物"}, null, "微生物", null, 2, 1));
        subList.add(generateItem(new String[]{"化验记录"}, null, "化验", null, 2, 2));*/
        //门诊记录
        JSONObject menzhen = new JSONObject();
        subList = new ArrayList<>();
        menzhen.put("type", "门诊记录");
        menzhen.put("level", 1);
        menzhen.put("rank", 3);
        menzhen.put("subType", subList);
        subList.add(generateItem(new String[]{"门诊记录"}, null, "门诊", null, 2, 1));
        //出院
        JSONObject chuyuan = new JSONObject();
        subList = new ArrayList<>();
        chuyuan.put("type", "出院记录");
        chuyuan.put("level", 1);
        chuyuan.put("rank", 4);
        chuyuan.put("subType", subList);
        subList.add(generateItem(new String[]{"死亡小结"}, containArray, "死亡小结", null, 2, 1));
        subList.add(generateItem(new String[]{"死亡"}, new String[]{"讨论"}, "死亡记录", null, 2, 2));
        subList.add(generateItem(new String[]{"出院小结", "出院小节", "住院小结"}, containArray, "出院小结", null, 2, 3));
        subList.add(generateItem(new String[]{"出院", "经超声胃镜超声造影记录"}, new String[]{"EUS+FNA", "EUS引导下FNA术", "穿刺置管记录", "穿刺记录", "引流术", "超声内镜+穿刺术", "超声胃镜+FNA术", "切除术", "操作记录", "消融术记录", "检查记录", "胃镜诊疗记录", "胃镜检查", "胃镜下介入治疗记录", "病程", "查房", "入出院", "出院指导"}, "出院记录", null, 2, 4));
        //入院
        JSONObject ruyuan = new JSONObject();
        subList = new ArrayList<>();
        ruyuan.put("type", "入院记录");
        ruyuan.put("level", 1);
        ruyuan.put("rank", 5);
        ruyuan.put("subType", subList);
        subList.add(generateItem(new String[]{"病案首页"}, containArray, "病案首页", null, 2, 1));
        subList.add(generateItem(new String[]{"小时内入出院", "24小时"}, containArray, "24小时内入出院", null, 2, 2));
        subList.add(generateItem(new String[]{"入院", "入出院", "甲状腺腺癌"}, new String[]{"EUS+FNA", "EUS引导下FNA术", "穿刺置管记录", "穿刺记录", "引流术", "超声内镜+穿刺术", "超声胃镜+FNA术", "切除术", "操作记录", "消融术记录", "检查记录", "胃镜诊疗记录", "胃镜检查", "胃镜下介入治疗记录", "护理评估", "介绍表", "入院模板", "病情告知", "死亡"}, "入院记录", null, 2, 3));
        //手术操作记录
        JSONObject operate = new JSONObject();
        subList = new ArrayList<>();
        operate.put("type", "手术操作记录");
        operate.put("level", 1);
        operate.put("rank", 6);
        operate.put("subType", subList);
        subList.add(generateItem(new String[]{"术前讨论"}, containArray, "术前讨论", null, 2, 1));
        subList.add(generateItem(new String[]{"引导下&&术记录", "EUS+FNA", "EUS引导下FNA术", "穿刺置管", "穿刺记录", "引流术", "超声内镜+穿刺术", "超声胃镜+FNA术", "切除术", "操作记录", "消融术记录"}, containArray, "操作记录", null, 2, 2));
        subList.add(generateItem(new String[]{"手术记录", "手术过程", "股静脉置管", "透析记录"}, containArray, "手术记录", null, 2, 3));
        //病理
        JSONObject bingli = new JSONObject();
        subList = new ArrayList<>();
        bingli.put("type", "病理");
        bingli.put("level", 1);
        bingli.put("rank", 7);
        bingli.put("subType", subList);
        subList.add(generateItem(new String[]{"病理", "TNM分期"}, null, "病理", null, 2, 1));
        //检查记录
        JSONObject inspection = new JSONObject();
        subList = new ArrayList<>();
        inspection.put("type", "检查记录");
        inspection.put("level", 1);
        inspection.put("rank", 8);
        inspection.put("subType", subList);
        subList.add(generateItem(new String[]{"检查记录", "胃镜诊疗记录", "胃镜检查", "胃镜下介入治疗记录"}, null, "检查记录", null, 2, 1));
        subList.add(generateItem(new String[]{"CT&&检查"}, null, "CT", null, 2, 2));
        subList.add(generateItem(new String[]{"心电图&&检查"}, null, "心电图", null, 2, 3));
        subList.add(generateItem(new String[]{"超声&&检查", "超声常规检查"}, null, "超声", null, 2, 4));
        subList.add(generateItem(new String[]{"MR&&检查"}, null, "MR", null, 2, 5));
        subList.add(generateItem(new String[]{"X光&&检查"}, null, "X光", null, 2, 6));

        mapping.add(other);
        mapping.add(ruyuan);
        mapping.add(bingcheng);
        mapping.add(chuyuan);
        mapping.add(inspection);
//        mapping.add(assay);
        mapping.add(operate);
        mapping.add(menzhen);
        mapping.add(bingli);
        mongoTemplate.insert(mapping, "Mapping");
    }

    private static JSONObject generateItem(Object[] include, Object[] exclude, String typeValue, List<JSONObject> subType, Integer level, Integer rank) {
        /*
        rule :1 代表 包含key对应的值的规则，例如 include=宣教 exclude=清单 则rule为 包含'宣教' and 不包含'清单' 的取值mappedValue
        rule :2 待定
         */
        JSONObject item = new JSONObject();
        item.put("include", include);
        item.put("exclude", exclude);
        Map<String, String> rule = new HashMap<>();
        rule.put("include", "OR");
        rule.put("exclude", "AND");
        item.put("rule", rule);
        item.put("level", level);
        item.put("rank", rank);
        item.put("type", typeValue);
        item.put("subType", subType);
        return item;
    }

    public static String getMappedValue(List<Mapping> mappings, String content) {
        Collections.sort(mappings);
        for (Mapping mapping : mappings) {
            String mappedValue = executeMapping(mapping, content, mapping.getType());
            if (StringUtils.isNotBlank(mappedValue)) {
                return mappedValue;
            }
        }
        return "其他记录-其他";
    }

    /**
     * 递归遍历
     *
     * @param mapping
     * @param content
     * @param parentType
     * @return
     */
    private static String executeMapping(Mapping mapping, String content, String parentType) {
        String mappedValue = null;
        if (mapping == null || StringUtils.isBlank(content)) {
            return null;
        }

        List<Mapping> subType = mapping.getSubType();
        if (subType != null && !subType.isEmpty()) {
            //按照rank大小排序
            Collections.sort(subType);
            for (Mapping subMapping : subType) {
                mappedValue = executeMapping(subMapping, content, StringUtils.isBlank(parentType) ? subMapping.getType() : parentType);
                if (StringUtils.isNotBlank(mappedValue)) {
                    return mappedValue;
                }
            }
            return null;
        }
        String currentType = mapping.getType();
        String[] includes = mapping.getInclude();
        if (includes == null || includes.length == 0) {
            return null;
        }
        for (String includeKey : includes) {
            mappedValue = checkInclude(includeKey, mapping, content, currentType, parentType);
            if (StringUtils.isNotBlank(mappedValue)) {
                return mappedValue;
            }
        }
        return null;
    }

    /**
     * checkInclude
     *
     * @param includeKey
     * @param mapping
     * @param content
     * @param currentType
     * @param parentType
     * @return
     */
    private static String checkInclude(String includeKey, Mapping mapping, String content, String currentType, String parentType) {
        if (includeKey.indexOf("&&") > 0) {
            String[] strings = includeKey.split("&&");
            boolean IsMatched = true;
            for (String str : strings) {
                if (!content.contains(str)) {
                    IsMatched = false;
                    break;
                }
            }
            if (IsMatched) {
                return checkExclude(mapping, content, currentType, parentType);
            }
        } else if (content.contains(includeKey)) {
            return checkExclude(mapping, content, currentType, parentType);
        }
        return null;
    }

    /**
     * checkExclude
     *
     * @param mapping
     * @param content
     * @param currentType
     * @param parentType
     * @return
     */
    private static String checkExclude(Mapping mapping, String content, String currentType, String parentType) {
        String[] exclude = mapping.getExclude();
        //如果沒有需要去除的字段匹配，就直接返回
        if (exclude == null || exclude.length == 0) {
            return StringUtils.isBlank(parentType) ? currentType : parentType + "-" + currentType;
        }
        for (String excludeKey : exclude) {
            if (content.contains(excludeKey)) {
                return null;
            }
        }
        return StringUtils.isBlank(parentType) ? currentType : parentType + "-" + currentType;
    }

}
