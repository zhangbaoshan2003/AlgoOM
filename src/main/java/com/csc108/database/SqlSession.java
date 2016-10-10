package com.csc108.database;

import com.csc108.disruptor.concurrent.ILifetimeCycle;
import com.csc108.log.LogFactory;
import com.csc108.utility.FixUtil;
import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import org.apache.commons.io.FileUtils;
import quickfix.FileUtil;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class SqlSession implements ILifetimeCycle {
    private SQLServerConnection _connection;
    @Override
    public void start() {

    }

    @Override
    public void stop() {
        try {
            if(_connection!=null){
                _connection.close();
            }
        }catch (Exception ex){
            LogFactory.error("Close sql server connection error!",ex);
        }
    }

    public SqlSession() throws Exception {
        this("configuration/sqlserver.properties");
    }

    public SqlSession(String configFile) throws Exception {
        Properties pro = new Properties();

        File file = new File(configFile);
        if(file.exists()==false)
            throw new IllegalArgumentException("Can't find configuration to initialize sql session! @ "+configFile);

        Reader reader = new FileReader(file);
        pro.load(reader);

        String url = pro.getProperty("url");
        String password = pro.getProperty("password");
        String username = pro.getProperty("username");

        _connection = (SQLServerConnection)DriverManager.getConnection(url,username,password);
    }

    public ResultSet dataSetQuery(String sqlCmdText) throws Exception {
        Statement sta = _connection.createStatement();
        String Sql = sqlCmdText;
        ResultSet rs = sta.executeQuery(Sql);
        return rs;
    }
}
