package com.hitales.common.support;

import java.util.List;

public abstract class ExcelRowReader {

    /*public void getRows(int sheetIndex, int curRow, List<String> rowlist) {
        System.out.print(curRow + " ");
        for (int i = 0; i < rowlist.size(); i++) {
            System.out.print(rowlist.get(i) == "" ? "*" : rowlist.get(i) + " ");
        }
        System.out.println();
    }*/

    /**
     * 读取行数据
     *
     * @param sheetIndex 第几个sheet
     * @param curRow     行指针
     * @param rowlist    当前行数据
     */
    protected abstract void getRows(int sheetIndex, int curRow, List<String> rowlist);
}
