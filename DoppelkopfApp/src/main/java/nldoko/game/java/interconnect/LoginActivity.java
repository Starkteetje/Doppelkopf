package nldoko.game.java.interconnect;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import nldoko.game.R;
import nldoko.game.java.DokoActivity;
import nldoko.game.java.util.Uploader;

public class LoginActivity extends DokoActivity {
    public static final String REQUEST_PATH_UPLOAD_GAME = "/api/report";
    public static final String REQUEST_PATH_LOGIN = "/api/login";

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.login);
        setupDrawerAndToolbar(mContext.getResources().getString(R.string.str_login));
        // Use default local network
        ((EditText)this.findViewById(R.id.server_url)).setText("http://192.168.56.1:8080");
        setClickListener();
    }

    private void setClickListener() {
        Button mBtnLogin = (Button) findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new LoginClickListener());
    }

    private class LoginClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            View parent = (View) v.getParent();
            String username = ((EditText) parent.findViewById(R.id.user_name)).getText().toString();
            String password = ((EditText) parent.findViewById(R.id.user_password)).getText().toString();
            String uploadUrl = ((EditText) parent.findViewById(R.id.server_url)).getText().toString();
            // Store URL immediately
            SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("DokoServerTokenStorage", MODE_PRIVATE);
            sharedPreferences.edit().putString("url", uploadUrl).apply();
            ProgressDialog dialog = new ProgressDialog(v.getContext());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getResources().getString(R.string.str_login_in_progress));
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);

            Response.Listener<String> listener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    dialog.cancel();
                    Toast toast = Toast.makeText(v.getContext(), R.string.str_login_successful, Toast.LENGTH_SHORT);
                    View toastView = toast.getView();
                    toastView.getBackground().setColorFilter(getResources().getColor(R.color.black, v.getContext().getTheme()), PorterDuff.Mode.SRC_IN);
                    TextView toastText = (TextView)toastView.findViewById(android.R.id.message);
                    toastText.setTextColor(getResources().getColor(R.color.white, v.getContext().getTheme()));
                    toast.show();
                    v.setClickable(false);
                    v.setEnabled(false);
                    v.setBackgroundColor(getResources().getColor(R.color.gray, v.getContext().getTheme()));
                    SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("DokoServerTokenStorage", MODE_PRIVATE);
                    sharedPreferences.edit().putString("token", response).apply();
                }
            };
            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.cancel();
                    String errorMessage = getResources().getString(R.string.str_login_error);
                    NetworkResponse response = error.networkResponse;
                    if (response != null) {
                        int statusCode = response.statusCode;
                        if (statusCode == 401) {
                            errorMessage += getResources().getString(R.string.str_login_wrong_credentials);
                        } else {
                            errorMessage += getResources().getString(R.string.str_login_error_code) + " " + statusCode;
                        }
                    }

                    Toast toast = Toast.makeText(v.getContext(), errorMessage, Toast.LENGTH_SHORT);
                    View toastView = toast.getView();
                    toastView.getBackground().setColorFilter(getResources().getColor(R.color.black, v.getContext().getTheme()), PorterDuff.Mode.SRC_IN);
                    TextView toastText = (TextView)toastView.findViewById(android.R.id.message);
                    toastText.setTextColor(getResources().getColor(R.color.white, v.getContext().getTheme()));
                    toast.show();
                }
            };

            dialog.show();
            Uploader.requestToken(username, password, mContext, listener, errorListener);
        }
    }

}
