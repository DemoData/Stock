package com.hitales.service.standard;

/**
 * @author aron
 * @date 2018.02.27
 */
public interface IDataService {
    boolean processData();

    void setBasicInfo(Object pBasicInfo);

    void setXmlPath(String xmlPath);
}
