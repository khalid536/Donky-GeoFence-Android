package net.donky;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.messaging.rich.inbox.ui.components.RichInboxAndMessageActivityWithToolbar;
import net.donky.geo.GeoFenceActivity;

/**
 * Main demo app activity to showcase some of basic functionality.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserDetails user = DonkyAccountController.getInstance().getCurrentDeviceUser();
        if (user != null) {
            String idTitle = getResources().getString(R.string.id_title) +" "+user.getUserId();
            ((TextView) findViewById(R.id.user_id)).setText(idTitle);
        }
    }

    /**
     * Open Rich Messaging Inbox prebuilt UI
     * @param v View that registered this method on onClick
     */
    public void openInbox(View v) {
        Intent intent = new Intent(this, RichInboxAndMessageActivityWithToolbar.class);
        startActivity(intent);
    }

    public void openGeoModule(View view){
        Intent intent = new Intent(this, GeoFenceActivity.class);
        startActivity(intent);
    }

}
