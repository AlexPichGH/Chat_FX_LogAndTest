import gb.server.AuthService;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class TestChat {

    @Test
    public void test1() {
        try {
            Assert.assertEquals("nick1", AuthService.testConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {

    }
}
