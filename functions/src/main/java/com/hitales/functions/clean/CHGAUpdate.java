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

public class CHGAUpdate {

    /*static MongoCredential mongoCredential = MongoCredential.createCredential("aron", "HRS", "aron".toCharArray());
    //static ServerAddress serverAddress = new ServerAddress("localhost", 3718);
    static ServerAddress serverAddress = new ServerAddress("192.168.1.153", 27017);

    static List<MongoCredential> mongoCredentials = new ArrayList<>();

    static {
        mongoCredentials.add(mongoCredential);
    }

    //static ServerAddress serverAddress = new ServerAddress("localhost", 27017);
    static MongoClient mongo = new MongoClient(serverAddress, mongoCredentials, new MongoClientOptions.Builder().build());
    //static MongoClient mongo = new MongoClient("localhost", 27017);
    static MongoDatabase db = mongo.getDatabase("HRS");*/

    static MongoCredential mongoCredential = MongoCredential.createCredential("yy", "HRS-live", "rf1)Rauwu3dpsGid".toCharArray());

    //static ServerAddress serverAddress = new ServerAddress("localhost", 3718);
    static ServerAddress serverAddress = new ServerAddress("localhost", 3718);

    static List<MongoCredential> mongoCredentials = new ArrayList<>();
    static {
        mongoCredentials.add(mongoCredential);
    }
    //static ServerAddress serverAddress = new ServerAddress("localhost", 27017);
    static MongoClient mongo = new MongoClient(serverAddress, mongoCredentials, new MongoClientOptions.Builder().build());
    //static MongoClient mongo = new MongoClient("localhost", 27017);
    static MongoDatabase db = mongo.getDatabase("HRS-live");
    static MongoCollection dc = db.getCollection("Record");

    private static MongoTemplate hrsMongoTemplate;

    static {
        MongoProperties mongoProperties = new MongoProperties();
        mongoProperties.setHost("localhost");
        mongoProperties.setPort(3718);
        mongoProperties.setDatabase("HRS-live");
        mongoProperties.setUsername("yy");
        mongoProperties.setPassword("rf1)Rauwu3dpsGid".toCharArray());
        hrsMongoTemplate = DBConnection.generateTemplate(mongoProperties);
    }


    static List<String> anchorsList = new ArrayList<>();

    static {
        anchorsList.add("死者姓名");
        anchorsList.add("婚否");
        anchorsList.add("身份证编号");
        anchorsList.add("体 格 检 查");
        anchorsList.add("体    格    检    查");
        anchorsList.add("体  格  检  查");
        anchorsList.add("邮编");
    }

    //禁用的锚点
    static List<String> notAnchorList = new ArrayList<>();

    static {
        notAnchorList.add("出院带药");
        notAnchorList.add("住院");
        notAnchorList.add("影像号");
        notAnchorList.add("疼痛评分");
        notAnchorList.add("门诊");
        notAnchorList.add("健康指导");
        notAnchorList.add("MRI号");
        notAnchorList.add("住院号");
        notAnchorList.add("CT号");
        notAnchorList.add("病人ID");
        notAnchorList.add("登记号");
        notAnchorList.add("彩超号");
        notAnchorList.add("电话");
    }

    //前面不是中文的需要打上锚点
    static List<String> prefAnchorList = new ArrayList<>();

    static {
        prefAnchorList.add("性别");
        prefAnchorList.add("出生日期");
        prefAnchorList.add("年龄");
    }

    //前面是中文的锚点要去掉
    static List<String> prefNotAnchorList = new ArrayList<>();

    static {
        prefNotAnchorList.add("住院医师");
        prefNotAnchorList.add("主治医师");
        prefNotAnchorList.add("建议");
    }

    //前面不能为中文，后面跟冒号作为锚点
    static List<String> colonAnchorList = new ArrayList<>();

    static {
        colonAnchorList.add("主  诉");
        colonAnchorList.add("并发症");
        colonAnchorList.add("医疗院长");
        colonAnchorList.add("抢救措施");
        colonAnchorList.add("查体");
        colonAnchorList.add("住院经过及死亡时情况");
        colonAnchorList.add("治疗情况");
        colonAnchorList.add("病史叙述人");
        colonAnchorList.add("现病史");
        colonAnchorList.add("普通门诊");
        colonAnchorList.add("中医诊断");
        colonAnchorList.add("医疗院长");
        colonAnchorList.add("MRI号");
        colonAnchorList.add("住院号");
        colonAnchorList.add("CT号");
        colonAnchorList.add("病人ID");
        colonAnchorList.add("登记号");
        colonAnchorList.add("彩超号");
        colonAnchorList.add("门诊");
        colonAnchorList.add("出院情况");
        colonAnchorList.add("专 科 检 查");
    }

    //中括号包围的锚点，中括号是特殊字符
    static List<String> bracketAnchoList = new ArrayList<>();

    static {
    }

    static List<String> colonEndAnchorList = new ArrayList<>();

    static {
        colonEndAnchorList.add("死亡时间");
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
        colonEndAnchorList.add("住院医师");
        colonEndAnchorList.add("入院时病史、体征及辅助检查主要发现");

    }

    public static void main(String[] args) {
        BasicDBObject docQuery = new BasicDBObject();
        docQuery.append("batchNo", "shch2018040901");
        BasicDBList recordTypeList = new BasicDBList();
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
            if (!jsonObject.getJSONObject("info").containsKey("text_back")) {
                jsonObject.getJSONObject("info").put("text_back", textARS);
            } else {
                textARS = jsonObject.getJSONObject("info").getString("text_back");
            }
            /*String textARS = jsonObject.getJSONObject("info").getString("textARS");
            textARS = TextFormatter.formatTextByAnchaor(textARS);*/
            textARS = textARS.replaceAll("【【【【", "【【").replaceAll("】】】】", "】】");
            /*for(String anchor : anchorsList){
                textARS = textARS.replaceAll(anchor, "【【" + anchor + "】】");
            }*/
            textARS = textARS.replaceAll("生前【【工作单位】】", "【【生前工作单位】】");
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
            textARS = textARS.replaceAll("【【门诊】】复查", "门诊复查");
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
                    if ('：' == textARS.charAt(index + anchor.length()) || ':' == textARS.charAt(index + anchor.length())) {
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
            BathUpdateOptions bathUpdateOption = BathUpdateOptions.getInstance();
            bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(jsonObject.get("_id"))));
            bathUpdateOption.setUpdate(Update.update("info.text", textARS));
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
