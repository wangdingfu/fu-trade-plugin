package com.wdf.trade.toolwindow;

import com.google.common.collect.Lists;
import com.intellij.find.editorHeaderActions.Utils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.IconUtil;
import com.wdf.trade.common.FuBundle;
import com.wdf.trade.strategy.StockInfo;
import com.wdf.trade.util.ToolBarUtils;
import com.wdf.trade.view.StockView;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FuTradeWindow extends SimpleToolWindowPanel implements DataProvider {

    private final Project project;
    private final JPanel rootPanel;
    private final DefaultActionGroup actionGroup;
    private final JBTabsImpl tabs;
    private static final String ADD_STOCK_GROUP_TITLE = FuBundle.message("add.stock.group.title");
    private static final String ADD_STOCK_GROUP_MESSAGE = FuBundle.message("add.stock.group.message");
    private static final String ADD_STOCK_TITLE = FuBundle.message("add.stock.title");
    private static final String ADD_STOCK_MESSAGE = FuBundle.message("add.stock.message");

    private final Map<String,StockView> stockViewMap = new HashMap<>();

    public FuTradeWindow(@NotNull Project project, ToolWindow toolWindow) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;
        this.rootPanel = new JPanel(new BorderLayout());
        this.actionGroup = new DefaultActionGroup();
        tabs = new JBTabsImpl(project);
        setContent(this.rootPanel);
        this.rootPanel.add(initToolBarUI(), BorderLayout.NORTH);
        this.rootPanel.add(tabs, BorderLayout.CENTER);

        //初始化工具栏事件
        initActionGroup();
    }

    private JPanel initToolBarUI() {
        JPanel toolBarPanel = new JPanel(new BorderLayout());
        Utils.setSmallerFontForChildren(toolBarPanel);
        //创建及初始化工具栏
        ToolBarUtils.addActionToToolBar(toolBarPanel, "add.stock.toolBar", actionGroup, BorderLayout.WEST);
        return toolBarPanel;
    }

    private void initActionGroup() {
        this.actionGroup.add(new AnAction("添加股票分组", "", IconUtil.getAddIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                String userInput = Messages.showInputDialog(project, ADD_STOCK_GROUP_MESSAGE, ADD_STOCK_GROUP_TITLE, IconUtil.getAddIcon(), "我的分组", null);
                if (StringUtils.isNotBlank(userInput)) {
                    StockView stockView = new StockView(project, userInput);
                    TabInfo tabInfo = new TabInfo(stockView.getRootPanel());
                    tabInfo.setText(userInput);
                    tabs.addTab(tabInfo);
                    // 可选：切换到新添加的标签
                    tabs.select(tabInfo, true);
                    stockViewMap.put(userInput, stockView);
                }
            }
        });
        this.actionGroup.add(new AnAction(ADD_STOCK_TITLE, "", IconUtil.getAddIcon()) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                String userInput = Messages.showInputDialog(project, ADD_STOCK_MESSAGE, ADD_STOCK_TITLE, IconUtil.getAddIcon(), "sz300037", null);
                // 处理用户输入（点击“确定”返回输入内容，点击“取消”返回 null）
                if (userInput != null) {
                    TabInfo selectedInfo = tabs.getSelectedInfo();
                    if(Objects.isNull(selectedInfo)){
                        return;
                    }
                    StockView stockView = stockViewMap.get(selectedInfo.getText());
                    if(Objects.isNull(stockView)){
                        return;
                    }
                    stockView.addStock(userInput);
                }
            }
        });
    }


}
