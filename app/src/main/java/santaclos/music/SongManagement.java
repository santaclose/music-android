package santaclos.music;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;

import java.io.File;

import santaclos.music.Types.Song;

public class SongManagement {

    public static void DownloadSong(final MainActivity activity, final SharedPreferences.Editor prefsEditor, final Song song, final boolean loadSongs)
    {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultAction() {

                    @Override
                    public void onGranted() {

                        try
                        {
                            String url = song.getMp3Url();
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                            request.setDescription("...");
                            request.setTitle(song.getTitle());
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir("/music/", song.getFilePath());
                            DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.enqueue(request);
                            prefsEditor.putBoolean(song.getFilePath(), true);
                            prefsEditor.commit();
                            if(loadSongs)
                                activity.LoadSongs();
                        }
                        catch (Exception e)
                        {
                            Log.d("Could not download song",e.toString());
                        }
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(activity,
                                "Storage Permission not granted",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void DeleteSong(final MainActivity activity, final SharedPreferences.Editor prefsEditor, final Song song, final boolean loadSongs)
    {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultAction() {

                    @Override
                    public void onGranted() {
                        File todelete = new File(Environment.getExternalStorageDirectory()+"/music/"+song.getFilePath());
                        if(loadSongs)
                            Toast.makeText(activity, todelete.delete()?"File deleted":"File not deleted", Toast.LENGTH_SHORT).show();
                        prefsEditor.putBoolean(song.getFilePath(), false);
                        prefsEditor.commit();
                        if(loadSongs)
                            activity.LoadSongs();
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(activity,
                                "Storage Permission not granted",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
