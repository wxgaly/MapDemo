package wxgaly.android.gaodemapdemo

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.MyLocationStyle
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), LocationSource, AMapLocationListener {


    private var aMap: AMap? = null
    private var mlocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    private var mListener: LocationSource.OnLocationChangedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map
        location()
    }

    private fun location() {
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
//        myLocationStyle.interval(2000) //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.strokeColor(Color.RED)
        aMap?.apply {
            //            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
            setMyLocationStyle(myLocationStyle) //设置定位蓝点的Style
            uiSettings.isMyLocationButtonEnabled = true //设置默认定位按钮是否显示，非必需设置。
            isMyLocationEnabled = true // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
//            myLocationStyle.showMyLocation(true)
            setOnMyLocationChangeListener {
                Log.d("wxg", "altitude : ${it.latitude}")
                Log.d("wxg", "longitude : ${it.longitude}")
            }
        }
//        aMap?.apply {
//            setLocationSource(this@MainActivity)
//            isMyLocationEnabled = true
////            setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE)
//        }

    }

    override fun deactivate() {
        mListener = null
        mlocationClient?.stopLocation()
        mlocationClient?.onDestroy()
        mlocationClient = null
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        mListener = listener

        if (mlocationClient == null) {
            mlocationClient = AMapLocationClient(application)

            mLocationOption = AMapLocationClientOption()

            mlocationClient?.setLocationListener(this@MainActivity)

            mLocationOption?.apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            }

            mlocationClient?.startLocation()
        }
    }

    override fun onLocationChanged(location: AMapLocation?) {
        mListener?.apply {
            location?.apply {
                if (errorCode == 0) {
                    Log.d("wxg", "latitude : $latitude")
                    Log.d("wxg", "longitude : $longitude")
                    mListener?.onLocationChanged(location)

                } else {
                    Log.d("wxg", "location error-- $errorCode ----$errorInfo")

                }
            }
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
        mlocationClient?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState)
    }
}
