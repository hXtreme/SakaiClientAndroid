package com.example.development.sakaiclientandroid.utils.requests;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by Development on 5/21/18.
 */

public class RequestCallback {

    public void onCoursesSuccess() { }

    public void onCoursesFailure(Throwable throwable) { }

    public void onSiteGradesSuccess() { }

    public void onSiteGradesFailure(Throwable throwable) { }

    public void onAllGradesSuccess() { }

    public void onAllGradesFailure(Throwable throwable) { }
}