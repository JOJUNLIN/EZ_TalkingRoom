import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ServerThread implements Runnable{

    //统计在线用户数量
    int count = 0;
    //客户端Socket
    Socket client = null;
    //服务器ServerSocket
    ServerSocket server = null;
    //用户名
    String name = null;
    //客户端IP
    String ip = null;
    //客户端端口
    int port = 0;
    //uid为ip和端口的结合
    String uid = null;

    //用动态数组存储所有uid，uid由ip和端口字符串拼接而成
    static ArrayList<String> uid_arr = new ArrayList<>();
    //用散列表存储所有uid, ServerThread对象组成的对
    static Map<String, ServerThread> map = new ConcurrentHashMap<>();
    //提取收信者信息
    String[] receiveArr = null;

    public ServerThread(Socket client, ServerSocket server,String ip, int port) {
        this.client = client;
        this.server = server;
        this.ip = ip;
        this.port = port;
        this.uid = ip+":"+port;
    }

    @Override
    public void run() {

        //将当前客户端uid存入的ArrayList
        uid_arr.add(uid);
        //将当前服务线程存入Map中
        map.put(uid,this);

        //显示时间
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            //获取客户端的输入输出流
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            //向客户端输入连接成功的信息
            String success = time.format(new Date())+"\n成功连接服务器！\n服务器IP: "
                    + server.getInetAddress().getLocalHost().getHostAddress()
                    + ", 端口: 6666\n客户端IP: " + ip + ", 端口: " + port + "\n";
            out.write(success.getBytes());

            //更新在线名单
            updateOnlineList(out);

            //准备缓冲区
            byte[] buf = new byte[1024];
            int length = 0;

            //持续监听并转发客户端消息
            while(true){

                //获取客户端给服务器发送的信息
                length = in.read(buf);
                String message = new String(buf,0,length);

                //消息类型：退出或者聊天
                String type = message.substring(0,message.indexOf('/'));
                //聊天内容：空或者聊天内容
                String chat = message.substring(message.indexOf('/')+1);

                /*
                根据消息类型分别处理
                 */
                //注销
                if(type.equals("Exit")){
                    //在线人数-1
                    count--;
                    //更新ArrayList和Map，移除注销用户
                    uid_arr.remove(uid_arr.indexOf(this.uid));
                    map.remove(this.uid);
                    //广播更新在线名单
                    updateOnlineList(out);
                    break;  //跳出循环结束此线程
                }
                //聊天
                else if(type.equals("Chat")) {

                    //提取收信者信息
                    receiveArr = null;
                    receiveArr = chat.substring(0, chat.indexOf('/')).split(",");
                    //提取聊天内容
                    String word = chat.substring(chat.indexOf('/') + 1);
                    //System.out.println(word);
                    //向收信者广播发出聊天信息
                    chatOnlineList(out, uid, receiveArr, word);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //发送消息
    public void chatOnlineList(OutputStream out, String uid, String[] receiveArr, String word)throws Exception {

        for(String tmp:receiveArr){
            out = map.get(tmp).client.getOutputStream();
            //发送聊天信息
            out.write(("Chat/" + uid + "/" + word).getBytes());
        }
    }

    //更新在线列表
    private void updateOnlineList(OutputStream out) throws Exception {

        for (String tmp : uid_arr) {
            count++;
            //获取广播收听者的输出流
            out = map.get(tmp).client.getOutputStream();
            //将当前在线名单以逗号为分割组合成长字符串一次传送
            StringBuilder sb = new StringBuilder("OnlineListUpdate/");
            for (String member : uid_arr) {
                sb.append(member);
                //以逗号分隔uid，除了最后一个
                if (uid_arr.indexOf(member) != uid_arr.size() - 1)
                    sb.append(",");
            }
            //向每个客户端输入更新在线的名单
            out.write(sb.toString().getBytes());
        }

    }

}

//服务端
public class Server_2 {
    public static void main(String[] args) throws Exception{

        //建立服务器
        ServerSocket server=new ServerSocket(6666);
        //提示服务端建立成功

        while(true) {
            //接收客户端Socket
            Socket client = server.accept();
            //提取客户端Ip
            String ip=client.getInetAddress().getHostAddress();
            //提取客户端端口号
            int port=client.getPort();
            //建立新的服务器线程, 向该线程提供服务器ServerSocket，客户端Socket，客户端IP和端口
            new Thread(new ServerThread(client, server, ip, port)).start();
        }
    }
}
