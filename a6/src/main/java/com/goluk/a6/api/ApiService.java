package com.goluk.a6.api;

import com.goluk.a6.http.responsebean.BindList;
import com.goluk.a6.http.responsebean.DeviceStatus;
import com.goluk.a6.http.responsebean.EventCollectInfo;
import com.goluk.a6.http.responsebean.EventVideoList;
import com.goluk.a6.http.responsebean.TrackDetail;
import com.goluk.a6.http.responsebean.TrackList;

import likly.reverse.Call;
import likly.reverse.annotation.CallExecuteListener;
import likly.reverse.annotation.FormBody;
import likly.reverse.annotation.GET;
import likly.reverse.annotation.POST;
import likly.reverse.annotation.Part;
import likly.reverse.annotation.Query;
import likly.reverse.annotation.ServiceInvokeListener;

/**
 * 请求定义
 */
//@BaseUrl("https://test2bservice.goluk.cn")
@ServiceInvokeListener(OnApiServiceRequestListener.class)
@CallExecuteListener(ApiCallExecuteListener.class)
@SuppressWarnings("all")
public interface ApiService {

    /**
     * 获取事件视频列表
     *
     * @param imei      设备IMEI
     * @param operation 0:首次; 1:下拉; 2:上拉
     * @param index     首次进入为空, 使用返回数据中的xxx的index
     * @param pagesize  默认20个
     * @param callback
     * @return
     */
    @GET("/record/events")
    Call<EventVideoList> getEventVideoList(@Query("imei") String imei, @Query("operation") int operation,
                                           @Query("index") int index, @Query("pagesize") int pagesize,
                                           Callback<EventVideoList> callback);

    /**
     * 获取收藏的事件视频列表
     *
     * @param imei      设备IMEI
     * @param operation 0:首次; 1:下拉; 2:上拉
     * @param index     首次进入为空, 使用返回数据中的xxx的index
     * @param pagesize  默认20个
     * @param callback
     * @return
     */
    @GET("/record/event/collections")
    Call<EventVideoList> getCollectionEventVideoList(@Query("operation") int operation,
                                                     @Query("index") int index, @Query("pagesize") int pagesize,
                                                     Callback<EventVideoList> callback);

    /**
     * 查询设备在线状态/位置信息
     *
     * @param imei     IMEI
     * @param type     0
     * @param callback
     * @return
     */
    @GET("/carbox/info")
    Call<DeviceStatus> queryDeviceStatus(@Query("imei") String imei, @Query("type") int type,
                                         Callback<DeviceStatus> callback);

    /**
     * 查询用户当前已绑定的设备列表
     */
    @FormBody
    @POST("/carbox/app/bindings")
    Call<BindList> queryBindList(@Part("operation") int operation, @Part("index") int index, @Part("pagesize") int pagesize,
                                 Callback<BindList> callback);

    /**
     * 查询历史轨迹列表
     *
     * @param imei      设备IMEI
     * @param operation 0:首次; 1:下拉; 2:上拉
     * @param index     首次进入为空, 使用返回数据中的xxx的index
     * @param pagesize  默认20个
     * @param callback
     * @return
     */
    @GET("/tracks")
    Call<TrackList> trackList(@Query("imei") String imei, @Query("operation") int operation,
                              @Query("index") int index, @Query("pagesize") int pagesize,
                              Callback<TrackList> callback);

    /**
     * 轨迹详情
     *
     * @param trackId  trackid
     * @param callback
     * @return
     */
    @GET("/track")
    Call<TrackDetail> trackDetail(@Query("trackId") String trackId, Callback<TrackDetail> callback);

    /**
     * 实时轨迹
     *
     * @param trackId  trackid
     * @param offset   offset
     * @param callback
     * @return
     */
    @GET("/track/realtime")
    Call<TrackDetail> realtimeTrack(@Query("trackId") String trackId, @Query("offset") int offset, Callback<TrackDetail> callback);

    /**
     * 获取事件是否已经收藏
     *
     * @param eventId  事件ID
     * @param callback
     * @return
     */
    @GET("/record/event/collection")
    Call<EventCollectInfo> eventCollectInfo(@Query("eventId") String eventId, Callback<EventCollectInfo> callback);

    /**
     * 收藏/取消收藏事件
     *
     * @param eventId  事件ID
     * @param type     0:取消收藏; 1:收藏
     * @param callback
     * @return
     */
    @FormBody
    @POST("/record/event/collection")
    Call<String> collectEvent(@Part("eventId") String eventId, @Part("type") int type, Callback<String> callback);

    /**
     * 事件详情
     *
     * @param eventId 事件Id
     */
    @GET("/record/event")
    Call<EventVideoList.EventVideo> eventDetail(@Query("eventId") String eventId, Callback<EventVideoList.EventVideo> callback);

}
