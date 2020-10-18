package a.gautham.smartswitchboard.services;

import a.gautham.smartswitchboard.models.GeoResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface LocationApiService {
    @GET("json")
    Call<GeoResponse> getLocation();
}
