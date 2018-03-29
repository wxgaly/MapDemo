package nova.android.baidumapdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = "wxg"

    private var mLocationClient: LocationClient? = null
    private var mBaiduMap: BaiduMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SDKInitializer.initialize(applicationContext)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        mBaiduMap = bmapView.map

        val option = LocationClientOption()
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        option.setCoorType("bd09ll")
        option.setScanSpan(0)
        option.isOpenGps = true
        option.setIgnoreKillProcess(false)
        option.isLocationNotify = true
        option.setIsNeedLocationDescribe(true)
        option.setIsNeedLocationPoiList(true)

        mLocationClient = LocationClient(application)
        mLocationClient?.apply {

            registerLocationListener(object : BDAbstractLocationListener() {
                override fun onReceiveLocation(location: BDLocation?) {

                    location?.apply {
                        Log.d(TAG, "radius : $radius")
                        Log.d(TAG, "latitude : $latitude")
                        Log.d(TAG, "longitude : $longitude")
                        val locData = MyLocationData.Builder()
                                .accuracy(radius)
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                                .direction(100f).latitude(latitude)
                                .longitude(longitude).build()


                        mBaiduMap?.apply {
                            isMyLocationEnabled = true

                            setMyLocationData(locData)
                            val mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_background)
                            setMyLocationConfiguration(MyLocationConfiguration(MyLocationConfiguration.LocationMode
                                    .NORMAL, false, mCurrentMarker))

                            val ll = LatLng(latitude, longitude)
                            setMapStatus(MapStatusUpdateFactory.newMapStatus(MapStatus.Builder().target(ll).zoom(18f).build()))
//                            isMyLocationEnabled = false
                        }
                    }
                }
            })

            locOption = option
            start()
            Log.d(TAG, "start")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        bmapView.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        bmapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        bmapView.onPause()
    }


}
