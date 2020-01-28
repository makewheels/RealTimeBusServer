package com.eg.realtimebusserver.util;

import com.alibaba.fastjson.JSON;
import com.eg.realtimebusserver.RealTimeBusServerApplication;
import com.eg.realtimebusserver.bean.stationinfo.amap.AmapStationInfo;
import com.eg.realtimebusserver.bean.stationinfo.amap.Buslines;
import com.eg.realtimebusserver.bean.stationinfo.amap.Busstops;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @time 2020-01-28 19:27
 */
public class BusLineUtil {
    //公交线路
    public static Map<String, AmapStationInfo> busLineMap = new HashMap<>();

    public static void loadAllStations() {
        //读取所有文件
        String amapFolderPath = RealTimeBusServerApplication.class.getResource("/bus/stationinfo/amap").getPath();
        File amapFolder = new File(amapFolderPath);
        File[] files = amapFolder.listFiles();
        //解析为java对象
        for (File file : files) {
            String json = null;
            try {
                json = FileUtils.readFileToString(file, "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            busLineMap.put(FilenameUtils.getBaseName(file.getName()),
                    JSON.parseObject(json, AmapStationInfo.class));
        }
        //只留前两个线路
        Set<String> keySet = busLineMap.keySet();
        for (String key : keySet) {
            AmapStationInfo amapStationInfo = busLineMap.get(key);
            List<Buslines> buslines = amapStationInfo.getBuslines();
            for (int i = 0; i < buslines.size(); i++) {
                if (i >= 3) {
                    buslines.remove(i);
                }
            }
        }
        //为了节省内存，去除路线坐标polyline
        for (String key : keySet) {
            AmapStationInfo amapStationInfo = busLineMap.get(key);
            List<Buslines> buslines = amapStationInfo.getBuslines();
            for (Buslines busline : buslines) {
                busline.setPolyline(null);
            }
        }
    }

    /**
     * 通过经纬度位置获取，在这条公交线上，最近的站点的，序号
     *
     * @param busName
     * @param direction
     * @param position
     * @return
     */
    public static int getStationSequenceByPosition(String busName, String direction, String position) {
        //拿到这辆车
        AmapStationInfo amapStationInfo = busLineMap.get(busName);
        List<Buslines> buslines = amapStationInfo.getBuslines();
        //解析出经纬度坐标
        double clientLat = LocationUtils.getLat(position);
        double clientLng = LocationUtils.getLng(position);
        //判断方向
        Buslines busLine;
        if (direction.equals("a")) {
            busLine = buslines.get(0);
        } else {
            busLine = buslines.get(1);
        }
        //寻找最近的站点序号
        List<Busstops> busstops = busLine.getBusstops();
        int minIndex = 0;
        //初始化最新距离
        double minDistance = LocationUtils.getDistance(clientLat, clientLng,
                LocationUtils.getLat(busstops.get(0).getLocation()),
                LocationUtils.getLng(busstops.get(0).getLocation()));
        //遍历所有站点
        for (int i = 0; i < busstops.size(); i++) {
            Busstops busstop = busstops.get(i);
            double distance = LocationUtils.getDistance(clientLat, clientLng,
                    LocationUtils.getLat(busstop.getLocation()), LocationUtils.getLng(busstop.getLocation()));
            if (distance < minDistance) {
                minIndex = i;
                minDistance = distance;
            }
        }
        //返回序号
        return Integer.valueOf(busstops.get(minIndex).getSequence());
    }

    public static void main(String[] args) {
        loadAllStations();
        int a = getStationSequenceByPosition("50", "b", "125.156268,46.582009");
        System.out.println(a);
    }

}
