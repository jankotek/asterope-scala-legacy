package org.asterope.util

import java.util.Properties

class ConfigStoreTest extends ScalaTestCase{
	
	lazy val store = new ConfigStore
	
	def testReadWriteProps{
		val n = "testConfigStore"
		store.deleteFile(n);
		
		val props = store.loadProps(n)
		assert(props.isEmpty)		
		props.put("aa","11")
		store.storeProps(n, props)
		
		val props2 = store.loadProps(n)
		assert(props2.get("aa") === "11")
		assert(props.keySet.size === 1)
		
		store.deleteFile(n)
	}
	

	
	def testReadWriteCase{
		val n = "testConfigStore"
		store.deleteFile(n);
		
		val aa = store.loadCase(n,new AA)
		assert(aa === new AA) //file does not exist, so default values should be used		
		
		//set new values and write them to store
		val aa2 = new AA(v1=222, v2="hejsa",v3=false);		
		
		store.storeCase(n, aa2)		
		val aa3 = store.loadCase(n,new AA)
		assert(aa2 === aa3)
		
		store.deleteFile(n)
	}
	

	def testCaseToProps{
		val aa = new AA(v1=123, v2="lalala")
		val props = store.caseToProps(aa)
		assert(props.keySet.size === 3)
		assert(props.get("v1")==="123")
		assert(props.get("v2")==="lalala")
		assert(props.get("v3")==="true")		
	}
	
	def testPropsToCase{
		val props = new Properties()
		props.put("v1","123")
		props.put("v2","lalala")		
		val aa = store.propsToCase(props, new AA())
		assert(aa.v1 === 123)
		assert(aa.v2 === "lalala")
		assert(aa.v3 === true)
	}

}

case class AA(v1:Int = 111, v2:String = "hopla", v3:Boolean = true){
	var aa = 11
}