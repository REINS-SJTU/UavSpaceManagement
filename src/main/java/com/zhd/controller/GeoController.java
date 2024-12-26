package com.zhd.controller;

import com.zhd.entity.tmp.DivisionPlan;
import com.zhd.service.GeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public void setNewShape(@RequestParam("xu")Double xu,
                                   @RequestParam("xl")Double xl,
                                   @RequestParam("yu")Double yu,
                                   @RequestParam("yl")Double yl,
                                   @RequestParam("zu")Double zu,
                                   @RequestParam("zl")Double zl){
        geoService.registerDevice(xu, xl, yu, yl, zu, zl);
    }

    /**
     * 某设备上报位置
     */
    @PostMapping("/position")
    public void updatePosition(@RequestParam("uid")Long uid,
                                 @RequestParam("x")Double x,
                                 @RequestParam("y")Double y,
                                 @RequestParam("z")Double z,
                                 @RequestParam("vx")Double vx,
                                 @RequestParam("vy")Double vy,
                                 @RequestParam("vz")Double vz,
                               @RequestParam("hvx")Double hvx,
                               @RequestParam("hvy")Double hvy,
                               @RequestParam("hvz")Double hvz,
                                 @RequestParam("ts")Long ts){
        geoService.uploadPosition(uid,x,y,z,vx,vy,vz,hvx,hvy,hvz,ts);
    }

    /**
     * 获取最新的安全区域划分
     * @return 每个设备即对应的区域编码
     */
    @GetMapping("/safeArea")
    public List<DivisionPlan> getSafeArea(){
        List<DivisionPlan> divisionPlans = geoService.planSafeArea();
//        System.out.println("=======safe area division =========");
//        for(DivisionPlan plan:divisionPlans) System.out.println(plan);
        return divisionPlans;
    }
}
