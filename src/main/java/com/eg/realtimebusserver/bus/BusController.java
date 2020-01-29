package com.eg.realtimebusserver.bus;

import com.alibaba.fastjson.JSON;
import com.eg.realtimebusserver.bean.timetable.ClientTimeTable;
import com.eg.realtimebusserver.bean.timetable.my.DepartTime;
import com.eg.realtimebusserver.bean.timetable.my.TimeTable;
import com.eg.realtimebusserver.util.Constants;
import com.eg.realtimebusserver.util.HttpUtil;
import com.eg.realtimebusserver.util.LocationUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 公交
 *
 * @time 2020-01-27 15:34
 */
@Controller
@RequestMapping("bus")
public class BusController {

    /**
     * 获取我最近的车站，应该还有，经过这个站的公交列表
     *
     * @param position
     * @return
     */
    @RequestMapping("getNearestStations")
    @ResponseBody
    public String getNearestStations(@RequestParam String position) {
        return null;
    }

    /**
     * 获取距离
     *
     * @param position  我现在的经纬度
     * @param busName   公交名
     * @param direction 方向
     * @return
     */
    @RequestMapping("getDistance")
    @ResponseBody
    public String getDistance(@RequestParam String position,
                              @RequestParam String busName,
                              @RequestParam String direction) {
        //当客户端发送获取距离请求时
        //比较这条公交线路上的所有站点，找到离的最近的站点，这就是客户端要上车的地方
        int mySequence = BusLineUtil.getStationSequenceByPosition(busName, direction, position);
        //向公交服务器发请求，获取当前公交位置
        String url;
        if (direction.equals("a")) {
            url = Constants.BASE_URL + "/gongjiaoluxian/xianlu" + busName + "a.php";
        } else {
            url = Constants.BASE_URL + "/gongjiaoluxian/xianlu" + busName + "b.php";
        }
        List<BusPositionResult> busPositionResultList =
                JSON.parseArray(HttpUtil.get(url), BusPositionResult.class);
        //如果现在没有公交
        if (busPositionResultList.size() == 0) {
            DistanceResponse distanceResponse = new DistanceResponse();
            distanceResponse.setHasBus(false);
            return JSON.toJSONString(distanceResponse);
        }
        //能到这里说明有公交
        //解析出距离最近的公交，并且需要是还没来的，不能是已经过去的
        //这是现在在路上的所有的公交的位置的站点的序号
        List<Integer> busSequenceList = new ArrayList<>();
        //遍历这条线上的所有站点
        for (BusPositionResult busPositionResult : busPositionResultList) {
            int busSequence = BusLineUtil.getStationSequenceByPosition(busName, direction,
                    busPositionResult.getLng() + "," + busPositionResult.getLat());
            busSequenceList.add(busSequence);
        }
        //把路上的公交，和我的位置比较
        for (int i = 0; i < busSequenceList.size(); i++) {
            //如果已经是过去的车了，就删掉
            if (busSequenceList.get(i) < mySequence) {
                busSequenceList.remove(i);
                busPositionResultList.remove(i);
            }
        }
        //遍历找到最短距离
        double myLat = LocationUtils.getLat(position);
        double myLng = LocationUtils.getLng(position);
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < busPositionResultList.size(); i++) {
            BusPositionResult busPositionResult = busPositionResultList.get(i);
            double distance = LocationUtils.getDistance(myLat, myLng,
                    Double.parseDouble(busPositionResult.getLng()),
                    Double.parseDouble(busPositionResult.getLat()));
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        //返回结果
        DistanceResponse distanceResponse = new DistanceResponse();
        distanceResponse.setHasBus(true);
        distanceResponse.setDistance(minDistance);
        return JSON.toJSONString(distanceResponse);
    }

    @RequestMapping("getCurrentBusStation")
    @ResponseBody
    public String getCurrentBusStation() {
        return null;
    }

    @RequestMapping("getTimeTable")
    @ResponseBody
    public String getTimeTable(@RequestParam String busName,
                               @RequestParam String direction) {
        // 读发车时间
        String filename;
        if (direction.equals("a")) {
            filename = "f" + busName + ".json";
        } else {
            filename = busName + ".json";
        }
        ClientTimeTable clientTimeTable = new ClientTimeTable();
        TimeTable timeTable;
        List<LocalTime> localTimeList = new ArrayList<>();
        List<DepartTime> departTimeList;
        try {
            // 解析时刻表
            String json = FileUtils.readFileToString(new File(
                    BusController.class.getResource("/bus/timetable/my/").getPath(), filename), "utf-8");
            timeTable = JSON.parseObject(json, TimeTable.class);
            // 拿到发车时间list结合
            departTimeList = timeTable.getDepartTimeList();
        } catch (Exception e) {
            // 如果找不到文件
            e.printStackTrace();
            clientTimeTable.setMsg("error");
            return JSON.toJSONString(clientTimeTable);
        }
        for (DepartTime departTime : departTimeList) {
            localTimeList.add(departTime.getLocalTime());
        }
        // 目的：从现在的时间开始，往后的发车时间，优先显示。所以要调整顺序
        List<String> stringList = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < localTimeList.size(); i++) {
            // 找到第一个比现在晚的
            if (localTimeList.get(i).isAfter(LocalTime.now(ZoneId.of("UTC+8")))) {
                index = i;
                break;
            }
        }
        // 现在到最后
        for (int i = index; i < localTimeList.size(); i++) {
            stringList.add(localTimeList.get(i).toString());
        }
        // 今天和明天发车时间的分隔行
        stringList.add("----------");
        // 开头到现在
        for (int i = 0; i < index - 1; i++) {
            stringList.add(localTimeList.get(i).toString());
        }
        // 回写给客户端
        clientTimeTable.setMsg("ok");
        clientTimeTable.setTopic(timeTable.getOriginalTopic());
        clientTimeTable.setTimeList(stringList);
        return JSON.toJSONString(clientTimeTable);
    }
}
