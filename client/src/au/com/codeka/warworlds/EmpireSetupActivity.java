package au.com.codeka.warworlds;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import au.com.codeka.BackgroundRunner;
import au.com.codeka.common.Log;
import au.com.codeka.common.protobuf.Messages;
import au.com.codeka.warworlds.api.ApiClient;
import au.com.codeka.warworlds.api.ApiException;

/**
 * This activity lets you set up your Empire before you actually join the game. You need
 * to give your Empire a name, race and what-not.
 */
public class EmpireSetupActivity extends BaseActivity {
    private static Log log = new Log("EmpireSetupActivity");
    private Context mContext = this;

    @Override
    public void onResume() {
        super.onResume();

        setScreenContent();
    }

    /**
     * Sets up the contents of the home screen.
     */
    private void setScreenContent() {
        setContentView(R.layout.empire_setup);

        View rootView = findViewById(android.R.id.content);
        ActivityBackgroundGenerator.setBackground(rootView);

        final TextView empireName = (TextView) findViewById(R.id.empire_setup_name);
        final Button doneButton = (Button) findViewById(R.id.empire_setup_done);

        empireName.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                saveEmpire();
                return true;
            }
        });

        doneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEmpire();
            }
        });

    }

    private void saveEmpire() {
        final TextView empireName = (TextView) findViewById(R.id.empire_setup_name);
        saveEmpire(empireName.getText().toString());
    }

    private void saveEmpire(final String empireName) {
        // if we've saved off the authentication cookie, cool!
        SharedPreferences prefs = Util.getSharedPreferences();
        final String accountName = prefs.getString("AccountName", null);
        if (accountName == null) {
            // TODO error!
            return;
        }

        final ProgressDialog pleaseWaitDialog = ProgressDialog.show(mContext, null, 
                "Please wait...", true);

        new BackgroundRunner<Boolean>() {
            private String mErrorMsg;

            @Override
            protected Boolean doInBackground() {
                Messages.Empire empire = Messages.Empire.newBuilder().setDisplayName(empireName)
                        .setState(Messages.Empire.EmpireState.ACTIVE)
                        .setEmail(accountName)
                        .build();

                try {
                    return ApiClient.putProtoBuf("empires", empire);
                } catch(ApiException e) {
                    log.error("An unexpected error occured!", e); // TODO??
                    mErrorMsg = e.getServerErrorMessage();
                    return false;
                }
            }

            @Override
            protected void onComplete(Boolean wasSuccessful) {
                pleaseWaitDialog.dismiss();

                // say 'hello' again, to reset the empire details
                ServerGreeter.clearHello();

                if (!wasSuccessful) {
                    new StyledDialog.Builder(mContext)
                            .setTitle("Error")
                            .setMessage(mErrorMsg)
                            .setNeutralButton("OK", null)
                            .create().show();
                    return;
                }

                EmpireSetupActivity.this.setResult(RESULT_OK);
                EmpireSetupActivity.this.finish();
            }
        }.execute();

    }
}
