package com.centerm.util.apk;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;

import android.content.res.AXmlResourceParser;
import android.util.TypedValue;

/*
 * 文件名：AndroidAPKTools.java
 * 版权：Copyright by www.centerm.com
 * 描述�?
 */

/**
 * @author zhangwen 
 * 修改时间
 * 跟踪单号
 * 修改单号
 * 修改内容
 * 
 * */

public class ApkUtil {
    
    public static void main(String[] args) {
        String apkFile = "E:/set.apk";
        
        ApkUtil apkUtil = new ApkUtil(); 
        
        try{
            AndroidApkPkgInfo aapi = apkUtil.getApkInfo(apkFile);
            System.out.println("versionCode:" + aapi.getVersionCode());
            System.out.println("versionName:" + aapi.getVersionName());
            System.out.println("package:" + aapi.getPackageCode());
            System.out.println("label:"+ aapi.getLabel());
            System.out.println("minSdkVersion:"+ aapi.getMinSdkVersion());
            System.out.println("icon:" + aapi.getIcon().length());
            
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    /**
     * geticonstream
     * LTZ
     * 返回图标文件流 
     */
	@SuppressWarnings("resource")
	private byte[] geticonstream(String path){
		File file = new File(path);
		if(!file.exists()){			
			return null;
		}
		
		InputStream inputStream = null;
		ByteArrayOutputStream baos = null;
		ZipFile zip = null;
		try {
			zip = new ZipFile(path);
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while(entries.hasMoreElements()){
				ZipEntry item = entries.nextElement();
				String name = item.getName();
				if(name.contains("app_ico")
						|| name.contains("ic_launcher")){
					inputStream = zip.getInputStream(item);
					baos = new ByteArrayOutputStream();
					byte buff[] = new byte[1024];
					int length = 0;
					while ((length = inputStream.read(buff)) > 0) {
						baos.write(buff, 0, length);
					}
					baos.flush();
					byte[] data = baos.toByteArray();
					return data;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(inputStream != null){
					inputStream.close();
				}
				
				if(baos != null){
					baos.close();
				}
				
				if(zip != null){
					zip.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
        return null;
	}
    
	/**
	 * getApkInfo
	 * LTZ
	 * 获取得到apk的基本信息
	 */
    public  AndroidApkPkgInfo getApkInfo(String path) throws Exception {
		File file = new File(path);
		if(!file.exists()){
			return null;
		}
		
		AndroidApkPkgInfo aapi = new AndroidApkPkgInfo();
		aapi.setLabel(file.getName());
		
        ZipFile zip = null;
        InputStream inputStream = null;
        try {
            zip = new ZipFile(path);
            ZipEntry entry = zip.getEntry("AndroidManifest.xml");
            inputStream = zip.getInputStream(entry);
            
            AXmlResourceParser parser=new AXmlResourceParser();
            parser.open(inputStream);
            
            while (true) {
                int type=parser.next();
                if (type==XmlPullParser.END_DOCUMENT) {
                    break;
                }
                switch (type) {
                    case XmlPullParser.START_TAG:{
                        for (int i=0;i!=parser.getAttributeCount();++i) {
                            if("versionName".equals(parser.getAttributeName(i))){
                                aapi.setVersionName(getAttributeValue(parser,i));
                            }else if("package".equals(parser.getAttributeName(i))){
                            	aapi.setPackageCode(getAttributeValue(parser,i));
                            }else if("versionCode".equals(parser.getAttributeName(i))){
                            	aapi.setVersionCode(getAttributeValue(parser,i));
                            }else if("minSdkVersion".equals(parser.getAttributeName(i))){
                            	aapi.setMinSdkVersion(getAttributeValue(parser,i));
                            }
                        }
                    }
                }
            }
            
            aapi.setIcon(new String(this.geticonstream(path)));
            return aapi;
            
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(zip != null){
                zip.close();
            }
            if(inputStream != null){
                inputStream.close();
            }
        }
        
        return null;
    }
    
    private static String getAttributeValue(AXmlResourceParser parser,int index) {
        int type=parser.getAttributeValueType(index);
        int data=parser.getAttributeValueData(index);
        
        if (type==TypedValue.TYPE_STRING) {
            return parser.getAttributeValue(index);
        }
        if (type==TypedValue.TYPE_ATTRIBUTE) {
            return String.format("?%s%08X",getPackage(data),data);
        }
        if (type==TypedValue.TYPE_REFERENCE) {
            return String.format("@%s%08X",getPackage(data),data);
        }
        if (type==TypedValue.TYPE_FLOAT) {
            return String.valueOf(Float.intBitsToFloat(data));
        }
        if (type==TypedValue.TYPE_INT_HEX) {
            return String.format("0x%08X",data);
        }
        if (type==TypedValue.TYPE_INT_BOOLEAN) {
            return data!=0?"true":"false";
        }
        if (type==TypedValue.TYPE_DIMENSION) {
            return Float.toString(complexToFloat(data))+
                DIMENSION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
        }
        if (type==TypedValue.TYPE_FRACTION) {
            return Float.toString(complexToFloat(data))+
                FRACTION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
        }
        if (type>=TypedValue.TYPE_FIRST_COLOR_INT && type<=TypedValue.TYPE_LAST_COLOR_INT) {
            return String.format("#%08X",data);
        }
        if (type>=TypedValue.TYPE_FIRST_INT && type<=TypedValue.TYPE_LAST_INT) {
            return String.valueOf(data);
        }
        return String.format("<0x%X, type 0x%02X>",data,type);
    }
    
    private static String getPackage(int id) {
        if (id>>>24==1) {
            return "android:";
        }
        return "";
    }
    
    /////////////////////////////////// ILLEGAL STUFF, DONT LOOK :)
    private static float complexToFloat(int complex) {
        return (float)(complex & 0xFFFFFF00)*RADIX_MULTS[(complex>>4) & 3];
    }
    
    private static final float RADIX_MULTS[]={
        0.00390625F,3.051758E-005F,1.192093E-007F,4.656613E-010F
    };
    private static final String DIMENSION_UNITS[]={
        "px","dip","sp","pt","in","mm","",""
    };
    private static final String FRACTION_UNITS[]={
        "%","%p","","","","","",""
    };

}