package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private String authCode;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView webView = findViewById(R.id.webview_login_activity);
        webView.getSettings().setJavaScriptEnabled(true);

        Uri baseUri = Uri.parse(RedditUtils.OAUTH_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(RedditUtils.CLIENT_ID_KEY, RedditUtils.CLIENT_ID);
        uriBuilder.appendQueryParameter(RedditUtils.RESPONSE_TYPE_KEY, RedditUtils.RESPONSE_TYPE);
        uriBuilder.appendQueryParameter(RedditUtils.STATE_KEY, RedditUtils.STATE);
        uriBuilder.appendQueryParameter(RedditUtils.REDIRECT_URI_KEY, RedditUtils.REDIRECT_URI);
        uriBuilder.appendQueryParameter(RedditUtils.DURATION_KEY, RedditUtils.DURATION);
        uriBuilder.appendQueryParameter(RedditUtils.SCOPE_KEY, RedditUtils.SCOPE);

        String url = uriBuilder.toString();

        CookieManager.getInstance().removeAllCookies(aBoolean -> {});

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains("&code=") || url.contains("?code=")) {
                    Uri uri = Uri.parse(url);
                    String state = uri.getQueryParameter("state");
                    if(state.equals(RedditUtils.STATE)) {
                        authCode = uri.getQueryParameter("code");

                        Map<String, String> params = new HashMap<>();
                        params.put(RedditUtils.GRANT_TYPE_KEY, "authorization_code");
                        params.put("code", authCode);
                        params.put("redirect_uri", RedditUtils.REDIRECT_URI);

                        RedditAPI api = mRetrofit.create(RedditAPI.class);
                        Call<String> accessTokenCall = api.getAccessToken(RedditUtils.getHttpBasicAuthHeader(), params);
                        accessTokenCall.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                if(response.isSuccessful()) {
                                    try {
                                        String accountResponse = response.body();
                                        if(accountResponse == null) {
                                            //Handle error
                                            return;
                                        }

                                        JSONObject responseJSON = new JSONObject(accountResponse);
                                        String accessToken = responseJSON.getString(RedditUtils.ACCESS_TOKEN_KEY);
                                        String refreshToken = responseJSON.getString(RedditUtils.REFRESH_TOKEN_KEY);

                                        FetchMyInfo.fetchAccountInfo(mOauthRetrofit, accessToken, new FetchMyInfo.FetchUserMyListener() {
                                            @Override
                                            public void onFetchMyInfoSuccess(String response) {
                                                ParseAndSaveAccountInfo.parseAndSaveAccountInfo(response, mRedditDataRoomDatabase, new ParseAndSaveAccountInfo.ParseAndSaveAccountInfoListener() {
                                                    @Override
                                                    public void onParseMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma) {
                                                        new ParseAndInsertNewAccountAsyncTask(name, accessToken, refreshToken, profileImageUrl, bannerImageUrl,
                                                                karma, authCode, mRedditDataRoomDatabase.accountDao(),
                                                                () -> {
                                                                    Intent resultIntent = new Intent();
                                                                    setResult(Activity.RESULT_OK, resultIntent);
                                                                    finish();
                                                                }).execute();
                                                    }

                                                    @Override
                                                    public void onParseMyInfoFail() {
                                                        Toast.makeText(LoginActivity.this, R.string.parse_user_info_error, Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFetchMyInfoFail() {
                                                Toast.makeText(LoginActivity.this, R.string.cannot_fetch_user_info, Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(LoginActivity.this, R.string.parse_json_response_error, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, R.string.retrieve_token_error, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                Toast.makeText(LoginActivity.this, R.string.retrieve_token_error, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else if (url.contains("error=access_denied")) {
                    Toast.makeText(LoginActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    view.loadUrl(url);
                }

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}
