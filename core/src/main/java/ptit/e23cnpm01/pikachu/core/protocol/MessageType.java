package ptit.e23cnpm01.pikachu.core.protocol;

/**
 * Các giá trị của trường type, theo bảng message ở pha 3, mục 7.
 *
 * <p>Phụ trách: Dev 2.
 */
public final class MessageType {
    public static final String REGISTER = "REGISTER";
    public static final String REGISTER_RESULT = "REGISTER_RESULT";
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_RESULT = "LOGIN_RESULT";
    public static final String LOGOUT = "LOGOUT";
    public static final String UPDATE_STATUS = "UPDATE_STATUS";
    public static final String ONLINE_LIST = "ONLINE_LIST";
    public static final String GET_RANKING = "GET_RANKING";
    public static final String RANKING = "RANKING";
    public static final String INVITE = "INVITE";
    public static final String INVITE_RECEIVED = "INVITE_RECEIVED";
    public static final String INVITE_REPLY = "INVITE_REPLY";
    public static final String INVITE_RESULT = "INVITE_RESULT";
    public static final String MATCH_START = "MATCH_START";
    public static final String MOVE = "MOVE";
    public static final String MOVE_RESULT = "MOVE_RESULT";
    public static final String BOARD_SHUFFLED = "BOARD_SHUFFLED";
    public static final String OPPONENT_PROGRESS = "OPPONENT_PROGRESS";
    public static final String LEAVE_MATCH = "LEAVE_MATCH";
    public static final String MATCH_END = "MATCH_END";
    public static final String REMATCH_REPLY = "REMATCH_REPLY";
    public static final String OPPONENT_LEFT = "OPPONENT_LEFT";

    private MessageType() {
    }
}
