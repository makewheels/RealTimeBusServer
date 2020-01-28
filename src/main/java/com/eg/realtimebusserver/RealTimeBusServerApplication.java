package com.eg.realtimebusserver;

import com.eg.realtimebusserver.util.BusLineUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RealTimeBusServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealTimeBusServerApplication.class, args);
        //加载所有站点数据
        BusLineUtil.loadAllStations();
    }

}
