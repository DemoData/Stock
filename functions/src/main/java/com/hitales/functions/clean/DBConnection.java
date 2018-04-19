package com.hitales.functions.clean;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
public class DBConnection {

    public static MongoTemplate generateTemplate(MongoProperties mongoProperties) {
        MongoDbFactory mongoDbFactory = generateFactory(mongoProperties);
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory),
                new MongoMappingContext());
        DefaultMongoTypeMapper defaultMongoTypeMapper = new DefaultMongoTypeMapper(null);
        converter.setTypeMapper(defaultMongoTypeMapper);
        return new MongoTemplate(mongoDbFactory, converter);
    }

    public static MongoDbFactory generateFactory(MongoProperties mongoProperties) {
        if (log.isInfoEnabled()) {
            log.info(mongoProperties.getDatabase());
        }
        if (StringUtils.isBlank(mongoProperties.getUsername())) {
            return new SimpleMongoDbFactory(new MongoClient(mongoProperties.getHost(), mongoProperties.getPort()),
                    mongoProperties.getDatabase());
        }
        MongoClientOptions.Builder builder = MongoClientOptions.builder().socketTimeout(6 * 60 * 60 * 1000);
        builder.socketKeepAlive(true);
        builder.heartbeatSocketTimeout(30000);
        if (log.isInfoEnabled()) {
            log.info("mongodb://" + mongoProperties.getUsername() + ":" + mongoProperties.getPassword().toString() + "@" + mongoProperties.getHost() + ":" + mongoProperties.getPort() + "/" + mongoProperties.getDatabase());
        }
        MongoClientURI mongoClientURI = new MongoClientURI(
                "mongodb://" + mongoProperties.getUsername() + ":" + new String(mongoProperties.getPassword()) + "@" + mongoProperties.getHost() + ":" + mongoProperties.getPort() + "/" + mongoProperties.getDatabase(), builder);
        if (log.isInfoEnabled()) {
            log.info(mongoClientURI.toString());
        }
        return new SimpleMongoDbFactory(new MongoClient(mongoClientURI),
                mongoProperties.getDatabase());
    }

}
