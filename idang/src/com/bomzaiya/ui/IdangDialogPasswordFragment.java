package com.bomzaiya.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.bomzaiya.app.idang.R;

public class IdangDialogPasswordFragment extends SherlockDialogFragment {
	public static final String BUNDLE_REFERENCE_TAG = "BUNDLE_REFERENCE_TAG";
	public static final String BUNDLE_DESCRIPTION = "BUNDLE_DESCRIPTION";
	
	OnDialogPasswordListener mDialogPasswordListener = null;
	private IdangFragmentActivity mActivity;

	private String mReferenceTag;
	private String mDescription;

	/**
	 * interface that activity must implement
	 * 
	 * @author bomzaiya
	 * 
	 */
	public interface OnDialogPasswordListener {
		public void onDialogConfirmed(String tag, String password);
		public void onDialogCancelled(String tag);
	}

	public static IdangDialogPasswordFragment newInstance(int title) {
		IdangDialogPasswordFragment frag = new IdangDialogPasswordFragment();
		Bundle args = new Bundle();
		args.putInt("title", title);
		frag.setArguments(args);
		return frag;
	}

	public void onAttach(IdangFragmentActivity activity) {
		super.onAttach(activity);
		try {
			mDialogPasswordListener = (OnDialogPasswordListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnDialogPasswordListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = (IdangFragmentActivity) getActivity();
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Bundle bundle = getArguments();
		
		mReferenceTag = bundle.getString(BUNDLE_REFERENCE_TAG);
		mDescription = bundle.getString(BUNDLE_DESCRIPTION);
		
		View view = inflater.inflate(R.layout.dialog_password, null);

		Button buttonOk = (Button) view.findViewById(R.id.buttonOk);
		Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
		TextView title = (TextView) view.findViewById(R.id.tvTitle);
		
		title.setText(mDescription);
		
		final EditText etPassword = (EditText) view.findViewById(R.id.etPassword);
		
		buttonOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mActivity.onDialogConfirmed(mReferenceTag, etPassword.getText().toString());
				IdangDialogPasswordFragment.this.dismiss();
			}
		});

		buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mActivity.onDialogCancelled(mReferenceTag);
				IdangDialogPasswordFragment.this.dismiss();
			}
		});

		return view;
	}
	
}
