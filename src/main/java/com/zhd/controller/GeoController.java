package com.zhd.controller;

import com.zhd.entity.tmp.DivisionPlan;
import com.zhd.entity.tmp.DivisionPlan2;
import com.zhd.service.GeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/geo")
public class GeoController {
    @Autowired
    private GeoService geoService;

    /**
     * 注册设备元信息
     */
    @PostMapping("/register")
    public String setNewShape(@RequestParam("uid") String uid,
                                   @RequestParam("xu")Double xu,
                                   @RequestParam("xl")Double xl,
                                   @RequestParam("yu")Double yu,
                                   @RequestParam("yl")Double yl,
                                   @RequestParam("zu")Double zu,
                                   @RequestParam("zl")Double zl){
        return geoService.registerDevice(uid, xu, xl, yu, yl, zu, zl);
    }

    /**
     * 某设备上报位置
     */
    @PostMapping("/position")
    public void updatePosition(@RequestParam("uid")String uid,
                                 @RequestParam("px")Double px,
                                 @RequestParam("py")Double py,
                                 @RequestParam("pz")Double pz,
                                 @RequestParam("vx")Double vx,
                                 @RequestParam("vy")Double vy,
                                 @RequestParam("vz")Double vz,
                               @RequestParam("theta")Double theta,
                               @RequestParam("phi")Double phi,
                                 @RequestParam("ts")Long ts){
        geoService.uploadPosition(uid,px,py,pz,vx,vy,vz,theta,phi,ts);
    }

    /**
     * 上报行人位置
     */
    @PostMapping("/humanPosition")
    public void updateHumanPosition(@RequestParam("hid")String hid,
                                    @RequestParam("px")Double px,
                                    @RequestParam("py")Double py,
                                    @RequestParam("pz")Double pz,
                                    @RequestParam("vx")Double vx,
                                    @RequestParam("vy")Double vy,
                                    @RequestParam("vz")Double vz,
                                    @RequestParam("ts")Long ts){
        geoService.uploadHumanPosition(hid,px,py,pz,vx,vy,vz,ts);
    }

    /**
     * 获取最新的安全区域划分
     * @return 每个设备即对应的区域编码
     */
    @GetMapping("/safeArea")
    public List<DivisionPlan2> getSafeArea(@RequestParam("ts")Long ts){
//        long current = new Date().getTime();
        List<DivisionPlan2> divisionPlans = geoService.planSafeArea(ts);
        return divisionPlans;
    }
}
