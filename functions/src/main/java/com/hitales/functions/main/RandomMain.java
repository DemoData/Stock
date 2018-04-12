package com.hitales.functions.main;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.util.PatternUtil;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

public class RandomMain {
    private static MongoCollection dc;
    private static List<JSONObject> result = new ArrayList<>();
    private final static String ANCHOR_EXCEL_PATH = "/Users/aron/stock/技术用-症状&体征-锚点使用.xlsx";
    private static List<String> anchors;

    static {
        MongoCredential mongoCredential = MongoCredential.createCredential("aron", "HRS", "aron".toCharArray());
        ServerAddress serverAddress = new ServerAddress("localhost", 27017);
        List<MongoCredential> mongoCredentials = new ArrayList<>();
        mongoCredentials.add(mongoCredential);
        MongoClient mongo = new MongoClient(serverAddress, mongoCredentials, new MongoClientOptions.Builder().build());
        MongoDatabase db = mongo.getDatabase("HRS");
        dc = db.getCollection("Record");
    }

    public static void main(String[] args) throws Exception {
        try {
            //制定需要获取的列
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i <= 23; i++) {
                if (i != 0 && i != 2 && i != 3) {
                    list.add(i);
                }
            }
            //锚点数据
            anchors = readExcelContent(ANCHOR_EXCEL_PATH, 0, list);
            imRecord();
            writer("/Users/aron/", "长海肝癌入出院50份_reason2", "xlsx", result, new String[]{"原类型", "子类型", "记录类型",
                    "原文", "锚点数量", "RID"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void imRecord() throws Exception {
//        String[] recordArr = new String[]{"出院记录", "出院小结", "死亡小结", "死亡记录", "入院记录", "病案首页", "24小时内入出院"};
        String[] recordArr = new String[]{"入院记录", "出院记录"};
        for (int i = 0; i < recordArr.length; i++) {
            System.out.println(recordArr[i]);
            List<Bson> bsons = new ArrayList<>();
            bsons.add(new Document("$match", new Document("batchNo", "shch2018040901")));
//            bsons.add(new Document("$match", new Document("source", "病历文书")));
            List<Document> recordTypeList = new ArrayList<Document>();
            recordTypeList.add(new Document("recordType", recordArr[i]));
//            bsons.add(new Document("$match", new Document("recordType", new Document("$ne", "入院记录"))));
//            bsons.add(new Document("$match", new Document("recordType", new Document("$ne", "出院记录"))));

            bsons.add(new Document("$match", new Document("$or", recordTypeList)));
            bsons.add(new Document("$sample", new Document("size", 50)));
            AggregateIterable<Document> iterable = dc.aggregate(bsons).allowDiskUse(true);
            MongoCursor<Document> itor = iterable.iterator();
            int j = 0;
            while (itor.hasNext()) {
                System.out.println(j++);
                Document document = itor.next();
                JSONObject jsonObject = JSONObject.parseObject(document.toJson());
                result.add(processJSONObject(jsonObject));
                //System.out.println(jsonObject.getJSONObject("info").getString("text"));
            }
        }

    }

    private static JSONObject processJSONObject(JSONObject jsonObject) throws Exception {
        String text = jsonObject.getJSONObject("info").getString("text");
//        String text = TextFormatter.addAnchor(textARS, anchors);
        JSONObject resultItem = new JSONObject();
        text = text.replaceAll("【【", "\n【【");
        resultItem.put("原文", text);
        resultItem.put("原类型", jsonObject.getString("sourceRecordType"));
        resultItem.put("子类型", jsonObject.getString("subRecordType"));
        resultItem.put("记录类型", jsonObject.getString("recordType"));
        resultItem.put("锚点数量", countAnchorCount(text));
        resultItem.put("RID", jsonObject.getString("_id"));
        return resultItem;
    }


    public static int countAnchorCount(String text) {
        int count = 0;
        Matcher matcher = PatternUtil.ANCHOR_PATTERN.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public static void writer(String path, String fileName, String fileType, List<JSONObject> result, String titleRow[]) throws IOException {
        Workbook wb = null;
        String excelPath = path + File.separator + fileName + "." + fileType;
        File file = new File(excelPath);
        Sheet sheet = null;
        //创建工作文档对象
        if (!file.exists()) {
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook();

            } else if (fileType.equals("xlsx")) {

                wb = new XSSFWorkbook();
            } else {
                throw new RuntimeException("文件格式不正确");
            }
            //创建sheet对象
            sheet = (Sheet) wb.createSheet("记录类型表");
            OutputStream outputStream = new FileOutputStream(excelPath);
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();

        } else {
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook();

            } else if (fileType.equals("xlsx")) {
                wb = new XSSFWorkbook();

            } else {
                throw new RuntimeException("文件格式不正确");
            }
        }
        //创建sheet对象
        if (sheet == null) {
            sheet = (Sheet) wb.createSheet("记录类型表");
        }

        //添加表头
        Row row = sheet.createRow(0);
        Cell cell;
        for (int i = 0; i < titleRow.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(titleRow[i]);
        }
        int rowIndex = 0;
        for (JSONObject jsonObject : result) {
            row = sheet.createRow(++rowIndex);
            for (int i = 0; i < titleRow.length; i++) {
                cell = row.createCell(i);
                cell.setCellValue(jsonObject.getString(titleRow[i]));
            }
        }
        //创建文件流
        OutputStream stream = new FileOutputStream(excelPath);
        //写入数据
        wb.write(stream);
        //关闭文件流
        stream.close();
    }

    /**
     * 读取Excel数据内容
     *
     * @param
     * @return Map 包含单元格数据内容的Map对象
     */
    private static ArrayList<String> readExcelContent(String excelFile, int sheetNum, List<Integer> readColList) throws IOException {
        POIFSFileSystem fs;
        XSSFWorkbook wb = null;
        XSSFSheet sheet;
        XSSFRow row;
        InputStream is = new FileInputStream(excelFile);
        ArrayList<String> content = new ArrayList<String>();
        try {
            String str = "";
            wb = new XSSFWorkbook(is);
            sheet = wb.getSheetAt(sheetNum);
            // 得到总行数
            int rowNum = sheet.getLastRowNum();
            row = sheet.getRow(0);
            int colNum = row.getPhysicalNumberOfCells();
            // 正文内容应该从第二行开始,第一行为表头的标题
            for (int i = 1; i <= rowNum; i++) {
                row = sheet.getRow(i);
                int j = 0;
                while (j < colNum) {
                    if (readColList.contains(j)) {
                        str = getCellFormatValue(row.getCell((short) j)).trim();
                        if (str.length() > 0) {
                            content.add(str);
                        }
                    }
                    j++;
                }
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 根据HSSFCell类型设置数据
     *
     * @param cell
     * @return
     */
    private static String getCellFormatValue(XSSFCell cell) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case HSSFCell.CELL_TYPE_NUMERIC:
                case HSSFCell.CELL_TYPE_FORMULA: {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        // 如果是Date类型则，转化为Data格式

                        //方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
                        //cellvalue = cell.getDateCellValue().toLocaleString();

                        //方法2：这样子的data格式是不带带时分秒的：2011-10-12
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue = sdf.format(date);
                    }
                    // 如果是纯数字
                    else {
                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case HSSFCell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                // 默认的Cell值
                default:
                    cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }

}
