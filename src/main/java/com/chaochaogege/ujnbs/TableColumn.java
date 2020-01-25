package com.chaochaogege.ujnbs;

import java.util.ArrayList;

public class TableColumn {
    public String tableName;
    public String primaryKey;
    public ArrayList<String> columns;
    public TableColumn(String tableName, String primaryKey, ArrayList<String>columns) {
        this.tableName = tableName;
        this.primaryKey = primaryKey;
        this.columns = columns;
    }
}
