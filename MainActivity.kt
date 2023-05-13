package com.example.drawingapp

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var iagecurrentpaint : ImageButton? = null
    private var Drawingview : drawingview? = null
    private var cpd : Dialog? = null

    val request : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            permissions.entries.forEach{
               val permisionname = it.key
               val isGranted = it.value
                if(isGranted){
                    Toast.makeText(this@MainActivity, "Permission granted for storage", Toast.LENGTH_SHORT).show()
                    val pickintent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    opengallerylauncher.launch(pickintent)

                }
                else {
                    if (permisionname == Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(this, "you denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private fun isreadstorageallowed() : Boolean{
        val result = ContextCompat.checkSelfPermission(this,
              Manifest.permission.READ_EXTERNAL_STORAGE
            )
        return result  == PackageManager.PERMISSION_GRANTED
    }
    private fun requeststoragepermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )) {
            showrationaldialoug(
                "Drawing app" ,
                "Drawing app" + "needs to access your storage"

            )
        }else {
            request.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_main)
       Drawingview = findViewById(R.id.drawing_view)


       Drawingview?.setsizeforbrush(20.toFloat())

        val linearlayoutpaintcolor = findViewById<LinearLayout>(R.id.paintcolors)
        iagecurrentpaint = linearlayoutpaintcolor[2] as ImageButton
        iagecurrentpaint!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressedl))

       val brushbtn : ImageButton = findViewById(R.id.brush)
       brushbtn.setOnClickListener{
           showbrushsizechooserdialouge()
       }
        val undu : ImageButton = findViewById(R.id.undu)
        undu.setOnClickListener{
           Drawingview?.onclickundu()
        }
        val stor  : ImageButton = findViewById(R.id.gallery)
        stor.setOnClickListener{
            requeststoragepermission()

        }
        val save  : ImageButton = findViewById(R.id.save)
        save.setOnClickListener{
           if (isreadstorageallowed()){
               customprogressbar()
               CoroutineScope(IO).launch{
                   val fldrawingview:FrameLayout = findViewById(R.id.dvcontainer)
                   savebitmapfile(getbitmapfromview(fldrawingview))
               }
           }

        }
    }

    private fun showbrushsizechooserdialouge(){
        val  brushdialog = Dialog(this)
        brushdialog.setContentView(R.layout.dialouge_brush_size)
        brushdialog.setTitle("Brush Size: ")
        val verysmallbtn : ImageButton = brushdialog.findViewById(R.id.ib_very_small_brush)
        verysmallbtn.setOnClickListener{
            Drawingview?.setsizeforbrush(5.toFloat())
            brushdialog.dismiss()
        }
        val smallbtn : ImageButton = brushdialog.findViewById(R.id.ib_small_brush)
        smallbtn.setOnClickListener{
            Drawingview?.setsizeforbrush(10.toFloat())
            brushdialog.dismiss()
        }
        val mediumbtn : ImageButton = brushdialog.findViewById(R.id.ib_medium_brush)
        mediumbtn.setOnClickListener{
            Drawingview?.setsizeforbrush(20.toFloat())
            brushdialog.dismiss()
        }
        val largebtn : ImageButton = brushdialog.findViewById(R.id.ib_large_brush)
        largebtn.setOnClickListener{
            Drawingview?.setsizeforbrush(30.toFloat())
            brushdialog.dismiss()
        }
        brushdialog.show()
    }
    fun paintclicked (view : View){

        if(view !== iagecurrentpaint ){
            val imagebtnnn = view as ImageButton
            val colortag = imagebtnnn.tag.toString()
            Drawingview?.setcolor(colortag)

            imagebtnnn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressedl))
            iagecurrentpaint?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_normal))
            iagecurrentpaint = view
        }



//        Toast.makeText(this,"clicked paint" , Toast.LENGTH_LONG).show()
    }

    private fun showrationaldialoug(title: String, message: String, ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message).setPositiveButton("Cancel") { dialog, _ ->
            builder.setCancelable(false)
            dialog.dismiss()
        }
        builder.create().show()
    }


    val opengallerylauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode == RESULT_OK && result.data!=null){
             val imgback  : ImageView = findViewById(R.id.iv_background)
             imgback.setImageURI(result.data?.data)
        }
    }

    private fun getbitmapfromview(view : View) : Bitmap {
        val returnednbutmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnednbutmap)
        val bgdrwable = view.background
        if (bgdrwable!=null){
            bgdrwable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnednbutmap
    }
    private suspend fun savebitmapfile(mbitmap:Bitmap?) : String{
        var result = " "
       withContext(Dispatchers.IO){
           if(mbitmap != null){
               try {
                   val bytes = ByteArrayOutputStream()
                   mbitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                   val f = File(externalCacheDir?.absoluteFile.toString() +
                   File.separator + "DrawingApp_" + System.currentTimeMillis() /1000 + ".png"
                   )
                   val fo = FileOutputStream(f)
                   fo.write(bytes.toByteArray())
                   fo.close()

                   result = f.absolutePath

                   runOnUiThread{
                       cancelprogressbar()
                       if(result.isNotEmpty()){
                           Toast.makeText(this@MainActivity, "File saved successfully : $result", Toast.LENGTH_SHORT).show()
                           shareimage(result)
                       }
                       else{
                           Toast.makeText(this@MainActivity, "Something went wrong : ", Toast.LENGTH_SHORT).show()
                       }
                   }
               }
               catch (e:Exception){
                   result = ""
                   e.printStackTrace()
               }
           }
       }
        return result
    }
    private fun customprogressbar(){
        cpd = Dialog(this)
        cpd?.setContentView(R.layout.cpd)
        cpd?.show()
    }
    private fun cancelprogressbar(){
        if(cpd != null){
            cpd?.dismiss()
            cpd = null
        }
    }
    private fun shareimage(result :  String){
        MediaScannerConnection.scanFile(this, arrayOf(result),null){
            path,uri ->
            val shareintent = Intent()
            shareintent.action = Intent.ACTION_SEND
            shareintent.putExtra(Intent.EXTRA_STREAM,uri)
            shareintent.type = "image/png"
            startActivity(Intent.createChooser(shareintent,"Share"))
        }
    }

}