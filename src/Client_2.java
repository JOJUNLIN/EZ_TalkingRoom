import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

//客户端
public class Client_2 {

    //建立客户端
    public static Socket client=null;

    //消息接收者uid
    public static StringBuilder uidReceiver = null;
    //统计在线用户数量
    public static int count = 0;

    public static void main(String[] args) throws Exception{

        //创建客户端窗口对象
        ClientFrame cframe = new ClientFrame();
        //窗口关闭键无效，必须通过退出键退出客户端以便善后
        cframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //窗口位置
        cframe.setLocation(418, 82);
        //设置客户端窗口为可见
        cframe.setVisible(true);

        try {
            //连接服务器
            client = new Socket(InetAddress.getLocalHost(), 6666);
            //输入输出流
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            //成功连接服务端信息
            byte[] success = new byte[1024];
            int length = in.read(success);

            //将服务端发来的连接成功信息打印在聊天消息框
            cframe.jtaChat.append(new String(success, 0, length));
            cframe.jtaChat.append("\n");

            //持续等待服务器信息直至退出
            while (true) {

                //读取服务器发来的信息
                in = client.getInputStream();
                length = in.read(success);

                //处理服务器传来的消息
                String message = new String(success, 0, length);

                //消息类型：更新在线名单或者聊天
                String type = message.substring(0, message.indexOf("/"));
                //消息本体：更新后的名单或者聊天内容
                String chat = message.substring(message.indexOf("/")+1);

                /*
                根据消息类型分别处理
                 */
                //更新在线名单
                if (type.equals("OnlineListUpdate")) {

                    //提取在线列表的数据
                    DefaultTableModel dtm = (DefaultTableModel) cframe.jtbOnline.getModel();
                    //清除在线名单列表
                    dtm.setRowCount(0);
                    //更新在线列表
                    String[] onlineList = chat.split(",");
                    //重新计数在线人数，随着列表更新
                    count = 0;
                    for (String member : onlineList) {

                        count++;
                        //保存在线成员的IP、端口号
                        String[] tmp = new String[3];

                        //在好友列表中去掉自己
                        String me = member.substring(member.indexOf("~") + 1);
                        if (me.equals(InetAddress.getLocalHost().getHostAddress() + ":" + client.getLocalPort())) {
                            continue;
                        }
                        //获取成员信息
                        tmp[0] = member.substring(member.indexOf(":") + 1);
                        tmp[1] = member.substring(0, member.indexOf(":"));
                        tmp[2] = member.substring(member.indexOf(":") + 1);

                        //在在线列表中添加在线者信息
                        dtm.addRow(tmp);
                    }
                    //列表数据居中
                    DefaultTableCellRenderer tbr = new DefaultTableCellRenderer();
                    tbr.setHorizontalAlignment(JLabel.CENTER);
                    cframe.jtbOnline.setDefaultRenderer(Object.class, tbr);
                }
                //接收消息
                else if (type.equals("Chat")) {
                    //获取发送者信息和信息内容
                    String sender = chat.substring(0, chat.indexOf("/"));
                    String word = chat.substring(chat.indexOf("/") + 1);
                    //在聊天窗打印聊天信息
                    cframe.jtaChat.append(cframe.time.format(new Date()) + "\n来自 " + sender + ":\n" + word + "\n\n");

                    //显示最新消息
                    cframe.jtaChat.setCaretPosition(cframe.jtaChat.getDocument().getLength());
                }
            }
        }catch(Exception e)
        {
            cframe.jtaChat.append("未连接到服务器！请重试！\n");
            e.printStackTrace();
        }
    }
}
