/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sinchana.test;

import java.util.Set;
import sinchana.Server;
import sinchana.SinchanaStoreInterface;
import sinchana.thrift.DataObject;

/**
 *
 * @author DELL
 */
public class TestDataStore {
    private Server server;
    private int testId;
    private ServerUI gui = null;
    private TesterController testerController;

    private static  int storeResponseCount=0,storeCount=0;
    public TestDataStore(Tester tester,TesterController tc) {
        
        
        this.testId = tester.getTestId();
            this.testerController = tc;
            this.server = tester.getServer();
            this.gui = tester.getGui();
            
            server.registerSinchanaStoreInterface(new SinchanaStoreInterface() {

            @Override
            public void store(DataObject dataObject) {
                storeCount++;
                System.out.println("storing object success "+dataObject.dataValue+" count: "+storeCount);
            }

            @Override
            public void get(Set<DataObject> dataObjectSet) {
                System.out.print("data set retrieved : ");
                for (DataObject dataObject : dataObjectSet) {
                    System.out.print(dataObject.dataValue+" ");
                }
                System.out.println("");
            }

            @Override
            public void remove(DataObject dataObject) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void isStored(Boolean success) {
                storeResponseCount++;
                System.out.println("Stored response count : "+storeResponseCount+" "+success);
            }

            @Override
            public void isRemoved(Boolean success) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
            
            
    }
    
    
}