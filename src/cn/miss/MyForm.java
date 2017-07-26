package cn.miss;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @Author MissNull
 * @Description:
 * @Date: Created in 2017/7/20.
 */
public class MyForm implements ActionListener, TextAppend {
    private JTextField srcField;
    private JPanel panel;
    private JTextField sourField;
    private JLabel sourLabel;
    private JButton srcBtn;
    private JButton sourBtn;
    private JLabel srcLabel;
    private JTextPane textPanel;
    private JButton startBtn;
    private JButton clearBtn;
    private JFileChooser chooser;
    private JFrame frame;

    public void start() {
        frame = new JFrame("WEB工程升级工具   by:MissNull");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//设置窗体包含方式
        frame.pack();
        frame.setVisible(true);//可见性
        frame.setSize(500, 400);//设置窗体大小
        frame.setLocationRelativeTo(null);//设置居中
        srcBtn.addActionListener(this);
        sourBtn.addActionListener(this);
        startBtn.addActionListener(this::startGo);
        clearBtn.addActionListener(e -> textPanel.setText(""));
        textPanel.setEnabled(false);
        textPanel.setDisabledTextColor(Color.BLACK);
        chooser = new JFileChooser(new File("D://"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//只能选择一个文件夹
        JOptionPane.showMessageDialog(panel, "使用前请确保已经安装了svn且设置了仓库地址。", "提示", JOptionPane.WARNING_MESSAGE);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
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

    //开始升级
    private void startGo(Object o) {
        FileUtils fileUtils = new FileUtils(srcField.getText(), sourField.getText());
        fileUtils.start(this);
    }

    //textArea 追加文字说明
    @Override
    public void append(String msg) {
        try {
            Document document = textPanel.getDocument();
            document.insertString(document.getLength(), msg + "\n", null);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }
}
