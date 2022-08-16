package com.feigle.test.httpservice;

import com.feigle.test.bean.RequestModal;
import com.feigle.test.bean.ResponseModal;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TemplateMessageService {
    @POST("message/template/send?access_token=59_gFrOwHClE_yUAE1CtupfRH6OZKCzJqZcr7VOYBp4uzzzUFKsrh21NBqHrqogKCGC9AKfwaCeJuyOnpHNb6Je9wHXVReDB97owwFBAl5t9_3Th3OozCapBNqQeg73uSxRDAcn94CftcC6TyuEUFRhAEAGRD")
    Call<ResponseModal> send(@Body RequestModal requestModal);
}
