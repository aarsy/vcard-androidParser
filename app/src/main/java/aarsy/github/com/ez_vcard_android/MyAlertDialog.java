package aarsy.github.com.ez_vcard_android;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyAlertDialog extends DialogFragment implements View.OnClickListener {

    private String accountType;
    private String accountName;

    public interface ItemAccountSelectedListener {
        void onAccountSelected(String account_name, String account_type);
    }

    public static MyAlertDialog newInstance(String account_name, String account_type) {
        Bundle args = new Bundle();
        MyAlertDialog fragment = new MyAlertDialog();
        args.putString("accountName", account_name);
        args.putString("accountType", account_type);
        fragment.setArguments(args);
        return fragment;
    }

    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((int) (size.x * 0.85), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        // Call super onResume after sizing
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_dialog_layout, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((LinearLayout) view.findViewById(R.id.ll_google_account)).setOnClickListener(this);
        ((LinearLayout) view.findViewById(R.id.ll_simcard1)).setOnClickListener(this);
        ((LinearLayout) view.findViewById(R.id.ll_device)).setOnClickListener(this);
        TextView tv_mail_id = (TextView) view.findViewById(R.id.tv_email_id);
        accountName = getArguments().getString("accountName");
        accountType = getArguments().getString("accountType");
        tv_mail_id.setText(accountName);
        getDialog().setTitle("Save to");
    }

    @Override
    public void onClick(View v) {
        ItemAccountSelectedListener listener = (ItemAccountSelectedListener) getActivity();
        switch (v.getId()) {
            case R.id.ll_google_account:
                listener.onAccountSelected(accountName, accountType);
                dismiss();
                break;
            case R.id.ll_simcard1:
                listener.onAccountSelected("SIMCARD", null);
                dismiss();
                break;
            case R.id.ll_device:
                listener.onAccountSelected("Phone", null);
                dismiss();
                break;
        }
    }
}
