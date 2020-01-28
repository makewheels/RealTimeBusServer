package com.eg.realtimebusserver.bus;

import com.alibaba.fastjson.JSON;
import com.eg.realtimebusserver.util.Constants;
import com.eg.realtimebusserver.util.HttpUtil;
import com.eg.realtimebusserver.util.LocationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
