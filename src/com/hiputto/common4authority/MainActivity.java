package com.hiputto.common4authority;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;

import com.hiputto.common4android.util.HP_DeviceUtils;

import android.Manifest;
import android.net.http.SslError;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

	private String SINA_APP_KEY = "1116767033";
	private String SINA_APP_SECRET = "ef228b7fb6f97ebc5fed23259ef6b025";
	private String SINA_APP_REDIRECT_URL = "http://www.youyouapp.com";
	private String SINA_SSO_CALLBACK_SCHEME = "sinaweibosso.youyouios";

	private WebView authorityWebView;
	private LinearLayout contentLinearLayout;

	private Button leftButton;
	private Button rightButton;

	public static String URL_OAUTH2_ACCESS_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUI();
	}

	private void initUI() {

		LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		contentLinearLayout = new LinearLayout(this);
		contentLinearLayout.setLayoutParams(linearLayoutParams);
		contentLinearLayout.setOrientation(LinearLayout.VERTICAL);
		setContentView(contentLinearLayout);

		initTopBar();

		initWebView();

	}

	private void initTopBar() {

		RelativeLayout topBarRelativeLayout = new RelativeLayout(this);
		RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				HP_DeviceUtils.dip2px(this, 48));
		topBarRelativeLayout.setLayoutParams(relativeLayoutParams);
		topBarRelativeLayout.setBackgroundColor(Color.RED);
		contentLinearLayout.addView(topBarRelativeLayout);

		RelativeLayout.LayoutParams topBarLeftButtonLayoutParams = new RelativeLayout.LayoutParams(
				HP_DeviceUtils.dip2px(this, 48),
				HP_DeviceUtils.dip2px(this, 48));
		topBarLeftButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		leftButton = new Button(this);
		leftButton.setText("刷新");
		leftButton.setLayoutParams(topBarLeftButtonLayoutParams);
		topBarRelativeLayout.addView(leftButton);

		RelativeLayout.LayoutParams topBarRightButtonLayoutParams = new RelativeLayout.LayoutParams(
				HP_DeviceUtils.dip2px(this, 48),
				HP_DeviceUtils.dip2px(this, 48));
		topBarRightButtonLayoutParams
				.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rightButton = new Button(this);
		rightButton.setText("取消");
		rightButton.setLayoutParams(topBarRightButtonLayoutParams);
		topBarRelativeLayout.addView(rightButton);

		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				authorityWebView.reload();
			}
		});

		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

	}

	private void initWebView() {
		authorityWebView = new WebView(this);
		RelativeLayout.LayoutParams webViewLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		authorityWebView.setLayoutParams(webViewLayoutParams);
		contentLinearLayout.addView(authorityWebView);

		authorityWebView.setVerticalScrollBarEnabled(false);
		authorityWebView.setHorizontalScrollBarEnabled(false);
		authorityWebView.getSettings().setJavaScriptEnabled(true);
		authorityWebView.setWebViewClient(new WeiboWebViewClient());

		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("client_id", SINA_APP_KEY);
		parameters.put("response_type", "token");
		parameters.put("redirect_uri", SINA_APP_REDIRECT_URL);
		parameters.put("display", "mobile");

		// if (accessToken != null && accessToken.isSessionValid()) {
		// parameters.add(KEY_TOKEN, accessToken.getToken());
		// }

		String url = URL_OAUTH2_ACCESS_AUTHORIZE + "?";

		Iterator<String> keyIterator = parameters.keySet().iterator();
		Iterator<String> valueIterator = parameters.values().iterator();

		while (keyIterator.hasNext()) {
			String key = keyIterator.next();
			String value = valueIterator.next();
			url += key + "=" + value + "&";
		}

		if (this.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
			Log.d("Error",
					"Application requires permission to access the Internet");
		} else {
			authorityWebView.loadUrl(url);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private String TAG = "Putto Xu :";

	private class WeiboWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith("sms:")) { // 针对webview里的短信注册流程，需要在此单独处理sms协议
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
				sendIntent.putExtra("address", url.replace("sms:", ""));
				sendIntent.setType("vnd.android-dir/mms-sms");
				// WeiboDialog.this.getContext().startActivity(sendIntent);
				return true;
			}
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onPageStarted(WebView webView, String url, Bitmap favicon) {
			Log.d(TAG, "onPageStarted URL: " + url);
			if (url.startsWith(SINA_APP_REDIRECT_URL)) {
				Bundle bundle = parseUrl(url);
				if (bundle.containsKey("error_code")) {
					String errorURI = bundle.getString("error_uri");
					String error = bundle.getString("error");
					String errorDescription = bundle.getString("error_description");
					String errorCode = bundle.getString("error_code");
					
					Log.d(TAG, "error_code: " + errorURI);
					Log.d(TAG, "error_uri: " + error);
					Log.d(TAG, "error: " + errorDescription);
					Log.d(TAG, "error_description: " + errorCode);
					
				}else{
					String accessToken = bundle.getString("access_token");
					String remindIn = bundle.getString("remind_in");
					String expiresIn = bundle.getString("expires_in");
					String uid = bundle.getString("uid");
					
					Log.d(TAG, "access_token: " + accessToken);
					Log.d(TAG, "remind_in: " + remindIn);
					Log.d(TAG, "expires_in: " + expiresIn);
					Log.d(TAG, "uid: " + uid);
					CookieSyncManager.getInstance().sync();
				}
				webView.stopLoading();
				return;
			}
			super.onPageStarted(webView, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d(TAG, "onPageFinished URL: " + url);

			super.onPageFinished(view, url);
		}

		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			handler.proceed();
		}

	}

	public static Bundle parseUrl(String url) {
		try {
			URL u = new URL(url);
			Bundle b = decodeUrl(u.getQuery());
			b.putAll(decodeUrl(u.getRef()));
			return b;
		} catch (MalformedURLException e) {
			return new Bundle();
		}
	}

	public static Bundle decodeUrl(String s) {
		Bundle params = new Bundle();
		if (s != null) {
			String array[] = s.split("&");
			for (String parameter : array) {
				String v[] = parameter.split("=");
				params.putString(URLDecoder.decode(v[0]), URLDecoder.decode(v[1]));
			}
		}
		return params;
	}
}
