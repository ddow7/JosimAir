package com.cbnu.josimair.ui.home;

import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.cbnu.josimair.Model.OutdoorAir;
import com.cbnu.josimair.Model.RestAPIService;
import com.cbnu.josimair.Model.Communication;
import com.cbnu.josimair.ui.MainActivity;
import com.cbnu.josimair.R;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private Communication communication;
    private RestAPIService svc;
    private TextView airInfoTextView;
    private TextView airQualityTextView;
    private TextView outdoorAirQualityTextView;

    private Button btBtn;
    private Button locationBtn;
    Geocoder geoCoder;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        communication = MainActivity.communication;
        svc = MainActivity.svc;


        btBtn = (Button) root.findViewById(R.id.btBtn);
        locationBtn = (Button) root.findViewById(R.id.locationBtn);

        airInfoTextView = (TextView) root.findViewById(R.id.airInfoTextView);
        airQualityTextView = (TextView) root.findViewById(R.id.airQualityTextView);
        outdoorAirQualityTextView = (TextView)root.findViewById(R.id.outdoorAirQualityTextView);

        geoCoder = new Geocoder(root.getContext());

        if(communication.enable()){
            btBtn.setText(R.string.bluetooth_enabled_btn);
        }else{
            btBtn.setText(R.string.bluetooth_connect_btn);
        }

        setCallback();
        updateOutdoorAirInfo();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setCallback(){
        btBtn.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        if(!communication.enable()) {
                            communication.showDialog();
                        }else{ // 여기서 블루투스 리스트에서 디바이스를 선택하여 연결
                            //Communication thread 시작
                            communication.start_Test_Using_RandomData();
                            //startActivityForResult(new Intent(v.getContext(), DeviceListActivity.class),Communication.RESULT_CODE_BTLIST);
                        }
                    }
                }
        );
        communication.setReceivedCallback(new Communication.ReceivedListener() {
            @Override
            public void onReceivedEvent() {
                Log.i("HomeFragment","실내 공기정보 수신");
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            homeViewModel.updateAirInfo(airInfoTextView,airQualityTextView,communication.getReceivedAir());
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        svc.setOnReceivedEvent(new RestAPIService.ReceivedListener() {
            @Override
            public void onReceivedEvent(final OutdoorAir air) {
               updateOutdoorAirInfo();
            }
        });
        svc.setOnErrorOccurredEvent(new RestAPIService.ErrorOccurredListener() {
            @Override
            public void onErrorOccurredEvent() {
                updateOutdoorAirInfo();
            }
        });
    }

    public void updateOutdoorAirInfo(){
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    homeViewModel.updateOutdoorAirInfo(outdoorAirQualityTextView, MainActivity.outdoorAir);
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}