import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.println("BindPort");
        int Port = Integer.parseInt(input.nextLine());
        System.out.println("ToIP");
        String ToIP = input.nextLine();
        System.out.println("ToPort");
        int ToPort = Integer.parseInt(input.nextLine());
        ServerSocket serverSocket = new ServerSocket(Port);
        System.gc();
        while (true) {
            Socket connection = serverSocket.accept();
            System.out.println(connection.getRemoteSocketAddress().toString() + "已连接");
            ConnectTo connectTo = new ConnectTo(ToIP,ToPort,connection);
            Thread thread = new Thread(connectTo);
            thread.start();
        }
    }

}

class ReMessage implements Runnable {
    InputStream inputStream;
    Socket socket,server;
    OutputStream outputStream;
    byte[] bytes = new byte[1024*1024*1];
    byte[] com = new byte[1024*1024*1];
    String RemoteIP;
    int mode;
    LZ4Compressor compressor;
    LZ4FastDecompressor decompressor;
    @Override
    public void run() {
        int len;
        LZ4Factory factory = LZ4Factory.fastestInstance();
        if(mode == 0){
            compressor = factory.fastCompressor();
        }else if(mode == 1){
            decompressor = factory.fastDecompressor();
        }
        while (true){
            try{
                len = inputStream.read(bytes);
                if(mode == 0){
                    len = compressor.compress(bytes,0,len,com,0,com.length);
                }
                if(mode == 1){
                    len = decompressor.decompress(bytes,0,com,0,com.length);
                }
                outputStream.write(com,0,len);
            }
            catch (Exception error){
                try {
                    socket.close();
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println();
                System.gc();
                return;
            }
        }
    }

    public ReMessage(Socket connection,Socket S,String ReIP,int Mode)  {
        socket = connection;
        server = S;
        RemoteIP = ReIP;
        mode = Mode;
        try {
            outputStream = server.getOutputStream();
            inputStream = socket.getInputStream();
        }
        catch (Exception error){
            System.out.print(error.toString());
            System.gc();
            return;
        }
    }

}
class ConnectTo implements Runnable{
    String ToIP;
    int ToPort;
    Socket connection;
    @Override
    public void run() {
        try {
            Socket socket = new Socket(ToIP, ToPort);
            ReMessage re = new ReMessage(connection, socket,connection.getRemoteSocketAddress().toString(),0);
            ReMessage send = new ReMessage(socket, connection,"",1);
            Thread r = new Thread(re);
            Thread s = new Thread(send);
            r.start();
            s.start();
        }
        catch (Exception error){
            System.out.println(connection.getRemoteSocketAddress().toString() +"异常断开");
            try {
                connection.close();
            }
            catch (Exception e){}

        }
    }
    public ConnectTo(String TOIP,int TOPort,Socket Connection){
        ToIP = TOIP;
        ToPort = TOPort;
        connection = Connection;
    }
}
