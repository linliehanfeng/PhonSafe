package com.example.hanfeng.phonsafe.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanfeng on 2017/10/23.
 */

public class StreamUtil {
    /**
     * 流转换成字符串
     * @param stream 流对象
     * @return   返回null代表异常
     */
    public static String streamToString(InputStream stream) {

        //1.在读取的过程中,江都区的内容存储至缓存中,然后一次性转换为字符串
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        //2.读流操作,读到没有为止(循环)
        byte[] bytes = new byte[1024];
        //3.记录读取内容的临时变量
        int temp = -1;
        try {
            while((temp=stream.read(bytes))!=-1){
                outputStream.write(bytes,0,temp);
            }
            //4.返回读取的数据
            return outputStream.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                //关闭
                outputStream.close();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        return null;

    }
}
