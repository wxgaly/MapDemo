package wxgaly.android.gaodemapdemo

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MainActivity : AppCompatActivity(), LocationSource, AMapLocationListener, View.OnClickListener {

    private val TAG = "wxg"
    private val RESULT_CODE = 1000

    private var aMap: AMap? = null
    private var mLocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var mCameraUpdate: CameraUpdate? = null
    //    private var mMarker: Marker? = null
    private var mGeocoderSearch: GeocodeSearch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListener()
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map
        showGPSWithPermissionCheck()
    }

    private fun initListener() {

        ib_location.setOnClickListener(this@MainActivity)
    }

    private fun location() {
//        val myLocationStyle = MyLocationStyle()
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
////        myLocationStyle.interval(2000) //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
//        myLocationStyle.strokeColor(Color.RED)
//        aMap?.apply {
//            //            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
//            setMyLocationStyle(myLocationStyle) //设置定位蓝点的Style
//            uiSettings.isMyLocationButtonEnabled = true //设置默认定位按钮是否显示，非必需设置。
//            isMyLocationEnabled = true // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
////            myLocationStyle.showMyLocation(true)
//            setOnMyLocationChangeListener {
//                Log.d("wxg", "altitude : ${it.latitude}")
//                Log.d("wxg", "longitude : ${it.longitude}")
//
//            }
//        }
//        aMap?.apply {
//            setLocationSource(this@MainActivity)
//            isMyLocationEnabled = true
////            setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE)
//        }

        aMap?.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChangeFinish(cameraPosition: CameraPosition?) {
                calculatePosition(cameraPosition)
                startJumpAnimation()
            }

            override fun onCameraChange(cameraPosition: CameraPosition?) {

            }

        })

        aMap?.setOnMapLoadedListener {
            Log.d(TAG, "OnMapLoaded")
        }


        mLocationClient = AMapLocationClient(applicationContext)

        mLocationOption = AMapLocationClientOption()

        mLocationClient?.setLocationListener(this@MainActivity)

        mLocationOption?.apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = true
        }

        mLocationClient?.apply {
            setLocationOption(mLocationOption)
            startLocation()
        }

        mGeocoderSearch = GeocodeSearch(applicationContext)
        mGeocoderSearch?.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(result: RegeocodeResult?, code: Int) {
                if (code == RESULT_CODE) {
                    Log.d(TAG, "onRegeocodeSearched")
                    result?.let { it ->
                        if (it.regeocodeAddress.roads.isNotEmpty()) {
                            tv_marker_title.text = it.regeocodeAddress.city + it.regeocodeAddress
                                    .district
                            if (it.regeocodeAddress.pois.isNotEmpty()) {
                                tv_marker_content.text = it.regeocodeAddress.pois[0].title
                                Log.d(TAG, "${it.regeocodeAddress.pois}")
                            }
//                            val latLng = LatLng(latLonPoint.latitude, latLonPoint.longitude)
//                            mMarker?.setMarkerOptions(MarkerOptions()
//                                    .anchor(1f, 1f)
//                                    .position(latLng)
//                                    .draggable(true)
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location3))
//                                    .title(it.regeocodeAddress.city + it.regeocodeAddress.district)
//                                    .snippet(it.regeocodeAddress.formatAddress)
//                                    .setFlat(true))

//                            mMarker?.apply {
//                                title = it.regeocodeAddress.city + it.regeocodeAddress.district
//                                snippet = it.regeocodeAddress.formatAddress
//                            }


//                            mCameraUpdate = CameraUpdateFactory.newCameraPosition(CameraPosition(latLng, 16f, 0f,
//                                    0f))
//                            aMap?.animateCamera(mCameraUpdate)
//                            mMarker?.showInfoWindow()
                        }

                    }

                } else {
                    Log.d(TAG, "onRegeocodeSearched error")
                }
            }

            override fun onGeocodeSearched(result: GeocodeResult?, code: Int) {

            }

        })

    }

    override fun onClick(v: View?) {
        v?.apply {
            when (id) {
                R.id.ib_location -> location()
                else -> Log.d(TAG, "nothing ")
            }
        }
    }

    private fun calculatePosition(cameraPosition: CameraPosition?) {
        cameraPosition?.apply {
            val query = RegeocodeQuery(LatLonPoint(target.latitude, target.longitude), 200f,
                    GeocodeSearch.AMAP)
            mGeocoderSearch?.getFromLocationAsyn(query)
        }
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    fun showGPS() {
        location()
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
    fun onGPSDenied() {
        Toast.makeText(this, "获取GPS权限失败", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun deactivate() {
        mListener = null
        mLocationClient?.stopLocation()
        mLocationClient?.onDestroy()
        mLocationClient = null
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        mListener = listener
    }

    override fun onLocationChanged(location: AMapLocation?) {
//        mListener?.apply {
        location?.apply {
            if (errorCode == 0) {
                Log.d(TAG, "latitude : $latitude")
                Log.d(TAG, "longitude : $longitude")
//                mListener?.onLocationChanged(location)
                showMarker(latitude, longitude)

            } else {
                Log.d(TAG, "location error-- $errorCode ----$errorInfo")

            }
        }
//        }

    }

    private fun showMarker(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        aMap?.apply {

            uiSettings.isCompassEnabled = true
            uiSettings.isScaleControlsEnabled = true
            uiSettings.isGestureScaleByMapCenter = true
            uiSettings.isZoomControlsEnabled = false


            mCameraUpdate = CameraUpdateFactory.newCameraPosition(CameraPosition(latLng, 16f, 0f,
                    0f))
            animateCamera(mCameraUpdate)

//            mMarker = addMarker(MarkerOptions()
//                    .anchor(1f, 1f)
////                    .position(latLng)
//                    .draggable(true)
//                    .title("龙城铭园")
//                    .snippet("二期")
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location3)))
//
//            setInfoWindowAdapter(CustomInfoWindowAdapter())

//            tv_marker_title.text = "龙城铭园"
//            tv_marker_content.text = "二期"

//            val latLngCenter = cameraPosition.target
//            val screenPosition = projection.toScreenLocation(latLngCenter)
//            mMarker?.setPositionByPixels(screenPosition.x, screenPosition.y)
//
//            mMarker?.showInfoWindow()


//            setOnMapClickListener {
//
//
//            }


            setOnMapTouchListener { motionEvent: MotionEvent? ->
                motionEvent?.apply {
                    if (action == MotionEvent.ACTION_UP) {

//                        val latLng = aMap?.projection?.fromScreenLocation(Point(displayMetrics.widthPixels
//                                / 2, displayMetrics.heightPixels / 2))
//                        mMarker?.apply {
//                            val query = RegeocodeQuery(LatLonPoint(position.latitude, position
//                                    .longitude), 400f, GeocodeSearch.AMAP)
//
//                            mGeocoderSearch?.getFromLocationAsyn(query)
//                        }
                    }
                }
            }

//            setOnMarkerDragListener(object : AMap.OnMarkerDragListener {
//
//                // 在marker拖动完成后回调此方法, 这个marker的位置可以通过getPosition()方法返回。
//                // 这个位置可能与拖动的之前的marker位置不一样。
//                // marker 被拖动的marker对象。
//                override fun onMarkerDragEnd(p0: Marker?) {
//                    p0?.let {
//                        Log.d(TAG, "onMarkerDragEnd $p0")
//                    }
//                }
//
//                // 当marker开始被拖动时回调此方法, 这个marker的位置可以通过getPosition()方法返回。
//                // 这个位置可能与拖动的之前的marker位置不一样。
//                // marker 被拖动的marker对象。
//                override fun onMarkerDragStart(p0: Marker?) {
//                    p0?.let {
//                        Log.d(TAG, "onMarkerDragStart $p0")
//                    }
//
//                }
//
//                // 在marker拖动过程中回调此方法, 这个marker的位置可以通过getPosition()方法返回。
//                // 这个位置可能与拖动的之前的marker位置不一样。
//                // marker 被拖动的marker对象。
//                override fun onMarkerDrag(p0: Marker?) {
//                    p0?.let {
//                        Log.d(TAG, "onMarkerDrag $p0")
//                    }
//                }
//
//            })
        }
    }

    private fun startJumpAnimation() {
        aMap?.apply {
            Log.d(TAG, "startJumpAnimation")
            //根据屏幕距离计算需要移动的目标点
//            val latLng = cameraPosition!!.target
//            val point = projection.toScreenLocation(latLng)
//            val y = iv_marker.y - ScreenUtils.getInstance(application).dip2px(applicationContext,
//                    125f)
//            val target = aMap!!.projection.fromScreenLocation(point)
            //使用TranslateAnimation,填写一个需要移动的目标点
//            val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation
//                    .RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 125f, Animation
//                    .RELATIVE_TO_SELF, 0f)
            val animation = TranslateAnimation(0f, 0f, -50f, 50f)

            animation.setInterpolator { input ->
                // 模拟重加速度的interpolator
                if (input <= 0.5) {
                    (0.5f - 2.0 * (0.5 - input) * (0.5 - input)).toFloat()
                } else {
                    (0.5f - Math.sqrt(((input - 0.5f) * (1.5f - input)).toDouble())).toFloat()
                }
            }
            //整个移动所需要的时间
            animation.duration = 400

            iv_marker.startAnimation(animation)

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    rl_marker.visibility = View.VISIBLE
                }

                override fun onAnimationStart(animation: Animation?) {
                    rl_marker.visibility = View.GONE
                }

            })
        }
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy()
        mLocationClient?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState)
    }

//    inner class CustomInfoWindowAdapter : AMap.InfoWindowAdapter {
//
//        override fun getInfoContents(marker: Marker?): View {
//            val infoWindow = LayoutInflater.from(applicationContext).inflate(R.layout
//                    .info_window_adapter_item, null)
//
//            render(marker, infoWindow)
//
//            return infoWindow
//        }
//
//        override fun getInfoWindow(marker: Marker?): View {
//
//            val infoWindow = LayoutInflater.from(applicationContext).inflate(R.layout
//                    .info_window_adapter_item, null)
//
//            render(marker, infoWindow)
//
//            return infoWindow
//        }
//
//        private fun render(marker: Marker?, infoWindow: View?) {
//            infoWindow?.apply {
//                marker?.apply {
//                    findViewById<TextView>(R.id.info_window_tv_title).text = title
//                    findViewById<TextView>(R.id.info_window_tv_content).text = snippet
//                }
//            }
//        }
//
//    }

}
