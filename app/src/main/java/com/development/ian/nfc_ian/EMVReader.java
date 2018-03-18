/**************************************************************************
 *
 *  Copyright 2014, Roger Brown
 *
 *  This file is part of Roger Brown's Toolkit.
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 *  more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
 
/* 
 * $Id: EMVReader.java 1 2014-06-07 22:37:15Z rhubarb-geek-nz $
 */

package com.development.ian.nfc_ian;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rogerb
 */

public class EMVReader 
{
    public boolean doTrace=true;
    final static String UTF8="utf-8";

    final CardReader reader;
    final byte [] adf;

    public Integer expiryMonth,expiryYear;
    public String pan;
    public String cardholderName;
    public String decision=null;
    public String applicationCryptogram=null;
    public String atc=null;
    public String iad=null;
    public byte [] aid;
    public String issuer;
    public String rid;
    public String capk_index;
    public String issuer_pk_encrypted_certificate;
    public String issuer_pk_remainder;
    public String issuer_pk_exponent;
    public String icc_pk_encrypted;
    public String icc_pk_remainder;
    public String icc_pk_exponent;
    public String sdad_encrypted;
    public String aip;
    public String apduLog;

    public byte [] PDOL;
    public byte [] CDOL1;
    private String ttq="68000000";
    private String amount="000000000010";
    private String amountOther="000000000000";
    private String terminalCountryCode="0250";
    private String tvr="0000000000";
    private String curcy="0978";
    private String txDate="180207";
    private String txType="00";
    private String unpredicatableNumber;
    private String terminalType;
    private String gacp1;
    private ArrayList<ReadApplicationTemplate> applicationTemplates;

    public static final byte [] SELECT_PPSE={
        0x00,(byte)0xA4,0x04,0x00,0x0E,
        '2','P','A','Y','.','S','Y','S','.','D','D','F','0','1',0x00
    };
    public static final byte [] AID_PPSE={
        '2','P','A','Y','.','S','Y','S','.','D','D','F','0','1'    
    };
    public static final byte [] SELECT_PSE={
        0x00,(byte)0xA4,0x04,0x00,0x0E,
        '1','P','A','Y','.','S','Y','S','.','D','D','F','0','1'
    };
    public static final byte [] AID_PSE={
        '1','P','A','Y','.','S','Y','S','.','D','D','F','0','1'    
    };

    private ArrayList<AflRecord> afl=new ArrayList<>();

    private int getTagLen(byte[] data, int offset, int len)
    {
        int r=1;
        if ((data[offset]&0x1f)==0x1f)
        {
            r=2;
        }
        return r;
    }

    private int getTag(byte[] data, int offset, int tagLen) 
    {
        return BinaryTools.readInt(data, offset, tagLen);
    }

    private int getLenLen(byte[] data, int offset, int len) 
    {
        int r=0;
        int c=(0xff & data[offset]);
        if (c < 0x80)
        {
            r=1;
        }
        else
        {
            switch (c)
            {
                case 0x81:
                    r=2;
                    break;
                case 0x82:
                    r=3;
                    break;
            }
        }   
        return r;
    }

    private int getLen(byte[] data, int offset, int lenLen) 
    {
        int r=0;
        
        switch (lenLen)
        {
            case 1:
                r=(0x7f & data[offset]);
                break;
            case 2:
                r=(0xff & data[offset+1]);
                break;
            case 3:
                r=BinaryTools.readInt(data,offset+1, 2);
                break;
        }
        return r;
    }

    public class TLV
    {
        public int type,length;
        public byte [] value;
    }
    

    public interface CardReader
    {
        public void resetApduLog();
        public String getApduLog();
        public byte [] transceive(byte[] apdu) throws IOException;
    }

    /**
     *
     * @param r reference to paywaveHandler (allows to issue more read() commands)
     * @param b AID or null
     * @param a PPSE values
     */
    public EMVReader(CardReader r,byte [] b,byte [] a)
    {
        reader=r;
        aid=b;
        adf=a;
    }
    
    interface EnumCallback
    {
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException;
    }

    class AflRecord
    {
        public byte sfi; // Short File Identifier
        public byte firstrec; // First record to read (included)
        public byte lastrec; // Last record to read (included)
        public byte nrrecforxda; // Number of records participating in Data Authentication
    }
    class ReadPDOData implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("PDO %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0x57:
                    result=readTrack2Equivalent(data,offset,len);
                    break;
                case 0x82:
                    aip=Hex.encode(data,offset,len);
                    break;
                case 0x94:
                    result=readAFL(data,offset,len);
                    break;
                case 0x9F27:
                    result=readCryptogramInformationData(data,offset,len);
                    break;
                case 0x9F26:
                    result=readApplicationCryptogram(data,offset,len);
                    break;
                case 0x9F4B:
                    sdad_encrypted=Hex.encode(data,offset,len);
                    break;
                case 0x9F36:
                    result=readAtc(data,offset,len);
                    break;
                case 0x9F10:
                    result=readIAD(data,offset,len);
                    break;

            }
            
            return result;
        }
    }
    
    class ReadPDO implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("PDO %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0x70:
                case 0x77:
                    result=parse(new ReadPDOData(),data,offset,len);
                    break;
                case 0x80:
                    result=readAFL(data,offset+2,len-2);
                    break;
            }
            
            return result;
        }
    }
                        
    class ReadAppFCI implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("appFCI %04X,%d,",tag,len)+Hex.encode(data, offset, len));
                        
            switch (tag)
            {
                case 0x50:
                    try 
                    {
                        issuer=new String(data,offset,len,UTF8);
                    } 
                    catch (UnsupportedEncodingException ex) 
                    {
                        Logger.getLogger(EMVReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case 0x9F38:
                    PDOL=BinaryTools.bytesFrom(data,offset, len);
                    break;
            }
            return result;
        }   

        private boolean read() throws IOException 
        {
            boolean result=true;
            byte [] pdolData=null;
            
            if (PDOL!=null)
            {


                int pdolLen= getDOLlength(PDOL,0,PDOL.length);
                                
                pdolData=new byte[pdolLen+2];
                pdolData[0]=(byte)0x83;
                pdolData[1]=(byte)pdolLen;
                
                fillDOL(PDOL,0,PDOL.length,pdolData,2);

            }
            else
            {
                pdolData=new byte[]{(byte)0x83,0x00};
            }




            // GPO
            byte [] apdu=BinaryTools.catenate(new byte[][]{
                    new byte[]{(byte)0x80,(byte)0xa8,0x00,0x00,(byte)pdolData.length},
                    pdolData,
                    new byte[]{0}
                    });

            Log.i(getClass().getName(),"GPO in FCI"+toHex(apdu));
            byte [] resp=reader.transceive(apdu);

            
            if ((resp!=null)&&(resp.length>2))
            {
                ReadPDO pdo=new ReadPDO();
                
                result=parse(pdo,resp,0,resp.length-2);
            }

            // AFL indicates everything that needs to be read on the card
            // Now reads all necessary files
            Log.d(getClass().getName(),"Parse AFL");
            for(AflRecord aflRecord:afl) {
                byte sfi = aflRecord.sfi;
                byte firstRec = aflRecord.firstrec;
                byte lastRec = aflRecord.lastrec;
                byte authNum = aflRecord.nrrecforxda;

                while (result && (firstRec <= lastRec)) {
                    result = readRecord((byte) (0x1f & (sfi >> 3)), firstRec);

                    firstRec++;
                }
            }

            /*
            { // DELETE - THIS IS A TEST
                byte sfi = 8;
                byte firstRec = 2;
                byte lastRec = 2;
                byte authNum = 0;

                while (result && (firstRec <= lastRec)) {
                    result = readRecord((byte) (0x1f & (sfi >> 3)), firstRec);

                    firstRec++;
                }
            }
*/

            // Why not checking the Pin Try Counter ?
            apdu=BinaryTools.catenate(new byte[][]{new byte[]{(byte)0x80,(byte)0xCA,(byte)0x9F,(byte)0x17},new byte[]{0}});
            resp=reader.transceive(apdu);

            // For DDA cards, SDAD is obtained with internal authenticate
            // SDA cards don't do this
            // CDA card combine internal authenticate with Generate AC
            // VISA QVSDC cards already provided a cryptogram !


            if(applicationCryptogram==null) {
                Log.i(getClass().getName(), "Application cryptogram not yet generated - trying DDA internal authenticate ");
                if (CDOL1 != null) {
                    byte[] rndnr = new byte[4];
                    rndnr=BinaryTools.toBin(unpredicatableNumber);
                    byte[] generateAcApdu = BinaryTools.catenate(new byte[][]{ // Generate AC
                            new byte[]{(byte)0x00,(byte)0x88,(byte)0x00,(byte)0x00, (byte) rndnr.length}, // CLA INS P1 P2 LC
                            rndnr, // DATA
                            new byte[]{0} // LE
                    });

                    resp = reader.transceive(generateAcApdu);

                    if ((resp != null) && (resp.length > 2)) {
                        ReadPDO pdo = new ReadPDO();

                        result = parse(pdo, resp, 0, resp.length - 2);

                    }
                }
            }

            // byte[] pdolWithData=buildTerminalData(PDOL);
            // Log.i(getClass().getName(),"PDOL data to send : "+BinaryTools.toHex(pdolWithData));

            byte[] cdol1Data;
            if(applicationCryptogram==null)
            {
                Log.i(getClass().getName(),"Application cryptogram not yet generated Trying CDA ");
                if (CDOL1!=null)
                {
                    int cdol1len= getDOLlength(CDOL1,0,CDOL1.length);
                    cdol1Data=new byte[cdol1len];
                    fillDOL(CDOL1,0,CDOL1.length,cdol1Data,0);  // Prepare DOL for Generate AC

                    byte p1=BinaryTools.toBin(gacp1)[0];
                    byte [] generateAcApdu=BinaryTools.catenate(new byte[][]{ // Generate AC
                            new byte[]{(byte)0x80,(byte)0xae,p1,0x00,(byte)cdol1Data.length}, // CLA INS P1 P2 LC
                            cdol1Data, // DATA
                            new byte[]{0} // LE
                    });

                    resp=reader.transceive(generateAcApdu);

                    if ((resp!=null)&&(resp.length>2))
                    {
                        ReadPDO pdo=new ReadPDO();

                        result=parse(pdo,resp,0,resp.length-2);

                    }
                }
                else
                {
                    result=false;  // No CDOL1, no application cryptogram... giving up !
                }
            }

            return result;
        }
    }
    
    class ReadApplicationDataFileRecord implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("app %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0xA5:
                    {
                        ReadAppFCI app = new ReadAppFCI();
                        result=parse(app,data,offset,len);
                        
                        if (result)
                        {
                            result=app.read();
                        }
                    }
                    break;
            }
            
            return result;
        }   
    }
    
    class ReadApplicationDataFile implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("app %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0x6F:
                case 0x70:
                    result=parse(new ReadApplicationDataFileRecord(),data,offset,len);
                    break;
            }
            
            return result;
        }   
    }
    
    class ReadApplicationTemplate  implements EnumCallback
    {
        private byte applicationPriorityIndicator;
        public String rid;
        public byte[] aid;
        public String issuer;
        public byte getApplicationPriorityIndicator()
        {
            return applicationPriorityIndicator;
        }

        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("appTemp %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0x4F:
                    if(EmvKeys.getInstance().getBrand(rid,capk_index)==null)
                    { // rid is not known or wrong - give it another try
                        rid=Hex.encode(data,offset,len).substring(0,10);
                    }
                    else
                    {
                        Log.e(getClass().getName(),"Cowardly refusing to override rid "+rid);
                    }
                    Log.i(getClass().getName(),"rid="+rid);
                    aid=BinaryTools.bytesFrom(data,offset, len);
                    break;
                case 0x50:
                    try 
                    {
                        issuer=new String(data,offset,len,UTF8);
                    } 
                    catch (UnsupportedEncodingException ex) 
                    {
                        Logger.getLogger(EMVReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case 0x87:
                    applicationPriorityIndicator=BinaryTools.bytesFrom(data,offset, len)[0];
                    break;
            }
            
            return result;
        }   

        boolean read() throws IOException 
        {
            boolean result=true;
            byte [] apdu=BinaryTools.catenate(new byte[][]{
                    BinaryTools.bytesFrom(SELECT_PPSE,0,4),
                    new byte[]{(byte)aid.length},
                    aid,
                    new byte[]{0}
                    });
            Log.i(getClass().getName(),"ReadApplicationTemplate"+toHex(apdu));
            byte [] resp=reader.transceive(apdu);

            
            if ((resp!=null)&&(resp.length>2))
            {
                // parse results
                result=parse(new ReadApplicationDataFile(),resp,0,resp.length-2);

                // Sending Generate AC

            }
            
            return result;
        }
    }
    
    class ReadFCIIssuerDiscretionaryData implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("FCIidd %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0x61:
                    {
                        ReadApplicationTemplate app = new ReadApplicationTemplate();
                        result=parse(app,data,offset,len);
                        
                        if (result)
                        {
                            // result=app.read();
                            applicationTemplates.add(app);  // delay reading of application templates - right now, we don't know which application is prioritary !
                        }
                    }
                    break;
            }
            
            return result;
        }   
    }
    
    class ReadFCIPropTemplate implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("FCIpt %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0xBF0C:
                    result=parse(new ReadFCIIssuerDiscretionaryData(),data,offset,len);
                    break;
                case 0x88:
                    if (len==1)
                    {
                        result=readPSERecord(data[offset]);
                    }
                    break;
            }
            
            return result;
        }   
    }
    
    class ReadPPSErecord implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("PPSE %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0xA5:
                    result=parse(new ReadFCIPropTemplate(),data,offset,len);
                    break;
                case 0x84:
                    if (false)
                    {
                        byte [] aid2=BinaryTools.bytesFrom(data,offset, len);
                        
                        if (!matchBytes(aid2,aid))
                        {
                            aid=aid2;
                            
                            result=new ReadApplicationTemplate().read();                     
                        }
                    }
                    break;
            }
            
            return result;
        } 
    }
    
    class ReadPPSE implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("PPSE %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0x6f:
                    result=parse(new ReadPPSErecord(),data,offset,len);
                    break;
            }
            
            return result;
        } 
    }
    
    boolean parse(EnumCallback c,byte [] data,int offset,int len) throws IOException
    {
        boolean b=true;

        if (doTrace)
        {
            Log.i(getClass().getName(),"parse "+c.getClass().getSimpleName());
            Log.i(getClass().getName(),Hex.encode(data, offset, len));
        }
        
        while (b && (len > 0))
        {
            Log.i(getClass().getName(),Hex.encode(data,0,data.length));
            Log.i(getClass().getName(),(new String(new char[offset]).replace("\0", "  "))+"^");
            int tagLen=getTagLen(data,offset,len);
            
            if (tagLen < 1) break;
            
            int tag=getTag(data,offset,tagLen);
            
            offset+=tagLen;
            len-=tagLen;
            
            int lenLen=getLenLen(data,offset,len);
            
            int dlen=getLen(data,offset,lenLen);
            
            offset+=lenLen; len-=lenLen;
            
            if (doTrace)
            {
                System.err.println("parse: "+String.format("%04X,%d:", tag,dlen)+Hex.encode(data,offset,dlen));
            }
            
            b=c.found(tag, dlen, data, offset);
            
            len-=dlen;
            offset+=dlen;
        }
        
        return b;
    }

    // Entry point
    public void read(String ttq,String amount,String amountOther,String terminalCountryCode,String tvr,String curcy,String txDate,String txType,String unpredicatableNumber,String terminalType,String gacp1) throws IOException
    {
        this.ttq=ttq;
        this.amount=amount;
        this.amountOther=amountOther;
        this.terminalCountryCode=terminalCountryCode;
        this.tvr=tvr;
        this.curcy=curcy;
        this.txDate=txDate;
        this.txType=txType;
        this.unpredicatableNumber=unpredicatableNumber;
        this.terminalType=terminalType;
        this.gacp1=gacp1;


        byte [] ppse=adf;
        applicationTemplates=new ArrayList<ReadApplicationTemplate>();
        reader.resetApduLog(); // clears APDU log

        if (ppse==null)
        {
            byte [] resp=reader.transceive(ppse);
            Log.i(getClass().getName(),"PPSE="+toHex(ppse));
        }
        else
        {
            Log.i(getClass().getName(),"PPSE="+toHex(ppse));
        }
        if ((ppse!=null)&&(ppse.length>2))
        {
            parse(new ReadPPSE(),ppse,0,ppse.length-2);
        }
        if ((applicationTemplates!=null)&&(applicationTemplates.size()>=1))
        {
            ReadApplicationTemplate rat=null;
            byte maxPriority=0;

            for(ReadApplicationTemplate candidate:applicationTemplates)
            {
                if(candidate.getApplicationPriorityIndicator()>maxPriority)
                {
                    maxPriority=candidate.getApplicationPriorityIndicator();
                    aid=candidate.aid;
                    rid=candidate.rid;
                    issuer=candidate.issuer;
                    rat=candidate;
                }
            }
            // Just reading the application having minimal application priority indicator
            rat.read();
        }
        // All APDU work is done - but we may have to decrypt SDAD now (mastercard)
        if(applicationCryptogram==null && sdad_encrypted!=null)
        {
            decrypt_sdad();
        }

        Log.i(getClass().getName(),"FINISHED !!!!!!!!!");
        apduLog=reader.getApduLog();
        return;
    }

    int getDOLlength(byte[] PDOL, int offset, int len)
    {
        int tot=0;
        
        while (len > 0)
        {
            int tagLen=getTagLen(PDOL,offset,len);
            int tag=getTag(PDOL,offset,tagLen);
            offset+=tagLen;
            len-=tagLen;
            int optLen=getLenLen(PDOL,offset,len);
            int actLen=getLen(PDOL,offset,optLen);
            
            offset+=optLen;
            len-=optLen;
            
//            System.out.println(String.format("PDOL %04X,%d",tag,actLen));
            
            tot+=actLen;
        }
        
        return tot;
    }
    
    void fillDOL(byte[] PDOL, int offset, int len, byte[] pdolData, int i)
    {
        while (len > 0)
        {
            int tagLen=getTagLen(PDOL,offset,len);
            int tag=getTag(PDOL,offset,tagLen);
            offset+=tagLen;
            len-=tagLen;
            int optLen=getLenLen(PDOL,offset,len);
            int actLen=getLen(PDOL,offset,optLen);
            
            offset+=optLen;
            len-=optLen;
            
//            System.out.println(String.format("PDOL %04X,%d",tag,actLen));

            // Fills if known - otherwise binary zeroes will fill the tag
            Log.d(getClass().getName(),"Filling PDOL, i="+i+" - actlen="+actLen+" -  tag "+String.format("PDOL %04X,%d",tag,actLen));

            switch (tag)
            {
                case 0x9F1A:    /* country code */
                    if (actLen==2)
                    {
                        System.arraycopy(BinaryTools.toBin(terminalCountryCode),0,pdolData,i,2);
                        // pdolData[i]=0x05;
                        // pdolData[i+1]=0x54;
                    }
                    break;
                case 0x5F2A:    /* currency */
                    if (actLen==2)
                    {
                        System.arraycopy(BinaryTools.toBin(curcy),0,pdolData,i,2);
                        //pdolData[i]=0x05;
                        //pdolData[i+1]=0x54;
                    }
                    break;
                case 0x9F66:
                    switch (actLen)
                    {
                        case 4: /* kernel 3 */
                            System.arraycopy(BinaryTools.toBin(ttq),0,pdolData,i,4);
                            // pdolData[i]=0x30;
                            // pdolData[i+1]=0x00;
                            // pdolData[i+2]=0x00;
                            // pdolData[i+3]=0x00;
                            break;
                    }
                    break;
                case 0x9F37: /* random number */
                    if (actLen > 0)
                    {
                        System.arraycopy(BinaryTools.toBin(unpredicatableNumber),0,pdolData,i,4);
                         //Random r=new Random();
                         //byte []m=new byte[actLen];
                         //r.nextBytes(m);
                         //System.arraycopy(m,0,pdolData,i,actLen);
                    }
                    break;
                case 0x9F02:
                    if (actLen==6)
                    {
                        System.arraycopy(BinaryTools.toBin(amount),0,pdolData,i,6);
                    }
                    break;
                case 0x9F03:
                    if(actLen==6)
                    {
                        System.arraycopy(BinaryTools.toBin(amountOther),0,pdolData,i,6);
                    }
                    break;
                case 0x95:
                    if(actLen==5)
                    {
                        System.arraycopy(BinaryTools.toBin(tvr),0,pdolData,i,5);
                    }
                    break;
                case 0x9a:
                    if(actLen==3)
                    {
                        System.arraycopy(BinaryTools.toBin(txDate),0,pdolData,i,3);
                    }
                    break;
                case 0x9c:
                    if(actLen==1)
                    {
                        System.arraycopy(BinaryTools.toBin(txType),0,pdolData,i,1);
                    }
                    break;
                case 0x9f35:
                    if(actLen==1)
                    {
                        System.arraycopy(BinaryTools.toBin(terminalType),0,pdolData,i,1);
                        // pdolData[i]=0x23;  // attended, offline only
                    }
                    break;
                default:
                    Log.e(getClass().getName(),"Unknown tag requested by card - filling zeroes for tag "+Hex.encode(new byte[]{(byte)(tag>>8),(byte)(tag&0xff)},0,2));
                    break;
            }
            
            i+=actLen;
        }
    }
    
    boolean readTrack2Equivalent(byte [] data,int offset,int len)
    {
        boolean result=true;
        String cards=Hex.encode(data, offset, len);
        int i=cards.indexOf('D');
        if (i > 0)
        {
            pan=cards.substring(0, i);
            expiryYear=Integer.parseInt(cards.substring(i+1,i+3));
            expiryMonth=Integer.parseInt(cards.substring(i+3,i+5));
            result=true;
        }
        return result;
    }

    boolean readAtc(byte[] data,int offset,int len)
    {
        atc=Hex.encode(data,offset,len);
        return true;
    }

    boolean readIAD(byte[] data,int offset,int len)
    {
        iad=Hex.encode(data,offset,len);
        return true;
    }

    boolean readApplicationCryptogram(byte[] data,int offset,int len)
    {
        applicationCryptogram=Hex.encode(data,offset,len);
        return true;
    }
    boolean readCryptogramInformationData(byte[] data,int offset,int len)
    {

        Log.d(getClass().getName(),"readCryptogramInformationData() is called with "+BinaryTools.toHex(data));
        byte check=(byte)(data[offset]&0xc0);
        if(check==(byte)0x80)
        {
            Log.i(getClass().getName(),"ARQC detected");
            decision="0x80 - Go Online";
        }
        else if(check==(byte)0x40)
        {
            Log.i(getClass().getName(),"TC detected");
            decision="0x40 - Accepted offline";
        }
        else if(check==(byte)0x00)
        {
            Log.i(getClass().getName(),"AAC detected");
            decision="0x00 - Rejected offline";
        }
        else if(check==(byte)0xc0)
        {
            Log.i(getClass().getName(),"RFU detected");
            decision="0xc0 - RFU";
        }

        return true;
    }

    boolean readAFL(byte [] data,int offset,int len) throws IOException
    {
        boolean result=true;

        afl=new ArrayList<AflRecord>();

        while (result && (len > 0))
        {
            // Reading all SFI records has been moved after GPO is completely parsed - here we just store all AFL records
            AflRecord aflRecord=new AflRecord();
            aflRecord.sfi=data[offset++];
            aflRecord.firstrec=data[offset++];
            aflRecord.lastrec=data[offset++];
            aflRecord.nrrecforxda=data[offset++];

            afl.add(aflRecord);

            len-=4;
        }
        
        return result;
    }
    
    class ReadRecordData implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException
        {
            boolean result=true;
                        
            switch (tag)
            {
                case 0x57:
                // case 0x9f6b:
                    result=readTrack2Equivalent(data,offset,len);
                    break;
                case 0x5A:
                    pan=Hex.encode(data, offset, len);
                    if(pan.endsWith("F")) // padding because of odd size
                        pan=pan.substring(0,pan.length()-1);
                    break;
                case 0x5F24:
                    expiryMonth=Integer.parseInt(String.format("%x",data[offset+1]));
                    expiryYear=Integer.parseInt(String.format("%x",data[offset]));
                    // result=(pan==null); // don't stop treatment
                    break;
                case 0x5F20:
                    cardholderName=new String(data,offset,len, Charset.forName("ISO8859-1"));
                    result=true;
                    break;
                case 0x8C:
                    // Read CDOL1 - example
                    //9F02.06 9F03.06 9F1A.02 95.05 5F2A.02 9A.03 9C.01 9F37.04 9F35.01 9F45.02 9F4C.08 9F34.03
                    CDOL1=BinaryTools.bytesFrom(data,offset, len);
                    break;
                case 0x8F:
                    capk_index=Hex.encode(data,offset, len);
                    break;
                case 0x9F32:
                    issuer_pk_exponent=Hex.encode(data,offset,len);
                    break;
                case 0x92:
                    issuer_pk_remainder=Hex.encode(data,offset,len);
                    break;
                case 0x90:
                    issuer_pk_encrypted_certificate=Hex.encode(data,offset,len);
                    break;
                case 0x9F47:
                    icc_pk_exponent=Hex.encode(data,offset,len);
                    break;
                case 0x9F48:
                    icc_pk_remainder=Hex.encode(data,offset,len);
                    break;
                case 0x9F46:
                    icc_pk_encrypted=Hex.encode(data,offset,len);
                    break;
                case 0x9F4B:
                    sdad_encrypted=Hex.encode(data,offset,len);
                    break;

                // Todo


                // 93 - Signed static application data
                // 5F25 - application effective date
                // 5F28 - issuer country code
                // 5F34 - Pan sequence number
                // 9F35 - Terminal Type
                // 9F45 - Data authentication code
                // 9F4C - ICC Dynamic Number (?)
                // , 9F21, 9F7C, 9F45, 9F34
            }
                       
            return result;
        }  
    }
    
    class ReadRecord implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("RR %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0x70:
                    result=parse(new ReadRecordData(),data,offset,len);
                    break;
            }
                       
            return result;
        }  
    }
    
    class ReadPSERecord implements EnumCallback
    {
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
            switch (tag)
            {
                case 0x70:
                    result=parse(new ReadFCIIssuerDiscretionaryData(),data,offset,len);
                    break;
            }
            
            return result;
        }
    }

    boolean readPSERecord(byte sfi) throws IOException 
    {
        byte num=1;
        boolean result=true;
        byte []apdu={0x00,(byte)0xB2,num,(byte)((sfi<<3)+4),0x00 };

        Log.i(getClass().getName(),"readPSERecord");
        byte [] data=reader.transceive(apdu);

        if ((data!=null)&&(data.length==2)&&(data[0]==0x6c))
        {
            byte []apduLen={0x00,(byte)0xB2,num,(byte)((sfi<<3)+4),data[1]};
            // data=reader.transceive(apduLen);
            Log.i(getClass().getName(),"readMorePSERecord : "+toHex(apduLen));
            data=reader.transceive(apdu);

        }
        
        if ((data!=null)&&(data.length>2))
        {
            result=parse(new ReadPSERecord(),data,0,data.length-2);
        }
        
        return result;
    }
    
    boolean readRecord(byte sfi, byte num) throws IOException 
    {
        boolean result=true;
        byte []apdu={0x00,(byte)0xB2,num,(byte)((sfi<<3)+4),0x00 };

        Log.i(getClass().getName(),"readRecord");
        byte []data=reader.transceive(apdu);
       
        if ((data!=null)&&(data.length==2)&&(data[0]==0x6c))
        {
            byte []apduLen={0x00,(byte)0xB2,num,(byte)((sfi<<3)+4),data[1]};
            Log.i(getClass().getName(),"readMoreRecord");
            data=reader.transceive(apduLen);
        }
        
        if ((data!=null)&&(data.length>2))
        {
            result=parse(new ReadRecord(),data,0,data.length-2);
        }
        
        return result;
    }

    static boolean matchBytes(byte[] aid2, byte[] aid) 
    {
        boolean match=(aid==null)&&(aid2==null);
        
        if (!match)
        {
            if ((aid!=null)&&(aid2!=null))
            {
                if (aid.length==aid2.length)
                {
                    int i=aid.length;
                    
                    while (0!=i--)
                    {
                        if (aid[i]!=aid2[i])
                        {
                            break;
                        }
                    }
                    
                    match=(i < 0);
                }
            }
        }
        
        return match;
    }

    private static String toHex(byte[] in)
    {
        if(in==null)return "";
        StringBuilder sb = new StringBuilder(in.length * 2);
        for(byte b: in)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void decrypt_sdad()
    {
        EmvKeys cakeys=EmvKeys.getInstance();
        if(cakeys.getBrand(rid,capk_index)==null)
        {
            Log.e(getClass().getName(),"Could not find a public key matching "+rid+"/"+capk_index);
            return;
        }
        BigInteger modulo=new BigInteger(cakeys.getModulo(rid,capk_index),16); // from CAPK table
        BigInteger encrypted=new BigInteger(issuer_pk_encrypted_certificate,16);
        BigInteger expo=new BigInteger(cakeys.getExponent(rid,capk_index)); // from CAPK table
        BigInteger result=encrypted.modPow(expo, modulo);
        String issuer_data_decrypted=result.toString(16);
        if(!(issuer_data_decrypted.startsWith("6a")))
        {
            Log.e(getClass().getName(),"Header error after decryption of issuer_data : "+issuer_data_decrypted);
            return;
        }
        String issuer_id=issuer_data_decrypted.substring(4,12);
        String cert_exp=issuer_data_decrypted.substring(12,16);
        String cert_serial_nr=issuer_data_decrypted.substring(16,22);
        String hash_algorithm_indicator=issuer_data_decrypted.substring(22,24);
        String issuer_pk_algorithm_indicator=issuer_data_decrypted.substring(24,26);
        String issuer_pk_mod_length=issuer_data_decrypted.substring(26,28);
        String issuer_pk_exp_length=issuer_data_decrypted.substring(28,30);
        int nca=cakeys.getModulo(rid,capk_index).length()/2;
        String issuer_pk_mod_part=issuer_data_decrypted.substring(30,30+(nca-36)*2);
        String hash_result=issuer_data_decrypted.substring(30+nca-36,30+(nca-36)*2+40);

        String issuer_pk_mod=issuer_pk_mod_part+issuer_pk_remainder;

        // Decrypts ICC PK
        modulo=new BigInteger(issuer_pk_mod,16);
        encrypted=new BigInteger(icc_pk_encrypted,16);
        expo=new BigInteger(issuer_pk_exponent,16);
        result=encrypted.modPow(expo, modulo);

        String icc_data_decrypted=result.toString(16);
        if(!(icc_data_decrypted.startsWith("6a")))
        {
            Log.e(getClass().getName(),"Header error after decryption of icc_data : "+icc_data_decrypted);
            return;
        }
        String application_pan=icc_data_decrypted.substring(4,24);
        String icc_cert_exp_date=icc_data_decrypted.substring(24,28);
        String icc_cert_ser_nr=icc_data_decrypted.substring(28,34);
        String icc_hash_algo_ind=icc_data_decrypted.substring(34,36);
        String icc_pk_algo_ind=icc_data_decrypted.substring(36,38);
        String icc_pk_mod_length=icc_data_decrypted.substring(38,40);
        String icc_pk_exp_length=icc_data_decrypted.substring(40,42);
        int ni=issuer_pk_mod.length()/2;
        int iccmodlen=Integer.parseInt(icc_pk_mod_length,16);
        String icc_pk_mod;

        if(iccmodlen>=(ni-42)) { // there is a remainder
            String icc_pk_mod_part = icc_data_decrypted.substring(42, 42 + (ni - 42) * 2);
            icc_pk_mod = icc_pk_mod_part + icc_pk_remainder;
        }
        else
        {
            icc_pk_mod=icc_data_decrypted.substring(42,42+iccmodlen*2);
        }
        // Decrypts SDAD data !!!
        modulo=new BigInteger(icc_pk_mod,16);
        encrypted=new BigInteger(sdad_encrypted,16);
        expo=new BigInteger(icc_pk_exponent,16);
        result=encrypted.modPow(expo, modulo);


        String sdad_data_decrypted=result.toString(16);
        if(!(sdad_data_decrypted.startsWith("6a")))
        {
            Log.e(getClass().getName(),"Header error after decryption of sdad_data : "+sdad_data_decrypted);
            return;
        }
        String sdad_hash_algo_ind=sdad_data_decrypted.substring(4,6);
        String sdad_data_length=sdad_data_decrypted.substring(6,8);
        int ldd=Integer.parseInt(sdad_data_length,16);
        String sdad_data=sdad_data_decrypted.substring(8,8+ldd*2);

        Log.i(getClass().getName(),"sdad_data:"+sdad_data);
        applicationCryptogram=sdad_data.substring(20,36);
        Log.i(getClass().getName(),"application Cryptogram:"+applicationCryptogram);
    }

}
