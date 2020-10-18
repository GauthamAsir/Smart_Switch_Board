package a.gautham.smartswitchboard.services;

import a.gautham.smartswitchboard.models.MyResponse;
import a.gautham.smartswitchboard.models.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface APIService {

    @Headers(
            {
                    "Content-Type: application/json",
                    "Authorization:key=AAAAwPyWjJw:APA91bFWrr3il8G-i0iqRMCOWAMX9Nosy01zKQzHSm2QAWxgl-5OXkWGp7ldZERpq67dJdWukAG65wQpkYHIv1SzmiayZPNQZiGamF0DoqcPfTtrwiSOgtzTNPN22XZUup27GT06_h4-"
            }

    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
