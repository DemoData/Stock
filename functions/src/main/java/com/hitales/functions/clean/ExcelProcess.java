package com.hitales.functions.clean;

import java.io.File;
import java.io.FilenameFilter;

public class ExcelProcess {

    public static void main(String[] args) {
        ExcelProcess excelProcess = new ExcelProcess();

    }

    public void getExcel(){
        File file = new File("");
        FilenameFilter filenameFilter =new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.contains(".xls")){

                }
                return false;
            }
        };

        File[] files = file.listFiles();
    }

}
