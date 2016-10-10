package infrastructure;

import com.csc108.configuration.GlobalConfig;
import com.csc108.infrastructure.pooledActiveMQ.ConnectionFactoryProvider;
import com.csc108.infrastructure.pooledActiveMQ.PooledConnectionFactory;
import com.csc108.infrastructure.pooledActiveMQ.PooledSession;
import com.csc108.utility.Alert;
import com.csc108.utility.AlertManager;
import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by LEGEN on 2016/6/19.
 */
public class ActiveMQTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        AlertManager.getInstance().init();
    }

    @Override
    protected void tearDown() throws Exception {
        AlertManager.getInstance().stop();
    }

    public void testSendMessage() throws Exception {
        int numOfThreadsSentMsg =100000;
        CountDownLatch gateWay = new CountDownLatch(numOfThreadsSentMsg);
        Executor executor = Executors.newFixedThreadPool(4);

        long startTime = System.currentTimeMillis();
        for (int i=0;i<numOfThreadsSentMsg;i++)executor.execute(new Runnable() {
            @Override
            public void run() {
                Alert.fireAlert(Alert.Severity.Info, "Test", "test message", null);
                gateWay.countDown();
            }
        });

        gateWay.await();
        long stopTime = System.currentTimeMillis();

        System.out.printf("Total cost %d", stopTime - startTime);

    }

    public void testPooledConnection() throws Exception {
        int numOfMaxConnections = GlobalConfig.getMaxNumOfActiveMqConnections();

        ArrayList<Connection> connections = new ArrayList();

        ConnectionFactory connectionFactory  = new PooledConnectionFactory(new ActiveMQConnectionFactory(GlobalConfig.getAlertMqUrl()));
        ((PooledConnectionFactory)connectionFactory).setMaxConnections(GlobalConfig.getMaxNumOfActiveMqConnections());

        for (int i=0;i<numOfMaxConnections;i++){
            Connection connection = connectionFactory.createConnection(null,null);
            connections.add(connection);
            connection.setClientID(Integer.toString(i+17));
            System.out.printf("Connection %s created \r\n", connection.getClientID());
        }

        Connection connection = connectionFactory.createConnection(null,null);
        System.out.printf("Connection %s created\r\n", connection.getClientID());
        Session session=null;
        for (int i=0;i<numOfMaxConnections;i++){
            long start = System.nanoTime();
            session= connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            long end = System.nanoTime();

            System.out.printf("Session %s created, cost %d \r\n", session.toString(),(end-start));
            //session.close();

        }
        session.close();

        long start = System.nanoTime();
        session= connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        long end = System.nanoTime();
        System.out.printf("Session %s created, cost %d \r\n", session.toString(),(end-start));

//        Optional<Connection> existedConnection = connections.stream().filter(x-> x.getClientID()==connection.getClientID())
//                .findAny();
//
//        assertEquals(true, existedConnection.isPresent());

    }
}
