package me.arifbanai.vShop.managers;

import me.arifbanai.easypool.DataSourceManager;
import me.arifbanai.easypool.enums.DataSourceType;
import me.arifbanai.vShop.interfaces.AsyncQueries;
import me.arifbanai.vShop.interfaces.Queries;

public abstract class QueryManager implements Queries, AsyncQueries {

    protected final DataSourceManager dataSourceManager;
    protected final DataSourceType dataSourceType;

    protected QueryManager(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
        this.dataSourceType = dataSourceManager.getDataSourceType();
    }

}
