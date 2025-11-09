package com.wdf.trade.toolwindow;

import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.IconUtil;
import com.wdf.trade.strategy.FetchStockStrategy;
import com.wdf.trade.strategy.StockInfo;
import com.wdf.trade.strategy.TencentFetchStockStrategy;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
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
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(stockTable);
        decorator.addExtraAction(new AnActionButton("添加股票","", IconUtil.getAddIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                String userInput = Messages.showInputDialog(
                        project,
                        "请输入股票代码：",  // 提示信息
                        "添加股票",    // 窗口标题
                        IconUtil.getAddIcon(),  // 图标
                        "sz300037",  // 默认文本（空表示无默认值）
                        null  // 输入验证器（可选，后面会讲）
                );
                // 处理用户输入（点击“确定”返回输入内容，点击“取消”返回 null）
                if (userInput != null) {
                    // 这里可以添加你的业务逻辑，例如显示输入的内容
                    List<StockInfo> stockInfos = fetchStockData(Lists.newArrayList(userInput));
                    if(CollectionUtils.isNotEmpty(stockInfos)){
                        stockInfos.forEach(stockInfo -> tableModel.addRow(toTableData(stockInfo)));
                    }
                }

            }
        });
        // 添加滚动条（避免数据过多溢出）
        this.rootPanel.add(decorator.createPanel(), BorderLayout.CENTER);
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
            java.util.List<StockInfo> stockInfoList = fetchLatestStockData();
            // 在EDT线程中更新UI（Swing线程安全）
            SwingUtilities.invokeLater(() -> updateStockData(stockInfoList));
        }, 1, 3, TimeUnit.SECONDS);
    }

    private List<StockInfo> fetchLatestStockData() {
        // 实际应替换为真实API请求（如通过OkHttp、HttpClient）
        Vector<Vector> dataVector = tableModel.getDataVector();
        List<String> codeList = Lists.newArrayList();
        dataVector.forEach(data->{
            Object o = data.get(0);
            if(Objects.nonNull(o)){
                codeList.add(o.toString());
            }
        });
        return fetchStockData(codeList);
    }


    private List<StockInfo> fetchStockData(List<String> codeList) {
        if(CollectionUtils.isEmpty(codeList)){
            return Lists.newArrayList();
        }
        FetchStockStrategy fetchStockStrategy = new TencentFetchStockStrategy();
        return fetchStockStrategy.fetch(codeList);
    }


    // 提供方法更新数据（实际可通过接口实时刷新）
    public void updateStockData(List<StockInfo> stockInfoList) {
        tableModel.setRowCount(0); // 清空现有数据
        stockInfoList.forEach(f->tableModel.addRow(toTableData(f)));
        // 2. 更新时间标签（格式化当前时间）
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        showTextLabel.setText("最后更新时间：" + currentTime);
    }


    private Vector<Object> toTableData(StockInfo stockInfo){
        Vector<Object> vector = new Vector<>();
        vector.add(stockInfo.getStockCode());
        vector.add(stockInfo.getStockName());
        vector.add(stockInfo.getCurrentPrice());
        vector.add(stockInfo.getIncreaseRate());
        vector.add(stockInfo.getVolume());
        return vector;
    }

}
