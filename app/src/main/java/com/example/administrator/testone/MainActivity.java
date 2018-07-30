package com.example.administrator.testone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.administrator.testone.overlayutil.PoiOverlay;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawerLayout mDrawerLayout;                                                            //各个控件定义
    private EditText cityNameText;
    private EditText searchText;

    public static boolean isLogin = false;                                                        //表示是否在登录状态

    public LocationClient mLocationClient;                                                         //百度地图相关的定位客户端实例

    private MapView mapView;                                                                       //百度地图视图显示

    private BaiduMap baiduMap;                                                                     //地图的总控制器

    List<LatLng> points = new ArrayList();                                                          //地图上添加折线所需要的点集合

    private boolean isFirstLocate = true;                                                        //判断是否是第一次定位

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());                             //百度地图定位客户端实例
        mLocationClient.registerLocationListener(new MyLocationListener());

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        //设置定位的属性，包括定位模式、是否开启方向、设置自定义定位图标、精度圈填充颜色，精度圈边框颜色
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, BitmapDescriptorFactory.fromResource(R.drawable.icon_position));
        baiduMap.setMyLocationConfiguration(config);
        /*
        * 地图普通页面点击以及POI点击监听事件
        * */
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            //地图单击事件回调方法
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(MainActivity.this, "点击到地图上了！纬度" + latLng.latitude + "经度" + latLng.longitude, Toast.LENGTH_SHORT).show();
            }

            //Poi 单击事件回调方法，比如点击到地图上面的商店，公交车站，地铁站等等公共场所
            @Override
            public boolean onMapPoiClick(final MapPoi mapPoi) {
                Snackbar.make(mapView,"点到了一个兴趣点,是否设为起点?",Snackbar.LENGTH_SHORT)
                        .setAction("是", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //创建InfoWindow展示的view
                                Button button = new Button(getApplicationContext());
                                button.setBackgroundResource(R.drawable.popup);
                                button.setText("起点");
                                button.setTextColor(0xAAFF0000);
                                //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                                InfoWindow mInfoWindow = new InfoWindow(button, mapPoi.getPosition(), -47);
                                //显示InfoWindow
                                baiduMap.showInfoWindow(mInfoWindow);
                            }
                        }).show();
                return true;
            }
        });

        /*
        * 检索结果覆盖物监听事件
        * 设置弹窗说明该POI
        * */
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getBaseContext(), "点击了标记物", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        final PoiSearch search = PoiSearch.newInstance();                                                  //poi检索
        /*
        * POI检索的监听对象
        * */
        OnGetPoiSearchResultListener resultListener = new OnGetPoiSearchResultListener() {
            //获得POI的检索结果，一般检索数据都是在这里获取
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                //如果搜索到的结果不为空，并且没有错误
                if (poiResult != null && poiResult.error == PoiResult.ERRORNO.NO_ERROR) {
                    MyOverLay overlay = new MyOverLay(baiduMap, search);                            //这传入search对象，因为一般搜索到后，点击时方便发出详细搜索
                    //设置数据
                    overlay.setData(poiResult);
                    //添加到地图
                    overlay.addToMap();
                    //将显示视图拉倒正好可以看到所有POI兴趣点的缩放等级
                    overlay.zoomToSpan();//计算工具
                    //设置标记物的点击监听事件
                    baiduMap.setOnMarkerClickListener(overlay);
                } else {
                    Toast.makeText(getApplication(), "搜索不到你需要的信息！", Toast.LENGTH_SHORT).show();
                }
            }
            //获得POI的详细检索结果，如果发起的是详细检索，这个方法会得到回调(需要uid)
            //详细检索一般用于单个地点的搜索，比如搜索一大堆信息后，选择其中一个地点再使用详细检索
            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getApplication(), "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                } else {// 正常返回结果的时候，此处可以获得很多相关信息
                    Toast.makeText(getApplication(), poiDetailResult.getName() + ": "
                                    + poiDetailResult.getAddress(),
                            Toast.LENGTH_LONG).show();
                }
            }

            //获得POI室内检索结果
            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            }
        };

        //设置POI检索监听者；
        search.setOnGetPoiSearchResultListener(resultListener);


        /*
        * 写的有点乱,大概就是上面是百度地图的搜索交互等
        * 下面是控件的初始化以及点击事件
        * */
        //toolbar相关设置
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("  ");
        setSupportActionBar(toolbar);                                                       //将actionbar改为toolbar
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);              //侧滑菜单和界面总Layout

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);      //侧滑菜单布局

        cityNameText = (EditText)findViewById(R.id.city_name_text);
        searchText = (EditText)findViewById(R.id.search_text);
        cityNameText.setFocusable(false);
        cityNameText.setFocusableInTouchMode(false);
        /*
        * 搜索按钮加载及其点击事件
        * */
        Button searchButton = (Button)findViewById(R.id.startSearch);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyString = searchText.getText().toString().trim();
                String cityName = cityNameText.getText().toString().trim();
                if (TextUtils.isEmpty(keyString)) {
                    Toast.makeText(MainActivity.this, "输入内容不能为空", Toast.LENGTH_SHORT).show();
                }
                //发起检索
                PoiCitySearchOption poiCity = new PoiCitySearchOption();
                poiCity.keyword(keyString).city(cityName);                                            //这里还能设置显示的个数，默认显示10个
                search.searchInCity(poiCity);                                                        //执行搜索，搜索结束后，在搜索监听对象里面的方法会被回调
            }
        });

        FloatingActionButton make_sure = (FloatingActionButton) findViewById(R.id.sure);
        make_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                                        //确认下单按钮监听事件
                Toast.makeText(MainActivity.this, "点击成功", Toast.LENGTH_SHORT).show();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {                                                                    //设置toolbar的HomeAsUp按钮可用以及图片
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.personal);
        }

        /*
        * 申请百度地图使用相关权限
        * */
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        /*
        * 加载侧滑菜单的头部布局并设置监听事件
        * */
        View headLayout = navigationView.inflateHeaderView(R.layout.nav_header);
        View headImage = headLayout.findViewById(R.id.icon_image);
        headImage.setOnClickListener(this);
        /*
        * 设置侧滑菜单nav_menu部分的点击事件
        * */
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_order:
                        Intent intent = new Intent(MainActivity.this, OrderManageActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_setting:
                        Intent intent1 = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(intent1);
                        break;
                }

                return true;
            }
        });
    }

    /**
     * 重写和设置最基础的覆盖类
     * 用来处理搜索到的对象的信息数据
     * 这里的PoiOverlay是工具类里面的类，需要自己去复制过来使用
     */
    public class MyOverLay extends PoiOverlay {
        /**
         * 构造函数
         */
        PoiSearch poiSearch;

        public MyOverLay(BaiduMap baiduMap, PoiSearch poiSearch) {
            super(baiduMap);
            this.poiSearch = poiSearch;
        }

        /**
         * 覆盖物被点击时
         */
        @Override
        public boolean onPoiClick(int i) {
            //获取点击的标记物的数据
            PoiInfo poiInfo = getPoiResult().getAllPoi().get(i);
            Toast.makeText(MainActivity.this, poiInfo.name + "   " + poiInfo.address + "   " + poiInfo.phoneNum, Toast.LENGTH_SHORT).show();
            Log.e("TAG", poiInfo.name + "   " + poiInfo.address + "   " + poiInfo.phoneNum);
            //  发起一个详细检索,要使用uid
            poiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(poiInfo.uid));
            return true;
        }
    }

    /*
    * 将位置移动到目前所在位置(默认北京)
    * */
    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            LatLng latLng = new LatLng(43.838889, 125.170671);
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(18f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    /*
    * 下面的两个方法对mapView进行管理,及时释放资源
    * */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /*
            * 启动定位客户端
            * */
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    /*
    * 设置百度地图刷新事件间隔以及其他界面配置
    * */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();

        //配置定位SDK参数
        option.setCoorType("bd09ll");   //设置返回经纬度坐标类型，默认gcj02
        option.setScanSpan(5000);        //设置发起定位请求的间隔，int类型，单位ms
        option.setOpenGps(true);          //设置是否使用gps，默认false,使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setIsNeedAddress(true);     //是否需要地址信息，默认为不需要，即参数为false
        option.setLocationNotify(true);
        //绘制地图折线代码实现
        LatLng p1 = new LatLng(43.838889, 125.170671);
        LatLng p2 = new LatLng(43.839465, 125.173954);
        LatLng p3 = new LatLng(43.841854, 125.17523);
        LatLng p4 = new LatLng(43.843577, 125.17134);
        LatLng p5 = new LatLng(43.841549, 125.165879);
        points.add(p1);
        points.add(p2);
        points.add(p3);
        points.add(p4);
        points.add(p5);
        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                .color(0xAAFF0000).points(points);
        Polyline mPolyline = (Polyline) baiduMap.addOverlay(ooPolyline);


        mLocationClient.setLocOption(option);
    }

    /*
    * 活动销毁时停止定位,防止程序一直在后台进行定位,消耗电量
    * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    //申请相关权限的函数
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    /*
        * 加载menu目录下的toolbar的菜单布局
        * */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    /*
    * toolbar上各个按钮的监听事件
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    /*
    * 按钮监听器点击事件
    * */
    @Override
    public void onClick(View view) {
        if (MainActivity.isLogin) {
            Intent intent1 = new Intent(MainActivity.this, PersonalInforActivity.class);
            startActivity(intent1);
        } else {
            Intent intent2 = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent2);
        }
    }

    /*
    * 百度地图的定位功能实现
    * */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            //加上搜索城市名
            cityNameText.setText(bdLocation.getCity());
//
//            //创建InfoWindow展示的view
//            Button button = new Button(getApplicationContext());
//            button.setBackgroundResource(R.drawable.popup);
//        double distance = DistanceUtil.getDistance(p1, p2);
//        String sDistance = String.valueOf(distance);
//        button.setText(sDistance);
//            button.setText("正门");
//            button.setTextColor(0xAAFF0000);
            //定义用于显示该InfoWindow的坐标点
            LatLng pt1 = new LatLng(43.838889, 125.170671);
            LatLng pt2 = new LatLng(43.839465, 125.173954);
            LatLng pt3 = new LatLng(43.841854, 125.17523);
            LatLng pt4 = new LatLng(43.843577, 125.17134);
            LatLng pt5 = new LatLng(43.841549, 125.165879);
            //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
//            InfoWindow mInfoWindow = new InfoWindow(button, pt1, -47);
            //显示InfoWindow
//            baiduMap.showInfoWindow(mInfoWindow);
            CircleOptions circle1 = new CircleOptions().center(pt1).fillColor(0x80ff0000).radius(15);
            CircleOptions circle2 = new CircleOptions().center(pt2).fillColor(0x80ff0000).radius(15);
            CircleOptions circle3 = new CircleOptions().center(pt3).fillColor(0x80ff0000).radius(15);
            CircleOptions circle4 = new CircleOptions().center(pt4).fillColor(0x80ff0000).radius(15);
            CircleOptions circle5 = new CircleOptions().center(pt5).fillColor(0x80ff0000).radius(15);
            baiduMap.addOverlay(circle1);
            baiduMap.addOverlay(circle2);
            baiduMap.addOverlay(circle3);
            baiduMap.addOverlay(circle4);
            baiduMap.addOverlay(circle5);
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(bdLocation);
            }

        }
    }

}
