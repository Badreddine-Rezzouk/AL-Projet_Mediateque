package bserveur;

import java.io.*;
import java.net.Socket;

public abstract class Service implements Runnable {
    protected final Socket client_socket;
    protected BufferedReader sin;
    protected PrintWriter sout;

    public Service(Socket client_socket) {
        this.client_socket = client_socket;
    }

    @Override
    public abstract void run();

    protected abstract void executeService() throws IOException;
}