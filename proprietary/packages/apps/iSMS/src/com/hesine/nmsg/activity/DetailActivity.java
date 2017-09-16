package com.hesine.nmsg.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hesine.nmsg.R;
import com.hesine.nmsg.bean.ServiceInfo;
import com.hesine.nmsg.common.EnumConstants;
import com.hesine.nmsg.db.DBUtils;
import com.hesine.nmsg.ui.HeaderView;
import com.hesine.nmsg.ui.PopMenu;
import com.hesine.nmsg.ui.ShareDialog;
import com.hesine.nmsg.util.CommonUtils;
import com.hesine.nmsg.util.DeviceInfo;
import com.hesine.nmsg.util.Image;
import com.hesine.nmsg.util.MLog;

public class DetailActivity extends Activity {
	private Context mContext = null;
	private WebView mWebView = null;
	private String imageUrl = null;
	private Bitmap mBitmap = null;
	final Activity context = this;
	private View mProgressBar = null;
	private TextView mEmptyView = null;	
	private xWebChromeClient xwebchromeclient = new xWebChromeClient();
	private View xCustomView;
	private HeaderView mHeader = null;
	private PopMenu popMenu;
	private ServiceInfo serviceInfo;
	private String msgId = null;
	private int msgSubId = -1;
	private String serviceInfoAccount;
	private long mThreadId = 0;
	private String mUrl = null;
	private String mShortUrl = null;
	private LinearLayout mLayout = null;
	private static final int SET_EMPTY_GONE = 0;
	private boolean setEmptyGone = true;
	private String mSubject = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		setOverflowShowingAlways();
		mContext = this;
		initViews();
		Intent intent = getIntent();
		serviceInfoAccount = intent.getExtras().getString("user_account");
		msgId = intent.getStringExtra("msgId");
		msgSubId = intent.getIntExtra("msgSubId", -1);
		mThreadId = intent.getLongExtra("thread_id", 0);
		mUrl = intent.getStringExtra("URL");
		mShortUrl = intent.getStringExtra("shortUrl");
		mSubject = intent.getStringExtra("subject");
		if (serviceInfoAccount == null) {
			MLog.trace("DetailActivity", "serviceInfoAccount == null ");
			finish();
		}
		serviceInfo = DBUtils.getServiceInfo(serviceInfoAccount);
		if (serviceInfo == null) {
			MLog.trace("DetailActivity", "serviceInfo == null ");
			finish();
		}

		initHeader(intent.getStringExtra("Title"));
		initWebView(mUrl);

	//	getSharePackages();
		popMenu = new PopMenu(this);
		popMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0: // share
					showShareDialog();
					break;
				case 1:
					viewContact();
					break;
				case 2:
					// insertContact();
					addContact(serviceInfo);
					break;
				}
				popMenu.dismiss();
			}
		});
		//checkNetwork();
	}
	private void initViews(){
		mProgressBar = (View) findViewById(R.id.progress_bar);
		mEmptyView = (TextView) findViewById(R.id.empty_view);
		mEmptyView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//mEmptyView.setVisibility(View.GONE);
				mWebView.reload();
			}
		});
	}
	private void checkNetwork(){
        if(!DeviceInfo.isNetworkReady(this)){
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }else{
        	mWebView.reload();
        }
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView(String url) {
		mLayout = (LinearLayout) findViewById(R.id.activity_detail);
		mWebView = (WebView) findViewById(R.id.wv_main);
		WebSettings settings = mWebView.getSettings();
		settings.setUseWideViewPort(true); 
		settings.setAllowFileAccess(true);
//		settings.setSupportZoom(true);
//		settings.setBuiltInZoomControls(true);
//		settings.setDisplayZoomControls(false);
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setSavePassword(true);
		settings.setSaveFormData(true);
		settings.setDomStorageEnabled(true);
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptEnabled(true);
//		settings.setTextSize(WebSettings.TextSize.LARGER);
		mWebView.setInitialScale(0);
		mWebView.setWebViewClient(new MyWebViewClient());
		mWebView.addJavascriptInterface(new MyJavascriptInterface(),
				"imagelistner");
		mWebView.setWebChromeClient(xwebchromeclient);
		mWebView.setDownloadListener(new WebViewDownLoadListener());
		mWebView.loadUrl(url);
		((Activity) mContext).registerForContextMenu(mWebView);
		this.registerForContextMenu(mWebView);
	}

	private void initHeader(String title) {
		mHeader = (HeaderView) findViewById(R.id.header);
		mHeader.setTitle(title);
		mHeader.setMoreView(R.drawable.actionbar_more_icon);
//		loading = (ImageView) mHeader.findViewById(R.id.for_browser_loading);
		mHeader.setBackRsc(R.drawable.browser_back);
		mHeader.getMoreView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onKeyDown(KeyEvent.KEYCODE_MENU, null);
				if (popMenu != null) {
					popMenu.showAsDropDown(mHeader.getMoreView());
				}
			}
		});
		mHeader.getBackView().setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
               if( mWebView.canGoBack()){
                   mWebView.goBack();
               }else{
                   finish();
               }
            }
        });
	}

	private void startLoading() {
		mProgressBar.setVisibility(View.VISIBLE);
//		loading.setVisibility(View.VISIBLE);
//		Animation operatingAnim = AnimationUtils.loadAnimation(this,
//				R.anim.loading_rorate);
//		LinearInterpolator lin = new LinearInterpolator();
//		operatingAnim.setInterpolator(lin);
//		loading.startAnimation(operatingAnim);
	}

	private void stopLoading() {
		mProgressBar.setVisibility(View.GONE);
		LayoutParams lp = mProgressBar.getLayoutParams();
		lp.width = 4;
		mProgressBar.setLayoutParams(lp);//		if (loading != null) {
//			loading.clearAnimation();
//			loading.setVisibility(View.GONE);
//		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.browser_nemu, menu);
//		refreshItem = menu.getItem(0);
		return super.onPrepareOptionsMenu(menu);
	}

	private void addContact(final ServiceInfo si) {
	    new Thread(new Runnable() {
            
            @Override
            public void run() {
                if (CommonUtils.isExistSystemContactViaAccount(si)) {
                    runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            Toast.makeText(DetailActivity.this, R.string.add_contact_exist,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    if(CommonUtils.addContactInPhonebook(si)){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(DetailActivity.this, R.string.add_contact_success,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(DetailActivity.this, R.string.add_contact_fail,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLayout.removeView(mWebView);
		mWebView.removeAllViews();
		mWebView.destroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.menu_share:
			showShareDialog();
			break;

		case R.id.menu_add_contact:
			addContact(serviceInfo);
			break;

		case R.id.menu_view_contact:
			viewContact();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		WebView.HitTestResult result = ((WebView) v).getHitTestResult();

		if (result != null) {
			int type = result.getType();
			// Confirm type is an image
			if (type == WebView.HitTestResult.IMAGE_TYPE
					|| type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
				imageUrl = result.getExtra();
				showDialog(imageUrl);
			} else if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
				// mWebView.loadUrl(result.getExtra());
			}
		}
	}

	public static class saveBitmapHandler extends Handler {
        private final WeakReference<DetailActivity> mActivity;

        public saveBitmapHandler(DetailActivity activity) {
        	mActivity = new WeakReference<DetailActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
        	DetailActivity activity = mActivity.get();
          if (activity != null) {
  			super.handleMessage(msg);
  			switch (msg.what) {
  			case com.hesine.nmsg.common.EnumConstants.SAVE_IMG_SUCCESS:
  				activity.mBitmap = (Bitmap) msg.obj;
  				Toast.makeText(activity.getApplicationContext(),
  						activity.getString(R.string.saved) + Image.getImgPath(),
  						Toast.LENGTH_SHORT).show();
  				break;
  			case com.hesine.nmsg.common.EnumConstants.SAVE_IMG_FOR_SHOW_SUCCESS:
  				String path = (String)Image.getImgPath();
  				Intent intent = new Intent("android.intent.action.VIEW");
  		        intent.addCategory("android.intent.category.DEFAULT");
  		        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  		        Uri uri = Uri.fromFile(new File(path));
  		        intent.setDataAndType(uri, "image/*");
  		      activity.startActivity(intent);
  			default:
  				break;
  			}
          }
        }
	}

	private void showDialog(final String imageUrl) {

		new AlertDialog.Builder(DetailActivity.this)
				.setTitle(R.string.save_title)
				.setMessage(R.string.save_image)
				.setPositiveButton(R.string.btn_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String imgName = imageUrl.substring(imageUrl
										.lastIndexOf('/') + 1);
								saveBitmapHandler handler = new saveBitmapHandler(DetailActivity.this);
								Image.setHandler(handler);
								mBitmap = Image.saveImage2Local(context,
										imgName, imageUrl);
								if (mBitmap != null) {
									Toast.makeText(
											getApplicationContext(),
											getString(R.string.saved)
													+ Image.getImgPath(),
											Toast.LENGTH_SHORT).show();
								}
							}
						})
				.setNegativeButton(R.string.btn_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).create().show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    
		if (inCustomView()) {
			hideCustomView();
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (popMenu.isShowing()) {
				popMenu.dismiss();
			} else {
				popMenu.showAsDropDown(mHeader.getMoreView());
			}

			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK && popMenu.isShowing()) {
			popMenu.dismiss();
		}

		return super.onKeyDown(keyCode, event);
	}

	public class MyJavascriptInterface {
		@JavascriptInterface
		public void openImage(String img) {
			String imgName = img.substring(img
					.lastIndexOf('/') + 1);
			saveBitmapHandler handler = new saveBitmapHandler(DetailActivity.this);
			Image.setHandler(handler);
			String path = Image.saveImage2LocalForShowImg(context,
					imgName, img);
			if (path != null) {
				Intent intent = new Intent("android.intent.action.VIEW");
		        intent.addCategory("android.intent.category.DEFAULT");
		        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        Uri uri = Uri.fromFile(new File(path));
		        intent.setDataAndType(uri, "image/*");
		        startActivity(intent);
			}
		}
	}
    private static class MyHandler extends Handler {
        private final WeakReference<DetailActivity> mActivity;

        public MyHandler(DetailActivity activity) {
        	mActivity = new WeakReference<DetailActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
        	DetailActivity activity = mActivity.get();
          if (activity != null) {
        	  super.handleMessage(msg);
              switch (msg.what) {
              case SET_EMPTY_GONE:
            	  activity.mEmptyView.setVisibility(View.GONE);
                  break;
              }
          }
        }
      }

	public final Handler mHandler = new MyHandler(this);
    
	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return super.shouldOverrideUrlLoading(view, url);
		}

		@SuppressLint("SetJavaScriptEnabled")
		@Override
		public void onPageFinished(WebView view, String url) {
			view.getSettings().setJavaScriptEnabled(true);
			stopLoading();
			
		   String title = view.getTitle();
		  if(!TextUtils.isEmpty(title)){
		    mHeader.setTitle(title);
		  }
			
			if(url.contains(EnumConstants.domainName)){
			    addImageClickListner();
			}
			super.onPageFinished(view, url);
            if (setEmptyGone) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                            Message msg = new Message();
                            msg.what = SET_EMPTY_GONE;
                            mHandler.sendMessage(msg);
                        } catch (Exception e) {
                            MLog.PrintStackTrace(e);
                        }
                    }
                }).start();
            }
        }

		@SuppressLint("SetJavaScriptEnabled")
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
		    setEmptyGone = true;
			view.getSettings().setJavaScriptEnabled(true);
			startLoading();
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
		    setEmptyGone = false;
			stopLoading();
			checkNetwork();
			super.onReceivedError(view, errorCode, description, failingUrl);
		}
	}

	private void showShareDialog() {
		ShareDialog shareDialog = new ShareDialog(this)
				.setTitle(R.string.save_image_title);
		shareDialog.setmMsgId(msgId);
		shareDialog.setmSubMsgId(msgSubId);
		shareDialog.setmUrl(mShortUrl);		
		shareDialog.setmSubject(mSubject);
		shareDialog.show();
	}

	public class WebViewDownLoadListener implements DownloadListener {

		@Override
		public void onDownloadStart(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength) {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}

	}

	public class xWebChromeClient extends WebChromeClient {

		public void onProgressChanged(WebView view, int progress) {
			if (progress > 0) {
				int totalWidth = mHeader.getWidth();
//				int currentWidth = mProgressBar.getLayoutParams().width;
				LayoutParams lp = mProgressBar.getLayoutParams();
				lp.width = totalWidth * progress / 100;
				mProgressBar.setLayoutParams(lp);
				mProgressBar.forceLayout();
			}
		}
	       @Override 
	        public void onReceivedTitle(WebView view, String title) { 
	                super.onReceivedTitle(view, title); 
	                mHeader.setTitle(title);
	            } 
	}

	public boolean inCustomView() {
		return (xCustomView != null);
	}

	public void hideCustomView() {
		xwebchromeclient.onHideCustomView();
	}

	public void viewContact() {
		Intent intent = new Intent(mContext, SystemSetting.class);
		intent.putExtra(EnumConstants.NMSG_INTENT_EXTRA_ACCOUNT,
				serviceInfoAccount);
		intent.putExtra(EnumConstants.NMSG_INTENT_EXTRA_THREADID, mThreadId);
		mContext.startActivity(intent);
	}

	private void addImageClickListner() {
		mWebView.loadUrl("javascript:(function(){"
				+ "var objs = document.getElementsByTagName(\"img\"); "
				+ "for(var i=0;i<objs.length;i++)  " + "{"
				+ "    objs[i].onclick=function()  " + "    {  "
				+ "        window.imagelistner.openImage(this.src);  "
				+ "    }  " + "}" + "})()");
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}

	private void setOverflowShowingAlways() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
