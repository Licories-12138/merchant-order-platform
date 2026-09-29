package com.sky.constant;

/**
 * 运营数据报表相关常量
 */
public final class ReportConstants {

    // 私有构造器，防止被实例化（工具类/常量类的规范）
    private ReportConstants() {
    }

    /**
     * 报表统计天数
     */
    public static final int REPORT_DAYS = 30;

    /**
     * Excel模板文件路径
     */
    public static final String TEMPLATE_PATH = "template/运营数据报表模板.xlsx";

    /**
     * Excel Sheet页名称
     */
    public static final String SHEET_NAME = "Sheet1";

    /**
     * 概览数据 - 日期所在行 (Excel第2行)
     */
    public static final int OVERVIEW_DATE_ROW = 1;

    /**
     * 概览数据 - 第一行统计项所在行 (Excel第4行)
     */
    public static final int OVERVIEW_SUMMARY_ROW_1 = 3;

    /**
     * 概览数据 - 第二行统计项所在行 (Excel第5行)
     */
    public static final int OVERVIEW_SUMMARY_ROW_2 = 4;

    /**
     * 明细数据起始行 (Excel第8行)
     */
    public static final int DETAIL_START_ROW = 7;
}