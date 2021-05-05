package com.poppinjay13.dropboxplayground;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.poppinjay13.dropboxplayground.adapters.DirectoryAdapter;
import com.poppinjay13.dropboxplayground.entities.Meta;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity {

    DbxClientV2 client;

    CollapsingToolbarLayout toolBarLayout;
    RecyclerView dropboxRecycler;
    List<Meta> metaList;

    int MY_STORAGE_PERMISSION_CODE = 2;
    int FILE_REQUEST = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle("Playground");

        dropboxRecycler = findViewById(R.id.dropbox_recycler);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(view, "Please grant file read permissions to upload a file", Snackbar.LENGTH_LONG).setAction("Allow", v -> {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);

                    }).show();
                } else {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    photoPickerIntent.setType("*/*");
                    startActivityForResult(photoPickerIntent, FILE_REQUEST);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserDetails();
    }

    private void loadUserDetails() {
        View view = findViewById(R.id.app_bar);
        /*
            I'm using a thread coz I don't want to perform network requests on the main thread
            It is more advisable to use an AsyncTask or a Service / IntentService if you will be making regular requests
            And also to avoid memory leaks but I'm lazy.
            */
        Thread thread = new Thread(() -> {
            try {
                DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
                client = new DbxClientV2(config, Config.ACCESS_TOKEN);
                // Get current user account info
                FullAccount account = client.users().getCurrentAccount();
                //Updating UI on main thread
                runOnUiThread(() -> toolBarLayout.setTitle(account.getName().getDisplayName()));
                getFilesInRootDir();
            } catch (NetworkIOException ex) {
                Snackbar.make(view, "Unable to connect to Dropbox", Snackbar.LENGTH_LONG).setAction("Reload", v -> {
                    loadUserDetails();
                }).show();
            } catch (DbxException ex) {
                Snackbar.make(view, "A dropbox error occurred", Snackbar.LENGTH_LONG).show();
                ex.printStackTrace();
            } catch (Exception ex) {
                Snackbar.make(view, "An error occurred", Snackbar.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        });
        thread.start();
    }

    private void getFilesInRootDir() throws DbxException {
        metaList = new ArrayList<>();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("*/*");
                startActivityForResult(photoPickerIntent, FILE_REQUEST);
            } else {
                Toast.makeText(this, "Storage permissions were denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST && resultCode == RESULT_OK) {
            Thread thread = new Thread(() -> {
                try {
                    final Uri fileUri = data.getData();
                    InputStream in = getContentResolver().openInputStream(fileUri);
                    FileMetadata metadata = client.files().uploadBuilder("/test/test.jpg")
                            .uploadAndFinish(in);
                } catch (DbxException ex) {
                    Toast.makeText(this, "A dropbox error occurred", Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                } catch (IOException ex) {
                    Toast.makeText(this, "An error occurred reading the file", Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
            });
            thread.start();
        } else {
            Toast.makeText(this, "Please select a valid file", Toast.LENGTH_LONG).show();
        }

    }
}