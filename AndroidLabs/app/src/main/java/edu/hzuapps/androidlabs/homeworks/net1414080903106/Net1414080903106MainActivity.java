package edu.hzuapps.androidlabs.homeworks.net1414080903106;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.hzuapps.androidlabs.R;

/**
 * Created by Administrator on 2017/4/24.
 */

public class Net1414080903106MainActivity extends Activity implements View.OnClickListener {
    private TextView SelectCityBtn;
    private TextView CityName,Temperature,Climate;
    private Button UpdateBtn;
    private Button ParseXml;
    private String updateCityCode;
    Net1414080903106Weather weather=null;

    private Handler mHandler=new Handler(){
        public void handleMessage(android.os.Message message){
            switch(message.what){
                case 1:
                    updateWeather((Net1414080903106Weather) message.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net1414080903106_main);

        //绑定组件
        SelectCityBtn=(TextView) findViewById(R.id.title_city_locate);
        SelectCityBtn.setOnClickListener(this);
        CityName=(TextView) findViewById(R.id.wether_cityName);
        Temperature=(TextView) findViewById(R.id.weather_temperature);
        Climate=(TextView) findViewById(R.id.weather_climate);
        CityName.setText("N/A");
        Temperature.setText("N/A");
        Climate.setText("N/A");
        UpdateBtn=(Button) findViewById(R.id.weather_btn_update);
        UpdateBtn.setOnClickListener(this);
        ParseXml=(Button) findViewById(R.id.parse_xml);
        ParseXml.setOnClickListener(this);

        //检查网络连接状态
        if(Net1414080903106CheckNet.getNetState(this)==Net1414080903106CheckNet.NET_NONE){
            Log.d("MainActivity","网络不通");
            Toast.makeText(Net1414080903106MainActivity.this,"网络不通",Toast.LENGTH_LONG).show();
        }else{
            Log.d("MainActivity","网络OK");
            Toast.makeText(Net1414080903106MainActivity.this,"网络OK",Toast.LENGTH_LONG).show();
        }

        updateCityCode=getIntent().getStringExtra("citycode");
        if(updateCityCode!="-1"){
            getWeatherDatafromNet(updateCityCode);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.title_city_locate) {
            Intent intent=new Intent(this, Net1414080903106SelectCity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.weather_btn_update){
            getWeatherDatafromNet("101010100");
        }else if(v.getId()==R.id.parse_xml) {
            Intent intent=new Intent(this, Net1414080903106ParseXmlActivity.class);
            startActivity(intent);
        }
    }

    //获取网络上的数据
    private void getWeatherDatafromNet(String citycode){
        final String address="http://wthrcdn.etouch.cn/WeatherApi?citykey="+citycode;
        Log.d("Address",address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection=null;
                try{
                    URL url=new URL(address);
                    urlConnection=(HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(8000);
                    urlConnection.setReadTimeout(8000);
                    InputStream in=urlConnection.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    StringBuffer sb=new StringBuffer();
                    String str;
                    while((str=reader.readLine())!=null){
                        sb.append(str);
                        Log.d("data from url",str);
                    }
                    String response=sb.toString();
                    Log.d("response",response);

                    //调用parseXML函数
                    weather=parseXML(response);
                    if(weather!=null){
                        Log.d("weatherData",weather.toString());
                        Message message=new Message();
                        message.what=1;
                        message.obj=weather;
                        mHandler.sendMessage(message);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //通过xmlPullParser解析xml数据
    private Net1414080903106Weather parseXML(String xmlData){
        Net1414080903106Weather weather=null;
        int typeCount=0;
        try{
            XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));

            int eventType=xmlPullParser.getEventType();
            Log.d("MainActivity","start parse xml");

            while(eventType!=xmlPullParser.END_DOCUMENT){
                switch(eventType){
                    //文档开始位置
                    case XmlPullParser.START_DOCUMENT:
                        Log.d("MainActivity","start doc");
                        break;
                    //标签元素开始位置
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            weather=new Net1414080903106Weather();
                        }
                        if(weather!=null){
                            if(xmlPullParser.getName().equals("city")){
                                eventType=xmlPullParser.next();
                                Log.d("city",xmlPullParser.getText());
                                weather.setCity(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("wendu")){
                                eventType=xmlPullParser.next();
                                Log.d("wendu",xmlPullParser.getText());
                                weather.setWendu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("type")&&typeCount==0){
                                eventType=xmlPullParser.next();
                                Log.d("type",xmlPullParser.getText());
                                weather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType=xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d("parseXML",weather.toString());
        return weather;
    }

    //更新组件
    void updateWeather(Net1414080903106Weather weather){
        CityName.setText(weather.getCity());
        Temperature.setText(weather.getWendu()+"℃");
        Climate.setText(weather.getType());
        Log.d("MainActivity","更新成功");
        Toast.makeText(Net1414080903106MainActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
    }
}
