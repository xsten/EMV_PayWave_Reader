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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
    public String decision=null;
    public String applicationCryptogram=null;
    public String atc=null;
    public byte [] aid;
    public String issuer;
    byte [] PDOL;


    private static ArrayList<String> TERMINALTAGS=new ArrayList<String>();
    private String ttq="68000000";
    private String amount="000000000010";
    private String amountOther="000000000000";
    private String terminalCountryCode="0250";
    private String tvr="0000000000";
    private String curcy="0978";
    private String txDate="180207";
    private String txType="00";
    private String unpredicatableNumber="CAFEBABE";

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

        String[] tt=new String[]{"9f66","9f02","9f03","9f1a","95","5f2a","9a","9c","9f37"};
        TERMINALTAGS.addAll(Arrays.asList(tt));
    }
    
    interface EnumCallback
    {
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException;
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
                // If we encapsulate the calls, the treatment of tag 77 will be interrupted, and we will never get 9F27
                //case 0x94:
                //    result=readAFL(data,offset,len);
                //    break;
                case 0x9F27:
                    result=readCryptogramInformationData(data,offset,len);
                    break;
                case 0x9F26:
                    result=readApplicationCryptogram(data,offset,len);
                    break;
                case 0x9F36:
                    result=readAtc(data,offset,len);
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


                int pdolLen=getPDOLlength(PDOL,0,PDOL.length);
                                
                pdolData=new byte[pdolLen+2];
                pdolData[0]=(byte)0x83;
                pdolData[1]=(byte)pdolLen;
                
                fillPDOL(PDOL,0,PDOL.length,pdolData,2);

            }
           
            if (pdolData==null)
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


            // byte[] pdolWithData=buildTerminalData(PDOL);
            // Log.i(getClass().getName(),"PDOL data to send : "+BinaryTools.toHex(pdolWithData));


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
        @Override
        public boolean found(int tag, int len, byte[] data, int offset) throws IOException 
        {
            boolean result=true;
            
//            System.out.println(String.format("appTemp %04X,%d,",tag,len)+Hex.encode(data, offset, len));
            
            switch (tag)
            {
                case 0x4F:
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
                            result=app.read();
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
            Log.i(getClass().getName(),(new String(new char[offset]).replace("\0", " "))+"^");
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
    
    public void read(String ttq,String amount,String amountOther,String terminalCountryCode,String tvr,String curcy,String txDate,String txType,String unpredicatableNumber) throws IOException
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


        byte [] ppse=adf;

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
    }

    int getPDOLlength(byte[] PDOL, int offset, int len) 
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
    
    void fillPDOL(byte[] PDOL, int offset, int len, byte[] pdolData, int i) 
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
        
        while (result && (len > 0))
        {
            byte sfi=data[offset++];
            byte firstRec=data[offset++];
            byte lastRec=data[offset++];
            byte authNum=data[offset++];
            
            while (result && (firstRec <= lastRec))
            {
                result=readRecord((byte)(0x1f & (sfi>>3)),firstRec);
                
                firstRec++;
            }
            
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
                case 0x9f6b:
                    result=readTrack2Equivalent(data,offset,len);
                    break;
                case 0x5A:
                    pan=Hex.encode(data, offset, len);
                    result=(expiryMonth==null);

                    break;
                case 0x5F24:
                    expiryMonth=Integer.parseInt(String.format("%x",data[offset+1]));
                    expiryYear=Integer.parseInt(String.format("%x",data[offset]));
                    result=(pan==null);

                    break;
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

    /** from PDOL string builds a valid TLV structure containing all necessary data **/
    private byte[] xxxbuildTerminalData(byte[] pdol)
    {
        byte[] candidate;
        String candidateString;
        int len=0;
        byte[] value;
        ByteArrayOutputStream ret=new ByteArrayOutputStream();

        for(int i=0;i<pdol.length;)
        {
            // try one digit tag
            candidate=new byte[]{pdol[i]};
            candidateString=BinaryTools.toHex(candidate);
            Log.d(getClass().getName(),"Trying "+candidateString);

            if(TERMINALTAGS.contains(candidateString))
            { // found a 1-byte tag
                len=(int)pdol[i+1];
                i+=2;
            }
            else {
                // try two digits tag
                candidate = new byte[]{pdol[i], pdol[i + 1]};
                candidateString=BinaryTools.toHex(candidate);
                Log.d(getClass().getName(),"Trying "+candidateString);

                if(TERMINALTAGS.contains(candidateString))
                { // found a 2-bytes tag
                    len=(int)pdol[i+2];
                    i+=3;
                }
                else candidate=null; // not found
            }

            // candidate and candidateString contains tag, len contain length
            if(candidate==null)
            {
                Log.w(getClass().getName(),"Could not parse PDOL - unknown tag on position "+i+" : "+BinaryTools.toHex(pdol));
                //cardPdol.setText("Error: "+"Could not parse PDOL - unknown tag on position "+i+" : "+BinaryTools.toHex(pdol));
                return null;
            }
            if(len>64||len<=0)
            {
                Log.w(getClass().getName(),"Could not parse PDOL - Strange length "+len+" fond before index "+i+" on PDOL data : "+BinaryTools.toHex(pdol));
                //cardPdol.setText("Error: "+"Could not parse PDOL - Strange length "+len+" found before index "+i+" on PDOL data : "+BinaryTools.toHex(pdol));
                return null;
            }

            value=new byte[len];
            switch(candidateString)
            {
                case "9f66":
                    System.arraycopy(BinaryTools.toBin(ttq),0,value,0,len);
                    break;
                case "9f02":
                    System.arraycopy(BinaryTools.toBin(amount),0,value,0,len);
                    break;
                case "9f03":
                    System.arraycopy(BinaryTools.toBin(amountOther),0,value,0,len);
                    break;
                case "9f1a":
                    System.arraycopy(BinaryTools.toBin(terminalCountryCode),0,value,0,len);
                    break;
                case "95":
                    System.arraycopy(BinaryTools.toBin(tvr),0,value,0,len);
                    break;
                case "5f2a":
                    System.arraycopy(BinaryTools.toBin(curcy),0,value,0,len);
                    break;
                case "9a":
                    System.arraycopy(BinaryTools.toBin(txDate),0,value,0,len);
                    break;
                case "9c":
                    System.arraycopy(BinaryTools.toBin(txType),0,value,0,len);
                    break;
                case "9f37":
                    System.arraycopy(BinaryTools.toBin(unpredicatableNumber),0,value,0,len);
                    break;
                default:
                    Log.i(getClass().getName(),"Unknown tag "+candidateString+" - filling with zeroes");
            }

            try {
                ret.write(candidate);
                ret.write(new byte[]{(byte)len});
                ret.write(value);
            }
            catch(Exception e)
            {
                Log.e(getClass().getName(),"Caught Exception while building from PDOL",e);
            }

        }
        return ret.toByteArray();
    }

}
