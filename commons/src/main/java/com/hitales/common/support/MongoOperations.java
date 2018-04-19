package com.hitales.common.support;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author aron
 * Desc:mongodb基础操作支持类
 */
public class MongoOperations {

    private MongoTemplate mongoTemplate;

    private MongoCollection mongoCollection;

    private DBCollection dbCollection;

    public MongoOperations() {
    }

    public MongoOperations(MongoTemplate pMongoTemplate) {
        this.mongoTemplate = pMongoTemplate;
    }

    public MongoOperations(MongoCollection pMongoCollection) {
        this.mongoCollection = pMongoCollection;
    }

    public MongoOperations(DBCollection pDbCollection) {
        this.dbCollection = pDbCollection;
    }

    public <T> List<T> find(Query query, Class<T> entityClass, String collectionName) {
        return this.getMongoTemplate().find(query, entityClass, collectionName);
    }

    public <T> List<T> find(Query query, Class<T> entityClass) {
        return this.getMongoTemplate().find(query, entityClass);
    }

    public <T> T findOne(Query query, Class<T> entityClass, String collectionName) {
        return this.getMongoTemplate().findOne(query, entityClass, collectionName);
    }

    public <T> T findById(Object id, Class<T> entityClass) {
        return this.getMongoTemplate().findById(id, entityClass);
    }

    public <T> T findById(Object id, Class<T> entityClass, String collectionName) {
        return this.getMongoTemplate().findById(id, entityClass, collectionName);
    }

    public void save(JSONObject objectToSave) {
        objectToSave.put("updateTime", System.currentTimeMillis());
        this.getMongoTemplate().save(objectToSave);
    }

    public void save(JSONObject objectToSave, String collectionName) {
        objectToSave.put("updateTime", System.currentTimeMillis());
        this.getMongoTemplate().save(objectToSave, collectionName);
    }

    public void insert(Collection<JSONObject> batchToSave, Class<?> entityClass) {
        for (JSONObject objectToSave : batchToSave) {
            objectToSave.put("updateTime", System.currentTimeMillis());
        }
        this.getMongoTemplate().insert(batchToSave, entityClass);
    }

    public void insert(Collection<JSONObject> batchToSave, String collectionName) {
        for (JSONObject objectToSave : batchToSave) {
            objectToSave.put("updateTime", System.currentTimeMillis());
        }
        this.getMongoTemplate().insert(batchToSave, collectionName);
    }

    public int remove(Query query, String collectionName) {
        WriteResult remove = this.getMongoTemplate().remove(query, collectionName);
        return remove == null ? 0 : remove.getN();
    }

    public int updateMulti(Query query, Update update, String collectionName) {
        update.set("updateTime", System.currentTimeMillis());
        WriteResult writeResult = this.getMongoTemplate().updateMulti(query, update, collectionName);
        return writeResult == null ? 0 : writeResult.getN();
    }

    public int updateFirst(Query query, Update update, String collectionName) {
        update.set("updateTime", System.currentTimeMillis());
        WriteResult writeResult = this.getMongoTemplate().updateFirst(query, update, collectionName);
        return writeResult == null ? 0 : writeResult.getN();
    }

    /**
     * Finds all documents in the collection.
     *
     * @return
     */
    public FindIterable<Document> findAll() {
        return this.getMongoCollection().find();
    }

    public FindIterable<Document> find(Bson filter) {
        return this.getMongoCollection().find(filter);
    }

    public AggregateIterable<Document> aggregate(List<? extends Bson> pipeline) {
        return this.getMongoCollection().aggregate(pipeline);
    }

    public <TResult> DistinctIterable<TResult> distinct(String fieldName, Bson filter, Class<TResult> resultClass) {
        return this.getMongoCollection().distinct(fieldName, filter, resultClass);
    }

    public int batchUpdate(String collectionName,
                           List<BatchUpdateOption> options) {
        return doBatchUpdate(this.getMongoTemplate().getCollection(collectionName),
                collectionName, options, true);
    }

    public int doBatchUpdate(DBCollection pDbCollection, String collName,
                             List<BatchUpdateOption> options, boolean ordered) {
        DBObject command = new BasicDBObject();
        command.put("update", collName);
        List<BasicDBObject> updateList = new ArrayList<BasicDBObject>();
        for (BatchUpdateOption option : options) {
            //设置修改时间
            option.getUpdate().set("updateTime", System.currentTimeMillis());

            BasicDBObject update = new BasicDBObject();
            update.put("q", option.getQuery().getQueryObject());
            update.put("u", option.getUpdate().getUpdateObject());
            update.put("upsert", option.isUpsert());
            update.put("multi", option.isMulti());
            updateList.add(update);
        }
        command.put("updates", updateList);
        command.put("ordered", ordered);
        CommandResult commandResult = pDbCollection.getDB().command(command);
        if (commandResult == null || commandResult.get("n") == null) {
            return 0;
        }
        return Integer.parseInt(commandResult.get("n").toString());
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public MongoCollection getMongoCollection() {
        return mongoCollection;
    }

    public void setMongoCollection(MongoCollection mongoCollection) {
        this.mongoCollection = mongoCollection;
    }

    public DBCollection getDbCollection() {
        return dbCollection;
    }

    public void setDbCollection(DBCollection dbCollection) {
        this.dbCollection = dbCollection;
    }

}
