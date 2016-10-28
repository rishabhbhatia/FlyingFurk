package com.satiate.flyingfurk.retrofit;

import com.satiate.flyingfurk.models.FurkResponse;
import com.satiate.flyingfurk.utils.Const;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Created by Rishabh Bhatia on 28/10/16.
 */

public interface FurkNetworkService {

    @GET(Const.FUCK_API)
    Call<FurkResponse> getFurk(@Header("Accept") String accept);
}
