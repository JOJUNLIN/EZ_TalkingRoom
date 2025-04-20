import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

class ClientFrame extends JFrame {
    //时间显示格式
    SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //窗口宽度
    final int WIDTH = 700;
    //窗口高度
    final int HEIGHT = 700;

    //创建发送按钮
    JButton btnSend = new JButton("发送");
    //创建清除按钮
    JButton btnAll = new JButton("选中所有好友");
    //创建注销按钮
    JButton btnExit = new JButton("注销");

    //创建消息接收者标签
    JLabel lblReceiver = new JLabel(" ");

    //创建文本输入框, 参数分别为行数和列数
    JTextArea jtaWrite = new JTextArea();

    //创建聊天消息框
    JTextArea jtaChat = new JTextArea();


    //当前在线列表的列标题
    String[] colTitles = {"在线用户", "IP", "端口"};
    //当前在线列表的数据
    String[][] rowData = null;
    //创建当前在线列表
    JTable jtbOnline = new JTable
            (
                    new DefaultTableModel(rowData, colTitles) {
                        //表格不可编辑，只可显示
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    }
            );

    //创建聊天消息框的滚动窗
    JScrollPane jspChat = new JScrollPane(jtaChat);

    //创建当前在线列表的滚动窗
    JScrollPane jspOnline = new JScrollPane(jtbOnline);

    //设置默认窗口属性，连接窗口组件
    public ClientFrame() {
        //标题
        setTitle("聊天室");
        //大小
        setSize(WIDTH, HEIGHT);
        //不可缩放
        setResizable(false);
        //自定义布局
        setLayout(null);

        //设置按钮大小和位置
        btnSend.setBounds(20, 600, 100, 50);
        btnAll.setBounds(445, 400, 200, 50);
        btnExit.setBounds(550, 600, 100, 50);

        //设置标签大小和位置
        lblReceiver.setBounds(50, 10, 500, 30);

        //设置按钮文本的字体
        btnSend.setFont(new Font("宋体", Font.BOLD, 18));
        btnAll.setFont(new Font("宋体", Font.BOLD, 18));
        btnExit.setFont(new Font("宋体", Font.BOLD, 18));

        //添加按钮
        this.add(btnSend);
        this.add(btnAll);
        this.add(btnExit);

        //添加标签
        this.add(lblReceiver);

        //设置文本输入框大小和位置
        jtaWrite.setBounds(20, 460, 640, 120);
        //设置文本输入框字体
        jtaWrite.setFont(new Font("楷体", Font.BOLD, 16));
        //添加文本输入框
        this.add(jtaWrite);

        //聊天消息框自动换行
        jtaChat.setLineWrap(true);
        //聊天框不可编辑，只用来显示
        jtaChat.setEditable(false);
        //设置聊天框字体
        jtaChat.setFont(new Font("楷体", Font.BOLD, 16));

        //设置滚动窗的水平滚动条属性:不出现
        jspChat.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //设置滚动窗的垂直滚动条属性:需要时自动出现
        jspChat.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //设置滚动窗大小和位置
        jspChat.setBounds(20, 50, 360, 400);
        //添加聊天窗口的滚动窗
        this.add(jspChat);

        //设置滚动窗的水平滚动条属性:出现
        jspOnline.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        //设置滚动窗的垂直滚动条属性:需要时自动出现
        jspOnline.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //设置当前在线列表滚动窗大小和位置
        jspOnline.setBounds(420, 50, 250, 340);
        //添加当前在线列表
        this.add(jspOnline);

        //添加发送按钮的响应事件
        btnSend.addActionListener
                (
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent event) {
                                //显示最新消息
                                jtaChat.setCaretPosition(jtaChat.getDocument().getLength());
                                try {
                                    //有收信人才发送
                                    if (!Client_2.uidReceiver.toString().equals("")) {
                                        //在聊天窗打印发送动作信息
                                        jtaChat.append(time.format(new Date()) + "\n发往 " + Client_2.uidReceiver.toString() + ":\n");
                                        //显示发送消息
                                        jtaChat.append(jtaWrite.getText() + "\n\n");
                                        //向服务器发送聊天信息
                                        OutputStream out = Client_2.client.getOutputStream();
                                        out.write( ("Chat/" + Client_2.uidReceiver.toString() + "/" + jtaWrite.getText()) .getBytes());
                                    }
                                }
                                catch (Exception e) {}
                                finally {
                                    //文本输入框清除
                                    jtaWrite.setText("");
                                }
                            }
                        }
                );
        //添加选中所有人按钮的鼠标事件
        btnAll.addMouseListener
                (
                        new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent event) {
                                //取得在线列表的数据模型
                                DefaultTableModel tbm = (DefaultTableModel) jtbOnline.getModel();
                                //选中所有行
                                jtbOnline.selectAll();
                                //提取所有被选中行数
                                int[] selectedIndex = jtbOnline.getSelectedRows();
                                //将所有消息目标的uid拼接成一个字符串, 以逗号分隔
                                Client_2.uidReceiver = new StringBuilder("");
                                for (int i = 0; i < selectedIndex.length; i++) {
                                    Client_2.uidReceiver.append((String) tbm.getValueAt(selectedIndex[i], 1));
                                    Client_2.uidReceiver.append(":");
                                    Client_2.uidReceiver.append((String) tbm.getValueAt(selectedIndex[i], 2));
                                    if (i != selectedIndex.length - 1)
                                        Client_2.uidReceiver.append(",");
                                }
                                lblReceiver.setText("当前在线人数"+Client_2.count+"个 "+"发给：" + Client_2.uidReceiver.toString() + " 当前状态：群发消息");
                            }

                            @Override
                            public void mousePressed(MouseEvent event) {}

                            @Override
                            public void mouseReleased(MouseEvent event) {}

                            @Override
                            public void mouseEntered(MouseEvent event) {}

                            @Override
                            public void mouseExited(MouseEvent event) {}
                        }
                );
        //添加注销按钮的响应事件
        btnExit.addActionListener
                (
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent event) {
                                try {
                                    //向服务器发送注销请求
                                    OutputStream out = Client_2.client.getOutputStream();
                                    out.write("Exit/".getBytes());
                                    //注销
                                    System.exit(0);
                                }
                                catch (Exception e) {}
                            }
                        }
                );
        //添加在线列表被鼠标选中的事件
        jtbOnline.addMouseListener
                (
                        new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent event) {
                                //取得在线列表的数据模型
                                DefaultTableModel tbm = (DefaultTableModel) jtbOnline.getModel();
                                //提取鼠标选中的行
                                int[] selectedIndex = jtbOnline.getSelectedRows();
                                //将所有消息目标的uid拼接成一个字符串, 以逗号分隔
                                Client_2.uidReceiver = new StringBuilder("");
                                for (int i = 0; i < selectedIndex.length; i++) {
                                    Client_2.uidReceiver.append((String) tbm.getValueAt(selectedIndex[i], 1));
                                    Client_2.uidReceiver.append(":");
                                    Client_2.uidReceiver.append((String) tbm.getValueAt(selectedIndex[i], 2));
                                    if (i != selectedIndex.length - 1)
                                        Client_2.uidReceiver.append(",");
                                }
                                lblReceiver.setText("当前在线人数"+Client_2.count+"个 "+"发给：" + Client_2.uidReceiver.toString() + " 当前状态：私聊");
                            }

                            @Override
                            public void mousePressed(MouseEvent event) {}

                            @Override
                            public void mouseReleased(MouseEvent event) {}

                            @Override
                            public void mouseEntered(MouseEvent event) {}

                            @Override
                            public void mouseExited(MouseEvent event) {}
                        }
                );
    }
}
