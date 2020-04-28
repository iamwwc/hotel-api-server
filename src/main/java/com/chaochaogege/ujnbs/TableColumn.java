package com.chaochaogege.ujnbs;

import java.util.ArrayList;

public class TableColumn {
    public String tableName;
    public String primaryKey;
    public ArrayList<String> columns;

    /**
     *生成API路径名为如下格式
     * /tablename/[primaryKey].
     * 针对不同的调用，primaryKey 为可选
     * @see <a href="https://www.yuque.com/docs/share/e1137d32-934b-478d-9491-6806a3831f20?#">https://www.yuque.com/docs/share/e1137d32-934b-478d-9491-6806a3831f20?#</a>
     * @param tableName 表名
     * @param primaryKey 主键，必须使用32字节长度的字符串
     * @param columns 包含主键在内的表结构，即 table column name
     */
    public TableColumn(String tableName, String primaryKey, ArrayList<String>columns) {
        this.tableName = tableName;
        this.primaryKey = primaryKey;
        this.columns = columns;
    }
}
