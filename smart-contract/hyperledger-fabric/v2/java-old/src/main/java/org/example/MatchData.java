package org.example;

public class MatchData {
    public enum MatchType {
        CUSTOMER_LAST_NAME, ORDER_CUSTOMER_ID, PARSEABLE_NEWORDER
    }

    public MatchType type;
    public String c_last = null;
    public int o_c_id = -1;

    private MatchData(MatchType type) {
        this.type = type;
    }

    public static MatchData CLastMatchData(final String c_last) {
        MatchData resp = new MatchData(MatchType.CUSTOMER_LAST_NAME);
        resp.c_last = c_last;
        return resp;
    }

    public static MatchData OCIDMatchData(final int o_c_id) {
        MatchData resp = new MatchData(MatchType.ORDER_CUSTOMER_ID);
        resp.o_c_id = o_c_id;
        return resp;
    }

    public static MatchData ParseNewOrderMatchData() {
        return new MatchData(MatchType.PARSEABLE_NEWORDER);
    }
}
