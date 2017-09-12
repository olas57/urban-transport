package aspe.urbantransportticket;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

//@SuppressLint("JavascriptInterface")
public class UrbanttActivity extends AppCompatActivity
{
    private WebView urbview = null;
    private NfcAdapter mNfcAdapter = null;
    private MifareUltralightTagTester mMfUlTester = null;
    private String mNfcA = "android.nfc.tech.NfcA";
    private String mMifareUltralight = "android.nfc.tech.MifareUltralight";
    private String mNdef = "android.nfc.tech.Ndef";
    /////////////////////////////////////////////////////
    public NfcA mnfcA = null;
    /////////////////////////////////////////////////////
    private String [] mArrTech = null;
    private String[] mArrUid = null;
    /////////////////////////////////////////////////////
    private String mTickNum = "";
    private String mTickAppIdUlt = "";
    private String mTickIdUlt = "";
    private String mTickIsDateUlt = "";
    private String mTickValPerUlt = "";
    private String mTickRemTripUlt = "";
    private String mTickLastPassUlt = "";
    private String mTickLastTurnUlt = "";
    private String mTickTransTypeUlt = "";
    private int mCardIntervalUlt = 0;
    private String mCardLastTransferUlt = "";
    private String mCardRemMinUlt90 = "";
    private String mCardRemMinUltSingle = "";
    private String mCardRemFixedDays = "";
    private String mListStrTech = "";
    private String mType = "";
    private String mListStrUid = "";
    private StringBuilder mTick = null;
    private int tick = 0;
    private Tag tagFromIntent = null;
    private int mTickId = 0;
    private String mCardslife = "";
    /////////////////////////////////////////////////
    // Application Id
    static Map<String, String> map_AppId = new HashMap<String, String>();

    // Ticket Id
    static Map<String, String> map_TicketId = new HashMap<String, String>();

    // Transport type
    static Map<String, String> map_TransType = new HashMap<String, String>();
    /////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urbantt);
        urbview = (WebView)findViewById(R.id.WebView);
        urbview.loadDataWithBaseURL("file:///android_asset/", readAssetFileAsString("init.html"), "text/html", "UTF-8", null);
        WebSettings settings = this.urbview.getSettings();
        settings.setJavaScriptEnabled(true);
        urbview.addJavascriptInterface(new WebViewJavaScriptInterface(this), "app");
        onNewIntent(getIntent());
    }

    //JavaScript Interface. Web code can access methods in here
    //(as long as they have the @JavascriptInterface annotation)
    public class WebViewJavaScriptInterface
    {
        private Context context;
        // Need a reference to the context in order to sent a post message
        public WebViewJavaScriptInterface(Context context)
        {
            this.context = context;
        }
        // This method can be called from Android. @JavascriptInterface
        // required after SDK version 17.
        @JavascriptInterface
        public String makeDisp()
        {
            return mTick.toString();
        }

        @JavascriptInterface
        public int Tickettype()
        {
            return tick;
        }
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        String action = intent.getAction();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter == null)
        {
            AlertDialog.Builder alertbox = new AlertDialog.Builder(urbview.getContext());
            alertbox.setTitle("NFC");
            alertbox.setMessage(getString(R.string.msg_nfccap));
            alertbox.setPositiveButton
            (
                "Check NFC Support", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        {
                            Intent intent = new Intent("android.settings.NFC_SETTINGS");
                            startActivity(intent);
                        }
                        else
                        {

                        }
                    }
                }
            );
            alertbox.setNegativeButton
            (
                "Close", new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finish();
                    }
                }
            );
            alertbox.show();
        }
        else
        {
            ////////////////////////////////////////////////////////////////////////////
            if (!mNfcAdapter.isEnabled())
            {
                AlertDialog.Builder alertbox = new AlertDialog.Builder(urbview.getContext());
                alertbox.setTitle("NFC");
                alertbox.setMessage(getString(R.string.msg_nfcon));
                alertbox.setPositiveButton
                (
                    "Turn On", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                           {
                              Intent intent = new Intent("android.settings.NFC_SETTINGS");
                              startActivity(intent);
                           }
                           else
                           {
                           }
                       }
                   }
                );
                alertbox.setNegativeButton
                (
                    "Close", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finish();
                        }
                    }
                );
                alertbox.show();
            }
            ////////////////////////////////////////////////////////////////////////////
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
                if (mMfUlTester != null) {
                    mMfUlTester = null;
                }
                Process(intent);
            }
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                if (mMfUlTester != null) {
                    mMfUlTester = null;
                }
                Process(intent);
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        PendingIntent intent = PendingIntent.getActivity
        (
            this, 0, new Intent(this, getClass()).
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        );

        if(NfcAdapter.getDefaultAdapter(this) != null)
        {
            NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch
            (
               this, intent, null, null
            );
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if( NfcAdapter.getDefaultAdapter(this) != null)
        {
            NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
        }
    }

    public void Process(Intent intent)
    {
        tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tagFromIntent != null)
        {
            //Toast.makeText(getApplicationContext(), "Tag Discovered", Toast.LENGTH_LONG).show();
            String[] techs = tagFromIntent.getTechList();
            for(String t : techs)
            {
                if(mMifareUltralight.equals(t))
                {
                    ReadUltralight();
                }
                else if(mNdef.equals(t))
                {
                    ReadUltralight();
                }
                else
                {
                    ReadUltralight();
                }
            }
        }
    }

    private void displayMifareUltralightData()
    {
        int i = 0;
        int len = 0;
        mTick = new StringBuilder("");

        // Number
        mTick.append("Номер: ");
        mTick.append(mTickNum);
        mTick.append("\r\n");

        //shelf life
        mTick.append("Срок годности: ");
        mTick.append(mCardslife);
        mTick.append("\r\n");

        // Application Id
        mTick.append("Билет: ");
        mTick.append(mTickAppIdUlt);
        mTick.append("\r\n");

        // Ticket Id
        mTick.append("Число поездок: ");
        mTick.append(mTickIdUlt);
        mTick.append("\r\n");

        // Issue Date
        mTick.append("Дата продажи: ");
        mTick.append(mTickIsDateUlt);
        mTick.append("\r\n");

        // Valid Period
        mTick.append("Срок действия: ");
        mTick.append(mTickValPerUlt);
        mTick.append("\r\n");

        // Reminder Trip
        mTick.append("Осталось/использовано поездок: ");
        mTick.append(mTickRemTripUlt);
        mTick.append("\r\n");

        // Last Pass
        mTick.append("Последний проход: ");
        mTick.append(mTickLastPassUlt);
        mTick.append("\r\n");

        ////////////////////////////////////////////////////////
        // 90 min
        // last transfer
        if(mTickId == 437)
        {
            if(mCardIntervalUlt > 1)
            {
                mTick.append("Последняя пересадка: ");
                mTick.append(mCardLastTransferUlt);
                mTick.append("\r\n");
            }
            // Reminder minutes
            mTick.append("Осталось минут: ");
            mTick.append(mCardRemMinUlt90);
            mTick.append("\r\n");
        }
        //////////////////////////////////
        // Single
        // Reminder minutes
        if(mTickId == 410 || mTickId == 411 || mTickId == 412 || mTickId == 416 || mTickId == 418 ||
           mTickId == 607 || mTickId == 608
          )
        {
            mTick.append("Осталось минут: ");
            mTick.append(mCardRemMinUltSingle);
            mTick.append("\r\n");
        }
        if(mTickId == 419)
        {
            mTick.append("Осталось часов/минут: ");
            mTick.append(mCardRemFixedDays);
            mTick.append("\r\n");
        }
        ////////////////////////////////////////////////////////
        // Last Turnstile
        mTick.append("Последний валидатор: ");
        mTick.append( mTickLastTurnUlt);
        mTick.append("\r\n");

        // Transport type
        mTick.append("Транспорт: ");
        mTick.append( mTickTransTypeUlt);
        mTick.append("\r\n");

        urbview.loadDataWithBaseURL("file:///android_asset/", readAssetFileAsString("init.html"), "text/html", "UTF-8", null);
        /////////////////////////////////////////////////////////////////////////
    }

    private String readAssetFileAsString(String sourceHtmlLocation)
    {
        InputStream is;

        try
        {
            is = urbview.getContext().getAssets().open(sourceHtmlLocation);
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            return new String(buffer, "UTF-8");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public void ReadUltralight()
    {
        mType = "";
        mTickNum = "";
        mTickAppIdUlt = "";
        mTickIdUlt = "";
        mTickIsDateUlt = "";
        mTickValPerUlt = "";
        mTickRemTripUlt = "";
        mTickLastPassUlt = "";
        mTickLastTurnUlt = "";
        mTickTransTypeUlt = "";
        mCardIntervalUlt = 0;
        mCardLastTransferUlt = "";
        mCardRemMinUlt90 = "";
        mListStrTech = "";
        mListStrUid = "";
        mTick = null;
        tick = 0;
        mTickId = 0;
        mCardslife = "";
        ///////////////////////////////////////
        // App Id      dec
        map_AppId.put("279", "Единый");
        map_AppId.put("264", "ТАТ");
        // Ticket Id      dec
        map_TicketId.put("410", "1");
        map_TicketId.put("411", "1");
        map_TicketId.put("412", "2");
        map_TicketId.put("416", "20");
        map_TicketId.put("418", "60");
        map_TicketId.put("419", "1 сутки");
        //map_TicketId.put("437", "90 мин");
        map_TicketId.put("437", "90 мин 1 поездка");
        map_TicketId.put("607", "60");
        map_TicketId.put("608", "1");
        //
        // Transport Type  dec                             hex
        map_TransType.put("128", "Наземный транспорт");  // 0x80
        map_TransType.put("64", "Метро"); // 0x40
        map_TransType.put("192", "МЦК"); // 0xC0
        //////////////////////////////////////////////////
        Utils.NfcTagTypes type = Utils.decodeTagType(tagFromIntent);
        mType = type.toString();

        mMfUlTester = new MifareUltralightTagTester();
        mMfUlTester.Init();
        Utils.Tech();
        ////////////////////////////////////////////////
        if ((mType == "NFC_TAG_TYPE_2") || (mType == "NFC_TAG_TYPE_4A"))
        {
            // read TAG ???
            Toast.makeText(this, "Не удается прочитать карту", Toast.LENGTH_SHORT).show();
            ////////////////////////////////////////////
        }
        else
        {
            mnfcA = NfcA.get(tagFromIntent);

            if (mMfUlTester != null)
            {
                try
                {
                    mnfcA.connect();
                    mnfcA.setTimeout(2000);
                    //  Page 0
                    mMfUlTester.responseAPDU_0_15 = mnfcA.transceive(mMfUlTester.Bytes_0_15);
                    mMfUlTester.responseAPDU = mMfUlTester.responseAPDU_0_15;
                    //  Page 4
                    mMfUlTester.responseAPDU_16_31 = mnfcA.transceive(mMfUlTester.Bytes_16_31);
                    mMfUlTester.responseAPDU = mMfUlTester.responseAPDU_16_31;

                    mMfUlTester.GetAppId();
                    mTickAppIdUlt = map_AppId.get(mMfUlTester.mAppId);

                    mMfUlTester.GetTicketId();
                    mTickIdUlt = map_TicketId.get(mMfUlTester.mTickId);

                    mTickId = mMfUlTester.mTick;
                    tick = mMfUlTester.Tick;
                    ///////////////////////////////////////////////////

                    mMfUlTester.GetTicketNum();
                    mTickNum = mMfUlTester.mTickNum;

                    mMfUlTester.GetCardShelfLife();
                    mCardslife = mMfUlTester.mCardslife;
                    //////////////////////////////////////////////////////
                    //  Page 8
                    mMfUlTester.responseAPDU_32_47 = mnfcA.transceive(mMfUlTester.Bytes_32_47);
                    mMfUlTester.responseAPDU = mMfUlTester.responseAPDU_32_47;

                    //  Page 9
                    mMfUlTester.responseAPDU_48_63 = mnfcA.transceive(mMfUlTester.Bytes_48_63);
                    mMfUlTester.responseAPDU = mMfUlTester.responseAPDU_48_63;

                    mMfUlTester.GetTicketIsDate();
                    mTickIsDateUlt = mMfUlTester.mTickIsDate;

                    mMfUlTester.GetValPer();
                    mTickValPerUlt = mMfUlTester.mTickValPer;

                    mMfUlTester.GetTicketRemFixedDays();
                    mCardRemFixedDays = mMfUlTester.mCardRemFixedDay;
                    ////////////////////////////////////////

                    mMfUlTester.GetTicketLastPass();
                    mTickLastPassUlt = mMfUlTester.mTickLastPass;

                    mCardIntervalUlt = mMfUlTester.mCardInterval;
                    mCardLastTransferUlt = mMfUlTester.mCardLastTransfer;
                    mCardRemMinUlt90 = mMfUlTester.mCardRemMin90;
                    mCardRemMinUltSingle = mMfUlTester.mCardRemMinSingle;

                    mMfUlTester.GetTicketRemTrip();
                    mTickRemTripUlt = mMfUlTester.mTickRemTrip;

                    mMfUlTester.GetTicketTransTypes();
                    mTickTransTypeUlt = map_TransType.get(mMfUlTester.mTickTransType);

                    mMfUlTester.GetTicketLastTurnstile();
                    mTickLastTurnUlt = mMfUlTester.mTickLastTurn;

                    ///////////////////////////////////////////////////
                    System.out.println("success connection");
                }
                catch (IOException e)
                {
                    Toast.makeText(this, "Не удается прочитать карту", Toast.LENGTH_SHORT).show();
                }
            }
            if (mnfcA != null)
            {
                try
                {
                    mnfcA.close();
                }
                catch (IOException e)
                {
                    Toast.makeText(this, "Не удается прочитать карту", Toast.LENGTH_SHORT).show();
                }
            }
        }
        displayMifareUltralightData();
    }
}
