package com.itis.musicplayer

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private var player: MediaPlayerService? = null
    internal var serviceBound = false
    private var audioList: ArrayList<Audio>? = null
    private val context: Context? = null
    internal lateinit var toolbar: Toolbar

    internal lateinit var collapsingImageView: ImageView

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MediaPlayerService.LocalBinder
            player = binder.service
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        collapsingImageView = findViewById(R.id.collapsingImageView)

        if (checkAndRequestPermissions()) {
            loadAudioList()
        }

    }

    private fun loadAudioList() {
        loadAudio()

        initRecyclerView()
    }

    private fun checkAndRequestPermissions(): Boolean {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            val permissionReadPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            val permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            val listPermissionsNeeded = ArrayList<String>()

            if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
            }

            if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
                return false
            } else {
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {

        val tag = "LOG_PERMISSION"
        Log.d(tag, "Permission callback called-------")
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.READ_PHONE_STATE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions

                    if (perms[Manifest.permission.READ_PHONE_STATE] == PackageManager.PERMISSION_GRANTED && perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(tag, "Phone state and storage permissions granted")
                        // process the normal flow
                        //else any one or both the permissions are not granted
                        loadAudioList()
                    } else {
                        Log.d(tag, "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                      //shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                            showDialogOK("Phone state and storage permissions required for this app",
                                    DialogInterface.OnClickListener { _, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                            DialogInterface.BUTTON_NEGATIVE -> {
                                            }
                                        }// proceed with logic by disabling the related features or quit the app.
                                    })
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show()
                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }

    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show()
    }


    private fun initRecyclerView() {
        if (audioList != null && audioList!!.size > 0) {
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
            val adapter = RecyclerViewAdapter(audioList!!)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.addOnItemTouchListener(CustomTouchListener(this, object : OnItemClickListener {
                override fun onClick(view: View, index: Int) {
                    playAudio(index)
                }
            }))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }


    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("serviceStatus", serviceBound)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("serviceStatus")
    }


    private fun playAudio(audioIndex: Int) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            val storage = StorageUtil(applicationContext)
            storage.storeAudio(audioList!!)
            storage.storeAudioIndex(audioIndex)

            val playerIntent = Intent(this, MediaPlayerService::class.java)
            startService(playerIntent)
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            val audioTitle = audioList!![audioIndex].title
            supportActionBar!!.title = audioTitle
            replaceCollapsingImage(audioIndex)
        } else {
            //Store the new audioIndex to SharedPreferences
            val storage = StorageUtil(applicationContext)
            storage.storeAudioIndex(audioIndex)

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            sendBroadcast(broadcastIntent)
            val audioTitle = audioList!![audioIndex].title
            supportActionBar!!.title = audioTitle
            replaceCollapsingImage(audioIndex)
        }
    }

    private fun replaceCollapsingImage(index: Int) {
        val imageUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), audioList!![index].albumId!!)
        try {
            val b = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            collapsingImageView.setImageBitmap(b)
        } catch (e: IOException) {
            e.printStackTrace()
            collapsingImageView.setImageResource(R.drawable.no_album)
        }

    }

    private fun loadAudio() {
        val contentResolver = contentResolver

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        val cursor = contentResolver.query(uri, null, selection, null, sortOrder)

        if (cursor != null && cursor.count > 0) {
            audioList = ArrayList()
            while (cursor.moveToNext()) {
                val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val albumId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                // Save to audioList
                audioList!!.add(Audio(data, title, album, artist, albumId))
            }
        }

        cursor?.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            //service is active
            player!!.stopSelf()
        }
    }

    companion object {

        const val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
        const val Broadcast_PLAY_NEW_AUDIO = "com.itis.musicplayer.PlayNewAudio"
    }
}
