package com.wdf.trade.toolwindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class FuTradeToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        FuTradeWindow fuTradeWindow = new FuTradeWindow(project, toolWindow);
        Content content = contentFactory.createContent(fuTradeWindow, "", false);
//        DefaultActionGroup defaultActionGroup = FuRequestToolBarManager.getInstance(fuRequestWindow).initToolBar();
//        toolWindow.setTitleActions(Lists.newArrayList(defaultActionGroup.getChildActionsOrStubs()));
        toolWindow.getContentManager().addContent(content);
    }
}
