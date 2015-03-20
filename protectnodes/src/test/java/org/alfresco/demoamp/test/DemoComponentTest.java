package org.alfresco.demoamp.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.protectamp.ProtectNodesComponent;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

/**
 * A simple class demonstrating how to run out-of-container tests 
 * loading Alfresco application context.
 * 
 * This class uses the RemoteTestRunner to try and connect to 
 * localhost:4578 and send the test name and method to be executed on 
 * a running Alfresco. One or more hostnames can be configured in the @Remote
 * annotation.
 * 
 * If there is no available remote server to run the test, it falls 
 * back on local running of JUnits.
 * 
 * For proper functioning the test class file must match exactly 
 * the one deployed in the webapp (either via JRebel or static deployment)
 * otherwise "incompatible magic value XXXXX" class error loading issues will arise.  
 * 
 * @author Gabriele Columbro 
 * @author Maurizio Pillitu
 *
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass=SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class DemoComponentTest {
    
    private static final String ADMIN_USER_NAME = "admin";

    static Logger log = Logger.getLogger(DemoComponentTest.class);

    @Autowired
    protected ProtectNodesComponent demoComponent;
    
    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;
    
    @Autowired
    @Qualifier("TransactionService")
    protected TransactionService transactionService;

    @Autowired
    @Qualifier("nodeLocatorService")
    private NodeLocatorService nodeLocatorService;
    
    @Test
    public void testWiring() {
        assertNotNull(demoComponent);
    }
    
    @Test
    public void testGetCompanyHome() {
    	AuthenticationUtil.setFullyAuthenticatedUser(ADMIN_USER_NAME);
        NodeRef companyHome = demoComponent.getCompanyHome();
        assertNotNull(companyHome);
        String companyHomeName = (String) nodeService.getProperty(companyHome, ContentModel.PROP_NAME);
        assertNotNull(companyHomeName);
        assertEquals("Company Home", companyHomeName);
    }
    
    @Test
    public void testUndeletable() {
        //demoComponent.execute();
        List<String> undeletableList = new ArrayList<String>(10);
        
        undeletableList.add("/app:company_home/app:dictionary");
        undeletableList.add("/app:company_home/app:guest_home");
        undeletableList.add("/app:company_home/app:user_homes");
        undeletableList.add("/app:company_home/app:shared");
        undeletableList.add("/app:company_home/cm:Imap_x0020_Attachments");
        undeletableList.add("/app:company_home/cm:Imap_x0020_Home");
        undeletableList.add("/app:company_home/st:sites");
        

        
        for (String pathNode : undeletableList)
        {
            final String fPathNode = pathNode;
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {

                        TransactionService fTransactionService = transactionService;
                        final RetryingTransactionCallback<Object> emptyBinWork = new RetryingTransactionCallback<Object>()
                            {
                                public Object execute() throws Exception
                                {
                                    HashMap<String, Serializable> params = new HashMap<String, Serializable>();
                                    params.put("query", fPathNode);
                                    params.put("store_type", "workspace");
                                    params.put("store_id", "SpacesStore");
                                    NodeRef nodeToProtect = nodeLocatorService.getNode("xpath", null, params);
                                    assertTrue(nodeService.hasAspect(nodeToProtect, ContentModel.ASPECT_UNDELETABLE));
                                    
                                    return null;
                                }
                            };
                        fTransactionService.getRetryingTransactionHelper().doInTransaction(emptyBinWork);

                        return null;

                    }
                }, AuthenticationUtil.getSystemUserName());

        }
    }

}
