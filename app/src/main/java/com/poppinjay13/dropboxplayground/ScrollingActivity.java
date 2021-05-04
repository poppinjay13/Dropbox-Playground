package com.poppinjay13.dropboxplayground;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class ScrollingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            /*
            I'm using a thread coz I don't want to perform network requests on the main thread
            It is more advisable to use an AsyncTask or a Service / IntentService if you will be making regular requests
            And also to avoid memory leaks but I'm lazy.
            */
            Thread thread = new Thread(() -> {
                try {
                    DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
                    DbxClientV2 client = new DbxClientV2(config, Config.ACCESS_TOKEN);
                    // Get current user account info
                    FullAccount account = client.users().getCurrentAccount();
                    Snackbar.make(view, account.getName().getDisplayName(), Snackbar.LENGTH_LONG).show();
                } catch (DbxException ex) {
                    Snackbar.make(view, "A dropbox error occurred", Snackbar.LENGTH_LONG).show();
                    ex.printStackTrace();
                } catch (Exception ex) {
                    Snackbar.make(view, "An error occurred", Snackbar.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
            });
            thread.start();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.developer) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://poppinjay13.github.io/")));
        }
        return super.onOptionsItemSelected(item);
    }
}