package com.example.hellosdl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
import com.smartdevicelink.proxy.interfaces.IProxyListenerALM;
import com.smartdevicelink.proxy.rpc.AddCommandResponse;
import com.smartdevicelink.proxy.rpc.AddSubMenuResponse;
import com.smartdevicelink.proxy.rpc.AlertManeuverResponse;
import com.smartdevicelink.proxy.rpc.AlertResponse;
import com.smartdevicelink.proxy.rpc.ChangeRegistrationResponse;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteCommandResponse;
import com.smartdevicelink.proxy.rpc.DeleteFileResponse;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteSubMenuResponse;
import com.smartdevicelink.proxy.rpc.DiagnosticMessageResponse;
import com.smartdevicelink.proxy.rpc.DialNumberResponse;
import com.smartdevicelink.proxy.rpc.EndAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.GenericResponse;
import com.smartdevicelink.proxy.rpc.GetDTCsResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.ListFilesResponse;
import com.smartdevicelink.proxy.rpc.OnAudioPassThru;
import com.smartdevicelink.proxy.rpc.OnButtonEvent;
import com.smartdevicelink.proxy.rpc.OnButtonPress;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnDriverDistraction;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnHashChange;
import com.smartdevicelink.proxy.rpc.OnKeyboardInput;
import com.smartdevicelink.proxy.rpc.OnLanguageChange;
import com.smartdevicelink.proxy.rpc.OnLockScreenStatus;
import com.smartdevicelink.proxy.rpc.OnPermissionsChange;
import com.smartdevicelink.proxy.rpc.OnStreamRPC;
import com.smartdevicelink.proxy.rpc.OnSystemRequest;
import com.smartdevicelink.proxy.rpc.OnTBTClientState;
import com.smartdevicelink.proxy.rpc.OnTouchEvent;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.PerformAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.PerformInteractionResponse;
import com.smartdevicelink.proxy.rpc.PutFileResponse;
import com.smartdevicelink.proxy.rpc.ReadDIDResponse;
import com.smartdevicelink.proxy.rpc.ResetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.ScrollableMessageResponse;
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayout;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
import com.smartdevicelink.proxy.rpc.Show;
import com.smartdevicelink.proxy.rpc.ShowConstantTbtResponse;
import com.smartdevicelink.proxy.rpc.ShowResponse;
import com.smartdevicelink.proxy.rpc.SliderResponse;
import com.smartdevicelink.proxy.rpc.SpeakResponse;
import com.smartdevicelink.proxy.rpc.StreamRPCResponse;
import com.smartdevicelink.proxy.rpc.SubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SystemRequestResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.UpdateTurnListResponse;
import com.smartdevicelink.proxy.rpc.enums.ButtonName;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;

public class SdlService extends Service implements IProxyListenerALM {
    private final static String TAG = "SdlService";

    public int autoIncCorrId = 0;

    //The proxy handles communication between the application and SDL
    private SdlProxyALM proxy = null;

    private RadioStationManager radioStationManager = new RadioStationManager();

    public SdlService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (proxy == null) {
            try {
                //Create a new proxy using Bluetooth transport
                //The listener, app name,
                //whether or not it is a media app and the applicationId are supplied.
                proxy = new SdlProxyALM(this, "Hello SDL Radio", true, "555555666");
            } catch (SdlException e) {
                //There was an error creating the proxy
                if (proxy == null) {
                    //Stop the SdlService
                    stopSelf();
                }
            }
        }

        // Start the music service that controls audio playback
        startService(new Intent(this, MusicService.class));

        //use START_STICKY because we want the SDLService to be explicitly started and stopped as needed.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Dispose of the proxy
        if (proxy != null) {
            try {
                proxy.dispose();
            } catch (SdlException e) {
                e.printStackTrace();
            } finally {
                proxy = null;
            }
        }

        closeLockScreen();

        stopService(new Intent(this, MusicService.class));

        super.onDestroy();
    }

    private void closeLockScreen() {
        sendBroadcast(new Intent(LockScreenActivity.ACTION_CLOSE_LOCK_SCREEN));
    }

    int TEST_COMMAND_ID = 100;
    String TEST_COMMAND_NAME = "Show Alert";

    private void createCommandMenu() {
        // Creates an command in the HMIs command menu
//        MenuParams params = new MenuParams();
//        params.setMenuName(TEST_COMMAND_NAME);
//        params.setPosition(1);
//        AddCommand command = new AddCommand();
//        command.setCmdID(COMMAND_SHOW_ALERT);
//        command.setMenuParams(params);
//        sendRpcRequest(command);
    }

    private void sendRpcRequest(RPCRequest request) {
        request.setCorrelationID(autoIncCorrId++);
        try {
            proxy.sendRPCRequest(request);
        } catch (SdlException ex) {
            Log.e(TAG, "Failed to send rpc request", ex);
        }
    }

    boolean setup = false;

    @Override
    public void onOnHMIStatus(OnHMIStatus notification) {
        Log.d(TAG, "onOnHMIStatus: " + notification.getHmiLevel());
        switch(notification.getHmiLevel()) {
            case HMI_FULL:
            case HMI_BACKGROUND:
            case HMI_LIMITED:
                if(!setup) {
                    //createCommandMenu();
                    setup = true;

//                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.npr_logo);
//                    ByteArrayOutputStream stream= new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                    byte[] bitMapData = stream.toByteArray();


                    // TODO Upload icons
//                    PutFile putFile = new PutFile();
//                    putFile.setSdlFileName("npr.png");
//                    putFile.setBulkData(bitMapData);
//                    putFile.setCorrelationID(autoIncCorrId++);
//                    putFile.setPersistentFile(false);
//                    putFile.setSystemFile(false);
//                    putFile.setFileType(FileType.GRAPHIC_PNG);
//                    sendRpcRequest(putFile);

                    // Set-up display layout. These layouts are predefined and define positions of buttons and images.
                    SetDisplayLayout displayLayout = new SetDisplayLayout();
                    displayLayout.setDisplayLayout("MEDIA");
                    displayLayout.setCorrelationID(autoIncCorrId++);

                    try {
                        // Subscribe to OK (play/pause), seek left and right predefined buttons
                        proxy.subscribeButton(ButtonName.OK, autoIncCorrId++);
                        proxy.subscribeButton(ButtonName.SEEKLEFT, autoIncCorrId++);
                        proxy.subscribeButton(ButtonName.SEEKRIGHT, autoIncCorrId++);
                    } catch (SdlException e) {
                        e.printStackTrace();
                    }

                    render();
                }
                break;
            case HMI_NONE:
                setup = false;
                break;
            default:
                return;
        }
    }

    @Override
    public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {
        Log.d(TAG, "onProxyClosed");
        stopSelf();
    }

    @Override
    public void onServiceEnded(OnServiceEnded serviceEnded) {
        Log.d(TAG, "onServiceEnded");
    }

    @Override
    public void onServiceNACKed(OnServiceNACKed serviceNACKed) {
        Log.d(TAG, "onServiceNACKed");
    }

    @Override
    public void onOnStreamRPC(OnStreamRPC notification) {
        Log.d(TAG, "onOnStreamRPC");
    }

    @Override
    public void onStreamRPCResponse(StreamRPCResponse response) {
        Log.d(TAG, "onStreamRPCResponse");
    }

    @Override
    public void onError(String info, Exception e) {
        Log.e(TAG, "onError: " + info, e);
    }

    @Override
    public void onGenericResponse(GenericResponse response) {
        Log.d(TAG, "onGenericResponse");
    }

    @Override
    public void onOnCommand(OnCommand notification) {
        Log.d(TAG, "onOnCommand");

        switch (notification.getCmdID()) {
//            case COMMAND_SHOW_ALERT:
//                // Create softbuttons for the alert dialog
//                Vector<SoftButton> alertButtons = new Vector<>();
//                SoftButton softButton1 = new SoftButton();
//                softButton1.setType(SoftButtonType.SBT_TEXT);
//                softButton1.setText("YES");
//                softButton1.setSoftButtonID(1);
//                softButton1.setIsHighlighted(false);
//
//                SoftButton softButton2 = new SoftButton();
//                softButton2.setText("NO");
//                softButton2.setType(SoftButtonType.SBT_TEXT);
//                softButton2.setSoftButtonID(2);
//                softButton2.setIsHighlighted(false);
//
//                alertButtons.add(softButton1);
//                alertButtons.add(softButton2);
//
//                try {
//                    //proxy.alert("Hello!", false, autoIncCorrId++);
//                    proxy.alert("ALERT", "This is an alert?", null, null, false, 5000, alertButtons, autoIncCorrId++);
//                } catch(Exception ex) {
//                    Log.e(TAG, "alert failed", ex);
//                }
//                break;
        }

    }

    @Override
    public void onAddCommandResponse(AddCommandResponse response) {
        Log.i(TAG, "AddCommand response from SDL: " + response.getResultCode().name());
    }

    @Override
    public void onAddSubMenuResponse(AddSubMenuResponse response) {
        Log.d(TAG, "onAddSubMenuResponse");
    }

    @Override
    public void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse response) {
        Log.d(TAG, "onCreateInteractionChoiceSetResponse");
    }

    @Override
    public void onAlertResponse(AlertResponse response) {
        Log.d(TAG, "onAlertResponse " + response.getResultCode().name());
//        Log.e(TAG, response.getInfo());
    }

    @Override
    public void onDeleteCommandResponse(DeleteCommandResponse response) {
        Log.d(TAG, "onDeleteCommandResponse");
    }

    @Override
    public void onDeleteInteractionChoiceSetResponse(DeleteInteractionChoiceSetResponse response) {
        Log.d(TAG, "onDeleteInteractionChoiceSetResponse");
    }

    @Override
    public void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {
        Log.d(TAG, "onDeleteSubMenuResponse");
    }

    @Override
    public void onPerformInteractionResponse(PerformInteractionResponse response) {
        Log.d(TAG, "onPerformInteractionResponse");
    }

    @Override
    public void onResetGlobalPropertiesResponse(ResetGlobalPropertiesResponse response) {
        Log.d(TAG, "onResetGlobalPropertiesResponse");
    }

    @Override
    public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {
        Log.d(TAG, "onSetGlobalPropertiesResponse");
    }

    @Override
    public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {
        Log.d(TAG, "onSetMediaClockTimerResponse");
    }

    @Override
    public void onShowResponse(ShowResponse response) {
        Log.d(TAG, "onShowResponse: " + response.getResultCode().name());
    }

    @Override
    public void onSpeakResponse(SpeakResponse response) {
        Log.d(TAG, "onSpeakResponse");
    }

    @Override
    public void onOnButtonEvent(OnButtonEvent notification) {
        Log.d(TAG, "onOnButtonEvent " + notification.getButtonName() + " " + notification.getMessageType());
    }

    @Override
    public void onOnButtonPress(OnButtonPress notification) {
        Log.d(TAG, "onOnButtonPress:" + notification.getButtonName());
        Intent intent;

        // Handle default buttons
        switch (notification.getButtonName()) {
            case OK:
                // Toggle play or pause
                sendBroadcast(new Intent(MusicService.ACTION_TOGGLE_PLAYBACK));
                break;
            case SEEKLEFT:
                // TODO previous preset
                radioStationManager.previous();
                playNewRadioStation(radioStationManager.getCurrentStation());
                render();
                break;
            case SEEKRIGHT:
                // TODO next preset
                radioStationManager.next();
                playNewRadioStation(radioStationManager.getCurrentStation());
                render();
                break;
            case TUNEUP:
                break;
            case TUNEDOWN:
                break;
            case PRESET_0:
                break;
            case PRESET_1:
                break;
            case PRESET_2:
                break;
            case PRESET_3:
                break;
            case PRESET_4:
                break;
            case PRESET_5:
                break;
            case PRESET_6:
                break;
            case PRESET_7:
                break;
            case PRESET_8:
                break;
            case PRESET_9:
                break;
            case CUSTOM_BUTTON:
                radioStationManager.set(notification.getCustomButtonName());
                playNewRadioStation(radioStationManager.getCurrentStation());
                render();

                // TODO Figure out what soft button was clicked
                break;
            case SEARCH:
                break;
        }
    }

    private void playNewRadioStation(RadioStation station) {
        Intent playNewSourceIntent = new Intent(MusicService.ACTION_PLAY_NEW_SOURCE);
        playNewSourceIntent.putExtra("source", station.getSource());
        sendBroadcast(playNewSourceIntent);
    }

    private void playNewSource(String source) {
        Intent playNewSourceIntent = new Intent(MusicService.ACTION_PLAY_NEW_SOURCE);
        playNewSourceIntent.putExtra("source", source);
        sendBroadcast(playNewSourceIntent);
    }

    private void render() {
        Show show = new Show();

        show.setMainField1(radioStationManager.getCurrentStationName());
        show.setSoftButtons(radioStationManager.toSoftButtons());

//        Image image = new Image();
//        image.setValue("npr.png");
//        image.setImageType(ImageType.DYNAMIC);
//
//        show.setGraphic(image);

        sendRpcRequest(show);
    }

    @Override
    public void onSubscribeButtonResponse(SubscribeButtonResponse response) {
        Log.d(TAG, "onSubscribeButtonResponse" + response.getResultCode());
    }

    @Override
    public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {
        Log.d(TAG, "onUnsubscribeButtonResponse");
    }

    @Override
    public void onOnPermissionsChange(OnPermissionsChange notification) {
        Log.d(TAG, "onOnPermissionsChange");
    }

    @Override
    public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {
        Log.d(TAG, "onSubscribeVehicleDataResponse");
    }

    @Override
    public void onUnsubscribeVehicleDataResponse(UnsubscribeVehicleDataResponse response) {
        Log.d(TAG, "onUnsubscribeVehicleDataResponse");
    }

    @Override
    public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
        Log.d(TAG, "onGetVehicleDataResponse");
    }

    @Override
    public void onOnVehicleData(OnVehicleData notification) {
        Log.d(TAG, "onOnVehicleData");
    }

    @Override
    public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {
        Log.d(TAG, "onPerformAudioPassThruResponse");
    }

    @Override
    public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {
        Log.d(TAG, "onEndAudioPassThruResponse");
    }

    @Override
    public void onOnAudioPassThru(OnAudioPassThru notification) {
        Log.d(TAG, "onOnAudioPassThru");
    }

    @Override
    public void onPutFileResponse(PutFileResponse response) {
        Log.d(TAG, "onPutFileResponse");
    }

    @Override
    public void onDeleteFileResponse(DeleteFileResponse response) {
        Log.d(TAG, "onDeleteFileResponse");
    }

    @Override
    public void onListFilesResponse(ListFilesResponse response) {
        Log.d(TAG, "onListFilesResponse");
    }

    @Override
    public void onSetAppIconResponse(SetAppIconResponse response) {
        Log.d(TAG, "onSetAppIconResponse");
    }

    @Override
    public void onScrollableMessageResponse(ScrollableMessageResponse response) {
        Log.d(TAG, "onScrollableMessageResponse");
    }

    @Override
    public void onChangeRegistrationResponse(ChangeRegistrationResponse response) {
        Log.d(TAG, "onChangeRegistrationResponse");
    }

    @Override
    public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {
        Log.d(TAG, "SetDisplayLayout response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    @Override
    public void onOnLanguageChange(OnLanguageChange notification) {
        Log.d(TAG, "onOnLanguageChange");
    }

    @Override
    public void onOnHashChange(OnHashChange notification) {
        Log.d(TAG, "onOnHashChange");
    }

    @Override
    public void onSliderResponse(SliderResponse response) {
        Log.d(TAG, "onSliderResponse");
    }

    @Override
    public void onOnDriverDistraction(OnDriverDistraction notification) {
        Log.d(TAG, "onOnDriverDistraction");
    }

    @Override
    public void onOnTBTClientState(OnTBTClientState notification) {
        Log.d(TAG, "onOnTBTClientState");
    }

    @Override
    public void onOnSystemRequest(OnSystemRequest notification) {
        Log.d(TAG, "onOnSystemRequest");
    }

    @Override
    public void onSystemRequestResponse(SystemRequestResponse response) {
        Log.d(TAG, "onSystemRequestResponse");
    }

    @Override
    public void onOnKeyboardInput(OnKeyboardInput notification) {
        Log.d(TAG, "onOnKeyboardInput");
    }

    @Override
    public void onOnTouchEvent(OnTouchEvent notification) {
        Log.d(TAG, "onOnTouchEvent");
    }

    @Override
    public void onDiagnosticMessageResponse(DiagnosticMessageResponse response) {
        Log.d(TAG, "onDiagnosticMessageResponse");
    }

    @Override
    public void onReadDIDResponse(ReadDIDResponse response) {
        Log.d(TAG, "onReadDIDResponse");
    }

    @Override
    public void onGetDTCsResponse(GetDTCsResponse response) {
        Log.d(TAG, "onGetDTCsResponse");
    }

    @Override
    public void onOnLockScreenNotification(OnLockScreenStatus notification) {
        Log.d(TAG, "onOnLockScreenNotification " + notification.getShowLockScreen());
        switch (notification.getShowLockScreen()) {
            case OPTIONAL:
            case REQUIRED:
                Intent showLockScreenIntent = new Intent(this, LockScreenActivity.class);
                showLockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(showLockScreenIntent);
                break;
            case OFF:
                closeLockScreen();
                break;
        }
    }

    @Override
    public void onDialNumberResponse(DialNumberResponse response) {
        Log.d(TAG, "onDialNumberResponse");
    }

    @Override
    public void onSendLocationResponse(SendLocationResponse response) {
        Log.d(TAG, "onSendLocationResponse");
    }

    @Override
    public void onShowConstantTbtResponse(ShowConstantTbtResponse response) {
        Log.d(TAG, "onShowConstantTbtResponse");
    }

    @Override
    public void onAlertManeuverResponse(AlertManeuverResponse response) {
        Log.d(TAG, "onAlertManeuverResponse");
    }

    @Override
    public void onUpdateTurnListResponse(UpdateTurnListResponse response) {
        Log.d(TAG, "onUpdateTurnListResponse");
    }

    @Override
    public void onServiceDataACK() {
        Log.d(TAG, "onServiceDataACK");
    }
}
