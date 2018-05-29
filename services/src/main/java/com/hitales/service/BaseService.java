package com.hitales.service;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.config.MysqlDataSourceConfig;
import com.hitales.common.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 服务基类
 *
 * @author aron
 */
@Slf4j
@PropertySource("classpath:config/service.properties")
public abstract class BaseService extends GenericService {

    @Value("${page.size}")
    private int pageSize;
    @Value("${datasource.list}")
    private String dataSourceList;
    /**
     * record基本信息
     */
    private Object basicInfo;
    /**
     * 配置文件路径
     */
    private String xmlPath;

    /**
     * 用于createTime
     */
    protected Long currentTimeMillis = TimeUtil.getCurrentTimeMillis();

    private ExecutorService threadPool;

    /**
     * 多线程并行处理数据
     *
     * @return
     */
    @Override
    public boolean processData() {
        //初始化操作,例如加载xml配置文件
        initProcess();
        try {
            /*
             * 一个服务提供一个线程池,每个线程池最大运行线程数为8,当前服务结束后关闭线程池
             */
            threadPool = Executors.newFixedThreadPool(8);
            String dataSourceList = getDataSourceList();
            String[] dataSourceArray = dataSourceList.split(",");
            //execute data process in every dataSource
            for (String dataSource : dataSourceArray) {
                this.process(dataSource);
            }
            //处理结束
            threadPool.shutdown();
            if (!threadPool.awaitTermination(24 * 3600, TimeUnit.SECONDS)) {
                // 超时的时候向线程池中所有的线程发出中断(interrupted)
                threadPool.shutdownNow();
            }
            //清空，便于GC回收
            threadPool = null;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected void initProcess() {

    }

    /**
     * 处理数据
     *
     * @param dataSource 数据源标识
     */
    protected void process(String dataSource) throws Exception {
        executeByMultiThread(getCount(dataSource), dataSource);
    }

    /**
     * 转换为需要入库的json类型
     *
     * @param entity
     * @return
     */
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

    /**
     * 根据数据量拆分为对应线程总数
     *
     * @param count
     * @param dataSource
     */
    private void executeByMultiThread(Integer count, String dataSource) {
        if (count == 0) {
            log.error("executeByMultiThread(): count is 0");
            return;
        }
        int totalPage = 1;
        if (count > getPageSize()) {
            totalPage = count / getPageSize();
            int mod = count % getPageSize();
            if (mod > 0) {
                totalPage += 1;
            }
        }
        log.info("executeByMultiThread(): count:" + count + ",totalPage:" + totalPage);
        for (int i = 1; i < totalPage + 1; i++) {
            if (!threadPool.isShutdown()) {
                threadPool.execute(new storageRunnable(i, i + 1, dataSource));
            }
        }
    }

    /**
     * 通过数据源获取OdCategory
     * 注：已弃用，病种信息属于基本信息，现已转由basicInfo处理
     *
     * @param dataSource
     * @return
     */
    @Deprecated
    protected String getOdCategory(String dataSource) {
        String odCategorie = EMPTY_FLAG;
        if (MysqlDataSourceConfig.MYSQL_XZDM_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_XZDM;
        }
        if (MysqlDataSourceConfig.MYSQL_JKCT_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_JKCT;
        }
        if (MysqlDataSourceConfig.MYSQL_YX_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_YX;
        }
        if (MysqlDataSourceConfig.MYSQL_YXZW_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_YXZW;
        }
        if (MysqlDataSourceConfig.MYSQL_TNB_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_TNB;
        }
        if (MysqlDataSourceConfig.MYSQL_XZDM_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_XZDM;
        }
        if (MysqlDataSourceConfig.MYSQL_GA_DATASOURCE.equals(dataSource)) {
            odCategorie = OD_CATEGORIE_GA;
        }
        return odCategorie;
    }

    /**
     * 数据处理行为方法
     *
     * @param dataSource
     * @param startPage
     * @param endPage
     */
    protected abstract void runStart(String dataSource, Integer startPage, Integer endPage);

    /**
     * 获取数据量
     *
     * @param dataSource
     * @return
     */
    protected abstract Integer getCount(String dataSource);

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getDataSourceList() {
        return dataSourceList;
    }

    @Override
    public void setBasicInfo(Object pBasicInfo) {
        this.basicInfo = pBasicInfo;
    }

    public Object getBasicInfo() {
        return basicInfo;
    }

    public void setDataSourceList(String dataSourceList) {
        this.dataSourceList = dataSourceList;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    /**
     * 线程内部类
     */
    class storageRunnable implements Runnable {
        private Integer startPage;
        private Integer endPage;
        private String dataSource;

        storageRunnable(Integer pStartPage, Integer pEndPage, String pDataSource) {
            this.startPage = pStartPage;
            this.endPage = pEndPage;
            this.dataSource = pDataSource;
        }

        @Override
        public void run() {
            runStart(this.dataSource, this.startPage, this.endPage);
        }
    }

}
