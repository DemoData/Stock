package com.hitales.common.support;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SplitAnchor {

    private LineItem preLine;
    private LineItem line;
    private LineItem nextLine;
    //不需要检查后面是否需要冒号的锚点
    private static List<String> notNeed = new ArrayList<>();

    static {
        //notNeed.add("主诉");
        //notNeed.add("电话");
        //notNeed.add("住址");
        //notNeed.add("民族");
        //notNeed.add("婚否");
        //notNeed.add("职业");

        //notNeed.add("性别");
        //notNeed.add("年龄");
    }

    private static List<String> need = new ArrayList<>();

    static {
        need.add("治疗方案");
        need.add("进一步检查");
        need.add("出院医嘱");
        need.add("化验检查");
        need.add("出院带药");
        need.add("过敏史");
        need.add("专科检查");
        need.add("输血史");
        //need.add("体格检查");
        need.add("入院后完善相关检查");
        need.add("诊断");
        need.add("上级医师");
        need.add("主任医师");
        need.add("出院计划");
        need.add("自动出院");
        need.add("辅助检查");
        need.add("门诊诊断");
        need.add("治疗情况");
        need.add("手术日期");
        need.add("手术名称");
        need.add("主治医师");
        need.add("出院用药");
        need.add("姓名");
        need.add("性别");
        need.add("年龄");
        need.add("职业");
        need.add("婚姻");
        need.add("主诉");
        need.add("主  诉");
        need.add("民族");
        need.add("建议");
        need.add("病理结果");
        need.add("随访计划");
        need.add("上级医生");
        need.add("病案号");
        need.add("MR号");
        need.add("疼痛部位");
        need.add("病理号");
        need.add("病例特点");
        //need.add("现病史");
    }

    private static List<String> forbid = new ArrayList<>();

    static {
        forbid.add("晨僵");
    }

    private List<String> anchors;

    private ArrayList<AnchorInfo> anchorInfos = new ArrayList<AnchorInfo>() {
        @Override
        public boolean add(AnchorInfo anchorInfo) {
            for (int i = 0; i < anchorInfos.size(); i++) {
                AnchorInfo info = anchorInfos.get(i);
                if (info.startPos == anchorInfo.startPos && info.endPos == anchorInfo.endPos) {
                    return false;
                }
            }
            return super.add(anchorInfo);
        }
    };

    private String[] anchorCandicatePatterns = {
            "^【[\u4e00-\u9fa5]+】",
            //"^[1-9]、[\\u4e00-\\u9fa5]+[：|:]",
            "^[一|二|三|四|五|六|七|八|九]、[\u4e00-\u9fa5]+[：|:]",
            "^[\u4e00-\u9fa5]{1,20}[：|:]"
    };

    private int[][] keyAnchorCandidates = {
            {1, -1},
            //{2, -1},
            {2, -1},
            {0, -1}
    };

    /**
     * 除了第一项黑框表达式，其它的须有如下后缀
     */
    private String[] sugAnchorSuffixs = {"分析", "如下", "签字", "鉴别", "号码", "人员", "意见", "查房", "进展", "指出", "建议", "认为", "总结", "日期"
            , "发现", "于下", "发言", "措施", "回报", "查房录", "声明", "已返", "事项", "病史", "时刻", "及其它", "签名", "查房", "医师", "原因", "时间", "职称"
            , "情况"
    };

    protected Pattern[] candidatesPatterns = new Pattern[anchorCandicatePatterns.length];

    public SplitAnchor(LineItem preLine, LineItem line, LineItem nextLine, List<String> anchors) {
        this.preLine = preLine;
        this.line = line;
        this.nextLine = nextLine;
        this.anchors = anchors;
        for (int i = 0; i < candidatesPatterns.length; i++) {
            candidatesPatterns[i] = Pattern.compile(anchorCandicatePatterns[i]);
        }
        findAnchorCandidates();
    }

    /**
     * 发现候选锚点, 即推荐锚点
     */
    public void findAnchorCandidates() {
        /*try {
            for (int i = 0; i < candidatesPatterns.length; i++) {
                Matcher matcher = candidatesPatterns[i].matcher(line.line);
                boolean found = matcher.find();
                if (found){
                    int start = matcher.start(0);
                    int end = matcher.end(0);
                    String anchorCan = line.line.substring(start+keyAnchorCandidates[i][0], end+keyAnchorCandidates[i][1]);
                    if(forbid.contains(anchorCan)){
                        //System.out.println(anchorCan);
                        continue;
                    }
                    //刚好2个字符的默认也是可以的,也是合法锚点
                    if (i > 0 && anchorCan.length() != 2){
                        boolean isLegal = false;
                        for (int j = 0; j < sugAnchorSuffixs.length; j++) {
                            if (anchorCan.endsWith(sugAnchorSuffixs[j])){
                                isLegal = true;
                                break;
                            }
                        }
                        if (!isLegal){
                            //不推荐这个锚点，因为不符合尾词规范
                            break;
                        }
                    }
                    line.candidateAnchor = anchorCan;
                    break;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }*/
    }

    /**
     * 匹配锚点
     */
    public void tagAnchor() {
//        if (line.line.equals("主任医师签名：张子峰")){
//            System.out.println("dddd");
//        }
        //匹配
        for (int i = 0; i <= anchors.size(); i++) {

            String anchor = null;
            boolean isStandardAnchor = false;
            if (i == anchors.size()) {
                //实际匹配的anchor, 这里是动态匹配的锚点
                //TODO 这个动态匹配很有风险
                if (line.candidateAnchor != null) {
                    anchor = line.candidateAnchor;
                } else {
                    break;
                }
            } else {
                //配置好的anchor
                anchor = anchors.get(i);
                isStandardAnchor = true;
            }
            if ("建议".equals(anchors.get(i)) && !line.line.contains("出院诊断")) {
                continue;
            }
            int lastIndex = 0;
            while (line.line.indexOf(anchor, lastIndex) >= 0) {
                int index = line.line.indexOf(anchor, lastIndex);
                if (line.line.indexOf(anchor, lastIndex) != 0 && (line.line.charAt(index - 1) == '。') && !need.contains(anchor)) {

                } else {
                    if (!notNeed.contains(anchor)) {
                        isStandardAnchor = false;
                    }
                    //if (anchor.length() <= 2 && !isStandardAnchor || need.contains(anchor)){
                    if (!isStandardAnchor || need.contains(anchor)) {
                        //2个字内的锚点必须后续有冒号
                        if (!(line.line.length() > index + anchor.length() && (line.line.charAt(index + anchor.length()) == '：'
                                || line.line.charAt(index + anchor.length()) == '】' || line.line.charAt(index + anchor.length()) == ':'
                        ))) {
                            lastIndex = line.line.indexOf(anchor, lastIndex) + 1;
                            continue;
                        }
                    }
                    if (line.line.indexOf(anchor, lastIndex) != 0 && (line.line.charAt(index - 1) == '、' || line.line.charAt(index - 1) == '：'
                            || line.line.charAt(index - 1) == '见' || line.line.charAt(index - 1) == '.')) {
                        lastIndex = line.line.indexOf(anchor, lastIndex) + 1;
                        continue;
                    }
                    if ((line.line.indexOf(anchor, lastIndex) != 0 && isChinese(line.line.charAt(index - 1)))
                            && (line.line.length() > index + anchor.length() && isChinese(line.line.charAt(index + anchor.length())))) {
                        lastIndex = line.line.indexOf(anchor, lastIndex) + 1;
                        continue;
                    }
                }
                AnchorInfo anchorInfo = new AnchorInfo(line.line.indexOf(anchor, lastIndex), line.line.indexOf(anchor, lastIndex) + anchor.length() - 1, anchor);
                anchorInfos.add(anchorInfo);
                lastIndex = line.line.indexOf(anchor, lastIndex) + 1;
            }
        }

        //判断有效性
        for (int i = 0; i < anchorInfos.size(); i++) {
            for (int j = 0; j < anchorInfos.size(); j++) {
                anchorInfos.get(i).checkLegal(anchorInfos.get(j));
            }
        }

        //移除无效的
        ArrayList<AnchorInfo> anchorInfosOld = anchorInfos;
        anchorInfos = new ArrayList<AnchorInfo>();
        for (int i = 0; i < anchorInfosOld.size(); i++) {
            if (anchorInfosOld.get(i).isLegal()) {
                anchorInfos.add(anchorInfosOld.get(i));
            }
        }

        line.setAnchorInfos(anchorInfos);
    }

    public static boolean isChinese(char c) {
        return (c >= 0x4E00 && c <= 0x9FA5) || c == '【' || c == '（' || c == '(' ;// 根据字节码判断
    }

    public static void main(String[] args) {
        int a = "abc".indexOf("ab");
        System.out.println("abc".substring(0));
    }

}
