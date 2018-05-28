package com.hitales.functions.clean;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelProcess {


    private static JdbcTemplate jdbcTemplate;

    static {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:mysql://localhost:3306/local?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&useSSL=false");
        dataSourceBuilder.username("root");
        dataSourceBuilder.password("woshixuhu1217");
        dataSourceBuilder.driverClassName("com.mysql.jdbc.Driver");
        DataSource dataSource = dataSourceBuilder.build();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public static void main(String[] args) throws IOException {
        ExcelProcess excelProcess = new ExcelProcess();
//        System.out.println(excelProcess.readRtf(new File("/Users/aron/Desktop/24ry.rtf")));
        //遍历文件
        iteratorAllDoc("/Users/aron/Documents/入库/rjny_converted");
        /*System.out.println(fileList.size());
        for (File file : fileList) {
            String s = excelProcess.readDocx(file);
            System.out.println(s);
            System.out.println("=============================");
        }*/

        int count = 0;
        List<Object[]> records = new ArrayList<>();
        for (File file : fileList) {
            String absolutePath = file.getAbsolutePath();
            String[] split = absolutePath.split("/");
            String fileName = file.getName().toLowerCase();
            String recordType = null;
            String patientName = split[7];
            /*String[] groupName = null;
            if (split[0].equals("")) {
                patientName = split[split.length - 4];
                groupName = split[split.length - 3].split("-");
            } else {
                patientName = split[split.length - 3];
                groupName = split[split.length - 2].split("-");
            }
            if (patientName.contains("月病史") || patientName.contains("1.源文件")) {
                patientName = split[split.length - 3];
                groupName = split[split.length - 2].split("-");
            }*/
            String groupRecordName = "";
            /*String groupRecordName = null;
            for (String s : groupName) {
                if (s.contains(".")) {
                    continue;
                }
                StringBuffer prefix = new StringBuffer(s);
                if (s.length() < 10) {
                    prefix = new StringBuffer("3");
                    for (int i = 1; i < 10 - s.length(); i++) {
                        prefix.append("0");
                    }
                    prefix.append(s);
                }
                groupRecordName = prefix.toString();
                break;
            }*/
            /*if (groupRecordName.contains("何银香")) {
                groupRecordName = "3000090542";
                patientName = "何银香";
            }*/

            if (fileName.contains("bc")) {
                recordType = "病程";
            }
            if (fileName.contains("cy") || "c.doc".equals(fileName)) {
                recordType = "出院记录";
            }
            if (fileName.contains("ry") || "r.doc".equals(fileName)) {
                recordType = "入院记录";
            }
            if (fileName.contains("24ry") || "24r.doc".equals(fileName)) {
                recordType = "24小时内入院";
            }
            if (fileName.contains("24cy") || "24c.doc".equals(fileName)) {
                recordType = "24小时内出院";
            }
            if (recordType == null || "".equals(recordType)) {
                recordType = "病程";
            }
            String content = excelProcess.readDocx(file);
            String sourceFilePath = absolutePath.substring(absolutePath.indexOf("rjny_converted") + 14);
            //打锚点
//            String text = excelProcess.processAnchor(content);
            String[] params = {patientName, groupRecordName, content, recordType == null ? "" : recordType, sourceFilePath};
            records.add(params);
            System.out.println(count++ + "recordType:" + recordType + ",patientName:" + patientName + ",groupRecordName:" + groupRecordName);
        }
        System.out.println(">>>>>>>>>>>starting insert：" + records.size());
        int[] ints = jdbcTemplate.batchUpdate("insert into 仁济南院_medical_content(patientName,groupRecordName,content,recordType,sourceFilePath) values(?,?,?,?,?)", records);
        System.out.println("插入成功：" + ints.length);
    }

    private static List<File> fileList = new ArrayList<File>();

    private static void iteratorAllDoc(String fileDir) {
        File file = new File(fileDir);
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File file = new File(dir.getAbsolutePath() + "/" + name);
                if (file.isFile() && name.contains(".doc")) {
                    return true;
                }
                if (file.isDirectory()) {
                    return true;
                }
                return false;
            }
        };
        File[] files = file.listFiles(filenameFilter);// 获取目录下的所有文件或文件夹
        if (files == null) {// 如果目录为空，直接退出
            return;
        }
        // 遍历，目录下的所有文件
        for (File f : files) {
            if (f.isFile()) {
                fileList.add(f);
            } else if (f.isDirectory()) {
                iteratorAllDoc(f.getAbsolutePath());
            }
        }
    }

    /**
     * 需要POI 3.8版本，否則会存在部分错误信息
     *
     * @param in
     * @return
     */
    public String readDocx(File in) {
        String str = null;
        if (in == null) {
            return str;
        }
        try {
            FileInputStream fis = new FileInputStream(in);
            XWPFDocument xdoc = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
            str = extractor.getText();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public String readRtf(File in) {
        String text = null;
        RTFEditorKit rtf = new RTFEditorKit();
        DefaultStyledDocument dsd = new DefaultStyledDocument();
        try {
            rtf.read(new FileInputStream(in), dsd, 0);
            text = new String(dsd.getText(0, dsd.getLength()).getBytes("ISO8859_1"), "GB2312");
            /*text = text.replace("?�音", "啰音");
            text = text.replace("刘?�", "刘喆");
            text = text.replace("氯消西?�", "氯消西冸");
            text = text.replace("双嘧达?�抗凝", "双嘧达嗼抗凝");
            text = text.replace("�10^", "×10^");
            text = text.replace("�N动脉", "腘动脉");
            text = text.replace("�N静脉", "腘静脉");
            text = text.replace("抗体 � ", "抗体 ± ");
            text = text.replace("金阳�t", "金阳祎");
            text = text.replace("胡�B�h", "胡珺玥");
            text = text.replace("徐怡�B", "徐怡珺");
            text = text.replace("李则�S", "李则赟");
            text = text.replace("�C", "°C");
            text = text.replace("�QT", "°QT");
            text = text.replace("无?爸状�", "无Ⅰ°肿大");
            */

            if (text.contains("�")) {
                System.out.println(in.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

}
