package Nim;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class NimServer {
    private static Boolean verbose = false;
    private static int[] board = {3,4,5};
    public static void main(String[] args){
        if (args.length < 2 )usage();
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        //System.out.println(Arrays.toString(args));
        if (args.length > 2) {
            int size = args.length-2;
            if (args[2].equals("true")) {
                verbose = true;
                size-=1;
            }
            else if (Character.isAlphabetic(args[2].charAt(0))){
                System.out.printf("Unknown argument: %s\n",args[2]);
                usage();
            }
            if (size!=0) {
                int[] n = new int[size];
                int b = 0;
                int i = 2;
                while (i < args.length) {
                    if (args[i].equals("true")) i++;
                    else {
                        n[b]= Integer.parseInt(args[i]);
                        b++;
                        i++;
                    }
                }
                board = n;
            }
        }
        //System.out.println(Arrays.toString(board));

        try{
            ServerSocket serversocket = new ServerSocket();
            serversocket.bind(new InetSocketAddress(host,port));

            NimModel model = null;
            for (;;)
            {
                Socket socket = serversocket.accept();
                NimViewProxy proxy = new NimViewProxy (socket);
                proxy.setup(board);
                if (model == null || model.isFinished())
                {
                    //System.out.println(Arrays.toString(board));
                    model = new NimModel(board,verbose);
                    proxy.setListener (model);
                    //proxy.setup(board);
                }
                else
                {
                    proxy.setListener (model);
                    //proxy.setup(board);
                    model = null;
                }
            }

        }
        catch (SocketException e){
            System.out.printf("Unable to connect to host: %s\n",host);
            e.printStackTrace(System.err);
            System.exit(1);
        }
        catch (IllegalArgumentException e){
            System.out.printf("Invalid port number: %d\n",port);
            e.printStackTrace(System.err);
            System.exit(1);
        }catch (IOException e){
            e.printStackTrace(System.err);
            System.exit(1);
        }


    }
    private static void usage(){
        System.err.println("Usage: java NimServer hostname port-number [true] [pile1 [pile2 ...]]");
        System.exit(1);
    }
}
