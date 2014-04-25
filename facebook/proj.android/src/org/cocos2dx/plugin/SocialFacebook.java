package org.cocos2dx.plugin;

import java.util.Hashtable;
import java.util.List;

import org.json.JSONArray;

import lib.facebook.connection.GFacebook;
import lib.facebook.connection.GFacebookConfiguration;
import lib.facebook.connection.GPermissions;
import lib.facebook.entities.Photo;
import lib.facebook.entities.Profile;
import lib.facebook.entities.Story;
import lib.facebook.*;
import lib.facebook.g_interface.OnCompleteListener;
import lib.facebook.g_interface.OnInviteListener;
import lib.facebook.g_interface.OnLoginListener;
import lib.facebook.g_interface.OnPostScoreListener;
import lib.facebook.g_interface.OnPublishListener;
import lib.facebook.g_interface.OnScoresRequestListener;

import com.facebook.FacebookException;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class SocialFacebook implements InterfaceSocial {

	private static final String TAG = "SocialFacebook";
	private static Activity mContext = null;
	private static InterfaceSocial mSocialAdapter = null;
	protected static boolean bDebug = false;
	private static String CONSUMER_KEY="";
	private static String CONSUMER_SECRET="";
	private static String LINK_SHARE ="";
	private static String NAME_SHARE ="";
	private static String CAPTION_SHARE="";
	private static String PICTURE_SHARE = "";
	private static String DESCRIPTION_SHARE = "";
	private static String DIR_PHOTO = "";
	private static GFacebook gFacebook ;
	private static SocialFacebook mFacebook = null;
	private static boolean isInitialized = false;
	private static Hashtable<String, String> mShareInfo = null;
	private static String shareContent = "";
	private static String shareFile = "";
	private static String TYPE_SHARE = "";
	private static String jsonScore ="";

	protected static void LogE(String msg, Exception e) {
		Log.e(TAG, msg, e);
		e.printStackTrace();
	}

	protected static void LogD(String msg) {
		if (bDebug) {
			Log.d(TAG, msg);
		}
	}

	 public SocialFacebook(Context context) {
		mContext = (Activity) context;
		mSocialAdapter = this;
	}
	
	public static GFacebook getGFacebook(){
		return gFacebook;
	}
	@Override
	public void configDeveloperInfo(Hashtable<String, String> cpInfo) {
		LogD("initDeveloperInfo invoked " + cpInfo.toString());
		try {
			SocialFacebook.CONSUMER_KEY = cpInfo.get("FacebookKey");
			SocialFacebook.CONSUMER_SECRET = cpInfo.get("FacebookSecret");
			LogD("key : " + SocialFacebook.CONSUMER_KEY);
			LogD("secret : " + SocialFacebook.CONSUMER_SECRET);
			GPermissions[] permissions = new GPermissions[] {
					GPermissions.BASIC_INFO,
					GPermissions.PUBLISH_ACTION,
					GPermissions.PUBLISH_STREAM,
			};
			GFacebookConfiguration gFacebookConfiguration = new GFacebookConfiguration.Builder()
					.setAppId(SocialFacebook.CONSUMER_KEY)
					.setPermissions(permissions).build();
			GFacebook.setConfiguration(gFacebookConfiguration);
			
			if(isInitialized){
				return;
			}
			isInitialized = true;	
			mContext = (Activity) PluginWrapper.getContext();
			gFacebook = GFacebook.getInstance(mContext);
			
		} catch (Exception e) {
			LogE("Developer info is wrong!", e);
		}

	}

	@Override
	public void share(Hashtable<String, String> info) {
		LogD("share invoked " + info.toString());
		SocialFacebook.TYPE_SHARE = info.get("typeShare");
		if ( SocialFacebook.TYPE_SHARE.equals("photo") == true){
			SocialFacebook.DESCRIPTION_SHARE = info.get("Description");
			SocialFacebook.DIR_PHOTO = info.get("dirPhoto");
			shareFile = SocialFacebook.DIR_PHOTO;
		}
		else {
		SocialFacebook.LINK_SHARE = info.get("LinkShare");
		SocialFacebook.NAME_SHARE = info.get("Name");
		SocialFacebook.CAPTION_SHARE = info.get("Caption");
		SocialFacebook.PICTURE_SHARE = info.get("LinkPicture");
		SocialFacebook.DESCRIPTION_SHARE = info.get("Description");
		}
		
		if (! networkReachable()) {
			shareResult(SocialWrapper.SHARERESULT_FAIL, "Network error!");
			return;
		}

		if (! isInitialized) {
			shareResult(SocialWrapper.SHARERESULT_FAIL, "Initialize failed!");
			return;
		}

		// Kiem tra login hay chua , neu chua thi login
		if (gFacebook.isLogin() == false ){
			PluginWrapper.runOnMainThread(new Runnable() {
				
				@Override
				public void run() {
					gFacebook.login(mLoginListener);
				}
			});

			return;
		}
		
		// Neu da login roi thi share 
		PluginWrapper.runOnMainThread(new Runnable() {
				@Override
			public void run() {
				sharefb();
			}
		});
	}

	@Override
	public void setDebugMode(boolean debug) {
		bDebug = debug;
	}

	@Override
	public String getSDKVersion() {
		return "Unknown version";
	}

	private boolean networkReachable() {
		boolean bRet = false;
		try {
			ConnectivityManager conn = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = conn.getActiveNetworkInfo();
			bRet = (null == netInfo) ? false : netInfo.isAvailable();
		} catch (Exception e) {
			LogE("Fail to check network status", e);
		}
		LogD("NetWork reachable : " + bRet);
		return bRet;
	}

	private static void shareResult(int ret, String msg) {
		SocialWrapper.onShareResult(mSocialAdapter, ret, msg);
		LogD("SocialFacebook result : " + ret + " msg : " + msg);
	}
/*	private static void scoreResult(int ret , String score){
		SocialWrapper.onScoreResult(mSocialAdapter, ret, jsonScore);
	}
*/
	@Override
	public String getPluginVersion() {
		return "0.2.0";
	}
	
	private OnLoginListener mLoginListener = new OnLoginListener() {

		@Override
		public void onFail(String reason) {
		Log.d(TAG, "Dang nhap thanh cong ");
		}

		@Override
		public void onException(Throwable throwable) {
		Log.d(TAG, "Dang nhap co ngoai le ", throwable);

		}

		@Override
		public void onThinking() {
		Log.d(TAG, "Dang dang nhap ");
		}

		@Override
		public void onNotAcceptingPermissions() {

		}

		@Override
		public void onLogin() {
		Log.d(TAG,"Dang nhap thanh cong ");
		//publishFeedDialog();
		sharefb();
		}
	};
	
	
	private static void publishFeedDialog() {
		Bundle params = new Bundle();
		params.putString("name", SocialFacebook.NAME_SHARE);
		params.putString("caption", SocialFacebook.CAPTION_SHARE);
		params.putString("description", SocialFacebook.DESCRIPTION_SHARE);
		params.putString("link", SocialFacebook.LINK_SHARE);
		params.putString("picture", SocialFacebook.PICTURE_SHARE);

		OnCompleteListener onCompleteListener = new OnCompleteListener() {

		@Override
			public void onComplete(Bundle values ,  FacebookException error) {
			if (error == null) {
			// When the story is posted, echo the success
			// and the post Id.
				final String postId = values.getString("post_id");
				if (postId != null) {
				shareResult(SocialWrapper.SHARERESULT_SUCCESS, "success!");
				} else {
				// User clicked the Cancel button
				shareResult(SocialWrapper.SHARERESULT_FAIL, "Cancel public!");
				}
			} else {
				// Generic, ex: network error
				shareResult(SocialWrapper.SHARERESULT_FAIL, "Network error!");
				}

			}
		};

		gFacebook.publishFeedDialog(params, onCompleteListener);
	}
	static class Getbitmap extends AsyncTask<Void, Void, Void> {
		//Bitmap bitmap;
		Bitmap bitmap;
	

		@Override
		protected Void doInBackground(Void... paramds) {
			try {
				bitmap = BitmapFactory.decodeFile(shareFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			shareContent = SocialFacebook.DESCRIPTION_SHARE;
			Photo photo = new Photo(bitmap);
			photo.addDescription(shareContent);
			// publish
			gFacebook.publish(photo, new OnPublishListener() {
				ProgressDialog dialog;

				@Override
				public void onFail(String reason) {
					// insure that you are logged in before publishing
					if (dialog != null)
						dialog.dismiss();
					Log.w(TAG, "Failed to publish" + reason);
				}

				@Override
				public void onException(Throwable throwable) {
					if (dialog != null)
						dialog.dismiss();
					Log.e(TAG, "Bad thing happened", throwable);
				}

				@Override
				public void onThinking() {
					dialog = new ProgressDialog(mContext);
					dialog.setCancelable(false);
					dialog.setMessage("Processing...");
					dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					dialog.setMax(100);
					dialog.show();
					// show progress bar or something to the user while
					// publishing
				}

				@Override
				public void onComplete(String id) {
					dialog.dismiss();
				}
			});
			super.onPostExecute(result);
		}
	}
	
	private static  OnPostScoreListener mOnScoreListener = new OnPostScoreListener() {
		
		@Override
		public void onFail(String arg0) {
			LogD(" Post score failed");
			
		}
		
		@Override
		public void onException(Throwable arg0) {
			LogD(" post score exception");
			
		}
		
		@Override
		public void onThinking() {
			LogD("Post score thinking");
			
		}
		
		@Override
		public void onComplete() {
			LogD("Post score compelete");
			
		}
	
		
	
	};
	public static void postScore()
	{
		gFacebook.postScore(45, mOnScoreListener);
	}
	
	public static void getScore()
	{
		
		gFacebook.getScores( new OnScoresRequestListener() {
			
			@Override
			public void onFail(String arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onException(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onThinking() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onComplete(JSONArray arg0) {
				jsonScore = arg0.toString();
				LogD(" thanh cong roi " + jsonScore);
				//scoreResult(SocialWrapper.SHARERESULT_SUCCESS,jsonScore);
				
				
			}
		});
		
		
	}
	public static void functionInvite(){
		gFacebook.invite("Nào chúng ta cùng chơi escape", new OnInviteListener() {
			
			@Override
			public void onFail(String arg0) {
				LogD("invite failed");
				
			}
			
			@Override
			public void onException(Throwable arg0) {
				LogD("exception");
				
			}
			
			@Override
			public void onComplete(List<String> arg0, String arg1) {
				LogD("invite complete");
			}
			
			@Override
			public void onCancel() {
				LogD("invite cancel");
				
			}
		});
		
	}
	public static void sharefb() {
		if (SocialFacebook.TYPE_SHARE.equals("photo")== true){
			 new Getbitmap().execute();
		}
		else if (SocialFacebook.TYPE_SHARE.equals("postscore")== true){
			postScore();
			
		}
		else if (SocialFacebook.TYPE_SHARE.equals("sharelink")== true){
			publishFeedDialog();
		}
		else if (SocialFacebook.TYPE_SHARE.equals("getscore")== true){
			getScore();
		}
		else if ( SocialFacebook.TYPE_SHARE.equals("invite")== true){
			functionInvite();
		}
	}
	
	
}

