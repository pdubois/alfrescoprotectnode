# alfrescoprotectnode
Alfresco module allowing protection of Alfresco “well known” nodes against accidental delete.
The module adds the aspect "sys:undeletable" to the "well know" nodes.

## Building the module:

```
mkdir work
cd work
git clone https://github.com/pdubois/alfrescoprotectnode.git
cd alfrescoprotectnode/protectnodes
mvn install
chmod +x run.sh
```

## Outcome:
The module "protectnodes.amp" is generated under "target" folder.

## Module options:
How to specify the list of nodes that will be protected against deletion?

You need to specify it in  `protectnodes/src/main/amp/config/alfresco/module/protectnodes/context/service-context.xml` configuring the path of all the nodes you need to protect.

Example, see property undeletableList for the list of nodes to be protected:

```
	<bean id="protect.nodesComponent" class="org.alfresco.protectamp.ProtectNodesComponent"
		parent="module.baseComponent" init-method="init">
		<property name="moduleId" value="${project.artifactId}" />  
		<property name="name" value="protectComponent" />
		<property name="description"
			value="Protect well known nodes against accidental deletion" />
		<property name="sinceVersion" value="1.0" />
		<property name="appliesFromVersion" value="1.0" />
		<property name="nodeService" ref="NodeService" />
		<property name="nodeLocatorService" ref="nodeLocatorService" />
		<!--  property name="executeOnceOnly" value="false" / -->
		<property name="undeletableList">
			<list>
				<value>/app:company_home/app:dictionary</value>
				<value>/app:company_home/app:guest_home</value>
				<value>/app:company_home/app:user_homes</value>
				<value>/app:company_home/app:shared</value>
				<value>/app:company_home/cm:Imap_x0020_Attachments</value>
				<value>/app:company_home/cm:Imap_x0020_Home</value>
				<value>/app:company_home/st:sites</value>
			</list>
		</property>
		<property name="transactionService" ref="TransactionService" />
		<property name="searchService" ref="SearchService" />
		<property name="namespacePrefixResolver">
            <ref bean="namespaceService" />
        </property>
	</bean>
```



