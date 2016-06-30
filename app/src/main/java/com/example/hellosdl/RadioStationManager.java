package com.example.hellosdl;

import com.smartdevicelink.proxy.rpc.SoftButton;
import com.smartdevicelink.proxy.rpc.enums.SoftButtonType;

import java.util.ArrayList;

public class RadioStationManager {

    private ArrayList<RadioStation> stations = new ArrayList<>();
    private int currentStation = -1;

    public RadioStationManager() {
        // Predefined radio stations
        stations.add(new RadioStation("NPR", "http://vprbbc.streamguys.net:80/vprbbc24.mp3"));
        stations.add(new RadioStation("Dance", "http://dancewave.online:80/dance.mp3"));
        stations.add(new RadioStation("Lounge", "http://185.33.22.15:11065"));
        stations.add(new RadioStation("Country", "http://95.141.24.242:80/"));
        stations.add(new RadioStation("Rock", "http://listen.radionomy.com:80/NewYorkClassicRock"));
        stations.add(new RadioStation("Talk", "http://50.7.130.101:80/"));
    }

    public void next() {
        if(currentStation < 0 || currentStation + 1 == stations.size()) {
            currentStation = 0;
        } else {
            currentStation++;
        }
    }

    public void previous() {
        if(currentStation - 1 <  0) {
            currentStation = stations.size() - 1;
        } else {
            currentStation--;
        }
    }

    public void set(int stationId) {
        currentStation = stationId;
    }

    public String getCurrentStationName() {
        RadioStation currentStation = getCurrentStation();
        String stationName = "";
        if(currentStation != null) {
            stationName = currentStation.getDisplayName();
        }

        return stationName;
    }

    public RadioStation getCurrentStation() {
        RadioStation current = null;

        if(currentStation >= 0 && currentStation < stations.size()) {
            current = stations.get(currentStation);
        }

        return current;
    }

    public ArrayList<SoftButton> toSoftButtons() {
        ArrayList<SoftButton> softButtons = new ArrayList<>();

        int size = stations.size();
        for(int i = 0; i < size; i++) {
            SoftButton softButton = new SoftButton();

            // The button will only display text
            softButton.setType(SoftButtonType.SBT_TEXT);

            // ID that will be used to reference this button when it is clicked
            softButton.setSoftButtonID(i);

            // Set display text of the soft button
            softButton.setText(stations.get(i).getDisplayName());

            // If this station is the current station then it should be highlighted in the HMI
            softButton.setIsHighlighted(i == currentStation);

            softButtons.add(softButton);
        }

        return softButtons;
    }
}
