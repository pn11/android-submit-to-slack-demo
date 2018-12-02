package io.github.pn11.submit2slack

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Method
import java.io.File
import java.io.IOException
import android.graphics.Bitmap
import android.os.Environment
import com.github.kittinunf.fuel.core.DataPart
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    val token = "TOKEN"
    val channel = "CHANNEL"
    val SELECT_IMAGE_CODE = 999
    private var buttonSelectImage: Button? = null
    private var buttonSubmit: Button? = null
    private var imageview: ImageView? = null
    private val tmpImgPath: String = "tmp_slack.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonSelectImage = findViewById<Button>(R.id.button_select_image)
        buttonSubmit = findViewById<Button>(R.id.button_submit)
        this.imageview = findViewById(R.id.imageView) as ImageView

        buttonSelectImage!!.setOnClickListener {
            val intent: Intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, SELECT_IMAGE_CODE)
            // Utils.showToast(this, "ギャラリー")
        }

        buttonSubmit!!.setOnClickListener{
            if (tmpImgPath != null){
                // imageURI.toString()
                // ファイルパスを取得できないため、画像を一時ファイルとして保存する。
                //Environment.getExternalStorageDirectory().getPath()
                val file =  File(getCacheDir(), tmpImgPath)
                uploadImage(file)
            }
        }
    }


    // オプションメニュー作成
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // オプションメニューのアイテムにイベント設定
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.setting_menu_item -> {
                Utils.showToast(this, "設定は未実装です。", 0, 100)
            }
            R.id.kannei_menu_item -> {
                submitMessage("甘寧一番乗り！！ http://image.itmedia.co.jp/news/articles/1402/17/l_yuo_sangokusi_17.jpg")
                // Utils.showToast(this, "甘寧一番乗り！！", 0, 100)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_IMAGE_CODE){
            if (resultCode != Activity.RESULT_OK) {
                Utils.showToast(this, "画像が選択されませんでした。")
                return
            }

            if (data != null)
            {
                val contentURI = data!!.data
                try
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    imageview!!.setImageBitmap(bitmap)
                    //val fos : FileOutputStream = openFileOutput(tmpImgPath, Context.MODE_PRIVATE)
                    val fos : FileOutputStream = FileOutputStream(File(getCacheDir(), tmpImgPath))
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.close()
                   // Utils.showToast(this, "保存完了")
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // メッセージを投稿する。
    private fun submitMessage(message: String) {
        val url = "https://slack.com/api/chat.postMessage"
        val json = listOf("token" to token, "text" to message, "channel" to channel)

        Fuel.post(url, json).responseString { _, response, result ->
            result.fold({ _ ->
                Log.d("res", response.toString())
            }, { err ->
                Log.e("err", err.toString())
            })
        }
    }

    // 画像をアップロードする
    private fun uploadImage(file: File) {
        val url = "https://slack.com/api/files.upload"
        Fuel.upload(url, Method.POST, listOf("token" to token, "content" to DataPart(File(cacheDir, tmpImgPath)))).dataParts { _, _ ->
            //file;
            listOf(DataPart(File(cacheDir, tmpImgPath)))
        }.responseString { _, response, result ->
            result.fold({ _ ->
                Log.d("res", response.toString())
               Log.d("body", response.data.toString())
            }, { err ->
                Log.e("err", err.toString())
            })
        }
    }
}
