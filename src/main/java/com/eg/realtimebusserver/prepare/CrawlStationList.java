package com.eg.realtimebusserver.prepare;

import com.eg.realtimebusserver.util.Constants;
import com.eg.realtimebusserver.util.HttpUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 爬取所有站点名
 *
 * @time 2020-01-27 23:44
 */
public class CrawlStationList {
    public static void main(String[] args) throws IOException {
        String resourcesPath = CrawlStationList.class.getResource("/").getPath();
        //读取文件
        File busNameListFile = new File(resourcesPath + "/bus/name/busNameList.txt");
        List<String> busNameList = FileUtils.readLines(busNameListFile, "utf-8");
        //遍历每一个公交
        for (String busName : busNameList) {
            //发请求
            String s = HttpUtil.get(Constants.BASE_URL + "/gongjiaoluxian/xianlul1.php");
            //解析html
            //保存到文件

        }
    }
}
