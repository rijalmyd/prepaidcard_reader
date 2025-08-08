package com.agusibrahim.labs.prepaidcard_reader;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import lib.NfcCardReaderListenerSBF;
import lib.NfcCardReaderSBF;
import static android.content.ContentValues.TAG;
public class PrepaidcardReaderPlugin implements FlutterPlugin, MethodCallHandler, NfcAdapter.ReaderCallback, ActivityAware {
    static MethodChannel channel;
    NfcAdapter mNfcAdapter;
    NfcBroacastReceiver mNfcBroadNfcBroacastReceiver = new NfcBroacastReceiver();
    IntentFilter mNfcBroadcastReceiverFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
    NfcCardReaderSBF mCardReader;
    NfcCardReaderListenerSBF mCardReaderListener;
    Activity activity;
    int flag = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V | NfcAdapter.FLAG_READER_NFC_BARCODE | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "read_card_nfc");
        channel.setMethodCallHandler(this);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(flutterPluginBinding.getApplicationContext());
        mCardReaderListener = new cekSaldo(channel);
        mCardReader = new NfcCardReaderSBF(flutterPluginBinding.getApplicationContext(), mCardReaderListener);
    }
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + Build.VERSION.RELEASE);
        }
        if (call.method.equals("Nfc#startSession")) {
            handleNfcStartSession(call, result);
        } else if (call.method.equals("Nfc#stopSession")) {
            disableReaderMode();
        } else {
            result.notImplemented();
        }
    }
    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
    public void onNFCStart() {
        Log.d(TAG, "Hey Hey");
    }
    public void handleNfcStartSession(@NonNull MethodCall call, @NonNull Result result) {
        if (mNfcAdapter == null) {
            Log.d(TAG, "NFC null");
        } else if (!mNfcAdapter.isEnabled()) {
            Log.d(TAG, "NFC Mati");
        } else {
            Log.d(TAG, "NFC Aktif");
        }
        mNfcAdapter.enableReaderMode(activity, this, flag, null);
    }
    public void disableReaderMode() {
        mNfcAdapter.disableReaderMode(activity);
    }
    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }
    @Override
    public void onDetachedFromActivityForConfigChanges() {
    }
    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }
    @Override
    public void onDetachedFromActivity() {
    }
    private static class NfcBroacastReceiver extends BroadcastReceiver {
        public NfcBroacastReceiver() {
        }
        public void onReceive(Context context, @NonNull Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: " + action);
            if (Objects.requireNonNull(action).equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);
                switch (state) {
                    case NfcAdapter.STATE_OFF:
                        break;
                    case NfcAdapter.STATE_TURNING_OFF:
                        break;
                    case NfcAdapter.STATE_ON:
                        break;
                    case NfcAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    }
    static class cekSaldo implements NfcCardReaderListenerSBF {
        @NonNull
        Locale localeID = new Locale("in", "ID");
        cekSaldo(MethodChannel cekSaldo) {
            Log.d(TAG, "cekSaldo: ");
        }
        @Override
        public void onBrizziCardFound(@NonNull String paramAnonymousString, long paramAnonymousLong) {
            DecimalFormat localDecimalFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance();
            DecimalFormatSymbols localDecimalFormatSymbols = new DecimalFormatSymbols();
            localDecimalFormatSymbols.setMonetaryDecimalSeparator(',');
            localDecimalFormatSymbols.setGroupingSeparator('.');
            localDecimalFormat.setDecimalFormatSymbols(localDecimalFormatSymbols);
            System.out.println("Brizzi");
            try {
                JSONObject obj = new JSONObject();
                obj.put("card_id", "");
                obj.put("card_code", "brz");
                obj.put("card_name", "Brizzi");
                obj.put("card_number", paramAnonymousString);
                obj.put("balance", String.valueOf(paramAnonymousLong));
                obj.put("another_info", "");
                channel.invokeMethod("onDiscovered", obj.toString());
            } catch (Exception e) {
                channel.invokeMethod("onDiscovered", "Error " + e.toString());
            }
        }
        @Override
        public void onEKTPCardFound(Bitmap paramBitmap) {
            System.out.println("EKTP Found");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            paramBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            System.out.println("EKTP LENGTH: " + (encoded.length()));
            try {
                JSONObject obj = new JSONObject();
                obj.put("card_id", "");
                obj.put("card_code", "ktp");
                obj.put("card_name", "KTP");
                obj.put("card_number", "");
                obj.put("balance", "");
                obj.put("another_info", encoded.replace("\n", ""));
                channel.invokeMethod("onDiscovered", obj.toString());
            } catch (Exception e) {
                channel.invokeMethod("onDiscovered", "Error " + e.toString());
            }
        }
        @Override
        public void onEMoneyCardFound(@NonNull String paramAnonymousString, long paramAnonymousLong) {
            Log.d(TAG, "onEMoneyCardFound: " + paramAnonymousString + " " + paramAnonymousLong);
            DecimalFormat localDecimalFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance();
            DecimalFormatSymbols localDecimalFormatSymbols = new DecimalFormatSymbols();
            localDecimalFormatSymbols.setMonetaryDecimalSeparator(',');
            localDecimalFormatSymbols.setGroupingSeparator('.');
            localDecimalFormat.setDecimalFormatSymbols(localDecimalFormatSymbols);
            try {
                JSONObject obj = new JSONObject();
                obj.put("card_id", "");
                obj.put("card_code", "emo");
                obj.put("card_name", "E-Money");
                obj.put("card_number", paramAnonymousString);
                obj.put("balance", String.valueOf(paramAnonymousLong));
                obj.put("another_info", "");
                channel.invokeMethod("onDiscovered", obj.toString());
            } catch (Exception e) {
                channel.invokeMethod("onDiscovered", "Error " + e.toString());
            }
        }
        @Override
        public void onError(String paramAnonymousString) {
            Log.d(TAG, "onError: " + paramAnonymousString);
        }
        @Override
        public void onFlazzCardFound(@NonNull String paramAnonymousString, long paramAnonymousLong) {
            DecimalFormat localDecimalFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance();
            DecimalFormatSymbols localDecimalFormatSymbols = new DecimalFormatSymbols();
            localDecimalFormatSymbols.setMonetaryDecimalSeparator(',');
            localDecimalFormatSymbols.setGroupingSeparator('.');
            localDecimalFormat.setDecimalFormatSymbols(localDecimalFormatSymbols);
            StringBuilder paramAnonymousStringBuilder = new StringBuilder(paramAnonymousString);
            int i = paramAnonymousString.length() - 4;
            while (i > 0) {
                paramAnonymousStringBuilder.insert(i, ' ');
                i -= 4;
            }
            paramAnonymousString = paramAnonymousStringBuilder.toString();
            try {
                JSONObject obj = new JSONObject();
                obj.put("card_id", "");
                obj.put("card_code", "flz");
                obj.put("card_name", "Flazz");
                obj.put("card_number", "");
                obj.put("balance", "");
                obj.put("another_info", paramAnonymousString);
                channel.invokeMethod("onDiscovered", obj.toString());
            } catch (Exception e) {
                channel.invokeMethod("onDiscovered", "Error " + e.toString());
            }
        }
        @Override
        public void onTapCashCardFound(@NonNull String paramAnonymousString, long paramAnonymousLong) {
            System.out.println("Tap Cash Found");
            DecimalFormat localDecimalFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance();
            DecimalFormatSymbols localDecimalFormatSymbols = new DecimalFormatSymbols();
            localDecimalFormatSymbols.setMonetaryDecimalSeparator(',');
            localDecimalFormatSymbols.setGroupingSeparator('.');
            localDecimalFormat.setDecimalFormatSymbols(localDecimalFormatSymbols);
            try {
                JSONObject obj = new JSONObject();
                obj.put("card_id", "");
                obj.put("card_code", "tcs");
                obj.put("card_name", "Tap Cash");
                obj.put("card_number", paramAnonymousString);
                obj.put("balance", String.valueOf(paramAnonymousLong));
                obj.put("another_info", "");
                channel.invokeMethod("onDiscovered", obj.toString());
            } catch (Exception e) {
                channel.invokeMethod("onDiscovered", "Error " + e.toString());
            }
        }
    }
    public void onTagDiscovered(@NonNull Tag paramTag) {
        Log.d(TAG, "onTagDiscovered: " + paramTag.toString());
        activity.runOnUiThread(new ExecuteInUI(paramTag, paramTag.getId()));
    }
    Tag tag;
    private class ExecuteInUI implements Runnable {
        Tag mTag;
        byte[] bytes;
        public ExecuteInUI(Tag paramTag, byte[] bytes) {
            this.mTag = paramTag;
            tag = paramTag;
            this.bytes = bytes;
        }
        public void run() {
            try {
                mCardReader.handleNfcTag(this.mTag, this.bytes);
            } catch (Exception e) {
                channel.invokeMethod("onDiscovered", "Error " + e.toString());
            }
        }
    }
}