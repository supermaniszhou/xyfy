package com.seeyon.apps.ext.DTdocument.quartz;

import com.seeyon.apps.ext.DTdocument.manager.ClearTemp40;
import com.seeyon.apps.ext.DTdocument.manager.SyncOrgData;
import com.seeyon.apps.ext.DTdocument.manager.WriteMiddleData;
import com.seeyon.apps.ext.DTdocument.util.OperationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * 周刘成   2019-11-4
 */
public class SyncDataTask implements Runnable {
    private Logger logger = LoggerFactory.getLogger(SyncDataTask.class);

    @Override
    public void run() {

        /**
         * 同步公文
         */
        try {
            System.out.println("开始了吗--------------------------");
            OperationUtil.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
