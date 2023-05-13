package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class drawingview(context :  Context , attrs : AttributeSet) : View(context , attrs) {
    private var mdrawpath : Custompath? = null
    private var mcanvasbitmap : Bitmap? = null
    private var mdrawpaint : Paint? = null
    private var mcanvaspaint : Paint? = null
    private var  mbrushsize : Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas :  Canvas? = null
    private var mpaths = ArrayList<Custompath>()
    private var undupaths = ArrayList<Custompath>()
   init {
      setupdrwaing()

   }
    fun onclickundu(){
       if(mpaths.size>0){
           undupaths.add(mpaths.removeAt(mpaths.size-1))
           invalidate()
       }

    }
    private fun setupdrwaing(){
        mdrawpaint = Paint()
        mdrawpath = Custompath(color,mbrushsize)
        mdrawpaint!!.color = color
        mdrawpaint!!.style = Paint.Style.STROKE
        mdrawpaint!!.strokeJoin = Paint.Join.ROUND
        mdrawpaint!!.strokeCap = Paint.Cap.ROUND
        mcanvaspaint = Paint(Paint.DITHER_FLAG)
//        mbrushsize = 20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mcanvasbitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mcanvasbitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mcanvasbitmap!!,0f,0f,mcanvaspaint)

        for (path in mpaths){
            mdrawpaint!!.strokeWidth = path.brushthickness
            mdrawpaint!!.color = path.color
            canvas.drawPath(path, mdrawpaint!!)
        }


        if (!mdrawpath!!.isEmpty) {
            mdrawpaint!!.strokeWidth = mdrawpath!!.brushthickness
            mdrawpaint!!.color = mdrawpath!!.color
            canvas.drawPath(mdrawpath!!, mdrawpaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchx = event?.x
        val touchy = event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                mdrawpath!!.color = color
                mdrawpath!!.brushthickness = mbrushsize
                mdrawpath!!.reset()
                if (touchx != null) {
                    if (touchy != null) {
                        mdrawpath!!.moveTo(touchx,touchy)
                    }
                }
            }
            MotionEvent.ACTION_MOVE ->{
                if (touchx != null) {
                    if (touchy != null) {
                        mdrawpath!!.lineTo(touchx,touchy)
                    }
                }
            }
            MotionEvent.ACTION_UP ->{
                mpaths.add(mdrawpath!!)
                mdrawpath = Custompath(color , mbrushsize)
            }
            else -> return false
        }
        invalidate()

        return true
    }
    fun setsizeforbrush(newsize : Float){
        mbrushsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newsize,resources.displayMetrics)
        mdrawpaint!!.strokeWidth = mbrushsize
    }
    fun setcolor (newcolor : String){
        color  = Color.parseColor(newcolor)
        mdrawpaint!!.color = color
    }
    inner class Custompath(var  color : Int,var brushthickness : Float) : Path(){

    }
}