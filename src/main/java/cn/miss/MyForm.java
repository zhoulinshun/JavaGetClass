package cn.miss;

import cn.miss.entity.ConfigEntity;
import cn.miss.entity.FileEntity;
import cn.miss.util.FileUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.miss.util.Utils.isEmpty;

/**
 * @Author MissNull
 * @Description:
 * @Date: Created in 2017/7/20.
 */
public class MyForm {
    private JTextField srcField;
    private JPanel panel;
    private JTextField sourField;
    private JLabel sourLabel;
    private JButton srcBtn;
    private JButton sourBtn;
    private JLabel srcLabel;
    private JTextArea showLog;
    private JButton startBtn;
    private JButton clearBtn;
    private JCheckBox isSave;
    private JButton showTrip;
    private JFileChooser chooser;
    private JFrame frame;
    private FileSelectDialog dialog;
    private RemoteExecute remoteExecute;
    private List<FileEntity> files;
    private ConfigEntity configEntity = new ConfigEntity("json");

    public void start() {
        dialog = new FileSelectDialog();
        frame = new JFrame("WEB工程升级工具   by:周林顺");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//设置窗体包含方式
        frame.pack();
        frame.setVisible(true);//可见性
        frame.setSize(500, 400);//设置窗体大小
        frame.setLocationRelativeTo(null);//设置居中
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                configEntity.put("autoSave", isSave.isSelected());
                configEntity.put("srcPath", srcField.getText());
                configEntity.put("outPath", sourField.getText());
                if (isSave.isSelected()) configEntity.save();
            }
        });
        srcBtn.addActionListener(this::actionPerformed);
        sourBtn.addActionListener(this::actionPerformed);
        startBtn.addActionListener(this::startGo);
        showTrip.addActionListener(e -> JOptionPane.showMessageDialog(panel, "使用前请确保已经安装了svn且设置了仓库地址。", "提示", JOptionPane.WARNING_MESSAGE));
        clearBtn.addActionListener(e -> showLog.setText(""));
        showLog.setDisabledTextColor(Color.BLACK);
        chooser = new JFileChooser(new File(configEntity.get("srcPath") == null ? "D://" : configEntity.get("srcPath")));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//只能选择一个文件夹
        init();
    }

    private void init() {
        srcField.setText(configEntity.get("srcPath"));
        sourField.setText(configEntity.get("outPath"));
        isSave.setSelected(configEntity.get("autoSave", Boolean.class, false));
    }

    //路径选择
    private void actionPerformed(ActionEvent e) {
        //打开文件选择窗口时触发
        if (chooser.showOpenDialog(null) != 1) {
            String absolutePath = chooser.getSelectedFile().getAbsolutePath();
            if (e.getSource().equals(srcBtn)) {
                srcField.setText(absolutePath);
                append("已选择输出文件目录:" + absolutePath);
            } else {
                sourField.setText(absolutePath);
                append("已选择源文件目录" + absolutePath);
            }
        }
    }

    //开始本地文件升级
    private void startGo(Object o) {
        startBtn.setEnabled(false);
        new FileUtils(srcField.getText(), sourField.getText(), configEntity.get("dataFormat")).start(this::append, this::beginSelect, this::fileSelectCallable);

    }


    //文件选择完成回调
    private void fileSelectCallable(List<FileEntity> files) {
        this.files = files;
        if (!configEntity.get("autoTranFile", Boolean.class, false)) {
            int i = JOptionPane.showConfirmDialog(panel, "是否上传文件？", "提示", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (i != JOptionPane.YES_OPTION) return;
        }
        List list = configEntity.get("ftps", List.class);
        if (list == null || list.size() < 1) {
            FtpInfoDialog ftpInfoDialog = new FtpInfoDialog();
            ftpInfoDialog.setCallable(this::ftpInfoDialogCallable);
            ftpInfoDialog.setVisible(true);
        } else
            ftpInfoDialogCallable(list);

    }

    //ftp信息填写完成dialog回调 开始执行远程上传
    private void ftpInfoDialogCallable(List<Map<String, String>> lists) {
        remoteExecute = new RemoteExecute(lists).setFiles(files).setAllSuccessCallable(this::allSuccessCallable);
        remoteExecute.sftpStart(this::append, this::tempGetShellPath);
    }

    private void allSuccessCallable() {
        startBtn.setEnabled(true);
    }

    //临时获取脚本路径回调
    private String tempGetShellPath() {
        String shellPath = JOptionPane.showInputDialog(panel, "请输入脚本路径");
        if (isEmpty(shellPath)) {
            return "";
        }
        return shellPath;
    }

    //开始文件选择
    private void beginSelect(List<String> list, Consumer<List<String>> consumer) {
        dialog.setList(list);
        dialog.setCallable(consumer);
        dialog.setVisible(true);
    }

    //textArea 打印日志日志
    private void append(String msg) {
        try {
            Document document = showLog.getDocument();
            document.insertString(document.getLength(), msg + "\n", null);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(6, 4, new Insets(0, 0, 0, 0), -1, -1));
        srcField = new JTextField();
        panel.add(srcField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        srcLabel = new JLabel();
        srcLabel.setText("工程目录");
        panel.add(srcLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        sourField = new JTextField();
        panel.add(sourField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sourLabel = new JLabel();
        sourLabel.setText("输出目录");
        panel.add(sourLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        srcBtn = new JButton();
        srcBtn.setText("选择");
        panel.add(srcBtn, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sourBtn = new JButton();
        sourBtn.setText("选择");
        panel.add(sourBtn, new GridConstraints(2, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startBtn = new JButton();
        startBtn.setText("开始");
        panel.add(startBtn, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel.add(scrollPane1, new GridConstraints(4, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        showLog = new JTextArea();
        scrollPane1.setViewportView(showLog);
        clearBtn = new JButton();
        clearBtn.setText("清除日志");
        panel.add(clearBtn, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isSave = new JCheckBox();
        isSave.setText("保存路径");
        panel.add(isSave, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showTrip = new JButton();
        showTrip.setText("显示提示");
        panel.add(showTrip, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}
