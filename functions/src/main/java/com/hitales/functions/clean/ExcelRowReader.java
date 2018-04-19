package com.hitales.functions.clean;

import java.util.List;

public class ExcelRowReader {
    public void getRows(int sheetIndex, int curRow, List<String> rowlist) {
        System.out.print(curRow + " ");
        for (int i = 0; i < rowlist.size(); i++) {
            System.out.print(rowlist.get(i) == "" ? "*" : rowlist.get(i) + " ");
        }
        System.out.println();
    }
}
