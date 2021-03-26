package com.seeyon.apps.ext.DTdocument.util;

import com.seeyon.apps.ext.DTdocument.manager.SyncOrgData;
import com.seeyon.apps.ext.DTdocument.manager.WriteMiddleData;

import java.sql.SQLException;

public class OperationUtil {

    public static void execute() throws SQLException {
        SyncOrgData.getInstance().syncSummary();
        WriteMiddleData.getInstance().batchSqlByType();
        //注释掉
//            ClearTemp40.getInstance().clearTableData();
    }
}
