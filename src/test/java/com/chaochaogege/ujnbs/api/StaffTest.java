package com.chaochaogege.ujnbs.api;

import com.chaochaogege.ujnbs.APIGenerator;
import com.chaochaogege.ujnbs.APIOptions;
import com.chaochaogege.ujnbs.TableColumn;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(VertxUnitRunner.class)
public class StaffTest {
    @Test
    public void staffTest(TestContext context) {
        APIOptions apiOptions = new APIOptions().setDatabase("hotel")
                .setPassword("wxlwuweichao");
        // 每一个表对应一个 column
        // TableColumn
        TableColumn column = new TableColumn("staff","uid",new ArrayList<>(Arrays.asList("username","email","phone","sex","uid","role")));
        ArrayList<TableColumn> columns = new ArrayList<>();
        columns.add(column);
        new APIGenerator(apiOptions,columns).run();
        context.async();
    }
}
