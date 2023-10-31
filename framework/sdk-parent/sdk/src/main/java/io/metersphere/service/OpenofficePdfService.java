package io.metersphere.service;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormatRegistry;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class OpenofficePdfService {

    @Value("${openoffice.host:}")
    private String LOCAL_HOST;
    @Value("${openoffice.port:}")
    private int LOCAL_PORT;
    private DocumentFormatRegistry formatFactory = new DefaultDocumentFormatRegistry();

    public File convert(InputStream inputStream, String fileName, String fileType) {
        FileOutputStream fileOutputStream = null;
        String path = File.separatorChar + "opt" + File.separatorChar + "metersphere" + File.separatorChar + fileName + ".pdf";
        File file = new File(path);
        try {
            fileOutputStream = new FileOutputStream(file);
            convert(inputStream, fileType, fileOutputStream, "pdf");
        } catch (Exception e) {
            path = "";
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(fileOutputStream);
        }
        return file;
    }

    /**
     *
     * @desc 根据文件路径转化pdf
     * @auth lhd
     * @param inputFilePath 待转换的文件路径
     * @param outputFilePath 输出文件路径
     */
    private void convert(String inputFilePath, String outputFilePath)
        throws ConnectException {
        if (StringUtils.isEmpty(inputFilePath) || StringUtils.isEmpty(outputFilePath)) {
            throw new IllegalArgumentException("转PDF参数异常！！");
        }
        OpenOfficeConnection connection = new SocketOpenOfficeConnection(LOCAL_HOST, LOCAL_PORT);
        connection.connect();
        DocumentConverter converter = getConverter(LOCAL_HOST, connection);
        converter.convert(new File(inputFilePath), new File(outputFilePath));
        connection.disconnect();
    }

    /**
     *
     * @desc 根据文件流转化pdf
     * @auth lhd
     * @param inputStream
     * @param inputFileExtension 待转换文件的扩展名，例如: xls，doc
     * @param outputStream
     * @param outputFileExtension 输出文件扩展名，例如：pdf
     */
    @Retryable(value = Exception.class, maxAttempts = 1,backoff = @Backoff(delay = 5*60*1000, multiplier = 1.5))
    private void convert(InputStream inputStream, String inputFileExtension, OutputStream outputStream,
                         String outputFileExtension) throws ConnectException {
        if (inputStream == null || StringUtils.isEmpty(inputFileExtension) || outputStream == null
            || StringUtils.isEmpty(outputFileExtension)) {
            throw new IllegalArgumentException("转PDF参数异常！！");
        }
        OpenOfficeConnection connection = new SocketOpenOfficeConnection(LOCAL_HOST, LOCAL_PORT);
        connection.connect();
        DocumentConverter converter = getConverter(LOCAL_HOST, connection);
        converter.convert(inputStream, formatFactory.getFormatByFileExtension(inputFileExtension), outputStream,
            formatFactory.getFormatByFileExtension(outputFileExtension));
        connection.disconnect();
    }

    private static DocumentConverter getConverter(String connectIp, OpenOfficeConnection connection) {
        DocumentConverter converter = "localhost".equals(connectIp) || "127.0.0.1".equals(connectIp)
            || "0:0:0:0:0:0:0:1".equals(connectIp) ? new OpenOfficeDocumentConverter(connection)
            : new StreamOpenOfficeDocumentConverter(connection);
        return converter;
    }
}
