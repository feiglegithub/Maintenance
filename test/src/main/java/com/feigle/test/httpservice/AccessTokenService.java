package com.feigle.test.httpservice;

import com.feigle.test.bean.ResponseAccesToken;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AccessTokenService {
    @GET("token?grant_type=client_credential&appid=wx0450c988352f7326&secret=b3ee3c5a5724fd3f659ef15eda8112a9")
    Call<ResponseAccesToken> get();
}
