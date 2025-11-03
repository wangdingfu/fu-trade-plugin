package com.wdf.trade.toolwindow;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FuTradeWindow extends SimpleToolWindowPanel implements DataProvider {

    private final JPanel rootPanel;
    private final ToolWindow toolWindow;
    private final Project project;
    private JBTable stockTable;
    private DefaultTableModel tableModel;
    private JLabel showTextLabel;
    public FuTradeWindow(@NotNull Project project, ToolWindow toolWindow) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;
        this.toolWindow = toolWindow;
        this.rootPanel = new JPanel(new BorderLayout());
        initTable();
        initTimeLabel();
        setContent(this.rootPanel);

        startDataRefreshTask();
    }



    private void initTable() {
        // 定义表格列名
        String[] columnNames = {"股票代码", "股票名称", "当前价格", "涨跌幅(%)", "成交量(万手)"};
        // 初始化表格模型（数据为空，可动态添加）
        tableModel = new DefaultTableModel(columnNames, 0) {
            // 设置单元格不可编辑
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // 使用 IDEA 风格的表格
        stockTable = new JBTable(tableModel);
        // 添加滚动条（避免数据过多溢出）
        this.rootPanel.add(new JBScrollPane(stockTable), BorderLayout.CENTER);
    }

    // 初始化时间标签（放在表格下方）
    private void initTimeLabel() {
        showTextLabel = new JLabel("最后更新时间：--");
        showTextLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // 添加上下左右边距
        this.rootPanel.add(showTextLabel, BorderLayout.PAGE_END);
    }

    private void startDataRefreshTask() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        // 每30秒刷新一次数据（首次延迟1秒）
        executor.scheduleAtFixedRate(() -> {
            // 实际场景：调用股票接口获取数据
            Object[][] newData = fetchLatestStockData();
            // 在EDT线程中更新UI（Swing线程安全）
            SwingUtilities.invokeLater(() -> updateStockData(newData));
        }, 1, 3, TimeUnit.SECONDS);
    }

    private Object[][] fetchLatestStockData() {
        // 实际应替换为真实API请求（如通过OkHttp、HttpClient）
        return new Object[][]{
                {"600036", "招商银行", 32.68, 1.56, 60.2},
                {"000858", "五粮液", 167.90, -1.02, 25.8},
                {"601318", "中国平安", 45.80, 0.72, 44.5}
        };
    }


    // 提供方法更新数据（实际可通过接口实时刷新）
    public void updateStockData(Object[][] newData) {
        tableModel.setRowCount(0); // 清空现有数据
        for (Object[] row : newData) {
            tableModel.addRow(row);
        }
        // 2. 更新时间标签（格式化当前时间）
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        showTextLabel.setText("最后更新时间：" + currentTime);
    }

}
