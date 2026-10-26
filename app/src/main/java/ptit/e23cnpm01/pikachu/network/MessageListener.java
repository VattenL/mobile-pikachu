package ptit.e23cnpm01.pikachu.network;

import ptit.e23cnpm01.pikachu.core.protocol.Message;

/**
 * Nhận message từ server trên luồng giao diện.
 *
 * <p>Phụ trách: Dev 2.
 */
public interface MessageListener {
    void onMessage(Message msg);
}
