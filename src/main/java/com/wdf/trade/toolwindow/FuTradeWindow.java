package com.wdf.trade.toolwindow;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class FuTradeWindow extends SimpleToolWindowPanel implements DataProvider {

    private final JPanel rootPanel;
    private final ToolWindow toolWindow;
    private final Project project;
    public FuTradeWindow(@NotNull Project project, ToolWindow toolWindow) {
        super(Boolean.TRUE, Boolean.TRUE);
        this.project = project;
        this.toolWindow = toolWindow;
        this.rootPanel = new JPanel(new BorderLayout());
        setContent(this.rootPanel);
    }
}
