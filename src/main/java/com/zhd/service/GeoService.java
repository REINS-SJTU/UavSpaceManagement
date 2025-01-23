package com.zhd.service;

import com.zhd.entity.HumanPosition;
import com.zhd.entity.UavPosition;
import com.zhd.entity.tmp.DivisionPlan;
import com.zhd.entity.tmp.DivisionPlan2;

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
    String registerDevice(String uid,
                        Double xu,Double xl,
                        Double yu,Double yl,
                        Double zu,Double zl);

    /**
     * 上报设备当前位置和速度
     */
    void uploadPosition(String uid,
                        Double px,Double py,Double pz,
                        Double vx,Double vy,Double vz,
                        Double theta,Double phi,
                        Long ts);

    /**
     *  上报行人的位置和速度
     */
    void uploadHumanPosition(String hid,
                             Double px,Double py,Double pz,
                             Double vx,Double vy,Double vz,
                             Long ts);

    void batchUploadHumanPosition(List<HumanPosition> humanPositions);

    void batchUploadDevicePosition(List<UavPosition> devicePositions);


    /**
     * 规划安全区域
     * @return
     */
    List<DivisionPlan2> planSafeArea(Long ts);
}
