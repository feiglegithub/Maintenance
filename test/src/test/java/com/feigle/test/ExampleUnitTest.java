package com.feigle.test;

import com.feigle.test.bean.Data;
import com.feigle.test.bean.RequestModal;
import com.feigle.test.bean.ResponseAccesToken;
import com.feigle.test.bean.ResponseModal;
import com.feigle.test.bean.Value;
import com.feigle.test.httpservice.AccessTokenService;
import com.feigle.test.httpservice.TemplateMessageService;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void send(){
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl("https://api.weixin.qq.com/cgi-bin/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        TemplateMessageService templateMessageService = retrofit.create(TemplateMessageService.class);
        Data data = new Data();
        Value first = new Value();
        first.value="测试";
        Value content = new Value();
        content.value="测试一下啦";
        content.color="#173177";
        data.content=content;
        data.first=first;
        RequestModal requestModal = new RequestModal();
        requestModal.data = data;
        requestModal.template_id="S_zS46lzApKCKeWoH74OpE9EUcUKy7sE5kQmXzjcspo";
        requestModal.touser="o95iK5hhvV4PPLSXrci9oy8mj0oI";
        Call<ResponseModal> call = templateMessageService.send(requestModal);
        try {
            Response<ResponseModal> response = call.execute();
            System.out.println(response.body().errmsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getToken(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.weixin.qq.com/cgi-bin/").addConverterFactory(GsonConverterFactory.create()).build();
        AccessTokenService accessTokenService = retrofit.create(AccessTokenService.class);
        Call<ResponseAccesToken> call = accessTokenService.get();
        try {
            Response<ResponseAccesToken> response = call.execute();
            System.out.println(response.body().access_token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}