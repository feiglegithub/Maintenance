import com.feigle.maintenance.bean.Parameter;
import com.feigle.maintenance.bean.RequestModal;
import com.feigle.maintenance.bean.ResponseModal;
import com.feigle.maintenance.httpservice.NoticeService;
import com.google.gson.Gson;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpTest {
    @Test
    public void testPost() throws IOException {
        Parameter parameter = new Parameter();
        parameter.Value = "11004028";
        List parameters = new ArrayList<>();
        parameters.add(parameter);

        com.feigle.maintenance.bean.Context mesContext = new com.feigle.maintenance.bean.Context();
        mesContext.InvOrgId = "1";

        RequestModal request = new RequestModal();
        request.Parameters = parameters;
        request.ApiType = "WatchSendController";
        request.Context = mesContext;
        request.Method = "GetWatchMessage";

        Gson gson = new Gson();
        String json = gson.toJson(request);

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://mestest.sfygroup.com:810/Server.svc/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        NoticeService noticeService = retrofit.create(NoticeService.class);

        Call<ResponseModal> call = noticeService.getNoticeByWorkNumber(request);

        Buffer buffer = new Buffer();

        call.request().body().writeTo(buffer);
        System.out.println("------------------request--------------------------------");
        System.out.println(call.request().url().url());
        System.out.println(buffer.readString(Charset.forName("utf-8")));

        Response<ResponseModal> res = call.execute();

        System.out.println("-------------------------respone-------------------------");
        System.out.println(res.body().Success);
        System.out.println(res.body().Message);
        System.out.println(res.body().Result);
    }

    @Test
    public void test1() throws IOException {
        RequestBody rb = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),"{\n" +
                "  \"ApiType\": \"WatchSendController\",\n" +
                "  \"Parameters\": [\n" +
                "    {\n" +
                "      \"Value\": \"11004028\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"Method\": \"GetWatchMessage\",\n" +
                "  \"Context\": {\n" +
                "    \"InvOrgId\": 1\n" +
                "  }\n" +
                "}");

        Buffer buffer = new Buffer();
        rb.writeTo(buffer);

        System.out.println(buffer.readString(Charset.forName("utf-8")));
        Request request = new Request.Builder()
                .url("http://mestest.sfygroup.com:810/Server.svc/api/invoke")
                .post(rb)
                .build();

        OkHttpClient client = new OkHttpClient();

        okhttp3.Call call = client.newCall(request);
        okhttp3.Response response = call.execute();
        System.out.println(response.body().string());
    }
}
