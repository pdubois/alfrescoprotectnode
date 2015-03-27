/*
    Licensed to the Apache Software Foundation (ASF) under one or more
	contributor license agreements.  See the NOTICE file distributed with
	this work for additional information regarding copyright ownership.
	The ASF licenses this file to You under the Apache License, Version 2.0
	(the "License"); you may not use this file except in compliance with
	the License.  You may obtain a copy of the License at
	
	http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.alfresco.protectamp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component that is adding or removing "undeletable" aspect following sets of nodes in
 * 
 * @author Philippe Dubois
 */
public class ProtectNodesComponent extends AbstractModuleComponent
{
    Log log = LogFactory.getLog(ProtectNodesComponent.class);

    private NodeService nodeService;

    private Set<String> undeletableList;

    private Set<String> deletableList;

    private NodeLocatorService nodeLocatorService;

    private TransactionService transactionService;

    private SearchService searchService;

    private NamespaceService namespacePrefixResolver;

    public void setDeletableList(Set<String> deletableList)
    {
        this.deletableList = deletableList;
    }

    public void setNamespacePrefixResolver(NamespaceService namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public ProtectNodesComponent()
    {
        super();
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setUndeletableList(Set<String> undeletableList)
    {
        this.undeletableList = undeletableList;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNodeLocatorService(NodeLocatorService nodeLocatorService)
    {
        this.nodeLocatorService = nodeLocatorService;
    }

    public void init()
    {
        // calling super.init() insure registering the component
        super.init();
        log.info("Init called in Prote");
    }

    /**
     * Bogus component execution
     */
    @Override
    protected void executeInternal() throws Throwable
    {
        final TransactionService fTransactionService = transactionService;
        // iterate on ProtectNodes
        for (String pathNode : undeletableList)
        {
            final String fPathNode = pathNode;
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {

                        TransactionService fTransactionService = transactionService;
                        final RetryingTransactionCallback<Object> protectWork = new RetryingTransactionCallback<Object>()
                            {
                                public Object execute() throws Exception
                                {
                                    HashMap<String, Serializable> params = new HashMap<String, Serializable>();
                                    params.put("query", fPathNode);
                                    params.put("store_type", "workspace");
                                    params.put("store_id", "SpacesStore");
                                    // NodeRef nodeToProtect = nodeLocatorService.getNode("xpath", null, params);
                                    List<NodeRef> list = searchService.selectNodes(getCompanyHome(), fPathNode, null,
                                            namespacePrefixResolver, false);
                                    NodeRef nodeToProtect = list.get(0);
                                    log.info("********************DemoComponent has been executed protect " + nodeToProtect
                                            + " " + fPathNode);
                                    if (!nodeService.hasAspect(nodeToProtect, ContentModel.ASPECT_UNDELETABLE))
                                    {
                                        log.info("Aspect Undeletable added:" + fPathNode);
                                        nodeService.addAspect(nodeToProtect, ContentModel.ASPECT_UNDELETABLE, null);
                                    }
                                    return null;
                                }
                            };
                        fTransactionService.getRetryingTransactionHelper().doInTransaction(protectWork, false, true);
                        return null;

                    }

                }, AuthenticationUtil.getSystemUserName());
        }
        
        for (String pathNode : deletableList)
        {
            final String fPathNode = pathNode;
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {

                        TransactionService fTransactionService = transactionService;
                        final RetryingTransactionCallback<Object> unProtectWork = new RetryingTransactionCallback<Object>()
                            {
                                public Object execute() throws Exception
                                {
                                    HashMap<String, Serializable> params = new HashMap<String, Serializable>();
                                    params.put("query", fPathNode);
                                    params.put("store_type", "workspace");
                                    params.put("store_id", "SpacesStore");
                                    // NodeRef nodeToProtect = nodeLocatorService.getNode("xpath", null, params);
                                    List<NodeRef> list = searchService.selectNodes(getCompanyHome(), fPathNode, null,
                                            namespacePrefixResolver, false);
                                    NodeRef nodeToUnProtect = list.get(0);
                                    log.info("********************DemoComponent has been executed unprotect " + nodeToUnProtect
                                            + " " + fPathNode);
                                    if (nodeService.hasAspect(nodeToUnProtect, ContentModel.ASPECT_UNDELETABLE))
                                    {
                                        log.info("Aspect Undeletable removed:" + fPathNode);
                                        nodeService.removeAspect(nodeToUnProtect, ContentModel.ASPECT_UNDELETABLE);
                                    }
                                    return null;
                                }
                            };
                        fTransactionService.getRetryingTransactionHelper().doInTransaction(unProtectWork, false, true);
                        return null;

                    }

                }, AuthenticationUtil.getSystemUserName());
        }
    }

    /**
     * This is a demo service interaction with Alfresco Foundation API. This sample method returns the number of child
     * nodes of a certain type under a certain node.
     * 
     * @return
     */
    public int childNodesCount(NodeRef nodeRef)
    {
        return nodeService.countChildAssocs(nodeRef, true);
        // return nodeService.countChildAssocs(nodeRef, true);
    }

    /**
     * Returns the NodeRef of "Company Home"
     * 
     * @return
     */
    public NodeRef getCompanyHome()

    {
        return nodeLocatorService.getNode("companyhome", null, null);
    }
}
