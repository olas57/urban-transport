package aspe.urbantransportticket;

/**
 * Created by Oleg on 11.05.2016.
 */
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;

import java.io.File;
import java.io.Writer;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils
{
    static Map<String, String> map_Tech = new HashMap<String, String>();

    public static String state;
    public static File extFiles;
    public static File locFiles;
    //////////////////////////////////////////////////
    private static Writer writer;
    private static String absolutePath;
    public static GregorianCalendar mycalendar;
    //////////////////////////////////////////

    static enum NfcTagTypes
    {
        NFC_TAG_TYPE_UNKNOWN,
        NFC_TAG_TYPE_1,
        NFC_TAG_TYPE_2,
        NFC_TAG_TYPE_3,
        NFC_TAG_TYPE_4A,
        NFC_TAG_TYPE_4B,
        NFC_TAG_TYPE_A,
        NFC_TAG_TYPE_B,
        NFC_TAG_TYPE_F,
        NFC_TAG_TYPE_V
    }

    static void Tech()
    {
        map_Tech.put("android.nfc.tech.NfcA", "NFC-A (ISO 14443-3A)");
        map_Tech.put("android.nfc.tech.NfcB", "NFC-B (ISO 14443-3B)");
        map_Tech.put("android.nfc.tech.NfcF", "NFC-F (JIS 6319-4)");
        map_Tech.put("android.nfc.tech.NfcV", " NFC-V (ISO 15693)");
        map_Tech.put("android.nfc.tech.NfcV", " NFC-V (ISO 15693)");
        map_Tech.put("android.nfc.tech.IsoDep", "ISO-DEP (ISO 14443-4)");
        map_Tech.put("android.nfc.tech.Ndef", "ISO-DEP (NDEF formatted");
        map_Tech.put("android.nfc.tech.NdefFormatable", "NDEF formattable");
        map_Tech.put("android.nfc.tech.MifareClassic", "MIFARE Classic");
        map_Tech.put("android.nfc.tech.MifareUltralight", " MIFARE Ultralight ");
    }
    /////////////////////////////////////////////////////////////////////////////////////////
    static NfcTagTypes decodeTagType (Tag pTag)
    {
        NfcTagTypes lType = NfcTagTypes.NFC_TAG_TYPE_UNKNOWN;
        List<String> lTechList = Arrays.asList(pTag.getTechList());
        String nfcTechPrefixStr = "android.nfc.tech.";
        // Try the Ndef technology
        Ndef lNdefTag = Ndef.get(pTag);
        if (lNdefTag != null)
        {
            if (lNdefTag.getType().equals(Ndef.NFC_FORUM_TYPE_1))
            {
                lType = NfcTagTypes.NFC_TAG_TYPE_1;
            }
            else if (lNdefTag.getType().equals(Ndef.NFC_FORUM_TYPE_2))
            {
                lType = NfcTagTypes.NFC_TAG_TYPE_2;
            }
            else if (lNdefTag.getType().equals(Ndef.NFC_FORUM_TYPE_3))
            {
                lType = NfcTagTypes.NFC_TAG_TYPE_3;
            }
            else if (lNdefTag.getType().equals(Ndef.NFC_FORUM_TYPE_4))
            {
                if (lTechList.contains(nfcTechPrefixStr+"NfcA"))
                {
                    lType = NfcTagTypes.NFC_TAG_TYPE_4A;
                } else if (lTechList.contains(nfcTechPrefixStr+"NfcB"))
                {
                    lType = NfcTagTypes.NFC_TAG_TYPE_4B;
                }
            }
        }
        else
        {
            // Try the IsoDep technology
            IsoDep lIsoDepTag = IsoDep.get(pTag);
            if (lIsoDepTag != null)
            {
                if (lTechList.contains(nfcTechPrefixStr+"NfcA"))
                {
                    lType = NfcTagTypes.NFC_TAG_TYPE_4A;
                }
                else if (lTechList.contains(nfcTechPrefixStr+"NfcB"))
                {
                    lType = NfcTagTypes.NFC_TAG_TYPE_4B;
                }
            }
            else
            {
                // Try the underlying technologies
                if (lTechList.contains(nfcTechPrefixStr+"NfcA"))
                {
                    lType = NfcTagTypes.NFC_TAG_TYPE_A;
                } else if (lTechList.contains(nfcTechPrefixStr+"NfcB"))
                {
                    lType = NfcTagTypes.NFC_TAG_TYPE_B;
                } else if (lTechList.contains(nfcTechPrefixStr+"NfcF"))
                {
                    lType = NfcTagTypes.NFC_TAG_TYPE_F;
                } else if (lTechList.contains(nfcTechPrefixStr+"NfcV"))
                {
                    lType = NfcTagTypes.NFC_TAG_TYPE_V;
                }
            }
        }
        return lType;
    }
 }
