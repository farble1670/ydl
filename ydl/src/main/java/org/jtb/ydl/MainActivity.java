package org.jtb.ydl;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.github.axet.vget.VGet;
import com.github.axet.vget.info.VGetParser;
import com.github.axet.vget.info.VideoInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity {
  private static final Handler handler = new Handler(Looper.getMainLooper());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

    String u = getIntent().getDataString();
    if (u == null) {
      u = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
    }
    Matcher m = Pattern.compile("https?://[^:^/]+(?::\\d+)?(?:/.*)?").matcher(u);
    if (!m.matches()) {
      if (m.find()) {
        u = m.group(0);
      }
    }

    final URL url;
    try {
      url = new URL(u);
    } catch (MalformedURLException e) {
      toast("Invalid URL: " + u);
      return;
    }


    new AsyncTask<Void,Void,Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        VGetParser user = VGet.parser(url);
        VideoInfo info = user.info(url);
        VGet v = new VGet(info, null);
        v.extract();

        Uri uri = Uri.parse(info.getInfo().getSource().toString());
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(info.getTitle());
        request.setDescription(url.toString());
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, info.getTitle() + info.getTitle() + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(info.getInfo().getContentType()));
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);

        toast("Download queued: " + info.getTitle() + " (" + url + ")");

        return null;
      }
    }.execute();
    finish();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void toast(final String s) {
    Log.i("vdl", s);
    handler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
      }
    });
  }
}
