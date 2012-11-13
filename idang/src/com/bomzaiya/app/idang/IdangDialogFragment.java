package com.bomzaiya.app.idang;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.bomzaiya.internet.InternetHelper;

public class IdangDialogFragment extends SherlockDialogFragment {
  public static final String BUNDLE_REFERENCE_TAG = "BUNDLE_REFERENCE_TAG";
  public static final String BUNDLE_DESCRIPTION = "BUNDLE_DESCRIPTION";
  public static final String BUNDLE_PARAM = "BUNDLE_PARAM";
  public static final String BUNDLE_DIALOG_TYPE = "BUNDLE_DIALOG_TYPE";

  public static final int DIALOG_TYPE_CONFIRMED = 0;
  public static final int DIALOG_TYPE_DIALER = 1;

  public int mDialogType = DIALOG_TYPE_CONFIRMED;
  public String mParam = "";

  OnDialogListener mDialogListener = null;
  private IdangFragmentActivity mActivity;

  private String mReferenceTag;
  private String mDescription;

  /**
   * interface that activity must implement
   * 
   * @author bomzaiya
   * 
   */
  public interface OnDialogListener {
    public void onDialogConfirmed(String tag);
    public void onDialogConfirmed(String tag, String param);
    public void onDialogCancelled(String tag);
  }

  public static IdangDialogFragment newInstance(int title) {
    IdangDialogFragment frag = new IdangDialogFragment();
    Bundle args = new Bundle();
    args.putInt("title", title);
    frag.setArguments(args);
    return frag;
  }

  public void onAttach(IdangFragmentActivity activity) {
    super.onAttach(activity);
    try {
      mDialogListener = (OnDialogListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnDialogListener");
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = (IdangFragmentActivity) getActivity();
    setStyle(DialogFragment.STYLE_NO_TITLE, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Bundle bundle = getArguments();

    mReferenceTag = bundle.getString(BUNDLE_REFERENCE_TAG);
    mDescription = bundle.getString(BUNDLE_DESCRIPTION);

    try {
      mDialogType = bundle.getInt(BUNDLE_DIALOG_TYPE);
      mParam = bundle.getString(BUNDLE_PARAM);
    } catch (NullPointerException e) {

    }
    

    View view = inflater.inflate(R.layout.dialog_confirmed, null);

    Button buttonOk = (Button) view.findViewById(R.id.buttonOk);
    Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
    TextView title = (TextView) view.findViewById(R.id.tvTitle);

    title.setText(mDescription);

    buttonOk.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        switch (mDialogType) {
        case DIALOG_TYPE_CONFIRMED:
          if (mParam != null) {
            mActivity.onDialogConfirmed(mReferenceTag, mParam);
          } else {
            mActivity.onDialogConfirmed(mReferenceTag);  
          }
          
          break;

        case DIALOG_TYPE_DIALER:
          if (mParam != null) {
            InternetHelper.dial(mActivity.getBaseContext(), mParam);
          }
          break;

        default:
          break;
        }

        // when do restart method, this will be null
        try {
          IdangDialogFragment.this.dismiss();
        } catch (NullPointerException e) {
        }
      }
    });

    buttonCancel.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        mActivity.onDialogCancelled(mReferenceTag);
        IdangDialogFragment.this.dismiss();
      }
    });

    return view;
  }

}
