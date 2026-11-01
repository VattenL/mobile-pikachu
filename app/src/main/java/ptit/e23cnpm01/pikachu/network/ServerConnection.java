package ptit.e23cnpm01.pikachu.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ptit.e23cnpm01.pikachu.R;
import ptit.e23cnpm01.pikachu.core.protocol.Message;
import ptit.e23cnpm01.pikachu.core.protocol.MessageType;

/**
 * Kết nối tới server (một đối tượng duy nhất): đọc ghi trên luồng nền, gọi MessageListener trên luồng giao diện.
 *
 * <p>connect() mở socket và phải gọi trên luồng nền; một luồng đọc riêng nhận từng dòng, dựng Message rồi chuyển về
 * luồng giao diện cho các listener đang đăng ký. send() xếp việc ghi vào một luồng ghi riêng (Android không cho dùng
 * mạng trên luồng giao diện). pingTimer gửi PING mỗi 5 giây. Khi socket báo lỗi: hủy pingTimer, xóa ClientSession,
 * đưa ứng dụng về giao diện chính ở chế độ khách với thông báo; giao diện chính kết nối lại khi hiện ra.
 *
 * <p>Phụ trách: Dev 2.
 */
public class ServerConnection {
    private static final String TAG = "ServerConnection";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final long PING_INTERVAL_MS = 5000;

    private static ServerConnection instance;

    private volatile Socket socket;
    private ArrayList<MessageListener> listeners;
    private BufferedReader reader;
    private volatile PrintWriter writer;
    /** Gửi PING mỗi 5 giây. */
    private Timer pingTimer;
    /** Context của ứng dụng, để đưa ứng dụng về giao diện chính khi mất kết nối. */
    private Context appContext;

    private final Object connectLock = new Object();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor();

    private ServerConnection() {
        listeners = new ArrayList<>();
    }

    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    /** Gán context của ứng dụng; giao diện chính gọi trước khi kết nối. */
    public void setContext(Context context) {
        appContext = context.getApplicationContext();
    }

    public boolean isConnected() {
        Socket s = socket;
        return s != null && !s.isClosed();
    }

    /** Ghi message thành một dòng JSON trên luồng ghi; chưa kết nối thì bỏ qua. */
    public void send(Message msg) {
        String line = msg.toJson();
        sendExecutor.execute(() -> {
            PrintWriter w = writer;
            Socket s = socket;
            if (w == null) {
                return;
            }
            w.print(line);
            w.print('\n');
            w.flush();
            if (w.checkError()) {
                onConnectionLost(s);
            }
        });
    }

    /** Đăng ký nhận message; gọi trên luồng giao diện (Activity gọi trong onStart()). */
    public void addListener(MessageListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /** Hủy đăng ký; gọi trên luồng giao diện (Activity gọi trong onStop()). */
    public void removeListener(MessageListener l) {
        listeners.remove(l);
    }

    /**
     * Mở socket tới server, chạy luồng đọc và pingTimer; trả true nếu kết nối được (hoặc đã kết nối sẵn).
     * Chặn tới khi kết nối xong hoặc quá 5 giây, nên phải gọi trên luồng nền.
     */
    public boolean connect(String host, int port) {
        synchronized (connectLock) {
            if (isConnected()) {
                return true;
            }
            Socket s = new Socket();
            try {
                s.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
                reader = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                Log.w(TAG, "connect " + host + ":" + port + " failed: " + e);
                try {
                    s.close();
                } catch (IOException ignored) {
                    // socket chưa mở
                }
                return false;
            }
            socket = s;
            BufferedReader r = reader;
            Thread readThread = new Thread(() -> readLoop(s, r), "server-reader");
            readThread.setDaemon(true);
            readThread.start();
            pingTimer = new Timer("ping", true);
            pingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    send(new Message(MessageType.PING));
                }
            }, PING_INTERVAL_MS, PING_INTERVAL_MS);
            return true;
        }
    }

    /** Luồng đọc: mỗi dòng một Message, chuyển về luồng giao diện cho các listener. */
    private void readLoop(Socket s, BufferedReader r) {
        try {
            String line;
            while ((line = r.readLine()) != null) {
                Message msg;
                try {
                    msg = Message.fromJson(line);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "ignored invalid line: " + line);
                    continue;
                }
                mainHandler.post(() -> {
                    for (MessageListener l : new ArrayList<>(listeners)) {
                        l.onMessage(msg);
                    }
                });
            }
        } catch (IOException e) {
            Log.w(TAG, "read failed: " + e);
        }
        onConnectionLost(s);
    }

    /**
     * Socket s báo lỗi (đọc hết luồng, lỗi đọc hoặc ghi): hủy pingTimer, đóng socket, xóa ClientSession, đưa ứng dụng
     * về giao diện chính ở chế độ khách với thông báo. Chỉ xử lý một lần cho mỗi socket.
     */
    private void onConnectionLost(Socket s) {
        synchronized (connectLock) {
            if (s == null || socket != s) {
                return;
            }
            socket = null;
            writer = null;
            pingTimer.cancel();
            try {
                s.close();
            } catch (IOException ignored) {
                // đã đóng
            }
        }
        mainHandler.post(() -> {
            ClientSession.clear();
            if (appContext == null) {
                return;
            }
            Toast.makeText(appContext, R.string.connection_lost, Toast.LENGTH_LONG).show();
            Intent intent = appContext.getPackageManager().getLaunchIntentForPackage(appContext.getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                appContext.startActivity(intent);
            }
        });
    }
}
