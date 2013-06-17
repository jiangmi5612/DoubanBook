package me.jiangmin.dbb;

import java.util.HashMap;

import me.jiangmin.jimmy.Location.GetLocationTask;
import me.jiangmin.jimmy.UI.ExitOneMoreTouch;
import android.R.integer;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Looper;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * 启动屏
 * 
 * @author Jimmy Chiang
 * 
 */
public class MainActivity extends Activity {

	/**
	 * 弹出窗口持有者
	 * 
	 * @author Jimmy Chiang
	 * 
	 */
	private static class PopupViewHolder {

		public EditText txtUsername;
		public EditText txtPassword;
		public CheckBox ckbIfRemember;
		public Button btnLogin;
		public LinearLayout layPop;

		PopupViewHolder(PopupWindow w) {
			View v = w.getContentView();
			txtUsername = (EditText) v.findViewById(R.id.txt_username);
			txtPassword = (EditText) v.findViewById(R.id.txt_password);
			ckbIfRemember = (CheckBox) v.findViewById(R.id.ckb_ifRemember);
			btnLogin = (Button) v.findViewById(R.id.btn_realLogin);
			layPop = (LinearLayout) v.findViewById(R.id.lay_pop);
		}
	}

	private LayoutInflater mInflater; // 界面填充器
	private FrameLayout mFrameLayout; // 蒙层布局
	private PopupWindow mPopupWindow; // 弹出窗口
	private PopupViewHolder mViewHolder; // 弹窗持有者
	private int mSoundCount; // 待加载的音效数量

	private SharedPreferences settings; // 局部配置
	private final String PREFERENCE_NAME = "me.jiangmin.dbb.settings"; // 配置名称
	private SoundPool pool; // 声效池
	private HashMap<Integer, Integer> soundMap; // 声效映射
	private ExitOneMoreTouch exitHelper = new ExitOneMoreTouch(); // 再按一次退出助手

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		// 隐藏蒙层
		mFrameLayout = (FrameLayout) findViewById(R.id.main);
		mFrameLayout.getForeground().setAlpha(0);

		loadPopupWindow();
		loadSound();

		// 设置音量调节模式
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	/**
	 * 加载登陆弹窗
	 */
	private void loadPopupWindow() {
		View login_popup = mInflater.inflate(R.layout.popup_login, null);

		mPopupWindow = new PopupWindow(login_popup, 420, 300);
		mViewHolder = new PopupViewHolder(mPopupWindow);

		mPopupWindow.setFocusable(true);
		mPopupWindow.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.black_rect));
		mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				// 去除蒙层
				rememberOrForgetUserName();
				mFrameLayout.getForeground().setAlpha(0);
			}
		});
	}

	/**
	 * 加载音效
	 */
	private void loadSound() {

		pool = new SoundPool(3, AudioManager.STREAM_MUSIC, 100);
		soundMap = new HashMap<Integer, Integer>();

		soundMap.put(1, pool.load(this, R.raw.ptt_sendover, 1));
		soundMap.put(2, pool.load(this, R.raw.notify, 1));

		mSoundCount = soundMap.size();

		pool.setOnLoadCompleteListener(new OnLoadCompleteListener() {

			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				mSoundCount--;
				if (mSoundCount <= 0) {
					say("音效加载完成");
				}
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * 单击用豆瓣账户登录按钮，在Layout中绑定的事件
	 * 
	 * @param view
	 *            被单击的按钮
	 */
	public void onLoginButtonClicked(View view) {

		mFrameLayout.getForeground().setAlpha(200);
		mPopupWindow.showAtLocation((View) view.getParent(),
				Gravity.CENTER_HORIZONTAL, 0, -100);

		// 还原用户设置及用户名
		settings = getSharedPreferences(PREFERENCE_NAME, 0);
		boolean ifRemeber = settings.getBoolean("ifRemember", true);
		String username = settings.getString("username", null);

		mViewHolder.ckbIfRemember.setChecked(ifRemeber);

		if (username != null) {
			mViewHolder.txtUsername.setText(username);
			mViewHolder.txtUsername.setSelection(mViewHolder.txtUsername
					.length());
		}
	}

	/**
	 * 单击登录按钮
	 * 
	 * @param view
	 */
	public void onRealLoginButtonClicked(View view) {

		// 记忆用户名
		rememberOrForgetUserName();

		// 判断用户所在城市
		new GetLocationTask().setOnLocationListener(
				new GetLocationTask.OnLocationListener() {

					@Override
					public void onLocationGot(Location loc) {
						// TODO 利用地理信息
					}
				}).execute(this);

		// TODO 验证身份
		if (mViewHolder.txtUsername.getText().toString().equals("jiangmin")
				&& mViewHolder.txtPassword.getText().toString().equals("1")) {
			// 验证通过，开启新的Activity
			// 播放音效
			playSound(1);
		} else {
			// 验证不通过，抖动窗口动画
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			mViewHolder.layPop.startAnimation(shake);
			playSound(2);
		}
	}

	/**
	 * 播放一遍声音
	 * 
	 * @param soundId
	 *            声音编号
	 */
	public void playSound(int soundId) {
		playSound(soundId, 0);
	}

	/**
	 * 播放声音
	 * 
	 * @param soundId
	 *            声音编号
	 * @param loop
	 *            额外循环次数，0表示播放一次
	 */
	public void playSound(int soundId, int loop) {
		AudioManager mgr = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeCurrent = mgr
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float streamVolumeMax = mgr
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = streamVolumeCurrent / streamVolumeMax;
		pool.play(soundMap.get(soundId), volume, volume, 1, loop, 1f);
	}

	/**
	 * 单击记住用户名复选框
	 * 
	 * @param view
	 */
	public void onRememberCheckBoxClicked(View view) {
		rememberOrForgetUserName();
	}

	/**
	 * 依据复选框状态决定记忆或是忘记用户名
	 */
	private void rememberOrForgetUserName() {

		boolean isChecked = mViewHolder.ckbIfRemember.isChecked();
		SharedPreferences.Editor editor = settings.edit();

		if (isChecked) {
			String username = mViewHolder.txtUsername.getText().toString();
			editor.putString("username", username);
		} else {
			editor.remove("username");
		}
		editor.putBoolean("ifRemeber", isChecked);
		editor.commit();
	}

	/**
	 * 调试用工具函数
	 * 
	 * @param msg
	 *            要显示的消息
	 */
	public void say(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		//处理退出
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (exitHelper.isExit()) {
				finish();
				return true;
			}
		} else {
			Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show(); 
			exitHelper.doExitInOneSecond(); 
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
