package com.atguigu.lease.web.admin.schedule;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduleTasks {

//    @Scheduled(cron = "0 0 0 * * ?")
//    public void scheduleTasks() {
//        System.out.println("scheduleTasks");
//    }
    @Autowired
    private LeaseAgreementService service;

    @Scheduled(cron = "0 0 0 * * *")//每天0点执行一次
    public void checkLeaseStatus() {
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<LeaseAgreement>();
        updateWrapper.le(LeaseAgreement::getLeaseEndDate, new Date());
        updateWrapper.in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING);
        updateWrapper.set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED);
        service.update(updateWrapper);

    }

}
