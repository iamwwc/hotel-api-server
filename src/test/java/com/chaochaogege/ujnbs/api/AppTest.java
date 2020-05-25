package com.chaochaogege.ujnbs.api;

import com.chaochaogege.ujnbs.APIGenerator;
import com.chaochaogege.ujnbs.APIOptions;
import com.chaochaogege.ujnbs.TableColumn;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

@RunWith(VertxUnitRunner.class)
public class AppTest {
    @Test
    public void appTest(TestContext context) {
        Properties p = new Properties();
        APIOptions apiOptions = new APIOptions().setDatabase("hotel")
                .setPassword("wxlwuweichao");
        context.async();
        apiOptions.allowCORS(true);
        apiOptions.setLogin(true);
        TableColumn column = new TableColumn("staff","id",new ArrayList<>(Arrays.asList("username","email","phone","sex","id","role")));
        TableColumn column_consumer = new TableColumn("consumer","id",new ArrayList<>(Arrays.asList("username","email","phone","sex","id","role")));
        TableColumn column_room = new TableColumn("room","roomtypeid",new ArrayList<>(Arrays.asList("roomtypeid","chairs","roomtype","picurl")));
        ArrayList<TableColumn> columns = new ArrayList<>(Arrays.asList(column, column_consumer,column_room));
        new APIGenerator(apiOptions,columns).run();
    }
}
