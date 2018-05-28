package com.hitales.functions.clean;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.support.SplitAnchor;
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
    static MongoCredential mongoCredential = MongoCredential.createCredential("xh", "HRS-live", "rt0hizu{j9lzJNqi".toCharArray());
    static ServerAddress serverAddress = new ServerAddress("localhost", 3718);
    static List<MongoCredential> mongoCredentials = new ArrayList<>();

    static {
        mongoCredentials.add(mongoCredential);
    }

    static MongoClient mongo = new MongoClient(serverAddress, mongoCredentials, new MongoClientOptions.Builder().build());
    static MongoDatabase db = mongo.getDatabase("HRS-live");
    static MongoCollection dc = db.getCollection("Record");
    private static MongoTemplate hrsMongoTemplate;

    static {
        MongoProperties mongoProperties = new MongoProperties();
        mongoProperties.setHost("localhost");
        mongoProperties.setPort(3718);
        mongoProperties.setDatabase("HRS-live");
        mongoProperties.setUsername("xh");
        mongoProperties.setPassword("rt0hizu{j9lzJNqi".toCharArray());
        hrsMongoTemplate = DBConnection.generateTemplate(mongoProperties);
    }

    static List<String> anchorsList = new ArrayList<>();

    static {
        /*anchorsList.add("体 格 检 查");
        anchorsList.add("体  格  检  查");
        anchorsList.add("体  格   检   查");
        anchorsList.add("体    格    检    查");*/
    }

    //禁用的锚点
    static List<String> notAnchorList = new ArrayList<>();

    static {
        notAnchorList.add("体格检查");
    }

    //前面不是中文的需要打上锚点
    static List<String> prefAnchorList = new ArrayList<>();

    static {
        prefAnchorList.add("体格检查");
    }

    //前面是中文的锚点要去掉
    static List<String> prefNotAnchorList = new ArrayList<>();

    static {

    }

    //前面不能为中文，后面跟冒号作为锚点
    static List<String> colonAnchorList = new ArrayList<>();

    static {

    }

    //中括号包围的锚点，中括号是特殊字符
    static List<String> bracketAnchoList = new ArrayList<>();

    static {
    }

    static List<String> colonEndAnchorList = new ArrayList<>();

    static {
        /*colonEndAnchorList.add("死亡时间");*/

    }

    public static void main(String[] args) {
        BasicDBObject docQuery = new BasicDBObject();
        docQuery.put("batchNo", new BasicDBObject("$regex", "shch"));
//        docQuery.append("batchNo", "shly20180424");
//        docQuery.append("source", "病历文书");
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
            String text_back = textARS;
            //第二次跑打开下面的注释
            if (jsonObject.getJSONObject("info").containsKey("text_back")) {
                text_back = jsonObject.getJSONObject("info").getString("text_back");
            }
//            String textARS = jsonObject.getJSONObject("info").getString("textARS");
//            textARS = TextFormatter.formatTextByAnchaor(textARS);
            textARS = textARS.replaceAll("【【【【", "【【").replaceAll("】】】】", "】】");
            /*for(String anchor : anchorsList){
                textARS = textARS.replaceAll(anchor, "【【" + anchor + "】】");
            }*/

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

//            textARS = textARS.replaceAll("见【【体格检查】】", "见体格检查");

            textARS = textARS.replaceAll("【【【【", "【【").replaceAll("】】】】", "】】");
            BathUpdateOptions bathUpdateOption = BathUpdateOptions.getInstance();
            bathUpdateOption.setQuery(Query.query(Criteria.where("_id").is(jsonObject.get("_id"))));
            bathUpdateOption.setUpdate(Update.update("info.text", textARS)
                    .set("info.text_back", text_back)
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
