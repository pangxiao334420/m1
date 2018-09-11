package com.goluk.a6.http.responsebean;

public class CollectResult {
    public int code;
    public String msg;
    public CollectBean data;

    public static class CollectBean {
        public int index;
        public int type;
        public String addtime;
        public String edittime;
        public String collectionId;
        public String uid;
        public String eventId;
    }
}
