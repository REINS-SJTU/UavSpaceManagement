package com.zhd.service;

import com.zhd.entity.tmp.DivisionPlan;

import java.util.List;

public interface GeoService {
    /**
     * 注册设备
     * @param xu
     * @param xl
     * @param yu
     * @param yl
     * @param zu
     * @param zl
     * @return 设备Id
     */
    void registerDevice(Double xu,Double xl,
                        Double yu,Double yl,
                        Double zu,Double zl);

    /**
     * 上报设备当前位置和速度
     */
    void uploadPosition(Long uid,
                        Double x,Double y,Double z,
                        Double vx,Double vy,Double vz,
                        Double hvx,Double hvy,Double hvz,
                        Long ts);

    /**
     * 规划安全区域
     * @return
     */
    List<DivisionPlan> planSafeArea();
}
