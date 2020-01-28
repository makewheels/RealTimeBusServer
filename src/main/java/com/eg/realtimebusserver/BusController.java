package com.eg.realtimebusserver;

import com.alibaba.fastjson.JSON;
import com.eg.realtimebusserver.bean.BusPositionResult;
import com.eg.realtimebusserver.util.HttpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 公交
 *
 * @time 2020-01-27 15:34
 */
@Controller
@RequestMapping("position")
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
        //比较这条线路上的所有站点，找到离的最近的站点，这就是客户端要上车的地方

        //再向公交服务器发请求，获取当前公交位置
        String url;
        if (direction.equals("a")) {
            url = "/gongjiaoluxian/xianlu" + busName + "a.php";
        } else {
            url = "/gongjiaoluxian/xianlu" + busName + "b.php";
        }
        List<BusPositionResult> busPositionResultList =
                JSON.parseArray(HttpUtil.get(url), BusPositionResult.class);
        //如果现在没有公交
        if (busPositionResultList.size() == 0) {

        }
        //能到这里说明有公交
        //解析出距离最近的公交，并且需要是还没来的，不能是已经过去的
        //遍历这条线上的所有站点


//        for (BusPositionResult busPositionResult : busPositionResultList) {
//            //我先把所有的距离都计算出来，放到结果对象中
//            double buslat = Double.parseDouble(busPositionResult.getLat());
//            double buslng = Double.parseDouble(busPositionResult.getLng());
//        }
//        //这是最小的索引
//        int minIndex = 0;
//        //然后我再遍历一次，找最小值
//        for (int i = 0; i < busPositionResultList.size(); i++) {
//            if (busPositionResultList.get(i).getDistance()
//                    < busPositionResultList.get(minIndex).getDistance()) {
//                minIndex = i;
//            }
//        }
//        //这回可以拿到最小的距离了
//        double minDistance = busPositionResultList.get(minIndex).getDistance();
//        //计算距离
//        //返回
        return null;
    }
}
