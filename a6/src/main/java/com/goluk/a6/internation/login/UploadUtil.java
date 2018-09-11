package com.goluk.a6.internation.login;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

public class UploadUtil {
	private static final String TAG = "uploadFile";


    private static final int TIME_OUT = 10 * 1000; // 超时时间


    private static final String CHARSET = "utf-8"; // 设置编码


//    /**
//     * Android上传文件到服务端
//     *
//     * @param file 需要上传的文件
//     * @param RequestURL 请求的rul
//     * @return 返回响应的内容
//     */
//    public static String uploadFile(File file, String RequestURL) {
//        String result = null;
//        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
//        String PREFIX = "--", LINE_END = "\r\n";
//        String CONTENT_TYPE = "multipart/form-data"; // 内容类型
//
//
//        try {
//            URL url = new URL(RequestURL);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setReadTimeout(TIME_OUT);
//            conn.setConnectTimeout(TIME_OUT);
//            conn.setDoInput(true); // 允许输入流
//            conn.setDoOutput(true); // 允许输出流
//            conn.setUseCaches(false); // 不允许使用缓存
//            conn.setRequestMethod("POST"); // 请求方式
//            conn.setRequestProperty("Charset", CHARSET); // 设置编码
//            conn.setRequestProperty("connection", "keep-alive");
//            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
//
//
//            if (file != null) {
//                /**
//                 * 当文件不为空，把文件包装并且上传
//                 */
//                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
//                StringBuffer sb = new StringBuffer();
//                sb.append(PREFIX);
//                sb.append(BOUNDARY);
//                sb.append(LINE_END);
//                /**
//                 * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
//                 * filename是文件的名字，包含后缀名的 比如:abc.png
//                 */
//
//
//                sb.append("Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
//                        + file.getName() + "\"" + LINE_END);
//                sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
//                sb.append(LINE_END);
//                dos.write(sb.toString().getBytes());
//                InputStream is = new FileInputStream(file);
//                byte[] bytes = new byte[1024];
//                int len = 0;
//                while ((len = is.read(bytes)) != -1) {
//                    dos.write(bytes, 0, len);
//                }
//                is.close();
//                dos.write(LINE_END.getBytes());
//                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
//                dos.write(end_data);
//                dos.flush();
//                /**
//                 * 获取响应码 200=成功 当响应成功，获取响应的流
//                 */
//                int res = conn.getResponseCode();
//                GolukDebugUtils.e(TAG, "response code:" + res);
//                GolukDebugUtils.e(TAG, "request success");
//                InputStream input = conn.getInputStream();
//                StringBuffer sb1 = new StringBuffer();
//                int ss;
//                while ((ss = input.read()) != -1) {
//                    sb1.append((char) ss);
//                }
//                result = sb1.toString();
//                GolukDebugUtils.e(TAG, "result : " + result);
//            }
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

    /**
     *
     * @param url
     * @param params
     * @param file
     * @return
     * @throws IOException
     */
    public static String uploadFile(String url, Map<String, String> params, File file) throws IOException {
        String BOUNDARY = UUID.randomUUID().toString();
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String PREFIX = "--", LINE_END = "\r\n";

        URL uri = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(10 * 1000); // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + "; boundary=" + BOUNDARY);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            conn.addRequestProperty(entry.getKey(), entry.getValue());
        }

        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
        /**
         * 当文件不为空，把文件包装并且上传
         */
        StringBuffer sb = new StringBuffer();
        sb.append(PREFIX);
        sb.append(BOUNDARY);
        sb.append(LINE_END);

        /**
         * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
         * filename是文件的名字，包含后缀名的 比如:abc.png
         */
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""
                + file.getName() + "\"" + LINE_END);
        sb.append("Content-Type: text/plain;" + LINE_END);
        sb.append(LINE_END);
        dos.write(sb.toString().getBytes());

        InputStream is = new FileInputStream(file);
        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = is.read(bytes)) != -1) {
            dos.write(bytes, 0, len);
        }
        is.close();
        dos.write(LINE_END.getBytes());

        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
        dos.write(end_data);
        dos.flush();
        // 得到响应码
        int res = conn.getResponseCode();
        InputStream in = conn.getInputStream();
        StringBuilder sb2 = new StringBuilder();
        if (res == 200) {
            int ch;
            while ((ch = in.read()) != -1) {
                sb2.append((char) ch);
            }
        }
        dos.close();
        conn.disconnect();
        return sb2.toString();
    }

    /**
     * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
     *
     * @param url Service net address
     * @param params text content
     * @param files pictures
     * @return String result of Service response
     * @throws IOException
     */
    public static String post(String url, Map<String, String> params, Map<String, File> files)
            throws IOException {
        String BOUNDARY = UUID.randomUUID().toString();
        String MULTIPART_FROM_DATA = "application/octest-stream";

        URL uri = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(10 * 1000); // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
       

        for (Map.Entry<String, String> entry : params.entrySet()) {
            conn.addRequestProperty("md5", entry.getValue());
        }

        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
        // 发送文件数据
        if (files != null)
            for (Map.Entry<String, File> file : files.entrySet()) {

                InputStream is = new FileInputStream(file.getValue());
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }

                is.close();
            }

        outStream.flush();
        // 得到响应码
        int res = conn.getResponseCode();
        InputStream in = conn.getInputStream();
        StringBuilder sb2 = new StringBuilder();
        if (res == 200) {
            int ch;
            while ((ch = in.read()) != -1) {
                sb2.append((char) ch);
            }
        }
        outStream.close();
        conn.disconnect();
        return sb2.toString();
    }

}
