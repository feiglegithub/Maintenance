package com.feigle.maintenance.httpservice;

import com.feigle.maintenance.bean.ResponseModal;
import com.feigle.maintenance.bean.RequestModal;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NoticeService {
    @POST("invoke")
    Call<ResponseModal> getNoticeByWorkNumber(@Body RequestModal request);
}
