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
 * ������
 * 
 * @author Jimmy Chiang
 * 
 */
public class MainActivity extends Activity {

	/**
	 * �������ڳ�����
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

	private LayoutInflater mInflater; // ���������
	private FrameLayout mFrameLayout; // �ɲ㲼��
	private PopupWindow mPopupWindow; // ��������
	private PopupViewHolder mViewHolder; // ����������
	private int mSoundCount; // �����ص���Ч����

	private SharedPreferences settings; // �ֲ�����
	private final String PREFERENCE_NAME = "me.jiangmin.dbb.settings"; // ��������
	private SoundPool pool; // ��Ч��
	private HashMap<Integer, Integer> soundMap; // ��Чӳ��
	private ExitOneMoreTouch exitHelper = new ExitOneMoreTouch(); // �ٰ�һ���˳�����

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		// �����ɲ�
		mFrameLayout = (FrameLayout) findViewById(R.id.main);
		mFrameLayout.getForeground().setAlpha(0);

		loadPopupWindow();
		loadSound();

		// ������������ģʽ
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	/**
	 * ���ص�½����
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
				// ȥ���ɲ�
				rememberOrForgetUserName();
				mFrameLayout.getForeground().setAlpha(0);
			}
		});
	}

	/**
	 * ������Ч
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
					say("��Ч�������");
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
	 * �����ö����˻���¼��ť����Layout�а󶨵��¼�
	 * 
	 * @param view
	 *            �������İ�ť
	 */
	public void onLoginButtonClicked(View view) {

		mFrameLayout.getForeground().setAlpha(200);
		mPopupWindow.showAtLocation((View) view.getParent(),
				Gravity.CENTER_HORIZONTAL, 0, -100);

		// ��ԭ�û����ü��û���
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
	 * ������¼��ť
	 * 
	 * @param view
	 */
	public void onRealLoginButtonClicked(View view) {

		// �����û���
		rememberOrForgetUserName();

		// �ж��û����ڳ���
		new GetLocationTask().setOnLocationListener(
				new GetLocationTask.OnLocationListener() {

					@Override
					public void onLocationGot(Location loc) {
						// TODO ���õ�����Ϣ
					}
				}).execute(this);

		// TODO ��֤���
		if (mViewHolder.txtUsername.getText().toString().equals("jiangmin")
				&& mViewHolder.txtPassword.getText().toString().equals("1")) {
			// ��֤ͨ���������µ�Activity
			// ������Ч
			playSound(1);
		} else {
			// ��֤��ͨ�����������ڶ���
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			mViewHolder.layPop.startAnimation(shake);
			playSound(2);
		}
	}

	/**
	 * ����һ������
	 * 
	 * @param soundId
	 *            �������
	 */
	public void playSound(int soundId) {
		playSound(soundId, 0);
	}

	/**
	 * ��������
	 * 
	 * @param soundId
	 *            �������
	 * @param loop
	 *            ����ѭ��������0��ʾ����һ��
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
	 * ������ס�û�����ѡ��
	 * 
	 * @param view
	 */
	public void onRememberCheckBoxClicked(View view) {
		rememberOrForgetUserName();
	}

	/**
	 * ���ݸ�ѡ��״̬����������������û���
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
	 * �����ù��ߺ���
	 * 
	 * @param msg
	 *            Ҫ��ʾ����Ϣ
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
		
		//�����˳�
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (exitHelper.isExit()) {
				finish();
				return true;
			}
		} else {
			Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show(); 
			exitHelper.doExitInOneSecond(); 
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
