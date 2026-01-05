package com.example.prctc;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/predict_mobile")
    Call<PriceResponse> predictMobile(@Body MobileRequest body);

    @POST("/predict_tablet")
    Call<PriceResponse> predictTablet(@Body TabletRequest body);

    @POST("/predict_laptop")
    Call<PriceResponse> predictLaptop(@Body LaptopRequest body);
}
