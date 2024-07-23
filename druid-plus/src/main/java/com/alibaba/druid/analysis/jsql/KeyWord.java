package com.alibaba.druid.analysis.jsql;

/**
 * @author LENOVO
 * @date 2024/6/5 10:30
 */
public enum KeyWord {

    MERGE("Merge"),
    INSERT("Insert"),
    UPDATE("Update"),
    SELECT("Select"),
    DELETE("Delete"),
    DROP("Drop"),
    CREATE("Create"),
    ALTER("Alter"),
    CREATEINDEX("CreateIndex"),
    DROPINDEX("DropIndex"),
    ADD("Add"),
    ADDPARTITION("AddPartition"),
    ANALYZE("Analyze");

    private String word;

    KeyWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }
}
