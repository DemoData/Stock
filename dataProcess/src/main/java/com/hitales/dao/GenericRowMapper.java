package com.hitales.dao;

import org.dom4j.Element;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class GenericRowMapper<T> implements RowMapper<T> {

    public static String RECORD = "record";
    public static String PRIMARY = "primary";
    public static String MULTI = "multi";

    protected void generateData(ResultSet rs, Element element, Map<String, Object> dataMap) throws SQLException {
        String tableType = element.attribute("type") == null ? "" : element.attribute("type").getValue();
        Iterator iterator = element.elementIterator();
        while (iterator.hasNext()) {
            Element columnElement = (Element) iterator.next();
            String columnName = columnElement.attribute("column-name").getValue();
            String keyName = columnElement.attribute("bean-name") == null ? columnElement.attribute("display-name").getValue()
                    : columnElement.attribute("bean-name").getValue();
            String type = "";
            Object value = "";
            if ("".equals(keyName)) {
                continue;
            }
            if ("".equals(columnName)) {
                value = columnElement.attribute("default-value") == null ? "" : columnElement.attribute("default-value").getValue();
                dataMap.put(keyName, value == null ? "" : value);
                continue;
            }
            if (RECORD.equals(tableType)) {
                type = columnElement.attribute("data-type").getValue();
                if ("string".equals(type)) {
                    value = rs.getObject(columnName) == null ? "" : rs.getObject(columnName).toString();
                } else if ("map".equals(type)) {
                    String[] keyNames = columnElement.attribute("key-name").getValue().split(",");
                    String[] split = columnName.split(",");
                    Map<String, Object> condition = new HashMap<>();
                    for (int i = 0; i < split.length; i++) {
                        Matcher matcher = Pattern.compile("\\{([\\s\\S]+?)}").matcher(split[i]);
                        String conditionValue = null;
                        if (matcher.find()) {
                            conditionValue = matcher.group(1);
                        }
                        condition.put(keyNames[i], conditionValue == null ? rs.getObject(split[i]) : conditionValue);
                    }
                    value = condition;
                }
                if (keyName == null || columnName == null) {
                    continue;
                }
            } else {
                if (columnName == null || keyName == null) {
                    continue;
                }
                value = rs.getObject(columnName) == null ? "" : rs.getObject(columnName);
            }
            if (keyName.contains("patientId")) {
                StringBuffer patientPrefix = new StringBuffer(columnElement.attribute("patient-prefix").getValue());
                value = patientPrefix.append(value.toString()).toString();
            }
            //处理字段值映射
            List<Element> options = columnElement.elements("option");
            if (options != null && !options.isEmpty()) {
                for (Element option : options) {
                    String optionValue = option.attribute("value").getValue();
                    if (optionValue != null && optionValue.equals(value.toString())) {
                        value = option.getText();
                        break;
                    }
                }
            }
            if (StringUtils.isEmpty(value)) {
                value = columnElement.attribute("default-value") == null ? "" : columnElement.attribute("default-value").getValue();
            }
            dataMap.put(keyName, value == null ? "" : value);
        }
    }
}
