package com.zhd.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConstantValues {
    @Value("${mappo.url}")
    public String MAPPO_URL;
    @Value("${octree.M}")
    public int M;
    @Value("${motion.R}")
    public double R;
    @Value("${motion.a}")
    public double a;
    @Value("${motion.v}")
    public double v;
    @Value("${motion.t}")
    public double t;
}
