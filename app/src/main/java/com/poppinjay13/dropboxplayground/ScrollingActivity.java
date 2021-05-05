package com.poppinjay13.dropboxplayground;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.poppinjay13.dropboxplayground.adapters.DirectoryAdapter;
import com.poppinjay13.dropboxplayground.entities.Meta;

import java.util.ArrayList;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity {

    RecyclerView dropboxRecycler;
    List<Meta> metaList = new ArrayList<>();
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        dropboxRecycler = findViewById(R.id.dropbox_recycler);

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
                    //Updating UI on main thread
                    runOnUiThread(() -> toolBarLayout.setTitle(account.getName().getDisplayName()));
                    getFilesInRootDir(client);
                } catch (NetworkIOException ex) {
                    Snackbar.make(view, "Unable to connect to Dropbox", Snackbar.LENGTH_LONG).show();
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

    private void getFilesInRootDir(DbxClientV2 client) throws DbxException {
        // Get files and folder metadata from Dropbox root directory
        ListFolderResult result = client.files().listFolder("");
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                //Converting to Meta POJO to access inaccessible fields in Metadata
                Meta meta = new Gson().fromJson(metadata.toString(), Meta.class);
                metaList.add(meta);
            }
            if (!result.getHasMore()) {
                break;
            }
            result = client.files().listFolderContinue(result.getCursor());
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        //updating UI
        runOnUiThread(() -> {
            dropboxRecycler.setLayoutManager(layoutManager);
            dropboxRecycler.setAdapter(new DirectoryAdapter(metaList, ScrollingActivity.this));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        }
        if (id == R.id.light_mode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (id == R.id.dark_mode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (id == R.id.developer) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://poppinjay13.github.io/")));
        }
        return super.onOptionsItemSelected(item);
    }
}