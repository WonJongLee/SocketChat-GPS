package com.example.user.gps_mission;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by user on 2016-08-19.
 */
public class TransByte {
    private static TransByte ourInstance = new TransByte();

    public static TransByte getInstance() {
        return ourInstance;
    }

    private TransByte() {
    }

    public static byte[] ItoB(String str) {
        int size = 0;
        int buf = 0;
        String strBuf;
        ArrayList<String> list = new ArrayList<String>();

        ByteBuffer byteBuffer;

        buf = Integer.parseInt(str);

        if(str.length()%2 == 1) {
            size = str.length() + 1;
        } else size = str.length();

        //자리수가 홀수일경우 앞에 0을 붙여주는 작업.
        strBuf = String.format("%0" + Integer.toString(size) + "d", buf);

        //2자리씩 끊어서 list에 저장. (String)
        for(int i=0 ; i<size ; i = i+2) {
            if (i != size)
                list.add(strBuf.substring(i, i + 2));
        }
        //2개씩 잘라서 bb배열에 넣어줌
        byte bb[] = new byte[list.size()];

        for (int i=0 ; i<list.size() ; i++) {
            int buffer = Integer.parseInt(list.get(i));
            bb[i] = (byte) buffer;
        }
        return bb;
    }

    public static byte[] ItoB(int in) {
        int size = 0;
        String buf;
        String strBuf;
        ArrayList<String> list = new ArrayList<String>();

        ByteBuffer byteBuffer;

        buf = Integer.toString(in);

        if(buf.length()%2 == 1) {
            size = buf.length() + 1;
        } else size = buf.length();

        strBuf = String.format("%0" + Integer.toString(size) + "d", in);
        for(int i=0 ; i<size ; i = i+2) {
            if (i != size)
                list.add(strBuf.substring(i, i + 2));
        }

        byte bb[] = new byte[list.size()];

        for (int i=0 ; i<list.size() ; i++) {
            int buffer = Integer.parseInt(list.get(i));
            bb[i] = (byte) buffer;
        }
        return bb;

    }

    public static byte[] StoB(String s) {
        byte[] b = new byte[s.length()];
        try {
            b = s.getBytes("ksc5601");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return b;
    }

    //byte를 int형으로 풀 때 잘 풀리는지 확인하기 위하여 만든 메소드
    public static int[] BtoI(byte[] b) {
        int[] ii = new int[b.length];
        for (int i=0 ; i<b.length ; i++) {
            ii[i] = b[i]&0xff;
        }
        return ii;
    }

    public static byte[] mergyByte(byte[] b1, byte[] b2) {
        byte[] temp = new byte[b1.length + b2.length];

        System.arraycopy(b1, 0, temp, 0, b1.length);
        System.arraycopy(b2, 0, temp, b1.length, b2.length);

        return temp;
    }


























    //    public byte[] YMDtoByte(int y, int m, int d) throws UnsupportedEncodingException {
//        String ymd = String.format("%02d",Integer.toString(y) ) +
//                String.format("%02d",Integer.toString(m)) +
//                String.format("%02d",Integer.toString(d));
//        return ymd.getBytes("ksc5601");    //한글로 바꿔주는 작업
//    }
//    public byte[] HMStoByte(int h, int m, int s) throws UnsupportedEncodingException {
//        String hms = String.format("%02d",Integer.toString(h) ) +
//                String.format("%02d",Integer.toString(m)) +
//                String.format("%02d",Integer.toString(s));
//        return hms.getBytes("ksc5601");    //한글로 바꿔주는 작업
//    }
//    public byte[] routeToByte(String splitStr[]) throws UnsupportedEncodingException {
//        String route = String.format("%05d",splitStr[0]) +
//                String.format("%02d",splitStr[0]) +
//                String.format("3") +
//                String.format("12");
//        return route.getBytes("ksc5601");
//    }
//
//    public byte[] integerToByte(int i) {
//        ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
//
//        // 인수로 넘어온 integer을 putInt로설정
//        buff.putInt(i);
//        return buff.array();
//    }
}
