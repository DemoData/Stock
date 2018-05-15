package com.hitales.functions.clean;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.SplitAnchor;
import com.hitales.common.support.TextFormatter;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

public class CHOperationAnchorUpdate {

    /*static MongoCredential mongoCredential = MongoCredential.createCredential("aron", "HRS", "aron".toCharArray());
    //static ServerAddress serverAddress = new ServerAddress("localhost", 3718);
    static ServerAddress serverAddress = new ServerAddress("192.168.1.153", 27017);
    static MongoCredential mongoCredential = MongoCredential.createCredential("xh", "HRS-live", "rt0hizu{j9lzJNqi".toCharArray());
    static ServerAddress serverAddress = new ServerAddress("localhost", 3718);
//    static ServerAddress serverAddress = new ServerAddress("192.168.1.153", 27017);

    static List<MongoCredential> mongoCredentials = new ArrayList<>();

    static {
        mongoCredentials.add(mongoCredential);
    }

    //static ServerAddress serverAddress = new ServerAddress("localhost", 27017);
    static MongoClient mongo = new MongoClient(serverAddress, mongoCredentials, new MongoClientOptions.Builder().build());
    //static MongoClient mongo = new MongoClient("localhost", 27017);
    static MongoDatabase db = mongo.getDatabase("HRS");*/

    static MongoCredential mongoCredential = MongoCredential.createCredential("aron", "HRS", "aron".toCharArray());

    //static ServerAddress serverAddress = new ServerAddress("localhost", 3718);
    static ServerAddress serverAddress = new ServerAddress("localhost", 27017);

    static List<MongoCredential> mongoCredentials = new ArrayList<>();

    static {
        mongoCredentials.add(mongoCredential);
    }

    //static ServerAddress serverAddress = new ServerAddress("localhost", 27017);
    static MongoClient mongo = new MongoClient(serverAddress, mongoCredentials, new MongoClientOptions.Builder().build());
    //static MongoClient mongo = new MongoClient("localhost", 27017);
    static MongoDatabase db = mongo.getDatabase("HRS");
    static MongoCollection dc = db.getCollection("Record");

    private static MongoTemplate hrsMongoTemplate;

    static {
        MongoProperties mongoProperties = new MongoProperties();
        mongoProperties.setHost("localhost");
        mongoProperties.setPort(27017);
        mongoProperties.setDatabase("HRS");
        mongoProperties.setUsername("aron");
        mongoProperties.setPassword("aron".toCharArray());
        hrsMongoTemplate = DBConnection.generateTemplate(mongoProperties);
    }

    static List<String> anchorsList = new ArrayList<>();

    static {
        /*anchorsList.add("死者姓名");
        anchorsList.add("婚否");
        anchorsList.add("身份证编号");
        anchorsList.add("体 格 检 查");
        anchorsList.add("体    格    检    查");
        anchorsList.add("体  格  检  查");
        anchorsList.add("邮编");
        anchorsList.add("体    格    检    查");*/

        /*anchorsList.add("手术日期");
        anchorsList.add("手术人员");
        anchorsList.add("麻醉人员");*/
//上海六院
        /*anchorsList.add("签署日期");
        anchorsList.add("主治医师签名");*/
    }

    //禁用的锚点
    static List<String> notAnchorList = new ArrayList<>();

    static {

        /*notAnchorList.add("术后诊断");
        notAnchorList.add("手术经过");
        notAnchorList.add("诊断");
        notAnchorList.add("结果");
        notAnchorList.add("术后向患者交代的注意事项");
        notAnchorList.add("穿刺过程");
        notAnchorList.add("内镜诊断");*/
//上海六院
        /*notAnchorList.add("体格检查");
        notAnchorList.add("神经系统");
        notAnchorList.add("手术外伤史");
        notAnchorList.add("输血史");
        notAnchorList.add("过敏史");*/
    }

    //前面不是中文的需要打上锚点
    static List<String> prefAnchorList = new ArrayList<>();

    static {
        prefAnchorList.add("日期");
        prefAnchorList.add("记录时间");
        prefAnchorList.add("病史陈述者");
        prefAnchorList.add("主治医师第一次查房诊断");
    }

    //前面是中文的锚点要去掉
    static List<String> prefNotAnchorList = new ArrayList<>();

    static {

    }

    //前面不能为中文，后面跟冒号作为锚点
    static List<String> colonAnchorList = new ArrayList<>();

    static {

        /*colonAnchorList.add("诊断");
        colonAnchorList.add("结果");
        colonAnchorList.add("术者");
        colonAnchorList.add("操作者");
        colonAnchorList.add("DSA");*/
//上海六院
        /*colonAnchorList.add("时间");
        colonAnchorList.add("手机");*/
    }

    //中括号包围的锚点，中括号是特殊字符
    static List<String> bracketAnchoList = new ArrayList<>();

    static {
    }

    static List<String> colonEndAnchorList = new ArrayList<>();

    static {
        /*colonEndAnchorList.add("死亡时间");
        colonEndAnchorList.add("诊疗经过(抢救经过)");
        colonEndAnchorList.add("死亡诊断");
        colonEndAnchorList.add("入院主要诊断");
        colonEndAnchorList.add("死亡时诊断");
        colonEndAnchorList.add("病理解剖诊断");
        colonEndAnchorList.add("医护工作检查");
        colonEndAnchorList.add("死亡通知书编号");
        colonEndAnchorList.add("科室主任");
        colonEndAnchorList.add("医院负责人意见");
        colonEndAnchorList.add("直接死亡原因");
        colonEndAnchorList.add("根本死亡原因");
        colonEndAnchorList.add("死亡原因");
        colonEndAnchorList.add("病理诊断");
        colonEndAnchorList.add("主治医师");
        colonEndAnchorList.add("住院医师");*/

        /*colonEndAnchorList.add("拟施手术");
        colonEndAnchorList.add("操作步骤");
        colonEndAnchorList.add("内镜诊断");
        colonEndAnchorList.add("穿刺过程");
        colonEndAnchorList.add("具体操作如下");
        colonEndAnchorList.add("手术经过如下");*/

        /*colonEndAnchorList.add("手术过程");
        colonEndAnchorList.add("手术经过");
        colonEndAnchorList.add("术前诊断");

        colonEndAnchorList.add("手术方式");
        colonEndAnchorList.add("麻醉方式");*/

        /*colonEndAnchorList.add("手术经过时间");
        colonEndAnchorList.add("手术记录");
        colonEndAnchorList.add("助手");
        colonEndAnchorList.add("DSA号");
        colonEndAnchorList.add("操作经过");
        colonEndAnchorList.add("操作人员");
        colonEndAnchorList.add("住院医师");
        colonEndAnchorList.add("操作过程如下");*/
//上海六院
        /*colonEndAnchorList.add("床位号");
        colonEndAnchorList.add("记录医生");
        colonEndAnchorList.add("家族史");
        colonEndAnchorList.add("联系方式");
        colonEndAnchorList.add("住院期间特殊检查结果（注明日期与检查号）");
        colonEndAnchorList.add("住院期间病程与治疗结果（注明手术日期、手术名称、输血量及抢救结果）");
        colonEndAnchorList.add("门(急)诊诊断");
        colonEndAnchorList.add("慢性呼吸衰竭II型呼衰");
        colonEndAnchorList.add("初步诊断");
        colonEndAnchorList.add("体 格 检 查");
        colonEndAnchorList.add("实 验 室 检 查");
        colonEndAnchorList.add("辅 助 检 查");
        colonEndAnchorList.add("本 科 检 查");
        //=====
        colonEndAnchorList.add("入院情况");
        colonEndAnchorList.add("入院诊断");
        colonEndAnchorList.add("诊疗经过");
        colonEndAnchorList.add("死亡原因");
        colonEndAnchorList.add("死亡诊断");
        colonEndAnchorList.add("死亡时间");

        colonEndAnchorList.add("主  诉");
        colonEndAnchorList.add("现病史");
        colonEndAnchorList.add("既往史");
        colonEndAnchorList.add("个人史");
        colonEndAnchorList.add("婚育史");
        colonEndAnchorList.add("主  诉");
        colonEndAnchorList.add("副主任");
        colonEndAnchorList.add("中医诊断");*/

        /*colonEndAnchorList.add("手术医师");
        colonEndAnchorList.add("麻醉医师");
        colonEndAnchorList.add("手术名称");
        colonEndAnchorList.add("手术日期");
        colonEndAnchorList.add("术前诊断");
        colonEndAnchorList.add("术中诊断");
        colonEndAnchorList.add("手术护士");
        colonEndAnchorList.add("麻醉方式");
        colonEndAnchorList.add("手术时间");
        colonEndAnchorList.add("手术经过");
        colonEndAnchorList.add("医生签名");

        colonEndAnchorList.add("操作记录");
        colonEndAnchorList.add("操作时间");
        colonEndAnchorList.add("操作过程");

        colonEndAnchorList.add("主持人");
        colonEndAnchorList.add("参加人员");
        colonEndAnchorList.add("手术指征");
        colonEndAnchorList.add("手术方案");
        colonEndAnchorList.add("术前准备");
        colonEndAnchorList.add("记录者签名");*/

        colonAnchorList.add("补充病史和体征");
        colonAnchorList.add("初步诊断");
        colonAnchorList.add("诊断依据");
        colonAnchorList.add("鉴别诊断");
        colonAnchorList.add("诊疗计划");
        colonAnchorList.add("注意事项");
        colonAnchorList.add("分析讨论");
        colonAnchorList.add("入院日期");
        colonAnchorList.add("出院日期");
        colonAnchorList.add("门诊诊断");
        colonAnchorList.add("入院诊断");
        colonAnchorList.add("出院诊断");
        colonAnchorList.add("入院时主要症状及体征");
        colonAnchorList.add("病程与治疗结果（注明手术日期、手术名称、输血量及抢救情况）");
        colonAnchorList.add("病程与治疗结果(注明手术日期、手术名称、输血量及抢救情况)");
        colonAnchorList.add("出院时情况（症状及体征）");
        colonAnchorList.add("出院时情况(症状及体征)");
        colonAnchorList.add("出院后用药及建议");
        colonAnchorList.add("医师签名");
        colonAnchorList.add("主　诉");
        colonAnchorList.add("现病史");
        colonAnchorList.add("既往史");
        colonAnchorList.add("个人史");
        colonAnchorList.add("婚育史");
        colonAnchorList.add("月经史");
        colonAnchorList.add("家族史");
        colonAnchorList.add("体　格　检　查");
        colonAnchorList.add("专　科　检　查");
        colonAnchorList.add("实验室及其他辅助检查");
        colonAnchorList.add("初步诊断");
        colonAnchorList.add("医师签名");
        colonAnchorList.add("日　　期");
        colonAnchorList.add("主要化验结果");
        colonAnchorList.add("特殊检验及重要会诊");
        colonAnchorList.add("合并症");
        colonAnchorList.add("治疗结果");
        colonAnchorList.add("病史可靠程度");
        colonAnchorList.add("病史陈述者");
        colonAnchorList.add("病史采集时间");
        colonAnchorList.add("入院时主要病状及体征");
        colonAnchorList.add("预约提示");
        colonAnchorList.add("主任医师");
        colonAnchorList.add("主治医师");
        colonAnchorList.add("住院医师");
        colonAnchorList.add("记录日期");
        colonAnchorList.add("系统回顾(阳性表现应在空间内填写发病时间及扼要诊疗经过)");
        colonAnchorList.add("姓名");
        colonAnchorList.add("门（急）诊诊断");
        colonAnchorList.add("与患者关系");
    }

    public static void main(String[] args) {
        BasicDBObject docQuery = new BasicDBObject();
        docQuery.append("batchNo", "shly20180424");
        docQuery.append("source", "病历文书");
        BasicDBList recordTypeList = new BasicDBList();
//        recordTypeList.add(new BasicDBObject("recordType", "手术操作记录"));
        recordTypeList.add(new BasicDBObject("recordType", "入院记录"));
        recordTypeList.add(new BasicDBObject("recordType", "出院记录"));
        docQuery.put("$or", recordTypeList);
        long sum = dc.count(docQuery);
        System.out.println(sum);
        FindIterable<Document> iterable = dc.find(docQuery);
        MongoCursor<Document> itor = iterable.iterator();
        int m = 0;
        List<BathUpdateOptions> options = new ArrayList<>();
        while (itor.hasNext()) {
            Document document = itor.next();
            JSONObject jsonObject = JSONObject.parseObject(document.toJson());
            System.out.println(jsonObject.getString("_id"));
            String textARS = jsonObject.getJSONObject("info").getString("text");

           /* String textARS = jsonObject.getJSONObject("info").getString("textARS");
            textARS = TextFormatter.formatTextByAnchaor(textARS);*/
//            String text_back = jsonObject.getJSONObject("info").getString("text_back");
//            textARS = text_back;
            /*if (!jsonObject.getJSONObject("info").containsKey("text_back")) {
                jsonObject.getJSONObject("info").put("text_back", text_back);
            } else {
                textARS = jsonObject.getJSONObject("info").getString("text_back");
            }*/
//            String textARS = jsonObject.getJSONObject("info").getString("textARS");
//            textARS = TextFormatter.formatTextByAnchaor(textARS);
            textARS = textARS.replaceAll("【【【【", "【【").replaceAll("】】】】", "】】");
            /*for(String anchor : anchorsList){
                textARS = textARS.replaceAll(anchor, "【【" + anchor + "】】");
            }*/
            /*textARS = textARS.replaceAll("生前【【工作单位】】", "【【生前工作单位】】");
            textARS = textARS.replaceAll("常住【【户口地址】】", "【【常住户口地址】】");
            textARS = textARS.replaceAll("主诊【【医师】】", "【【主诊医师】】");
            textARS = textARS.replaceAll("发出【【日期】】", "【【发出日期】】");
            textARS = textARS.replaceAll("报告【【日期】】", "【【报告日期】】");
            textARS = textARS.replaceAll("【【身份证】】编号", "【【身份证编号】】");
            textARS = textARS.replaceAll("【【住院经过】】及死亡时情况", "【【住院经过及死亡时情况】】");
            textARS = textARS.replaceAll("死者【【姓名】】", "【【死者姓名】】");
            textARS = textARS.replaceAll("死亡【【日期】】", "【【死亡日期】】");
            textARS = textARS.replaceAll("术后【【病理诊断】】", "【【术后病理诊断】】");
            textARS = textARS.replaceAll("康复【【建议】】", "康复建议");
            textARS = textARS.replaceAll("患者【【入院后完善相关检查】】", "【【患者入院后完善相关检查】】");
            textARS = textARS.replaceAll("检查【【单位】】", "检查单位");
            textARS = textARS.replaceAll("【【门诊】】复查", "门诊复查");*/

            //textARS = textARS.replaceAll("【【原文记录标题】】", "");
            for (String anchor : anchorsList) {
                textARS = textARS.replaceAll(anchor, "【【" + anchor + "】】");
            }
            for (String anchor : notAnchorList) {
                textARS = textARS.replaceAll("【【" + anchor + "】】", anchor);
            }
            for (String anchor : bracketAnchoList) {
                textARS = textARS.replaceAll("\\[" + anchor + "\\]", "【【" + anchor + "】】");
            }
            for (String anchor : prefAnchorList) {
                int lastIndex = 0;
                while (textARS.indexOf(anchor, lastIndex) != -1) {
                    int index = textARS.indexOf(anchor, lastIndex);
                    if (index == 0 || !SplitAnchor.isChinese(textARS.charAt(index - 1))) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(textARS.substring(0, index));
                        stringBuilder.append("【【" + anchor + "】】");
                        stringBuilder.append(textARS.substring(index + anchor.length()));
                        textARS = stringBuilder.toString();
                    }
                    lastIndex = index + anchor.length();
                }
            }
            for (String anchor : prefNotAnchorList) {
                int lastIndex = 0;
                String originAnchor = anchor;
                anchor = "【【" + anchor + "】】";
                while (textARS.indexOf(anchor, lastIndex) != -1) {
                    int index = textARS.indexOf(anchor, lastIndex);
                    if (index != 0 && SplitAnchor.isChinese(textARS.charAt(index - 1))) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(textARS.substring(0, index));
                        stringBuilder.append(originAnchor);
                        stringBuilder.append(textARS.substring(index + anchor.length()));
                        textARS = stringBuilder.toString();
                    }
                    lastIndex = index + anchor.length();
                }
            }
            for (String anchor : colonAnchorList) {
                int lastIndex = 0;
                while (textARS.indexOf(anchor, lastIndex) != -1) {
                    int index = textARS.indexOf(anchor, lastIndex);
                    if ((index == 0 || !SplitAnchor.isChinese(textARS.charAt(index - 1))) &&
                            ('：' == textARS.charAt(index + anchor.length()) || ':' == textARS.charAt(index + anchor.length()))) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(textARS.substring(0, index));
                        stringBuilder.append("【【" + anchor + "】】");
                        stringBuilder.append(textARS.substring(index + anchor.length()));
                        textARS = stringBuilder.toString();
                    }
                    lastIndex = index + anchor.length();
                }
            }
            for (String anchor : colonEndAnchorList) {
                int lastIndex = 0;
                while (textARS.indexOf(anchor, lastIndex) != -1) {
                    int index = textARS.indexOf(anchor, lastIndex);
                    if ('：' == textARS.charAt(index + anchor.length()) || ':' == textARS.charAt(index + anchor.length()) || ' ' == textARS.charAt(index + anchor.length())) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(textARS.substring(0, index));
                        stringBuilder.append("【【" + anchor + "】】");
                        stringBuilder.append(textARS.substring(index + anchor.length()));
                        textARS = stringBuilder.toString();
                    }
                    lastIndex = index + anchor.length();
                }
            }
            textARS = textARS.replaceAll("【【【【", "【【").replaceAll("】】】】", "】】");

            /*textARS = textARS.replaceAll("拟实施【【手术名称】】", "【【拟实施手术名称】】");
            textARS = textARS.replaceAll("实施【【手术名称】】", "【【实施手术名称】】");
            textARS = textARS.replaceAll("术后向患者交代的【【注意事项】】", "【【术后向患者交代的注意事项】】");
            textARS = textARS.replaceAll("向患者告知【【注意事项】】", "【【向患者告知注意事项】】");
            textARS = textARS.replaceAll("手术人员【【\\(第一为主刀医师\\)】】", "【【手术人员(第一为主刀医师)】】");
            textARS = textARS.replaceAll("手术人员【【（第一为主刀医师）】】", "【【手术人员（第一为主刀医师）】】");
            textARS = textARS.replaceAll("【【手术人员】】（第一为主刀医师）", "【【手术人员（第一为主刀医师）】】");
            textARS = textARS.replaceAll("【【手术人员】】\\(第一为主刀医师\\)", "【【手术人员(第一为主刀医师)】】");
            textARS = textARS.replaceAll("【【手术经过】】时间】】", "【【手术经过时间】】");
            textARS = textARS.replaceAll("患者【【术前诊断】】", "【【患者术前诊断】】");*/
            //上海六院
            textARS = textARS.replaceAll("主治【【医师签名】】", "【【主治医师签名】】");
            textARS = textARS.replaceAll("现居【【地址】】", "【【现居地址】】");
            textARS = textARS.replaceAll("主任\n【【医师】】", "【【主任医师】】");
            textARS = textARS.replaceAll("脑梗死【【个人史】】", "脑梗死个人史");
            textARS = textARS.replaceAll("住院【【医师】】", "【【住院医师】】");
            textARS = textARS.replaceAll("手术【【医师】】", "【【手术医师】】");
            textARS = textARS.replaceAll("麻醉【【医师】】", "【【麻醉医师】】");
            textARS = textARS.replaceAll("讨论【【日期】】", "【【讨论日期】】");
            textARS = textARS.replaceAll("\n体温T", "\n【【体格检查】】体温T");
            textARS = textARS.replaceAll("脑梗死【【个人史】】", "脑梗死个人史");
            textARS = textARS.replaceAll("心肌炎【【个人史】】", "心肌炎个人史");
            textARS = textARS.replaceAll("肿瘤【【个人史】】", "肿瘤个人史");
            textARS = textARS.replaceAll("\n主任\n", "\n【【主任】】：");

            /*if (textARS.lastIndexOf("【【术中诊断】】") > 0) {
                String temp = textARS.substring(0, textARS.lastIndexOf("【【术中诊断】】"));
                if (temp.lastIndexOf("【【手术经过】】") > 0) {
                    String pre = textARS.substring(0, textARS.lastIndexOf("【【术中诊断】】"));
                    String suffix = textARS.substring(textARS.lastIndexOf("【【术中诊断】】"));
                    textARS = pre + suffix.replaceAll("【【术中诊断】】", "术中诊断");
                }
            }*/
//上海六院
            int start = textARS.indexOf("诊诊断】】：");
            if (start > 0) {
                String temp = textARS.substring(start + 6, textARS.indexOf("\n【【", start));
                String[] split = temp.split("\n");
                if (split == null || split.length < 2) {
                    continue;
                }
                String newStr = null;
                if (split[0].equals(split[1])) {
                    newStr = split[0];
                    textARS = textARS.replaceAll(temp, newStr);
                    System.out.println("》》》》》》》》》》" + jsonObject.get("_id"));
                }
            }

            textARS = textARS.replaceAll("【【【【", "【【").replaceAll("】】】】", "】】");
            BathUpdateOptions bathUpdateOption = BathUpdateOptions.getInstance();
            bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(jsonObject.get("_id"))));
            bathUpdateOption.setUpdate(Update.update("info.text", textARS)
//                    .set("info.text_back", text_back)
                    .set("updateTime", System.currentTimeMillis()));
            bathUpdateOption.setMulti(true);
            bathUpdateOption.setUpsert(false);
            options.add(bathUpdateOption);
            System.out.println(sum + " " + ++m);
        }
        int count = options.size() / 1000 + (options.size() % 1000 == 0 ? 0 : 1);
        for (int i = 0; i < count; i++) {
            int result = doBathUpdate(hrsMongoTemplate.getCollection("Record"), "Record", options.subList(i * 1000, (i + 1) * 1000 > options.size() ? options.size() : (i + 1) * 1000), true);
            System.out.println(result);
        }
    }

    private static int doBathUpdate(DBCollection dbCollection, String collName,
                                    List<BathUpdateOptions> options, boolean ordered) {
        DBObject command = new BasicDBObject();
        command.put("update", collName);
        List<BasicDBObject> updateList = new ArrayList<BasicDBObject>();
        for (BathUpdateOptions option : options) {
            BasicDBObject update = new BasicDBObject();
            update.put("q", option.getQuery().getQueryObject());
            update.put("u", option.getUpdate().getUpdateObject());
            update.put("upsert", option.isUpsert());
            update.put("multi", option.isMulti());
            updateList.add(update);
        }
        command.put("updates", updateList);
        command.put("ordered", ordered);
        CommandResult commandResult = dbCollection.getDB().command(command);
        if (commandResult == null || commandResult.get("n") == null) {
            return 0;
        }
        return Integer.parseInt(commandResult.get("n").toString());
    }

    static class BathUpdateOptions {
        private Query query;
        private Update update;
        private boolean upsert = false;
        private boolean multi = false;

        public static BathUpdateOptions getInstance() {
            return new BathUpdateOptions();
        }

        public Query getQuery() {
            return query;
        }

        public void setQuery(Query query) {
            this.query = query;
        }

        public Update getUpdate() {
            return update;
        }

        public void setUpdate(Update update) {
            this.update = update;
        }

        public boolean isUpsert() {
            return upsert;
        }

        public void setUpsert(boolean upsert) {
            this.upsert = upsert;
        }

        public boolean isMulti() {
            return multi;
        }

        public void setMulti(boolean multi) {
            this.multi = multi;
        }
    }

}
